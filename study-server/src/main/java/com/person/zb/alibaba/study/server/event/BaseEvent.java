package com.person.zb.alibaba.study.server.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * @author : ZhouBin
 */
@Data
@AllArgsConstructor
public class BaseEvent<T> implements ResolvableTypeProvider {

    private T data;

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getData()));
    }
}
