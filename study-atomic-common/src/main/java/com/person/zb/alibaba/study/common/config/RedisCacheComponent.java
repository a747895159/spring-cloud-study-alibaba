package com.person.zb.alibaba.study.common.config;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.person.zb.alibaba.study.common.functional.FunRtn;
import com.person.zb.alibaba.study.common.functional.WorkRtnFun;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Desc: Redis批量查询缓存
 * @Author: ZhouBin
 * @Date: 2021/9/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class RedisCacheComponent {

    public static final String CACHE_PRE = ":WMS:CACHE_";

    private RedisTemplate<String, Object> redisTemplate;


    public <T> T query(String cacheKey, WorkRtnFun<T> rtnFun, Integer cacheSecond) {
        String val = (String) redisTemplate.opsForValue().get(genCacheKey(cacheKey));
        if (val != null) {
            TypeReference<T> type = new TypeReference<T>() {
            };
            return JSONObject.parseObject(val, type);
        } else {
            T t = rtnFun.doWork();
            redisTemplate.opsForValue().set(genCacheKey(cacheKey), JSONObject.toJSONString(t), cacheSecond, TimeUnit.SECONDS);
            return t;
        }
    }

    public <P, T> List<T> queryList(String cacheKeyPre, Set<P> primarySet, FunRtn<List<P>, Map<P, T>> rtnFun, Integer cacheSecond) {
        List<T> rtnList = new ArrayList<>();
        Map<P, String> existMap = multiGet(cacheKeyPre, primarySet);
        Set<P> copyPrimarySet = new HashSet<>(primarySet);
        copyPrimarySet.removeAll(existMap.keySet());
        List<P> waitQueryList = new ArrayList<>(copyPrimarySet);
        if (existMap.size() > 0) {
            String valStr = JSONObject.toJSONString(existMap.values());
            TypeReference<List<T>> typeReference = new TypeReference<List<T>>() {
            };
            List<T> parseArray = JSONObject.parseObject(valStr, typeReference);
            rtnList.addAll(parseArray);
        }
        if (waitQueryList.size() > 0) {
            Map<P, T> queryDataMap = rtnFun.execute(waitQueryList);
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            if (queryDataMap != null && queryDataMap.size() > 0) {
                rtnList.addAll(queryDataMap.values());
                redisTemplate.execute((RedisCallback<String>) connection -> {
                    queryDataMap.forEach((p, t) -> {
                        connection.set(keySerializer.serialize(genCacheKey(cacheKeyPre, p)), valueSerializer.serialize(JSONObject.toJSONString(t)),
                                Expiration.seconds(cacheSecond), RedisStringCommands.SetOption.UPSERT);
                    });
                    return null;
                });
            }
            log.info("放置redis结束");
        }


        return rtnList;
    }


    private <P> Map<P, String> multiGet(String cacheKeyPre, Set<P> keySet) {
        log.info("redis中查询值: {}", JSONObject.toJSONString(keySet));
        List<P> keyList = new ArrayList<>(keySet);
        List<String> cacheKeyList = keyList.stream().map(o -> genCacheKey(cacheKeyPre, o)).collect(Collectors.toList());
        List<Object> list = this.redisTemplate.opsForValue().multiGet(cacheKeyList);
        Map<P, String> map = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null) {
                    map.put(keyList.get(i), (String) list.get(i));
                }
            }
        }
        log.info("redis返回值: {}", JSONObject.toJSONString(map));
        return map;
    }

    public void removeKey(String cacheKey) {
        redisTemplate.delete(genCacheKey(cacheKey));
    }

    public <P> void removeKey(String cacheKeyPre, Set<P> primarySet) {
        primarySet.forEach(p -> redisTemplate.delete(genCacheKey(cacheKeyPre, p)));
    }

    public String genCacheKey(Object... strArr) {
        StringBuilder sb = new StringBuilder(CACHE_PRE);
        for (Object n : strArr) {
            if (n != null) {
                sb.append(n);
            }
        }
        return sb.toString();
    }
}
