package hu.webarticum.holodb.config.gradle.schemagen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaDraft;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;

import scala.Option;
import scala.collection.JavaConverters;

import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public class SchemaGeneratorMain {
    
    private static final String ANY_VALUE_NAME = "AnyValue";

    public static void main(String[] args) throws IOException {
        String outputPath = args[0];
        
        ObjectMapper mapper = JacksonSupport.createMapper();
        JsonSchemaConfig config = createSchemaConfig(mapper);
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper, config);
        JsonNode schema = generator.generateJsonSchema(HoloConfig.class);
        fixAnyValueSchema(schema);
        
        File schemaOutFile = new File(outputPath);
        File schemaOutDirectory = schemaOutFile.getParentFile();
        schemaOutDirectory.mkdirs();
        try (OutputStream out = new FileOutputStream(schemaOutFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, schema);
        }
    }
    
    private static JsonSchemaConfig createSchemaConfig(ObjectMapper mapper) {
        Map<String, Supplier<JsonNode>> jsonSuppliers = new HashMap<>();
        jsonSuppliers.put(ANY_VALUE_NAME, JsonNodeFactory.instance::objectNode);
        Map<Class<?>, Class<?>> classTypeReMapping = new HashMap<>();
        classTypeReMapping.put(Object.class, AnyValue.class);
        return new JsonSchemaConfig(
                true,
                Option.empty(),
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                scala.collection.immutable.Map.<String, String>newBuilder().result(),
                false,
                scala.collection.immutable.Set.<Class<?>>newBuilder().result(),
                scala.collection.immutable.Map.from(JavaConverters.asScala(classTypeReMapping)),
                scala.collection.immutable.Map.from(JavaConverters.asScala(jsonSuppliers)),
                new com.kjetland.jackson.jsonSchema.SubclassesResolverImpl(),
                true,
                new Class<?>[0],
                JsonSchemaDraft.DRAFT_04
            );
    }

    private static void fixAnyValueSchema(JsonNode schema) {
        ObjectNode anyValueSchema = JsonNodeFactory.instance.objectNode();
        anyValueSchema.put("description", "Any kind of value");
        ((ObjectNode) schema.get("definitions")).set(ANY_VALUE_NAME, anyValueSchema);
    }

    @JsonSchemaInject(jsonSupplierViaLookup = ANY_VALUE_NAME)
    public static class AnyValue {}
    
}
