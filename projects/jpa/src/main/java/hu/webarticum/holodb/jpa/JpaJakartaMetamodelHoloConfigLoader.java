package hu.webarticum.holodb.jpa;

import java.io.UncheckedIOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.annotations.Immutable;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.holodb.config.HoloConfigColumn;
import hu.webarticum.holodb.config.HoloConfigSchema;
import hu.webarticum.holodb.config.HoloConfigTable;
import hu.webarticum.holodb.config.HoloConfigColumn.ColumnMode;
import hu.webarticum.holodb.config.HoloConfigColumn.DistributionQuality;
import hu.webarticum.holodb.config.HoloConfigColumn.ShuffleQuality;
import hu.webarticum.holodb.jpa.annotation.HoloColumn;
import hu.webarticum.holodb.jpa.annotation.HoloColumnDistributionQuality;
import hu.webarticum.holodb.jpa.annotation.HoloColumnMode;
import hu.webarticum.holodb.jpa.annotation.HoloColumnShuffleQuality;
import hu.webarticum.holodb.jpa.annotation.HoloIgnore;
import hu.webarticum.holodb.jpa.annotation.HoloTable;
import hu.webarticum.holodb.jpa.annotation.HoloValue;
import hu.webarticum.holodb.jpa.annotation.HoloVirtualColumn;
import hu.webarticum.holodb.jpa.annotation.HoloWriteable;
import hu.webarticum.holodb.spi.config.SourceFactory;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EmbeddableType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;
import jakarta.persistence.metamodel.Type.PersistenceType;

// TODO: ignore column vs throw exception?
// TODO: handling mapped superclasses?
public class JpaJakartaMetamodelHoloConfigLoader {
    
    private final Pattern getMethodPattern = Pattern.compile("^get([A-Z])(.*)$");
    

    public HoloConfig load(Metamodel metamodel, String defaultSchemaName, LargeInteger seed) {
        Map<String, JpaSchemaInfo> schemas = new TreeMap<>();
        scanMetamodel(schemas, metamodel, defaultSchemaName);
        return renderConfig(schemas, seed);
    }
    
    private void scanMetamodel(Map<String, JpaSchemaInfo> schemas, Metamodel metamodel, String defaultSchemaName) {
        for (EntityType<?> entityType : metamodel.getEntities()) {
            scanEntityType(schemas, metamodel, entityType, defaultSchemaName);
        }
    }

    private void scanEntityType(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            EntityType<?> entityType,
            String defaultSchemaName) {
        Class<?> entityClazz = entityType.getJavaType();
        String schemaName = extractSchemaName(entityClazz, defaultSchemaName);
        String tableName = extractTableName(metamodel, entityClazz);
        JpaSchemaInfo jpaSchemaInfo = schemas.computeIfAbsent(schemaName, k -> new JpaSchemaInfo());
        JpaTableInfo jpaTableInfo = jpaSchemaInfo.tables.computeIfAbsent(tableName, k -> new JpaTableInfo());
        jpaTableInfo.annotatedElement = entityClazz;
        jpaTableInfo.idColumnName = extractIdColumnName(entityType);
        jpaTableInfo.columnNamesInOrder.addAll(loadColumnNamesInOrder(metamodel, entityType));
        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            scanAttribute(schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        }
    }
    
    private String extractIdColumnName(EntityType<?> entityType) {
        Type<?> idType = entityType.getIdType();
        if (idType == null) {
            return "";
        }
        
        Attribute<?, ?> idAttribute = entityType.getId(entityType.getIdType().getJavaType());
        Member idMember = idAttribute.getJavaMember();
        if (!(idMember instanceof AnnotatedElement)) {
            return "";
        }
        
        return extractColumnName((AnnotatedElement) idMember);
    }

    private String extractSchemaName(AnnotatedElement annotatedElement, String defaultSchemaName) {
        HoloTable holoTableAnnotation = annotatedElement.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null && !holoTableAnnotation.schema().isEmpty()) {
            return holoTableAnnotation.schema();
        }
        
