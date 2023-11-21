package com.person.zb.alibaba.study.common.utils;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.excel.imports.ExcelImportService;
import com.person.zb.alibaba.study.common.excel.model.ImportExcelRequest;
import com.person.zb.alibaba.study.common.excel.model.ImportExcelRequestV3;
import com.person.zb.alibaba.study.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static cn.afterturn.easypoi.util.PoiCellUtil.getCellValue;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2022/5/5
 */
@Slf4j
public class ExcelSheetImportUtil {

    private static final Long MB_CONVERT = 1024 * 1024L;

    /**
     * 默认导入最多条数
     */
    private static final Integer DEFAULT_MAX_LINE = 10000;

    /**
     * 导入最小条数
     */
    private static final Integer IMPORT_MIN_LIMIT = 1;

    /**
     * 默认导入文件大小（MB）
     */
    private static final int DEFAULT_FILE_MAX_SIZE = 2;

    /**
     * 两种类型Excel的文件后缀
     */
    private static final String XLS_SUFFIX = "xls";

    private static final String XLSX_SUFFIX = "xlsx";

    /**
     * 校验excel文件格式与大小
     *
     * @param maxFileSize 最大(MB)
     */
    public static void validateExcelSize(ImportExcelRequest request, int maxFileSize) {
        if (request.getFileSize() > maxFileSize * MB_CONVERT) {
            throw new SystemException(String.format("上传文件最大支持%sM", maxFileSize));
        }
        if (!XLS_SUFFIX.equalsIgnoreCase(request.getFileType()) && !XLSX_SUFFIX.equalsIgnoreCase(request.getFileType())) {
            throw new SystemException("数据导入上传的文件格式不正确");
        }

    }

    /**
     * 校验导入文件的 大小、多Sheet、行数等
     */
    public static void validateExcelInfo(String importUrl, ImportExcelRequestV3 request) {
        log.info("开始【{}】文件大小 {} KB.....", request.getFileName(), request.getFileSize() / 1024);
        int importMaxFileSize = request.getMaxFileSize() == null ? DEFAULT_FILE_MAX_SIZE : request.getMaxFileSize();
        validateExcelSize(request, importMaxFileSize);
        try (InputStream is = FileDownLoadUtil.downLoadFromUrl(importUrl)) {
            Workbook hssfWorkbook;
            if (XLS_SUFFIX.equalsIgnoreCase(request.getFileType())) {
                hssfWorkbook = new HSSFWorkbook(is);
            } else {
                hssfWorkbook = new XSSFWorkbook(is);
            }
            log.info("创建workbook结束.....");
            int numberOfSheets = hssfWorkbook.getNumberOfSheets();
            if (numberOfSheets < 1) {
                throw new SystemException("上传的Excel文件为空!");
            }
            // 取excel中第一个sheet
            Sheet sheet = hssfWorkbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            Integer importMaxLimit = request.getMaxLineNum() == null ? DEFAULT_MAX_LINE : request.getMaxLineNum();
            if (lastRowNum < IMPORT_MIN_LIMIT) {
                throw new SystemException(String.format("导入数量超过上限%s!", IMPORT_MIN_LIMIT));
            } else if (lastRowNum > importMaxLimit) {
                throw new SystemException(String.format("当前数据有%s行,超过%s行限制,请删除多余或空行数据!", lastRowNum, importMaxLimit));
            }
            int realRowNum = getRealRowNum(sheet);
            log.info("excel最大行数 {},有效数据行数 {}", lastRowNum, realRowNum);
            request.setRealRowNum(realRowNum);
        } catch (IOException e) {
            throw new SystemException("文件读取失败，请重试!");
        }
    }


    /**
     * 重新 ExcelImportUtil.importExcelMore 工具类实现
     * 将 needMore 流程中 失败数据 重写 excel等流程省略，避免大数量下，严重堆积占用内存
     * 此处 返回的 ExcelImportResult 只有 部分数据有值
     */
    public static <T> ExcelImportResult<T> importExcelMore(InputStream inputStream, Class<T> pojoClass, ImportParams params) throws Exception {
        final ExcelImportService excelImportService = new ExcelImportService();
        ExcelImportResult<T> excelImportResult = excelImportService.importExcelByIs(inputStream, pojoClass, params, false);
        final Field failCollection = ExcelImportService.class.getDeclaredField("failCollection");
        final Field verifyFailField = ExcelImportService.class.getDeclaredField("verfiyFail");
        failCollection.setAccessible(true);
        verifyFailField.setAccessible(true);
        List<T> failList = (List) failCollection.get(excelImportService);
        List<T> sucList = excelImportResult.getList();
        ExcelImportResult<T> importResult = new ExcelImportResult<T>();
        //校验失败数据
        importResult.setFailList(failList == null ? new ArrayList<>() : failList);
        //校验失败结果
        importResult.setVerfiyFail((Boolean) verifyFailField.get(excelImportService));
        //校验成功数据
        importResult.setList(sucList == null ? new ArrayList<>() : sucList);
        return importResult;
    }


    /**
     * 使用POI读取EXCEL时，使用getLastRowNum()方法会把没有值的行也获取到（比如行中有空格）。空行并没有意义，此方法返回有效的行数。
     */
    public static int getRealRowNum(Sheet sheet) {
        int rowNum = sheet.getLastRowNum();
        while (rowNum > 0) {
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                for (Cell cell : row) {
                    if (StringUtils.isNotEmpty(getCellValue(cell))) {
                        return rowNum;
                    }
                }
            }
            rowNum--;
        }
        return rowNum;
    }
}
