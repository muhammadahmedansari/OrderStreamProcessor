package order.stream.processor.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Constructs {@link ObjectMapper} instances with custom features enabled or disabled.
 */
final class ObjectMapperFactory {
    private ObjectMapperFactory() {
    }

    /* You may tweak the configuration of the mapper returned by this method as needed. */
    public static ObjectMapper createObjectMapper() {

        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();

        objectMapper.getFactory().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }
}