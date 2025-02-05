package com.person.zb.alibaba.study.server.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : ZhouBin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigKeyEvent {

    private ConfigEventEnum eventEnum;

    private List<String> keys;
}
