package com.insightservice.springboot.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

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

    @WritingConverter
    enum PathWriterConverter implements Converter<Path, String>
    {
        INSTANCE;

        @Override
        public String convert(Path path) {
            return path.toString();
        }
    }

    @ReadingConverter
    enum PathReaderConverter implements Converter<String, Path>
    {
        INSTANCE;

        @Override
        public Path convert(String pathString) {
            return Path.of(pathString);
        }
    }
}