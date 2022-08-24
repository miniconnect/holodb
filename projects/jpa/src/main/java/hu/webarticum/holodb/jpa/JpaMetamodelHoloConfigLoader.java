package hu.webarticum.holodb.jpa;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.annotations.Immutable;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;
import hu.webarticum.holodb.jpa.annotation.HoloColumn;
import hu.webarticum.holodb.jpa.annotation.HoloColumnMode;
import hu.webarticum.holodb.jpa.annotation.HoloIgnore;
import hu.webarticum.holodb.jpa.annotation.HoloTable;
import hu.webarticum.holodb.jpa.annotation.HoloVirtualColumn;

// TODO: ignore column vs throw exception?
public class JpaMetamodelHoloConfigLoader {
    
    private final Pattern getMethodPattern = Pattern.compile("^get([A-Z])(.*)$");
    

    public HoloConfig load(Metamodel metamodel, String defaultSchemaName, BigInteger seed) {
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
        jpaTableInfo.managedType = entityType;
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

    private String extractSchemaName(Class<?> entityClazz, String defaultSchemaName) {
        HoloTable holoTableAnnotation = entityClazz.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null && !holoTableAnnotation.schema().isEmpty()) {
            return holoTableAnnotation.schema();
        }
        
        Table tableAnnotation = entityClazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.schema().isEmpty()) {
            return tableAnnotation.schema();
        }
        
