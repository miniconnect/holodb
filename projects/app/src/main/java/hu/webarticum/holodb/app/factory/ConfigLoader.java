package hu.webarticum.holodb.app.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public class ConfigLoader {
    
    public enum ContentType {
        
        JSON(MappingJsonFactory::new),
        YAML(YAMLFactory::new),
        XML(XmlFactory::new);
        
        
        private Supplier<JsonFactory> jsonFactorySupplier;
        
        
        ContentType(Supplier<JsonFactory> jsonFactorySupplier) {
            this.jsonFactorySupplier = jsonFactorySupplier;
        }
        
        
        public JsonFactory createJsonFactory() {
            return jsonFactorySupplier.get();
        }
        
    }
    
    
    private final Supplier<Reader> readerSupplier;
    
    private final ContentType contentType;
    

    public ConfigLoader(String resource) {
        this(() -> createResourceReader(resource), detectContentType(resource));
    }
    
    public ConfigLoader(File file) {
        this(file, detectContentType(file.getName()));
    }
    
    public ConfigLoader(File file, ContentType contentType) {
        this(() -> createFileReader(file), contentType);
    }
    
    public ConfigLoader(Supplier<Reader> readerSupplier, ContentType contentType) {
        this.readerSupplier = readerSupplier;
        this.contentType = contentType;
    }

    
    private static ContentType detectContentType(String pathOrName) {
        if (pathOrName.endsWith(".json")) {
            return ContentType.JSON;
        } else if (pathOrName.endsWith(".xml")) {
            return ContentType.XML;
        } else {
            return ContentType.YAML;
        }
    }

    private static Reader createResourceReader(String resource) {
        InputStream resourceIn = ConfigLoader.class.getClassLoader().getResourceAsStream(resource);
        if (resourceIn == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        return new InputStreamReader(resourceIn);
    }
    
    private static Reader createFileReader(File file) {
        try {
            return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    
    public HoloConfig load() {
        ObjectMapper mapper = new ObjectMapper(contentType.createJsonFactory());
        mapper.registerModule(JacksonSupport.createModule());
        try {
            return mapper.readValue(readerSupplier.get(), HoloConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
}
