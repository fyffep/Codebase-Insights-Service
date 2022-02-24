package com.insightservice.springboot.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Changes the JSON form of a Path so that it is
 * displayed relative to the root of the user's cloned repo, not as an absolute path.
 */
@JsonComponent
public class PathSerializer extends JsonSerializer<Path>
{
    @Override
    public void serialize(Path path, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(path.toString());
    }
}