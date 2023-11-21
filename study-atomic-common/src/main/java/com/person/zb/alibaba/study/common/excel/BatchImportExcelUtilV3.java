package com.person.zb.alibaba.study.common.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.util.PoiPublicUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.person.zb.alibaba.study.common.enums.ImportResultEnum;
import com.person.zb.alibaba.study.common.enums.YNEnum;
import com.person.zb.alibaba.study.common.excel.model.*;
import com.person.zb.alibaba.study.common.exception.SystemException;
import com.person.zb.alibaba.study.common.utils.ApiExportUtil;
import com.person.zb.alibaba.study.common.utils.BeanCopierUtil;
import com.person.zb.alibaba.study.common.utils.ExcelSheetImportUtil;
import com.person.zb.alibaba.study.common.utils.FileDownLoadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2022/5/5
 */
@Slf4j
public class BatchImportExcelUtilV3 {

    /**
     * OSS空间
     */
    public static final String OWN_SERVER = "wms";

    /**
     * 有效时间为7天
     */
    public static final Long INVALID_TIME = 7 * 24 * 60 * 60 * 1000L;


    public static <T> ImportResultV3DTO excelImportV3(ImportExcelRequestV3 request, Class<T> importRecordClazz, IExcelBatchImportContextV3<T> context, boolean exportSuccessFlag) {
        if (request.getId() == null) {
            //创建导入记录
            ImportResultDTO createImportResultDTO = buildImportRecord(request, null);
            Long importResultId = context.createImportResult(createImportResultDTO);
            request.setId(importResultId);
        }
        ImportResultV3DTO resultV3DTO = process(request, importRecordClazz, context, exportSuccessFlag);
        ImportResultDTO importResultDTO = BeanCopierUtil.copyProperties(resultV3DTO, ImportResultDTO.class);
        importResultDTO.setUpdatedAt(new Date());
        context.updateImportResult(importResultDTO);
        return resultV3DTO;
    }

