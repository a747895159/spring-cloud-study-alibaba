package com.person.zb.alibaba.study.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Slf4j
public class FileDownLoadUtil {

    public static InputStream downLoadFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为5秒
            conn.setConnectTimeout(20 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            return conn.getInputStream();
        } catch (Exception e) {
            log.error("[down获取文件inputstream失败] url:{}", urlStr, e);
            throw new RuntimeException(e);
        }
    }
}
