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
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;

import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;

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
        String schemaName = defaultSchemaName;
        String tableName;
        if (metamodel instanceof MetamodelImpl) {
            MetamodelImpl hibernateMetamodel = (MetamodelImpl) metamodel;
            SingleTableEntityPersister entityPersister =
                    (SingleTableEntityPersister) hibernateMetamodel.entityPersister(entityClazz);
            tableName = entityPersister.getTableName();
        } else {
            Table tableAnnotation = entityClazz.getAnnotation(Table.class);
            if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
                tableName = tableAnnotation.name();
                if (!tableAnnotation.schema().isEmpty()) {
                    schemaName = tableAnnotation.schema();
                }
            } else {
                tableName = entityClazz.getSimpleName();
            }
        }
        
        JpaSchemaInfo jpaSchemaInfo = schemas.computeIfAbsent(schemaName, k -> new JpaSchemaInfo());
        JpaTableInfo jpaTableInfo = jpaSchemaInfo.tables.computeIfAbsent(tableName, k -> new JpaTableInfo());

        jpaTableInfo.idAttributeName = null;
        Type<?> idType = entityType.getIdType();
        if (idType != null) {
            jpaTableInfo.idAttributeName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        }
        
        jpaTableInfo.columnNamesInOrder.addAll(loadColumnNamesInOrder(metamodel, entityType));
        
        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            scanAttribute(schemas,attribute,  jpaTableInfo, defaultSchemaName);
        }
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
            Attribute<?, ?> attribute,
            JpaTableInfo jpaTableInfo,
            String defaultSchemaName) {
        PersistentAttributeType persistentAttributeType = attribute.getPersistentAttributeType();
        if (persistentAttributeType == PersistentAttributeType.ONE_TO_ONE) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType == PersistentAttributeType.ONE_TO_MANY) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType == PersistentAttributeType.MANY_TO_ONE) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType == PersistentAttributeType.MANY_TO_MANY) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType == PersistentAttributeType.EMBEDDED) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType == PersistentAttributeType.ELEMENT_COLLECTION) {
            
            // TODO
            return;
            
        } else if (persistentAttributeType != PersistentAttributeType.BASIC) {
            throw new IllegalArgumentException("Unsupported persistent attribute type: " + persistentAttributeType);
        }
        
        // TODO
        
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            throw new IllegalArgumentException("Unknown member type: " + member);
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        String columnName = extractColumnName(annotatedMember);
        JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.computeIfAbsent(columnName, k -> new JpaColumnInfo());
        jpaColumnInfo.attribute = attribute;
    }

    private String extractColumnName(AnnotatedElement annotatedMember) {
        Column columnAnnotation = annotatedMember.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        } else {
            return extractFieldName(annotatedMember);
        }
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
            tableConfigs.add(renderTableConfig(schemas, tableName, jpaTableInfo));
        }
        return new HoloConfigSchema(schemaName, tableConfigs);
    }


    private HoloConfigTable renderTableConfig(
            Map<String, JpaSchemaInfo> schemas, String tableName, JpaTableInfo jpaTableInfo) {
        boolean writeable = true; // TODO
        BigInteger size = BigInteger.TEN; // TODO
        List<HoloConfigColumn> columnConfigs = new ArrayList<>(jpaTableInfo.columns.size());
        List<String> orderedColumnNames = new ArrayList<>(jpaTableInfo.columns.keySet());
        orderedColumnNames.sort((c1, c2) -> Integer.compare(
                findColumnPosition(jpaTableInfo.columnNamesInOrder, jpaTableInfo.columns.get(c1)),
                findColumnPosition(jpaTableInfo.columnNamesInOrder, jpaTableInfo.columns.get(c2))));

        for (String columnName : orderedColumnNames) {
            JpaColumnInfo jpaColumnInfo = jpaTableInfo.columns.get(columnName);
            columnConfigs.add(renderColumnConfig(schemas, columnName, jpaTableInfo, jpaColumnInfo));
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
        if (!(member instanceof AnnotatedElement)) {
            throw new IllegalArgumentException("Member type not supported: " + member.getClass());
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        Class<?> type = extractType(annotatedMember);
        String attributeName = jpaColumnInfo.attribute.getName();
        boolean isId = jpaTableInfo.idAttributeName != null && attributeName.equals(jpaTableInfo.idAttributeName);
        boolean isNumber = Number.class.isAssignableFrom(type);
        
        // TODO
        ColumnMode mode = ColumnMode.DEFAULT;
        BigInteger nullCount = BigInteger.ZERO;
        List<Object> values = new ArrayList<>();
        List<BigInteger> valuesRange = null;
        String valuesBundle = null;
        List<String> valuesForeignColumn = null;
        
        if (isId && isNumber) {
            mode = ColumnMode.COUNTER;
        } else if (isNumber) {
            valuesRange = Arrays.asList(BigInteger.valueOf(1L), BigInteger.valueOf(5L));
        } else if (type == String.class) {
            valuesBundle = "lorem";
        } else {
            return null; // FIXME
        }
        
        return new HoloConfigColumn(
                columnName,
                type,
                mode,
                nullCount,
                values,
                null,
                valuesBundle,
                valuesRange,
                null,
                null,
                valuesForeignColumn);
    }

    private Class<?> extractType(AnnotatedElement annotatedMember) {
        if (annotatedMember instanceof Field) {
            return ((Field) annotatedMember).getType();
        } else if (annotatedMember instanceof Method) {
            return ((Method) annotatedMember).getReturnType();
        } else {
            throw new IllegalArgumentException("Member type not supported: " + annotatedMember.getClass());
        }
    }
    

    private class JpaSchemaInfo {
        
        private final Map<String, JpaTableInfo> tables = new TreeMap<>();
        
    }

    private class JpaTableInfo {
        
        private final Map<String, JpaColumnInfo> columns = new HashMap<>();

        private final List<String> columnNamesInOrder = new ArrayList<>();
        
        private String idAttributeName = null;

    }
    
    private class JpaColumnInfo {
        
        private String foreignSchema = null;
        
        private String foreignTable = null;
        
        private Attribute<?, ?> attribute = null;
        
    }
    
}