    public static <T> ImportResultV3DTO process(ImportExcelRequestV3 request, Class<T> importRecordClazz, IExcelBatchImportContextV3<T> context, boolean exportSuccessFlag) {
        log.info("执行上传文件导入功能,导入参数:{}", JSONObject.toJSONString(request));
        String importUrl;
        try {
            importUrl = context.findFileDownloadUrl(request.getFileuuid(), OWN_SERVER, "1");
        } catch (Exception e) {
            log.error("记录: {},获取导入文件url失败", request.getId(), e);
            return ImportResultV3DTO.builder().id(request.getId()).warehouseId(request.getWarehouseId())
                    .state(ImportResultEnum.EXECUTINGFAILED.getId()).result("数据导入上传的文件下载链接获取失败!").build();
        }
        try {
            //文件通用校验
            ExcelSheetImportUtil.validateExcelInfo(importUrl, request);
        } catch (SystemException e) {
            String result = StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : ImportResultEnum.EXECUTINGFAILED.getName();
            return ImportResultV3DTO.builder().id(request.getId()).warehouseId(request.getWarehouseId()).failUrl(null)
                    .importUrl(importUrl).state(ImportResultEnum.EXECUTINGFAILED.getId()).result(result).build();
        } catch (Exception e) {
            String result = ImportResultEnum.EXECUTINGFAILED.getName();
            return ImportResultV3DTO.builder().id(request.getId()).warehouseId(request.getWarehouseId()).failUrl(null)
                    .importUrl(importUrl).state(ImportResultEnum.EXECUTINGFAILED.getId()).result(result).build();
        }
        int importNum, successNum = 0, failNum;
        List<T> importRecordList;
        List<T> failRecordList = new ArrayList<>();
        ExcelImportResult<T> excelResult;
        long startTime = System.currentTimeMillis();
        try (InputStream inputStream = FileDownLoadUtil.downLoadFromUrl(importUrl)) {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            params.setNeedVerfiy(true);
            //读取指定有效行数,因含头含尾，顾尾部去1
            if (request.getRealRowNum() != null && request.getRealRowNum() > 1) {
                params.setReadRows(request.getRealRowNum() - 1);
            }
            excelResult = ExcelSheetImportUtil.importExcelMore(inputStream, importRecordClazz, params);
            importRecordList = excelResult.getList();
            importNum = excelResult.getList().size() + excelResult.getFailList().size();
            request.setExcelDataSize(importNum);
            failNum = excelResult.getFailList().size();
            log.info("导入文件解析结束耗时:{} s,总数 {},解析失败数 {}", calcSec(startTime), importNum, failNum);
            if (failNum > 0) {
                failRecordList.addAll(excelResult.getFailList());
            }
        } catch (Exception e) {
            log.error("记录: {},数据导入上传的文件载入失败", request.getId(), e);
            return ImportResultV3DTO.builder().id(request.getId()).warehouseId(request.getWarehouseId()).failUrl(null)
                    .importUrl(importUrl).state(ImportResultEnum.EXECUTINGFAILED.getId()).result("数据导入上传的文件载入失败!").build();
        }
        String successFileUrl = null;
        String failFileUrl = null;
        int totalSize = 0;
        if (CollectionUtils.isNotEmpty(importRecordList)) {
            BaseImportDataResponseV3<T> importExecuteResponse;
            totalSize = importRecordList.size();
            try {
                startTime = System.currentTimeMillis();
                importExecuteResponse = context.dataImportExecute(request, importRecordList);
                log.info("导入业务数据处理结束耗时:{},总条数:{}", calcSec(startTime), importRecordList.size());
            } catch (Exception e) {
                log.error("记录: {},执行导入时发生异常", request.getId(), e);
                return ImportResultV3DTO.builder().id(request.getId()).warehouseId(request.getWarehouseId()).failUrl(null)
                        .importUrl(importUrl).state(ImportResultEnum.EXECUTINGFAILED.getId()).result("执行导入时发生异常,请联系管理员!").build();
            }
            //记录导入成功的url
            if (exportSuccessFlag && CollectionUtils.isNotEmpty(importExecuteResponse.getSucList())) {
                successNum = importExecuteResponse.getSucList().size();
                SXSSFWorkbook successDataWb = getUploadBookSheet(importRecordClazz, importExecuteResponse.getSucList());
                UploadFileInfoVO uploadSuccessFileInfo = getUrlByDefinitionTime(successDataWb, String.format("%s-%s", "导入成功", request.getFileName()), context);
                if (uploadSuccessFileInfo != null) {
                    successFileUrl = context.findFileDownloadUrl(uploadSuccessFileInfo.getFileUuid(), OWN_SERVER, "7");
                }
            }
            if (CollectionUtils.isNotEmpty(importExecuteResponse.getFailList())) {
                failNum += importExecuteResponse.getFailList().size();
                failRecordList.addAll(importExecuteResponse.getFailList());
                if (successNum == 0) {
                    successNum = totalSize - importExecuteResponse.getFailList().size();
                }
            }
            if (CollectionUtils.isEmpty(importExecuteResponse.getFailList()) && CollectionUtils.isEmpty(importExecuteResponse.getSucList())) {
                successNum = totalSize;
            }

        }
        //记录导入失败的url
        if (CollectionUtils.isNotEmpty(failRecordList)) {
            startTime = System.currentTimeMillis();
            SXSSFWorkbook failDataWb = getUploadBookSheet(importRecordClazz, failRecordList);
            UploadFileInfoVO uploadFailFileInfo = getUrlByDefinitionTime(failDataWb, String.format("%s-%s", "导入失败", request.getFileName()), context);
            if (uploadFailFileInfo != null) {
                failFileUrl = context.findFileDownloadUrl(uploadFailFileInfo.getFileUuid(), OWN_SERVER, "7");
                log.info("导入失败文件上传结束耗时:{} s,总数 {},解析失败数 {}", calcSec(startTime), importNum, failNum);

            }
        }
        return fillResultDTO(request, importNum, successNum, failNum, successFileUrl, failFileUrl, importUrl);

    }

