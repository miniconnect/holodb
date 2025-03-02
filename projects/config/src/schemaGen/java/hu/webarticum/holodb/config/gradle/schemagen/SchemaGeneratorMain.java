package hu.webarticum.holodb.config.gradle.schemagen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public class SchemaGeneratorMain {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String className = args[0];
        String outputPath = args[1];
        
        Class<?> configClass = SchemaGeneratorMain.class.getClassLoader().loadClass(className);
        ObjectMapper mapper = JacksonSupport.createMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(configClass);
        
        File schemaOutFile = new File(outputPath);
        File schemaOutDirectory = schemaOutFile.getParentFile();
        schemaOutDirectory.mkdirs();
        try (OutputStream out = new FileOutputStream(schemaOutFile)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, schema);
        }
    }
    
}
