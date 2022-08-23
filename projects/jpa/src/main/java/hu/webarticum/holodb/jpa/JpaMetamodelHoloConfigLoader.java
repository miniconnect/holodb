package hu.webarticum.holodb.jpa;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;

import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.config.HoloConfigColumn;
import hu.webarticum.holodb.app.config.HoloConfigSchema;
import hu.webarticum.holodb.app.config.HoloConfigTable;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.holodb.app.config.HoloConfigColumn.ColumnMode;

public class JpaMetamodelHoloConfigLoader {
    
    private final Pattern getMethodPattern = Pattern.compile("^get([A-Z])(.*)$");
    

    public HoloConfig load(Metamodel metamodel, String selectedSchemaName, BigInteger seed) {
        Map<String, List<HoloConfigTable>> tablesBySchema = new TreeMap<>();
        for (EntityType<?> entityType : metamodel.getEntities()) {
            addTable(tablesBySchema, metamodel, selectedSchemaName, entityType);
        }
        List<HoloConfigSchema> schemas = new ArrayList<>(tablesBySchema.size());
        for (Map.Entry<String, List<HoloConfigTable>> entry : tablesBySchema.entrySet()) {
            String schemaName = entry.getKey();
            List<HoloConfigTable> tables = entry.getValue();
            schemas.add(new HoloConfigSchema(schemaName, tables));
        }
        return new HoloConfig(seed, schemas);
    }

    private void addTable(
            Map<String, List<HoloConfigTable>> tablesBySchema,
            Metamodel metamodel,
            String selectedSchemaName,
            EntityType<?> entityType) {
        String schemaName = selectedSchemaName; // TODO
        String tableName;
        boolean writeable = true; // TODO
        BigInteger tableSize = BigInteger.valueOf(20L); // TODO
        Class<?> entityClazz = entityType.getJavaType();
        if (metamodel instanceof MetamodelImpl) {
            MetamodelImpl hibernateMetamodel = (MetamodelImpl) metamodel;
            SingleTableEntityPersister entityPersister =
                    (SingleTableEntityPersister) hibernateMetamodel.entityPersister(entityClazz);
            tableName = entityPersister.getTableName();
        } else {
            Table tableAnnotation = entityClazz.getAnnotation(Table.class);
            if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
                tableName = tableAnnotation.name();
            } else {
                tableName = entityClazz.getSimpleName();
            }
        }
        String idAttributeName = null;
        Type<?> idType = entityType.getIdType();
        if (idType != null) {
            idAttributeName = entityType.getId(entityType.getIdType().getJavaType()).getName();
        }
        List<Attribute<?, ?>> attributes = new ArrayList<>(entityType.getAttributes());
        sortAttributes(entityClazz, attributes);
        List<HoloConfigColumn> columns = new ArrayList<>(attributes.size());
        for (Attribute<?, ?> attribute : attributes) {
            boolean isId = idAttributeName != null && attribute.getName().equals(idAttributeName);
            HoloConfigColumn columnConfig = createColumn(attribute, isId, tableSize);
            if (columnConfig != null) {
                columns.add(columnConfig);
            }
        }
        HoloConfigTable table = new HoloConfigTable(tableName, writeable, tableSize, columns);
        List<HoloConfigTable> tables = tablesBySchema.computeIfAbsent(schemaName, k -> new ArrayList<>());
        tables.add(table);
    }

    private void sortAttributes(Class<?> entityClazz, List<Attribute<?, ?>> attributes) {
        ImmutableList<String> orderedFieldNames = ImmutableList.of(entityClazz.getDeclaredFields()).map(Field::getName);
        attributes.sort((a1, a2) -> Integer.compare(
                findAttributeField(orderedFieldNames, a1),
                findAttributeField(orderedFieldNames, a2)));
    }
    
    private int findAttributeField(ImmutableList<String> orderedFieldNames, Attribute<?, ?> attribute) {
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            return -1;
        }
        String fieldName = extractFieldName((AnnotatedElement) member);
        return orderedFieldNames.indexOf(fieldName);
    }

    private HoloConfigColumn createColumn(Attribute<?, ?> attribute, boolean isId, BigInteger tableSize) {
        Member member = attribute.getJavaMember();
        if (!(member instanceof AnnotatedElement)) {
            throw new IllegalArgumentException("Member type not supported: " + member.getClass());
        }
        AnnotatedElement annotatedMember = (AnnotatedElement) member;
        String columnName = extractName(annotatedMember);
        Class<?> type = extractType(annotatedMember);
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
    
    private String extractName(AnnotatedElement annotatedMember) {
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

    private Class<?> extractType(AnnotatedElement annotatedMember) {
        if (annotatedMember instanceof Field) {
            return ((Field) annotatedMember).getType();
        } else if (annotatedMember instanceof Method) {
            return ((Method) annotatedMember).getReturnType();
        } else {
            throw new IllegalArgumentException("Member type not supported: " + annotatedMember.getClass());
        }
    }
    
}