        return defaultSchemaName;
    }

    private String extractTableName(Metamodel metamodel, Class<?> entityClazz) {
        HoloTable holoTableAnnotation = entityClazz.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null && !holoTableAnnotation.name().isEmpty()) {
            return holoTableAnnotation.name();
        }
        
        if (metamodel instanceof MetamodelImpl) {
            MetamodelImpl hibernateMetamodel = (MetamodelImpl) metamodel;
            EntityPersister entityPersister = hibernateMetamodel.entityPersister(entityClazz);
            if (entityPersister instanceof SingleTableEntityPersister) {
                return ((SingleTableEntityPersister) entityPersister).getTableName();
            }
        }
        
        Table tableAnnotation = entityClazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        
        return entityClazz.getSimpleName();
    }

    private List<String> loadColumnNamesInOrder(Metamodel metamodel, EntityType<?> entityType) {
        List<String> result = new ArrayList<>();
        addColumnNames(result, metamodel, entityType);
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
            scanManyToManyAttribute();
        } else if (persistentAttributeType == PersistentAttributeType.EMBEDDED) {
            scanEmbeddedAttribute(
                    schemas, metamodel, schemaName, tableName, jpaTableInfo, attribute, defaultSchemaName);
        } else if (persistentAttributeType == PersistentAttributeType.ELEMENT_COLLECTION) {
            scanElementCollectionAttribute();
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
                targetJpaSchemaInfo.tables.computeIfAbsent(targetTableName, k -> new JpaTableInfo());
        JoinColumn joinColumnAnnotation = annotatedMember.getAnnotation(JoinColumn.class);
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

    private void scanManyToManyAttribute( /* TODO */ ) {
        // TODO
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

    private void scanElementCollectionAttribute( /* TODO */ ) {
        // TODO
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

    private HoloConfig renderConfig(Map<String, JpaSchemaInfo> schemas, BigInteger seed) {
        List<HoloConfigSchema> schemaConfigs = new ArrayList<>(schemas.size());
        for (String schemaName : schemas.keySet()) {
            schemaConfigs.add(renderSchemaConfig(schemas, schemaName));
        }
        return new HoloConfig(seed, schemaConfigs);
    }

    private HoloConfigSchema renderSchemaConfig(Map<String, JpaSchemaInfo> schemas, String schemaName) {
        JpaSchemaInfo jpaSchemaInfo = schemas.get(schemaName);
        List<HoloConfigTable> tableConfigs = new ArrayList<>(jpaSchemaInfo.tables.size());
        for (Map.Entry<String, JpaTableInfo> entry : jpaSchemaInfo.tables.entrySet()) {
            String tableName = entry.getKey();
            JpaTableInfo jpaTableInfo = entry.getValue();
            Class<?> entityClazz = jpaTableInfo.managedType.getJavaType();
            if (entityClazz.getAnnotation(HoloIgnore.class) == null) {
                tableConfigs.add(renderTableConfig(schemas, tableName, jpaTableInfo));
            }
        }
        return new HoloConfigSchema(schemaName, tableConfigs);
    }


    private HoloConfigTable renderTableConfig(
            Map<String, JpaSchemaInfo> schemas, String tableName, JpaTableInfo jpaTableInfo) {
        Class<?> entityClazz = jpaTableInfo.managedType.getJavaType();
        boolean writeable = (entityClazz.getAnnotation(Immutable.class) != null);
        BigInteger size = BigInteger.TEN;
        HoloTable holoTableAnnotation = entityClazz.getAnnotation(HoloTable.class);
        if (holoTableAnnotation != null) {
            if (holoTableAnnotation.size() != -1L) {
                size = BigInteger.valueOf(holoTableAnnotation.size());
            } else if (!holoTableAnnotation.largeSize().isEmpty()) {
                size = new BigInteger(holoTableAnnotation.largeSize());
            }
        }
        List<HoloConfigColumn> columnConfigs = new ArrayList<>(jpaTableInfo.columns.size());
        List<String> orderedColumnNames = new ArrayList<>(jpaTableInfo.columns.keySet());
        orderedColumnNames.sort((c1, c2) -> Integer.compare(
                findColumnPosition(jpaTableInfo.columnNamesInOrder, jpaTableInfo.columns.get(c1)),
                findColumnPosition(jpaTableInfo.columnNamesInOrder, jpaTableInfo.columns.get(c2))));

        for (String columnName : orderedColumnNames) {
            JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.get(columnName);
            columnConfigs.add(renderColumnConfig(schemas, columnName, jpaTableInfo, jpaColumnInfo));
        }
        for (HoloVirtualColumn virtualColumnAnnotation : entityClazz.getAnnotationsByType(HoloVirtualColumn.class)) {
            columnConfigs.add(renderVirtualColumn(virtualColumnAnnotation));
        }
        return new HoloConfigTable(tableName, writeable, size, columnConfigs);
    }

    private int findColumnPosition(List<String> orderedFieldNames, JpaColumnInfo jpaColumnInfo) {
        Member member = jpaColumnInfo.attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return Integer.MAX_VALUE;
        }
        String fieldName = extractFieldName((AnnotatedElement) member);
        int index = orderedFieldNames.indexOf(fieldName);
        return index != -1 ? index : Integer.MAX_VALUE;
    }

    private HoloConfigColumn renderColumnConfig(
            Map<String, JpaSchemaInfo> schemas,
            String columnName,
            JpaTableInfo jpaTableInfo,
            JpaColumnInfo jpaColumnInfo) {
        Member member = jpaColumnInfo.attribute.getJavaMember();
        HoloColumn holoColumnAnnotation = null;
        if (member instanceof AnnotatedElement) {
            holoColumnAnnotation = ((AnnotatedElement) member).getAnnotation(HoloColumn.class);
        }
        Class<?> type = jpaColumnInfo.attribute.getJavaType();
        return new HoloConfigColumn(
                columnName,
                type,
                detectColumnMode(columnName, jpaTableInfo, jpaColumnInfo, holoColumnAnnotation),
                detectColumnNullCount(holoColumnAnnotation),
                detectColumnValues(holoColumnAnnotation),
                detectColumnValuesResource(holoColumnAnnotation),
                detectColumnValuesBundle(holoColumnAnnotation),
                detectColumnValuesRange(jpaColumnInfo, holoColumnAnnotation),
                detectColumnValuesPattern(holoColumnAnnotation),
                detectColumnValuesDynamicPattern(holoColumnAnnotation),
                detectColumnValuesForeignColumn(schemas, jpaColumnInfo, holoColumnAnnotation));
    }
    
    private ColumnMode detectColumnMode(
            String columnName,
            JpaTableInfo jpaTableInfo,
            JpaColumnInfo jpaColumnInfo,
            HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.mode() != HoloColumnMode.UNDEFINED) {
            return holoColumnAnnotation.mode().columnMode();
        }
        
        Class<?> type = jpaColumnInfo.attribute.getJavaType();
        boolean isId = !jpaTableInfo.idColumnName.isEmpty() && columnName.equals(jpaTableInfo.idColumnName);
        boolean isNumber = Number.class.isAssignableFrom(type);
        
        if (isId && isNumber) {
            return ColumnMode.COUNTER;
        }
        
        return ColumnMode.DEFAULT;
    }
    
    private BigInteger detectColumnNullCount(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.nullCount() != -1) {
            return BigInteger.valueOf(holoColumnAnnotation.nullCount());
        } else if (holoColumnAnnotation != null && !holoColumnAnnotation.largeNullCount().isEmpty()) {
            return new BigInteger(holoColumnAnnotation.largeNullCount());
        }
        
        return BigInteger.ZERO;
    }

    private List<Object> detectColumnValues(HoloColumn holoColumnAnnotation) {
        List<Object> result = new ArrayList<>();
        if (holoColumnAnnotation != null && holoColumnAnnotation.values().length != 0) {
            result.addAll(Arrays.asList(holoColumnAnnotation.values()));
        }
        return result;
    }

    private String detectColumnValuesResource(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesResource().isEmpty()) {
            return holoColumnAnnotation.valuesResource();
        }
        return null;
    }

    private String detectColumnValuesBundle(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesBundle().isEmpty()) {
            return holoColumnAnnotation.valuesBundle();
        } else if (isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
            return null;
        }
        // TODO: guess
        return "lorem";
    }
    
    private List<BigInteger> detectColumnValuesRange(JpaColumnInfo jpaColumnInfo, HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.valuesRange().length != 0) {
            long[] valuesRange = holoColumnAnnotation.valuesRange();
            return Arrays.asList(BigInteger.valueOf(valuesRange[0]), BigInteger.valueOf(valuesRange[1]));
        } else if (holoColumnAnnotation != null && holoColumnAnnotation.largeValuesRange().length != 0) {
            String[] largeValuesRange = holoColumnAnnotation.largeValuesRange();
            return Arrays.asList(new BigInteger(largeValuesRange[0]), new BigInteger(largeValuesRange[1]));
        } else if (isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
            return null;
        }
        
        Class<?> type = jpaColumnInfo.attribute.getJavaType();
        if (Number.class.isAssignableFrom(type)) {
            return Arrays.asList(BigInteger.valueOf(1L), BigInteger.valueOf(10L));
        }
        
        return null;
    }

    private String detectColumnValuesPattern(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesPattern().isEmpty()) {
            return holoColumnAnnotation.valuesPattern();
        }
        
        return null;
    }

    private String detectColumnValuesDynamicPattern(HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && !holoColumnAnnotation.valuesDynamicPattern().isEmpty()) {
            return holoColumnAnnotation.valuesDynamicPattern();
        }
        
        return null;
    }
    
    private List<String> detectColumnValuesForeignColumn(
            Map<String, JpaSchemaInfo> schemas, JpaColumnInfo jpaColumnInfo, HoloColumn holoColumnAnnotation) {
        if (holoColumnAnnotation != null && holoColumnAnnotation.valuesForeignColumn().length != 0) {
            return Arrays.asList(holoColumnAnnotation.valuesForeignColumn());
        } else if (isAnyValueFieldExplicitlySet(holoColumnAnnotation)) {
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
                return Arrays.asList(
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

    private HoloConfigColumn renderVirtualColumn(HoloVirtualColumn virtualColumnAnnotation) {
        return new HoloConfigColumn(
                virtualColumnAnnotation.name(),
                virtualColumnAnnotation.type(),
                virtualColumnAnnotation.mode().columnMode(),
                detectVirtualColumnNullCount(virtualColumnAnnotation),
                Arrays.asList((Object[]) virtualColumnAnnotation.values()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesResource()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesBundle()),
                detectVirtualColumnValuesRange(virtualColumnAnnotation),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesPattern()),
                nonEmptyStringOrNull(virtualColumnAnnotation.valuesDynamicPattern()),
                detectVirtualColumnValuesForeignColumn(virtualColumnAnnotation));
    }
    
    private BigInteger detectVirtualColumnNullCount(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.nullCount() != -1) {
            return BigInteger.valueOf(virtualColumnAnnotation.nullCount());
        } else if (!virtualColumnAnnotation.largeNullCount().isEmpty()) {
            return new BigInteger(virtualColumnAnnotation.largeNullCount());
        } else {
            return null;
        }
    }

    private List<BigInteger> detectVirtualColumnValuesRange(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.valuesRange().length != 0) {
            long[] valuesRange = virtualColumnAnnotation.valuesRange();
            return Arrays.asList(BigInteger.valueOf(valuesRange[0]), BigInteger.valueOf(valuesRange[1]));
        } else if (virtualColumnAnnotation.largeValuesRange().length != 0) {
            String[] largeValuesRange = virtualColumnAnnotation.largeValuesRange();
            return Arrays.asList(new BigInteger(largeValuesRange[0]), new BigInteger(largeValuesRange[1]));
        } else {
            return null;
        }
    }

    private List<String> detectVirtualColumnValuesForeignColumn(HoloVirtualColumn virtualColumnAnnotation) {
        if (virtualColumnAnnotation.valuesForeignColumn().length != 0) {
            return Arrays.asList(virtualColumnAnnotation.valuesForeignColumn());
        } else {
            return null;
        }
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
        
        private ManagedType<?> managedType = null;
        
        private String idColumnName = null;

    }
    
    private class JpaColumnInfo {
        
        private String foreignSchemaName = null;
        
        private String foreignTableName = null;
        
        private String foreignColumnName = null;
        
        private Attribute<?, ?> attribute = null;
        
    }
    
}
