package com.person.zb.alibaba.study.common.example;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.Arrays;

/**
 * @author : ZhouBin
 */
public class BloomFilter001 {

    public static void main(String[] args) {

        String[] dataArr = {"a1", "b", "c", "d", "五"};
        //布隆过滤器（使用的过滤器，预期数据量，误判率越小越精准）
        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 10000, 0.01);
        Arrays.stream(dataArr).forEach(bloomFilter::put);
        // 返回 true
        System.out.println(bloomFilter.mightContain("五"));
        // 返回 false
        System.out.println(bloomFilter.mightContain("是"));
    }
}
