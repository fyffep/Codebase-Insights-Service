package configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.insightservice.springboot.serialize.PathSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

@TestConfiguration
public class ObjectMapperConfiguration
{
    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Path.class, new PathSerializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
