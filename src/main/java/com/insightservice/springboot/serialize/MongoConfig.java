package com.insightservice.springboot.serialize;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig
{
    /**
     * Customizes the serialization behavior when saving/retrieving from MongoDB.
     */
    @Bean
    public MongoCustomConversions customConversions(){
        List<Converter<?,?>> converters = new ArrayList<>();
        converters.add(PathSerializer.PathWriterConverter.INSTANCE);
        converters.add(PathSerializer.PathReaderConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }
}