package hu.webarticum.holodb.config.gradle.schemagen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public class SchemaGeneratorMain {

    public static void main(String[] args) throws IOException {
        String outputPath = args[0];
        
        ObjectMapper mapper = JacksonSupport.createMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(HoloConfig.class);
        
        File schemaOutFile = new File(outputPath);
        File schemaOutDirectory = schemaOutFile.getParentFile();
        schemaOutDirectory.mkdirs();
        try (OutputStream out = new FileOutputStream(schemaOutFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, schema);
        }
    }
    
}
