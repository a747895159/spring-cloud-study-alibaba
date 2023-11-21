package com.person.zb.alibaba.study.common.utils;

import com.google.common.collect.Lists;
import com.person.zb.alibaba.study.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

/**
 * 图片工具类
 */
@Slf4j
public class ImageUtils {

    private static final Integer ZERO = 0;

    private static final Integer FIVE_ONE_TWO = 512;

    private static final Integer ONE_ZERO_TWO_FOUR = 1024;

    private static final Integer NINE_ZERO_ZERO = 900;

    private static final Integer THREE_TWO_SEVEN_FIVE = 3275;

    private static final Integer TWO_ZERO_FOUR_SEVEN = 2047;

    private static final Double ZERO_EIGHT_FIVE = 0.85;

    private static final Double ZERO_SIX = 0.6;

    private static final Double ZERO_FOUR_FOUR = 0.44;

    private static final Double ZERO_FOUR = 0.4;

    private static final List<String> IMAGE_SUFFIXS = Lists.newArrayList("jpg", "jpeg", "png", "bmp", "gif");

    /**
     * 校验图片格式是否正确
     */
    public static void checkImageFile(MultipartFile imagefile) {

        if (Objects.isNull(imagefile) || imagefile.isEmpty()) {
            throw new SystemException("图片不能为空文件");
        }

        String oirFilename = imagefile.getOriginalFilename();
        String suffix = StringUtils.substringAfterLast(oirFilename, ".").toLowerCase();

        if (!IMAGE_SUFFIXS.contains(suffix)) {
            throw new SystemException("图片类型不正确,请上传jpg|jpeg|png|bmp|gif类型的图片");
        }
    }

    /**
     * 不指定压缩大小则默认压缩到1024kb
     *
     * @param imageBytes 源图片字节数组
     * @return
     */
    public static byte[] compressPicForScale(byte[] imageBytes) {
        return compressPicForScale(imageBytes, ONE_ZERO_TWO_FOUR);
    }

    /**
     * 根据指定大小压缩图片 如果图片本身还没有指定的大小大 则不进行压缩
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize) {
        if (imageBytes == null || imageBytes.length <= ZERO || imageBytes.length < desFileSize * ONE_ZERO_TWO_FOUR) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        double accuracy = getAccuracy(srcSize / ONE_ZERO_TWO_FOUR);
        try {
            while (imageBytes.length > desFileSize * ONE_ZERO_TWO_FOUR) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            log.info("图片原大小={}kb, 压缩后大小={}kb", srcSize / ONE_ZERO_TWO_FOUR, imageBytes.length / ONE_ZERO_TWO_FOUR);
        } catch (Exception e) {
            log.error("图片压缩出现异常", e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     *
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long size) {
        double accuracy;
        if (size < NINE_ZERO_ZERO) {
            accuracy = ZERO_EIGHT_FIVE;
        } else if (size < TWO_ZERO_FOUR_SEVEN) {
            accuracy = ZERO_SIX;
        } else if (size < THREE_TWO_SEVEN_FIVE) {
            accuracy = ZERO_FOUR_FOUR;
        } else {
            accuracy = ZERO_FOUR;
        }
        return accuracy;
    }
}
