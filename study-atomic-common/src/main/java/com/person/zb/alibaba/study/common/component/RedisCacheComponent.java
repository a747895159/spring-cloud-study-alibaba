package com.person.zb.alibaba.study.common.component;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.person.zb.alibaba.study.common.functional.FunRtn;
import com.person.zb.alibaba.study.common.functional.WorkRtnFun;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCacheComponent {

    public static final String CACHE_PRE = ":WMS:CACHE_";

    private String nameSpace;

    private RedisTemplate<String, String> redisTemplate;


    public <T> T query(String cacheKey, WorkRtnFun<T> rtnFun, Integer cacheSecond) {
        String val = redisTemplate.opsForValue().get(genCacheKey(cacheKey));
        if (val != null) {
            TypeReference<T> typeReference = new TypeReference<T>() {
            };
            return JSON.parseObject(val, typeReference);
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
        TypeReference<T> typeReference = new TypeReference<T>() {
        };
        if (existMap.size() > 0) {

            existMap.values().forEach(s -> {
                T t = JSONObject.parseObject(s, typeReference);
                rtnList.add(t);
            });
           /* String valStr = JSONObject.toJSONString(existMap.values());
            Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            List<T> parseArray = JSONArray.parseArray(valStr, entityClass);
            rtnList.addAll(parseArray);*/
        }
        if (waitQueryList.size() > 0) {
            Map<P, T> queryDataMap = rtnFun.execute(waitQueryList);
            Map<String, String> map = new HashMap<>();
            queryDataMap.forEach((p, t) -> {
                map.put(genCacheKey(cacheKeyPre, p), JSONObject.toJSONString(t));
            });
            redisTemplate.opsForValue().multiSet(map);
            rtnList.addAll(queryDataMap.values());
        }


       /* RedisSerializer keySerializer = redisTemplate.getKeySerializer();
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
        if (queryDataMap != null && queryDataMap.size() > 0) {
            rtnList.addAll(queryDataMap.values());
            redisTemplate.executePipelined((RedisCallback<String>) connection -> {
                queryDataMap.forEach((p, t) -> {
                    connection.set(keySerializer.serialize(p), valueSerializer.serialize(t),
                            Expiration.seconds(cacheSecond), RedisStringCommands.SetOption.UPSERT);
                });
                return null;
            });
        }*/
        return rtnList;
    }


    private <P> Map<P, String> multiGet(String cacheKeyPre, Set<P> keySet) {
        List<P> keyList = new ArrayList<>(keySet);
        List<String> cacheKeyList = keyList.stream().map(o -> genCacheKey(cacheKeyPre, o)).collect(Collectors.toList());
        List<String> list = this.redisTemplate.opsForValue().multiGet(cacheKeyList);
        Map<P, String> map = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null) {
                    map.put(keyList.get(i), list.get(i));
                }
            }
        }
        return map;
    }

    public void removeKey(String cacheKey) {
        redisTemplate.delete(genCacheKey(cacheKey));
    }

    public <P> void removeKey(String cacheKeyPre, Set<P> primarySet) {
        primarySet.forEach(p -> redisTemplate.delete(genCacheKey(cacheKeyPre, p)));
    }

    public String genCacheKey(Object... strArr) {
        StringBuilder sb = new StringBuilder(nameSpace + CACHE_PRE);
        for (Object n : strArr) {
            if (n != null) {
                sb.append(n);
            }
        }
        return sb.toString();
    }
}
