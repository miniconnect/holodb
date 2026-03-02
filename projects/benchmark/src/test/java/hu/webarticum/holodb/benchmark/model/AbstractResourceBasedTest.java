package hu.webarticum.holodb.benchmark.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;

public abstract class AbstractResourceBasedTest {

    protected <T> T loadYaml(String resourcePath, String resourceDescription, Class<T> type) throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(resourcePath, resourceDescription)) {
            return mapper.readValue(in, type);
        }
    }

    protected InputStream openResourceInputStream(String resourcePath, String resourceDescription) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        assertThat(in).as(resourceDescription).isNotNull();
        return in;
    }

}