    private static ImportResultV3DTO fillResultDTO(ImportExcelRequestV3 request, int importNum, int successNum, int failNum, String successFileUrl, String failFileUrl, String importUrl) {
        String result = ImportResultEnum.EXECUTINGSUCCESS.getName();
        Integer state = ImportResultEnum.EXECUTINGSUCCESS.getId();
        if (successNum > 0 && failNum > 0) {
            state = ImportResultEnum.EXECUTING_PARTIAL_SUCCEED_DOWNLOAD_DETAIL.getId();
            result = ImportResultEnum.EXECUTING_PARTIAL_SUCCEED_DOWNLOAD_DETAIL.getName();
        }
        if (successNum == 0 && failNum > 0) {
            state = ImportResultEnum.EXECUTINGFAILED.getId();
            result = "导入失败,详情请下载明细查看";
        }
        if (successNum == 0 && failNum == 0) {
            state = ImportResultEnum.EXECUTINGFAILED.getId();
            result = ImportResultEnum.EXECUTINGFAILED.getName();
        }
        String commentUrl = StringUtils.isBlank(failFileUrl) ? importUrl : failFileUrl;
        return ImportResultV3DTO.builder().id(request.getId()).state(state).result(result).importNum(importNum).importUrl(importUrl)
                .successNum(successNum).successUrl(successFileUrl).failNum(failNum).failUrl(failFileUrl).build();
    }

    /**
     * 构建导入记录的对象
     */
    public static ImportResultDTO buildImportRecord(ImportExcelRequestV3 request, String importUrl) {
        Date currentTime = new Date();

        ImportResultDTO importResultDTO = new ImportResultDTO();
        importResultDTO.setWarehouseId(request.getWarehouseId());
        importResultDTO.setFileName(request.getFileName());
        importResultDTO.setRefObjectId(request.getRefObjectId());
        importResultDTO.setImportType(request.getType());
        importResultDTO.setImportUrl(importUrl);
        importResultDTO.setState(ImportResultEnum.EXECUTING.getId());
        importResultDTO.setResult(ImportResultEnum.EXECUTING.getName());
        importResultDTO.setCreatedAt(currentTime);
        importResultDTO.setCreatedBy(request.getUserId());
        importResultDTO.setUpdatedAt(currentTime);
        importResultDTO.setUpdatedBy(request.getUserId());
        importResultDTO.setIsDel(YNEnum.NO.getCode());

        return importResultDTO;
    }

    public static <T> SXSSFWorkbook getUploadBookSheet(Class<T> clazz, List<T> dataList) {
        SXSSFWorkbook wb = null;
        try {
            Field[] rows = PoiPublicUtil.getClassFields(clazz);
            List<String> titleList = new ArrayList<>();
            for (int i = 0; i < rows.length; i++) {
                Excel fields = rows[i].getAnnotation(Excel.class);
                if (fields != null && !titleList.contains(rows[i].getName() + "=" + fields.name())) {
                    titleList.add(rows[i].getName() + "=" + fields.name());
                }
            }
            String[] cellTitleName = titleList.toArray(new String[0]);
            //将VO List填充到Sheet里
            wb = ApiExportUtil.export(dataList, cellTitleName, "sheet1");
        } catch (Exception e) {
            log.error("创建Excel文件失败,异常信息:", e);
        }
        return wb;
    }

    public static UploadFileInfoVO getUrlByDefinitionTime(SXSSFWorkbook wb, String name, IExcelBatchImportContextV3 context) {
        UploadFileInfoVO uploadFileInfoVO = null;
        try {
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
            DiskFileItem item = (DiskFileItem) diskFileItemFactory.createItem("file", MediaType.TEXT_PLAIN_VALUE, true, name);
            OutputStream outputStream = item.getOutputStream();
            wb.write(outputStream);
            MultipartFile commonsMultipartFile = new CommonsMultipartFile(item);
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(commonsMultipartFile.getBytes());
            String md5 = MD5Utils.md5Hex(result);

            ExcelUploadParamsVO uploadParamsVO = new ExcelUploadParamsVO();
            uploadParamsVO.setFile(commonsMultipartFile);
            uploadParamsVO.setFileMd5(md5);
            uploadParamsVO.setGroupId("excel");
            uploadParamsVO.setInvalidTime(System.currentTimeMillis() + INVALID_TIME);
            uploadParamsVO.setOperatorId("admin");
            uploadParamsVO.setOwnServer("wms");
            uploadFileInfoVO = context.uploadFile(uploadParamsVO);
        } catch (Exception e) {
            log.error("文件上传服务器失败,异常信息:", e);
        }
        return uploadFileInfoVO;
    }

    private static double calcSec(long startTime) {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000.0;
    }
}