        Table tableAnnotation = annotatedElement.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.schema().isEmpty()) {
            return tableAnnotation.schema();
        }

        if (annotatedElement instanceof Member) {
            return extractSchemaNameFromAnnotatedMember(annotatedElement, defaultSchemaName);
        }
        
        return defaultSchemaName;
    }

    private String extractSchemaNameFromAnnotatedMember(AnnotatedElement annotatedElement, String defaultSchemaName) {
        JoinTable joinTableAnnotation = annotatedElement.getAnnotation(JoinTable.class);
        if (joinTableAnnotation != null && !joinTableAnnotation.schema().isEmpty()) {
            return joinTableAnnotation.schema();
        }
        
        CollectionTable collectionTableAnnotation = annotatedElement.getAnnotation(CollectionTable.class);
        if (collectionTableAnnotation != null && !collectionTableAnnotation.schema().isEmpty()) {
            return collectionTableAnnotation.schema();
        }

        return defaultSchemaName;
    }

    private String extractTableName(Metamodel metamodel, AnnotatedElement annotatedElement) {
        HoloTable holoTableAnnotation = annotatedElement.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null && !holoTableAnnotation.name().isEmpty()) {
            return holoTableAnnotation.name();
        }
        
        if (annotatedElement instanceof Member) {
            return extractTableNameFromAnnotatedMember(annotatedElement);
        } else if (!(annotatedElement instanceof Class)) {
            throw new IllegalArgumentException("Unknown element for table: " + annotatedElement);
        }
        
        Class<?> clazz = (Class<?>) annotatedElement;
        if (metamodel instanceof MetamodelImplementor) {
            // FIXME MetamodelImplementor is deprecated now
            MetamodelImplementor hibernateMetamodel = (MetamodelImplementor) metamodel;
            EntityPersister entityPersister = hibernateMetamodel.entityPersister(clazz);
            if (entityPersister instanceof SingleTableEntityPersister) {
                return ((SingleTableEntityPersister) entityPersister).getTableName();
            }
        }
        
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        
        return clazz.getSimpleName();
    }

    private String extractTableNameFromAnnotatedMember(AnnotatedElement annotatedElement) {
        JoinTable joinTableAnnotation = annotatedElement.getAnnotation(JoinTable.class);
        if (joinTableAnnotation != null && !joinTableAnnotation.name().isEmpty()) {
            return joinTableAnnotation.name();
        }
        
        CollectionTable collectionTableAnnotation = annotatedElement.getAnnotation(CollectionTable.class);
        if (collectionTableAnnotation != null && !collectionTableAnnotation.name().isEmpty()) {
            return collectionTableAnnotation.name();
        }

        // XXX
        return extractFieldName(annotatedElement);
    }

    private List<String> loadColumnNamesInOrder(Metamodel metamodel, ManagedType<?> managedType) {
        List<String> result = new ArrayList<>();
        addColumnNames(result, metamodel, managedType);
        return result;
    }
    
    private void addColumnNames(List<String> result, Metamodel metamodel, ManagedType<?> managedType) {
        Class<?> clazz = managedType.getJavaType();
        for (Field field : clazz.getDeclaredFields()) {
            Attribute<?, ?> attribute;
            try {
                attribute = managedType.getDeclaredAttribute(field.getName());
            } catch (IllegalArgumentException e) {
                continue;
            }
            PersistentAttributeType persistentAttributeType = attribute.getPersistentAttributeType();
            if (persistentAttributeType == PersistentAttributeType.EMBEDDED) {
                EmbeddableType<?> embeddableType = metamodel.embeddable(field.getType());
                addColumnNames(result, metamodel, embeddableType);
            } else if (persistentAttributeType == PersistentAttributeType.BASIC) {
                String columnName = extractColumnName(field);
                result.add(columnName);
            }
        }
    }

    private void scanAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            JpaTableInfo jpaTableInfo,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        Member member = attribute.getJavaMember();
        if (member instanceof AnnotatedElement && ((AnnotatedElement) member).getAnnotation(HoloIgnore.class) != null) {
            return;
        }
        
        PersistentAttributeType persistentAttributeType = attribute.getPersistentAttributeType();
        if (persistentAttributeType == PersistentAttributeType.BASIC) {
            scanBasicAttribute(attribute, jpaTableInfo);
        } else if (persistentAttributeType == PersistentAttributeType.ONE_TO_ONE) {
            scanOneToOneAttribute(
                    schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.ONE_TO_MANY) {
            scanOneToManyAttribute(schemas, metamodel, schemaName, tableName, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.MANY_TO_ONE) {
            scanManyToOneAttribute(metamodel, jpaTableInfo, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.MANY_TO_MANY) {
            scanManyToManyAttribute(
                    schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.EMBEDDED) {
            scanEmbeddedAttribute(
                    schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.ELEMENT_COLLECTION) {
            scanElementCollectionAttribute(
                    schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        } else {
            throw new IllegalArgumentException("Unsupported persistent attribute type: " + persistentAttributeType);
        }
    }
    
    private void scanBasicAttribute(Attribute<?, ?> attribute, JpaTableInfo jpaTableInfo) {
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return;
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        String columnName = extractColumnName(annotatedMember);
        JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.computeIfAbsent(columnName, k -> new JpaColumnInfo());
        jpaColumnInfo.attribute = attribute;
    }

    private void scanOneToOneAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            JpaTableInfo jpaTableInfo,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        boolean isOwner = true;
        Member member = attribute.getJavaMember();
        if (member instanceof AnnotatedElement) {
            AnnotatedElement annotatedMember = (AnnotatedElement) member;
            OneToOne oneToOneAnnotation = annotatedMember.getAnnotation(OneToOne.class);
            if (oneToOneAnnotation != null) {
                isOwner = oneToOneAnnotation.mappedBy().isEmpty();
            }
        }
        
        if (isOwner) {
            scanOwnerAttribute(schemas, metamodel, schemaName, tableName, attribute, defaultSchemaName);
        } else {
            scanOwnedAttribute(metamodel, jpaTableInfo, attribute, defaultSchemaName);
        }
    }

    private void scanOneToManyAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        scanOwnerAttribute(schemas, metamodel, schemaName, tableName, attribute, defaultSchemaName);
    }
    
    private void scanOwnerAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        if (!(attribute instanceof PluralAttribute)) {
            return;
        }
        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
        
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return;
        }
        
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        
        Class<?> targetType = pluralAttribute.getElementType().getJavaType();
        EntityType<?> targetEntityType;
        try {
            targetEntityType = metamodel.entity(targetType);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        Class<?> targetEntityClazz = targetEntityType.getJavaType();
        String targetSchemaName = extractSchemaName(targetEntityClazz, defaultSchemaName);
        String targetTableName = extractTableName(metamodel, targetEntityClazz);
        JpaSchemaInfo targetJpaSchemaInfo = schemas.computeIfAbsent(targetSchemaName, k -> new JpaSchemaInfo());
        JpaTableInfo targetJpaTableInfo =
                targetJpaSchemaInfo.tables.computeIfAbsent(targetTableName, k -> new JpaTableInfo()); // FIXME: init?
        JoinColumn joinColumnAnnotation = extractSingleJoinColumnAnnotation(annotatedMember);
        if (joinColumnAnnotation == null) {
            return;
        }

        String referencedColumnName = joinColumnAnnotation.referencedColumnName();
        String targetForeignKeyColumn = joinColumnAnnotation.name();
        if (targetForeignKeyColumn.isEmpty()) {
            return;
        }

        JpaColumnInfo targetForeignKeyColumnInfo =
                targetJpaTableInfo.columns.computeIfAbsent(targetForeignKeyColumn, k -> new JpaColumnInfo());
        mergeForeignLink(targetForeignKeyColumnInfo, schemaName, tableName, referencedColumnName);
    }
    
    private void scanManyToOneAttribute(
            Metamodel metamodel, JpaTableInfo jpaTableInfo, Attribute<?, ?> attribute, String defaultSchemaName) {
        scanOwnedAttribute(metamodel, jpaTableInfo, attribute, defaultSchemaName);
    }

    private void scanOwnedAttribute(
            Metamodel metamodel, JpaTableInfo jpaTableInfo, Attribute<?, ?> attribute, String defaultSchemaName) {
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return;
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        String columnName = extractColumnName(annotatedMember);
        JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.computeIfAbsent(columnName, k -> new JpaColumnInfo());
        jpaColumnInfo.attribute = attribute;
        
        Class<?> foreignEntityClazz = attribute.getJavaType();
        String foreignSchemaName = extractSchemaName(foreignEntityClazz, defaultSchemaName);
        String foreignTableName = extractTableName(metamodel, foreignEntityClazz);
        String foreignColumnName = "";
        JoinColumn joinColumnAnnotation = annotatedMember.getAnnotation(JoinColumn.class);
        if (joinColumnAnnotation != null) {
            foreignColumnName = joinColumnAnnotation.referencedColumnName();
        }
        mergeForeignLink(jpaColumnInfo, foreignSchemaName, foreignTableName, foreignColumnName);
    }

    private void scanManyToManyAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            JpaTableInfo jpaTableInfo,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        if (jpaTableInfo.idColumnName.isEmpty()) {
            return; // FIXME
        }
        
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return;
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        
        ManyToMany manyToManyAnnotation = annotatedMember.getAnnotation(ManyToMany.class);
        if (manyToManyAnnotation != null && !manyToManyAnnotation.mappedBy().isEmpty()) {
            return;
        }

        if (!(attribute instanceof PluralAttribute)) {
            return;
        }
        
        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
        Type<?> itemType = pluralAttribute.getElementType();
        Class<?> itemClazz = itemType.getJavaType();
        
        if (!(itemType instanceof EntityType)) {
            return;
        }
        EntityType<?> entityType = (EntityType<?>) itemType;
        
        String subSchemaName = extractSchemaName(annotatedMember, schemaName);
        String subTableName = extractTableName(metamodel, annotatedMember);
        JpaSchemaInfo jpaSchemaInfo = schemas.computeIfAbsent(subSchemaName, k -> new JpaSchemaInfo());
        JpaTableInfo subJpaTableInfo = jpaSchemaInfo.tables.computeIfAbsent(subTableName, k -> new JpaTableInfo());
        subJpaTableInfo.annotatedElement = annotatedMember;
        subJpaTableInfo.idColumnName = "";

        String targetSchemaName = extractSchemaName(annotatedMember, schemaName);
        String targetTableName = extractTableName(metamodel, itemClazz);
        String targetIdColumnName = extractIdColumnName(entityType);

        String parentIdColumnName = null;
        JoinColumn joinColumnAnnotation = extractSingleJoinColumnAnnotation(annotatedMember);
        if (joinColumnAnnotation != null) {
            parentIdColumnName = joinColumnAnnotation.name();
        }
        if (parentIdColumnName == null) {
            parentIdColumnName = tableName + "_id"; // FIXME
        }

        subJpaTableInfo.columnNamesInOrder.add(parentIdColumnName);
        JpaColumnInfo parentIdJpaColumnInfo =
                subJpaTableInfo.columns.computeIfAbsent(parentIdColumnName, k -> new JpaColumnInfo());
        parentIdJpaColumnInfo.foreignSchemaName = schemaName;
        parentIdJpaColumnInfo.foreignTableName = tableName;
        parentIdJpaColumnInfo.foreignColumnName = jpaTableInfo.idColumnName;
        parentIdJpaColumnInfo.mode = ColumnMode.DEFAULT;

        String foreignIdColumnName = null;
        JoinColumn inverseJoinColumnAnnotation = extractSingleInverseJoinColumnAnnotation(annotatedMember);
        if (inverseJoinColumnAnnotation != null) {
            foreignIdColumnName = inverseJoinColumnAnnotation.name();
        }
        if (foreignIdColumnName == null) {
            foreignIdColumnName = targetTableName + "_id"; // FIXME
        }

        subJpaTableInfo.columnNamesInOrder.add(foreignIdColumnName);
        JpaColumnInfo foreignIdJpaColumnInfo =
                subJpaTableInfo.columns.computeIfAbsent(foreignIdColumnName, k -> new JpaColumnInfo());
        foreignIdJpaColumnInfo.foreignSchemaName = targetSchemaName;
        foreignIdJpaColumnInfo.foreignTableName = targetTableName;
        foreignIdJpaColumnInfo.foreignColumnName = targetIdColumnName;
        foreignIdJpaColumnInfo.mode = ColumnMode.DEFAULT;

        OrderColumn orderColumnAnnotation = annotatedMember.getAnnotation(OrderColumn.class);
        if (orderColumnAnnotation != null && !orderColumnAnnotation.name().isEmpty()) {
            String orderColumnName = orderColumnAnnotation.name();
            subJpaTableInfo.columnNamesInOrder.add(orderColumnName);
            JpaColumnInfo orderJpaColumnInfo =
                    subJpaTableInfo.columns.computeIfAbsent(orderColumnName, k -> new JpaColumnInfo());
            parentIdJpaColumnInfo.type = LargeInteger.class;
            orderJpaColumnInfo.mode = ColumnMode.COUNTER; // FIXME: gaps are filled with nulls!
        }
        
        OrderBy orderByAnnotation = annotatedMember.getAnnotation(OrderBy.class);
        if (
                orderByAnnotation != null &&
                !orderByAnnotation.value().isEmpty() &&
                !subJpaTableInfo.columnNamesInOrder.contains(orderByAnnotation.value())) {
            String orderByName = orderByAnnotation.value();
            subJpaTableInfo.columnNamesInOrder.add(orderByName);
            JpaColumnInfo orderJpaColumnInfo =
                    subJpaTableInfo.columns.computeIfAbsent(orderByName, k -> new JpaColumnInfo());
            parentIdJpaColumnInfo.type = LargeInteger.class;
            orderJpaColumnInfo.mode = ColumnMode.COUNTER;
        }
    }

    private void scanEmbeddedAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            JpaTableInfo jpaTableInfo,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        Class<?> type = attribute.getJavaType();
        EmbeddableType<?> embeddableType = metamodel.embeddable(type);
        for (Attribute<?, ?> subAttribute : embeddableType.getAttributes()) {
            scanAttribute(schemas, metamodel, schemaName, tableName, jpaTableInfo, subAttribute, defaultSchemaName);
        }
    }

    private void scanElementCollectionAttribute(
            Map<String, JpaSchemaInfo> schemas,
            Metamodel metamodel,
            String schemaName,
            String tableName,
            JpaTableInfo jpaTableInfo,
            Attribute<?, ?> attribute,
            String defaultSchemaName) {
        if (jpaTableInfo.idColumnName.isEmpty()) {
            return; // FIXME
        }
        
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return;
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;

        if (!(attribute instanceof PluralAttribute)) {
            return;
        }
        
        PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
        Type<?> itemType = pluralAttribute.getElementType();
        Class<?> itemClazz = itemType.getJavaType();
        
        String subSchemaName = extractSchemaName(annotatedMember, schemaName);
        String subTableName = extractTableName(metamodel, annotatedMember);
        JpaSchemaInfo jpaSchemaInfo = schemas.computeIfAbsent(subSchemaName, k -> new JpaSchemaInfo());
        JpaTableInfo subJpaTableInfo = jpaSchemaInfo.tables.computeIfAbsent(subTableName, k -> new JpaTableInfo());
        subJpaTableInfo.annotatedElement = annotatedMember;
        subJpaTableInfo.idColumnName = "";
        
        EmbeddableType<?> embeddableType = null;
        try {
            embeddableType = metamodel.embeddable(itemClazz);
        } catch (IllegalArgumentException e) {
        }
        
        String parentIdColumnName = null;
        JoinColumn joinColumnAnnotation = extractSingleJoinColumnAnnotation(annotatedMember);
        if (joinColumnAnnotation != null) {
            parentIdColumnName = joinColumnAnnotation.name();
        }
        if (parentIdColumnName == null) {
            parentIdColumnName = tableName + "_id"; // FIXME
        }

        subJpaTableInfo.columnNamesInOrder.add(parentIdColumnName);
        JpaColumnInfo idJpaColumnInfo =
                subJpaTableInfo.columns.computeIfAbsent(parentIdColumnName, k -> new JpaColumnInfo());
        idJpaColumnInfo.foreignSchemaName = schemaName;
        idJpaColumnInfo.foreignTableName = tableName;
        idJpaColumnInfo.foreignColumnName = jpaTableInfo.idColumnName;
        idJpaColumnInfo.mode = ColumnMode.DEFAULT;

        OrderColumn orderColumnAnnotation = annotatedMember.getAnnotation(OrderColumn.class);
        if (orderColumnAnnotation != null && !orderColumnAnnotation.name().isEmpty()) {
            String orderColumnName = orderColumnAnnotation.name();
            subJpaTableInfo.columnNamesInOrder.add(orderColumnName);
            JpaColumnInfo orderJpaColumnInfo =
                    subJpaTableInfo.columns.computeIfAbsent(orderColumnName, k -> new JpaColumnInfo());
            idJpaColumnInfo.type = LargeInteger.class;
            orderJpaColumnInfo.mode = ColumnMode.COUNTER; // FIXME: gaps are filled with nulls!
        }
        
        if (embeddableType != null) {
            subJpaTableInfo.columnNamesInOrder.addAll(loadColumnNamesInOrder(metamodel, embeddableType));
            for (Attribute<?, ?> targetAttribute : embeddableType.getAttributes()) {
                scanAttribute(
                        schemas,
                        metamodel,
                        subSchemaName,
                        subTableName,
                        subJpaTableInfo,
                        targetAttribute,
                        defaultSchemaName);
            }
        } else if (itemType.getPersistenceType() == PersistenceType.BASIC) {
            String columnName = extractColumnName(annotatedMember);
            subJpaTableInfo.columnNamesInOrder.add(columnName);
            JpaColumnInfo jpaColumnInfo = subJpaTableInfo.columns.computeIfAbsent(columnName, k -> new JpaColumnInfo());
            jpaColumnInfo.type = itemClazz;
            jpaColumnInfo.attribute = attribute;
        }

        OrderBy orderByAnnotation = annotatedMember.getAnnotation(OrderBy.class);
        if (
                orderByAnnotation != null &&
                !orderByAnnotation.value().isEmpty() &&
                !subJpaTableInfo.columnNamesInOrder.contains(orderByAnnotation.value())) {
            String orderByName = orderByAnnotation.value();
            subJpaTableInfo.columnNamesInOrder.add(orderByName);
            JpaColumnInfo orderJpaColumnInfo =
                    subJpaTableInfo.columns.computeIfAbsent(orderByName, k -> new JpaColumnInfo());
            idJpaColumnInfo.type = LargeInteger.class;
            orderJpaColumnInfo.mode = ColumnMode.COUNTER;
        }
    }
    
    private JoinColumn extractSingleJoinColumnAnnotation(AnnotatedElement annotatedElement) {
        JoinColumn joinColumnAnnotation = annotatedElement.getAnnotation(JoinColumn.class);
        if (joinColumnAnnotation != null) {
            return joinColumnAnnotation;
        }
        
        JoinColumns joinColumnsAnnotation = annotatedElement.getAnnotation(JoinColumns.class);
        if (joinColumnsAnnotation != null) {
            JoinColumn[] joinColumns = joinColumnsAnnotation.value();
            if (joinColumns.length == 1) {
                return joinColumns[0];
            }
        }
        
        CollectionTable collectionTableAnnotation = annotatedElement.getAnnotation(CollectionTable.class);
        if (collectionTableAnnotation != null) {
            JoinColumn[] joinColumns = collectionTableAnnotation.joinColumns();
            if (joinColumns.length == 1) {
                return joinColumns[0];
            }
        }

        JoinTable joinTableAnnotation = annotatedElement.getAnnotation(JoinTable.class);
        if (joinTableAnnotation != null) {
            JoinColumn[] joinColumns = joinTableAnnotation.joinColumns();
            if (joinColumns.length == 1) {
                return joinColumns[0];
            }
        }
        
        return null;
    }
    
    private JoinColumn extractSingleInverseJoinColumnAnnotation(AnnotatedElement annotatedElement) {
        JoinTable joinTableAnnotation = annotatedElement.getAnnotation(JoinTable.class);
        if (joinTableAnnotation != null) {
            JoinColumn[] inverseJoinColumns = joinTableAnnotation.inverseJoinColumns();
            if (inverseJoinColumns.length == 1) {
                return inverseJoinColumns[0];
            }
        }
        
        return null;
    }
    
    private void mergeForeignLink(
            JpaColumnInfo jpaColumnInfo, String foreignSchemaName, String foreignTableName, String foreignColumnName) {
        if (jpaColumnInfo.foreignSchemaName != null && !foreignSchemaName.equals(jpaColumnInfo.foreignSchemaName)) {
            throw new IllegalArgumentException(
                    "Unmatching foreign schemas: " + foreignSchemaName + " != " + jpaColumnInfo.foreignSchemaName);
        }
        if (jpaColumnInfo.foreignTableName != null && !foreignTableName.equals(jpaColumnInfo.foreignTableName)) {
            throw new IllegalArgumentException(
                    "Unmatching foreign tables: " + foreignTableName + " != " + jpaColumnInfo.foreignTableName);
        }
        if (jpaColumnInfo.foreignColumnName != null && !jpaColumnInfo.foreignColumnName.isEmpty()) {
            if (foreignColumnName.isEmpty()) {
                foreignColumnName = jpaColumnInfo.foreignColumnName;
            } else {
                throw new IllegalArgumentException(
                        "Unmatching foreign columns: " + foreignColumnName + " != " + jpaColumnInfo.foreignColumnName);
            }
        }
        
        jpaColumnInfo.foreignSchemaName = foreignSchemaName;
        jpaColumnInfo.foreignTableName = foreignTableName;
        jpaColumnInfo.foreignColumnName = foreignColumnName;
    }

    private String extractColumnName(AnnotatedElement annotatedMember) {
        HoloColumn holoColumnAnnotation = annotatedMember.getAnnotation(HoloColumn.class);
        if (holoColumnAnnotation != null && !holoColumnAnnotation.name().isEmpty()) {
            return holoColumnAnnotation.name();
        }
        
        Column columnAnnotation = annotatedMember.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        
        return extractFieldName(annotatedMember);
    }

    private String extractFieldName(AnnotatedElement annotatedMember) {
        if (annotatedMember instanceof Field) {
            return ((Field) annotatedMember).getName();
        } else if (annotatedMember instanceof Method) {
            String memberName = ((Method) annotatedMember).getName();
            Matcher matcher = getMethodPattern.matcher(memberName);
            if (matcher.find()) {
                return matcher.group(1).toLowerCase() + matcher.group(2);
            } else {
                return memberName;
            }
        } else {
            throw new IllegalArgumentException("Member type not supported: " + annotatedMember.getClass());
        }
    }

    private HoloConfig renderConfig(Map<String, JpaSchemaInfo> schemas, LargeInteger seed) {
        List<HoloConfigSchema> schemaConfigs = new ArrayList<>(schemas.size());
        for (String schemaName : schemas.keySet()) {
            schemaConfigs.add(renderSchemaConfig(schemas, schemaName));
        }
        return new HoloConfig(seed, null, null, null, ImmutableList.fromCollection(schemaConfigs));
    }

    private HoloConfigSchema renderSchemaConfig(Map<String, JpaSchemaInfo> schemas, String schemaName) {
        JpaSchemaInfo jpaSchemaInfo = schemas.get(schemaName);
        List<HoloConfigTable> tableConfigs = new ArrayList<>(jpaSchemaInfo.tables.size());
        for (Map.Entry<String, JpaTableInfo> entry : jpaSchemaInfo.tables.entrySet()) {
            String tableName = entry.getKey();
            JpaTableInfo jpaTableInfo = entry.getValue();
            if (jpaTableInfo.annotatedElement.getAnnotation(HoloIgnore.class) == null) {
                tableConfigs.add(renderTableConfig(schemas, tableName, jpaTableInfo));
            }
        }
        return new HoloConfigSchema(schemaName, null, null, ImmutableList.fromCollection(tableConfigs));
    }


    private HoloConfigTable renderTableConfig(
            Map<String, JpaSchemaInfo> schemas, String tableName, JpaTableInfo jpaTableInfo) {
        boolean writeable = (jpaTableInfo.annotatedElement.getAnnotation(Immutable.class) != null);
        LargeInteger size = LargeInteger.TEN;
        HoloTable holoTableAnnotation = jpaTableInfo.annotatedElement.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null) {
            if (holoTableAnnotation.writeable() != HoloWriteable.UNDEFINED) {
                writeable = (holoTableAnnotation.writeable() == HoloWriteable.WRITEABLE);
            }
            if (holoTableAnnotation.size() != -1L) {
                size = LargeInteger.of(holoTableAnnotation.size());
            } else if (!holoTableAnnotation.largeSize().isEmpty()) {
                size = LargeInteger.of(holoTableAnnotation.largeSize());
            }
        }
        List<HoloConfigColumn> columnConfigs = new ArrayList<>(jpaTableInfo.columns.size());
        List<String> orderedColumnNames = new ArrayList<>(jpaTableInfo.columns.keySet());
        orderedColumnNames.sort((c1, c2) -> Integer.compare(
                findColumnPosition(jpaTableInfo.columnNamesInOrder, c1),
                findColumnPosition(jpaTableInfo.columnNamesInOrder, c2)));

        for (String columnName : orderedColumnNames) {
            JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.get(columnName);
            columnConfigs.add(renderColumnConfig(schemas, columnName, jpaTableInfo, jpaColumnInfo));
        }
        HoloVirtualColumn[] virtualColumnAnnotations =
                jpaTableInfo.annotatedElement.getAnnotationsByType(HoloVirtualColumn.class);
        for (HoloVirtualColumn virtualColumnAnnotation : virtualColumnAnnotations) {
            columnConfigs.add(renderVirtualColumn(virtualColumnAnnotation));
        }
        return new HoloConfigTable(
                tableName, writeable, size, null, ImmutableList.fromCollection(columnConfigs));
    }

    private int findColumnPosition(List<String> orderedFieldNames, String columnName) {
        int index = orderedFieldNames.indexOf(columnName);
        return index != -1 ? index : Integer.MAX_VALUE;
    }
    
    private HoloConfigColumn renderColumnConfig(
            Map<String, JpaSchemaInfo> schemas,
            String columnName,
            JpaTableInfo jpaTableInfo,
            JpaColumnInfo jpaColumnInfo) {
        HoloColumn holoColumnAnnotation = detectHoloColumnAnnotation(jpaColumnInfo);
        Class<?> type = detectColumnType(jpaColumnInfo);
        ColumnMode columnMode = detectColumnMode(columnName, jpaTableInfo, jpaColumnInfo, holoColumnAnnotation);
        return new HoloConfigColumn(
                columnName,
                type,
                columnMode,
                detectColumnNullCount(holoColumnAnnotation),
                detectColumnValues(holoColumnAnnotation),
                detectColumnValuesResource(holoColumnAnnotation),
                detectColumnValuesBundle(columnMode, holoColumnAnnotation),
                detectColumnValuesRange(jpaColumnInfo, columnMode, holoColumnAnnotation),
                detectColumnValuesPattern(holoColumnAnnotation),
                detectColumnValuesDynamicPattern(holoColumnAnnotation),
                detectColumnValuesForeignColumn(schemas, jpaColumnInfo, columnMode, holoColumnAnnotation),
                detectColumnDistributionQuality(holoColumnAnnotation),
                detectColumnShuffleQuality(holoColumnAnnotation),
                detectSourceFactory(holoColumnAnnotation),
                detectSourceFactoryData(holoColumnAnnotation),
                detectDefaultValue(holoColumnAnnotation));
    }

    private HoloColumn detectHoloColumnAnnotation(JpaColumnInfo jpaColumnInfo) {
        if (jpaColumnInfo.attribute == null) {
            return null;
        }
        
        Member member = jpaColumnInfo.attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return null;
        }
        
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        return annotatedMember.getAnnotation(HoloColumn.class);
    }
    
    private Class<?> detectColumnType(JpaColumnInfo jpaColumnInfo) {
        if (jpaColumnInfo.type != null) {
            return jpaColumnInfo.type;
        } else if (jpaColumnInfo.attribute != null) {
            return jpaColumnInfo.attribute.getJavaType();
        } else {
            return String.class; // FIXME
        }
    }
    
    private ColumnMode detectColumnMode(
            String columnName,
            JpaTableInfo jpaTableInfo,
            JpaColumnInfo jpaColumnInfo,
            HoloColumn holoColumnAnnotation) {
        if (jpaColumnInfo.mode != null) {
            return jpaColumnInfo.mode;
        }
        
        if (holoColumnAnnotation != null && holoColumnAnnotation.mode() != HoloColumnMode.UNDEFINED) {
            return columnModeOf(holoColumnAnnotation.mode());
        }
        
        if (jpaColumnInfo.attribute != null) {
            Class<?> type = jpaColumnInfo.attribute.getJavaType();
            boolean isId = !jpaTableInfo.idColumnName.isEmpty() && columnName.equals(jpaTableInfo.idColumnName);
            boolean isNumber = Number.class.isAssignableFrom(type);
            if (isId && isNumber) {
                return ColumnMode.COUNTER;
            }
        }
        
        return ColumnMode.DEFAULT;
    }
    
    private LargeInteger detectColumnNullCount(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.nullCount() != -1) {
            return LargeInteger.of(holoColumnAnnotation.nullCount());
        } else if (holoColumnAnnotation != null && !holoColumnAnnotation.largeNullCount().isEmpty()) {
            return LargeInteger.of(holoColumnAnnotation.largeNullCount());
        } else {
            return LargeInteger.ZERO;
        }
    }

    private ImmutableList<Object> detectColumnValues(HoloColumn holoColumnAnnotation) {
        List<Object> result = new ArrayList<>();
        if (holoColumnAnnotation != null && holoColumnAnnotation.values().length != 0) {
            result.addAll(Arrays.asList(holoColumnAnnotation.values()));
        }
        return ImmutableList.fromCollection(result);
    }

    private String detectColumnValuesResource(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesResource().isEmpty()) {
            return holoColumnAnnotation.valuesResource();
        } else {
            return null;
        }
    }

    private String detectColumnValuesBundle(ColumnMode columnMode, HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesBundle().isEmpty()) {
            return holoColumnAnnotation.valuesBundle();
        } else if (columnMode == ColumnMode.COUNTER || isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
            return null;
        } else {
            // TODO: guess
            return "lorem";
        }
    }
    
    private ImmutableList<LargeInteger> detectColumnValuesRange(
            JpaColumnInfo jpaColumnInfo, ColumnMode columnMode, HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.valuesRange().length != 0) {
            long[] valuesRange = holoColumnAnnotation.valuesRange();
            return ImmutableList.of(LargeInteger.of(valuesRange[0]), LargeInteger.of(valuesRange[1]));
        } else if (holoColumnAnnotation != null && holoColumnAnnotation.largeValuesRange().length != 0) {
            String[] largeValuesRange = holoColumnAnnotation.largeValuesRange();
            return ImmutableList.of(LargeInteger.of(largeValuesRange[0]), LargeInteger.of(largeValuesRange[1]));
        } else if (columnMode == ColumnMode.COUNTER || isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
            return null;
        } else if (jpaColumnInfo.attribute == null) {
            return null;
        } else if (Number.class.isAssignableFrom(jpaColumnInfo.attribute.getJavaType())) {
            return ImmutableList.of(LargeInteger.of(1L), LargeInteger.of(10L));
        } else {
            return null;
        }
    }

    private String detectColumnValuesPattern(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesPattern().isEmpty()) {
            return holoColumnAnnotation.valuesPattern();
        } else {
            return null;
        }
    }

    private String detectColumnValuesDynamicPattern(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesDynamicPattern().isEmpty()) {
            return holoColumnAnnotation.valuesDynamicPattern();
        } else {
            return null;
        }
    }
    
    private ImmutableList<String> detectColumnValuesForeignColumn(
            Map<String, JpaSchemaInfo> schemas,
            JpaColumnInfo jpaColumnInfo,
            ColumnMode columnMode,
            HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.valuesForeignColumn().length != 0) {
            return ImmutableList.of(holoColumnAnnotation.valuesForeignColumn());
        } else if (columnMode == ColumnMode.COUNTER || isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
            return null;
        }
        
        if (jpaColumnInfo.foreignTableName != null) {
            String foreignColumnName = jpaColumnInfo.foreignColumnName;
            if (foreignColumnName.isEmpty()) {
                foreignColumnName = schemas
                        .get(jpaColumnInfo.foreignSchemaName)
                        .tables
                        .get(jpaColumnInfo.foreignTableName)
                        .idColumnName;
            }
            if (!foreignColumnName.isEmpty()) {
                return ImmutableList.of(
                        jpaColumnInfo.foreignSchemaName, jpaColumnInfo.foreignTableName, foreignColumnName);
            }
        }
        
        return null;
    }

    private boolean isAnyValueFieldExplicitlySet(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation == null) {
            return false;
        }
        
        return
                holoColumnAnnotation.values().length != 0 ||
                !holoColumnAnnotation.valuesResource().isEmpty() ||
                !holoColumnAnnotation.valuesBundle().isEmpty() ||
                holoColumnAnnotation.valuesRange().length != 0 ||
                !holoColumnAnnotation.valuesPattern().isEmpty() ||
                !holoColumnAnnotation.valuesDynamicPattern().isEmpty() ||
                holoColumnAnnotation.valuesForeignColumn().length != 0;
    }

    private DistributionQuality detectColumnDistributionQuality(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null) {
            return distributionQualityOf(holoColumnAnnotation.distributionQuality());
        } else {
            return null;
        }
    }
    
    private ShuffleQuality detectColumnShuffleQuality(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null) {
            return shuffleQualityOf(holoColumnAnnotation.shuffleQuality());
        } else {
            return null;
        }
    }
    
    private Class<? extends SourceFactory> detectSourceFactory(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.sourceFactory() != Void.class) {
            return sourceFactoryClassOf(holoColumnAnnotation.sourceFactory());
        } else {
            return null;
        }
    }
    
    private Object detectSourceFactoryData(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null) {
            if (holoColumnAnnotation.sourceFactoryDataMap().length > 0) {
                Map<String, Object> result = new LinkedHashMap<>();
                for (HoloValue holoValue : holoColumnAnnotation.sourceFactoryDataMap()) {
                    String key = holoValue.key();
                    Object value = resolveValue(holoValue);
                    result.put(key, value);
                }
                return result;
            } else {
                HoloValue holoValue = holoColumnAnnotation.sourceFactoryData();
                return resolveValue(holoValue);
            }
        }
        
        return null;
    }

    private Object detectDefaultValue(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.defaultValue().isGiven()) {
            HoloValue holoValue = holoColumnAnnotation.defaultValue();
            return resolveValue(holoValue);
        }
        
        return null;
    }
    
    private HoloConfigColumn renderVirtualColumn(HoloVirtualColumn virtualColumnAnnotation) {
        return new HoloConfigColumn(
                virtualColumnAnnotation.name(),
                virtualColumnAnnotation.type(),
                columnModeOf(virtualColumnAnnotation.mode()),
                detectVirtualColumnNullCount(virtualColumnAnnotation),
                ImmutableList.of((Object[]) virtualColumnAnnotation.values()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesResource()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesBundle()),
                detectVirtualColumnValuesRange(virtualColumnAnnotation),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesPattern()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesDynamicPattern()),
                detectVirtualColumnValuesForeignColumn(virtualColumnAnnotation),
                detectVirtualColumnDistributionQuality(virtualColumnAnnotation),
                detectVirtualColumnShuffleQuality(virtualColumnAnnotation),
                detectVirtualSourceFactory(virtualColumnAnnotation),
                detectVirtualSourceFactoryData(virtualColumnAnnotation),
                detectVirtualDefaultValue(virtualColumnAnnotation));
    }
    
    private LargeInteger detectVirtualColumnNullCount(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.nullCount() != -1) {
            return LargeInteger.of(virtualColumnAnnotation.nullCount());
        } else if (!virtualColumnAnnotation.largeNullCount().isEmpty()) {
            return LargeInteger.of(virtualColumnAnnotation.largeNullCount());
        } else {
            return null;
        }
    }

    private ImmutableList<LargeInteger> detectVirtualColumnValuesRange(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.valuesRange().length != 0) {
            long[] valuesRange = virtualColumnAnnotation.valuesRange();
            return ImmutableList.of(LargeInteger.of(valuesRange[0]), LargeInteger.of(valuesRange[1]));
        } else if (virtualColumnAnnotation.largeValuesRange().length != 0) {
            String[] largeValuesRange = virtualColumnAnnotation.largeValuesRange();
            return ImmutableList.of(LargeInteger.of(largeValuesRange[0]), LargeInteger.of(largeValuesRange[1]));
        } else {
            return null;
        }
    }

    private ImmutableList<String> detectVirtualColumnValuesForeignColumn(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.valuesForeignColumn().length != 0) {
            return ImmutableList.of(virtualColumnAnnotation.valuesForeignColumn());
        } else {
            return null;
        }
    }

    private DistributionQuality detectVirtualColumnDistributionQuality(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation != null) {
            return distributionQualityOf(virtualColumnAnnotation.distributionQuality());
        } else {
            return null;
        }
    }
    
    private ShuffleQuality detectVirtualColumnShuffleQuality(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation != null) {
            return shuffleQualityOf(virtualColumnAnnotation.shuffleQuality());
        } else {
            return null;
        }
    }

    private Class<? extends SourceFactory> detectVirtualSourceFactory(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation != null && virtualColumnAnnotation.sourceFactory() != Void.class) {
            return sourceFactoryClassOf(virtualColumnAnnotation.sourceFactory());
        } else {
            return null;
        }
    }
    
    private Object detectVirtualSourceFactoryData(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation != null) {
            if (virtualColumnAnnotation.sourceFactoryDataMap().length > 0) {
                Map<String, Object> result = new LinkedHashMap<>();
                for (HoloValue holoValue : virtualColumnAnnotation.sourceFactoryDataMap()) {
                    String key = holoValue.key();
                    Object value = resolveValue(holoValue);
                    result.put(key, value);
                }
                return result;
            } else {
                HoloValue holoValue = virtualColumnAnnotation.sourceFactoryData();
                return resolveValue(holoValue);
            }
        } else {
            return null;
        }
    }

    private Object detectVirtualDefaultValue(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation != null) {
            HoloValue holoValue = virtualColumnAnnotation.defaultValue();
            return resolveValue(holoValue);
        } else {
            return null;
        }
    }
    
    private ColumnMode columnModeOf(HoloColumnMode holoColumnMode) {
        if (holoColumnMode != HoloColumnMode.UNDEFINED) {
            return ColumnMode.valueOf(holoColumnMode.name());
        } else {
            return null;
        }
    }

    private DistributionQuality distributionQualityOf(HoloColumnDistributionQuality holoColumnDistributionQuality) {
        if (holoColumnDistributionQuality != HoloColumnDistributionQuality.UNDEFINED) {
            return DistributionQuality.valueOf(holoColumnDistributionQuality.name());
        } else {
            return null;
        }
    }

    private ShuffleQuality shuffleQualityOf(HoloColumnShuffleQuality holoColumnShuffleQuality) {
        if (holoColumnShuffleQuality != HoloColumnShuffleQuality.UNDEFINED) {
            return ShuffleQuality.valueOf(holoColumnShuffleQuality.name());
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<SourceFactory> sourceFactoryClassOf(Class<?> clazz) {
        return (Class<SourceFactory>) clazz;
    }
    
    private Object resolveValue(HoloValue holoValue) {
        HoloValue.Type type = holoValue.type();
        switch (type) {
            case NULL:
                return null;
            case BOOLEAN:
                return holoValue.booleanValue();
            case BYTE:
                return holoValue.byteValue();
            case CHAR:
                return holoValue.charValue();
            case SHORT:
                return holoValue.shortValue();
            case INT:
                return holoValue.intValue();
            case LONG:
                return holoValue.longValue();
            case FLOAT:
                return holoValue.floatValue();
            case DOUBLE:
                return holoValue.doubleValue();
            case STRING:
                return holoValue.stringValue();
            case CLASS:
                return holoValue.classValue();
            case LARGE_INTEGER:
                return LargeInteger.of(holoValue.largeIntegerValue());
            case JSON:
                String dataJson = holoValue.json();
                try {
                    return new ObjectMapper().readValue(dataJson, Object.class);
                } catch (JsonProcessingException e) {
                    throw new UncheckedIOException(e);
                }
        }
        
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    private String nonEmptyStringOrNull(String value) {
        return value.isEmpty() ? null : value;
    }

    
    private class JpaSchemaInfo {
        
        private final Map<String, JpaTableInfo> tables = new TreeMap<>();
        
    }

    private class JpaTableInfo {
        
        private final Map<String, JpaColumnInfo> columns = new HashMap<>();

        private final List<String> columnNamesInOrder = new ArrayList<>();
        
        private AnnotatedElement annotatedElement = null;
        
        private String idColumnName = null;

    }
    
    private class JpaColumnInfo {
        
        private String foreignSchemaName = null;
        
        private String foreignTableName = null;
        
        private String foreignColumnName = null;
        
        private Class<?> type = null;
        
        private ColumnMode mode = null;
        
        private Attribute<?, ?> attribute = null;
        
    }
    
}
