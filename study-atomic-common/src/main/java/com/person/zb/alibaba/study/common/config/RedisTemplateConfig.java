package com.person.zb.alibaba.study.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/12/9
 */
@Configuration
@Slf4j
public class RedisTemplateConfig {

    @Value("spring.application.name")
    private String nameSpace;

    @Resource
    private RedisConnectionFactory redisConnectionFactory;


    @Bean
    public RedisCacheComponent redisCacheComponent(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheComponent(redisTemplate);
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> initRedisTemplate(@NotNull AddPrefixKeySerializer keySerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer valSerializer = new Jackson2JsonRedisSerializer(Object.class);
        valSerializer.setObjectMapper(initObjectMapper());

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valSerializer);
        redisTemplate.setHashValueSerializer(valSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate initStringRedisTemplate(@NotNull AddPrefixKeySerializer keySerializer) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        return template;
    }

    @Bean
    public AddPrefixKeySerializer initAddPrefixKeySerializer() {
        return new AddPrefixKeySerializer();
    }

    private ObjectMapper initObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // ??????????????????null??????
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // null????????????json???
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        // ??????????????????????????????toString????????????????????????getName()??????????????????false??????getName()??????
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        // ?????????????????????????????????toString????????????????????????getName()??????????????????false??????getName()??????
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        // ??????Json???????????????Java????????????????????????
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // ???????????????????????????null
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        // ??????????????????
        objectMapper.registerModule(new SimpleModule().addSerializer(java.sql.Date.class, new DateSerializer()));
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }

    public class AddPrefixKeySerializer extends StringRedisSerializer {

        @Override
        public String deserialize(byte[] bytes) {
            if (bytes == null) {
                return null;
            } else {
                String deserialize = super.deserialize(bytes);
                return deserialize == null ? null : deserialize.replace(nameSpace, "");
            }
        }

        @Override
        public byte[] serialize(String string) {
            if (string == null) {
                return null;
            } else {
                String body = nameSpace + string;
                return super.serialize(body);
            }
        }
    }
}
