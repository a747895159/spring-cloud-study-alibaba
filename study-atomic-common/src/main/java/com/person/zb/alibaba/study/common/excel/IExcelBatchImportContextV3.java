package com.person.zb.alibaba.study.common.excel;



import com.person.zb.alibaba.study.common.excel.model.ExcelUploadParamsVO;
import com.person.zb.alibaba.study.common.excel.model.ImportExcelRequestV3;
import com.person.zb.alibaba.study.common.excel.model.ImportResultDTO;
import com.person.zb.alibaba.study.common.excel.model.UploadFileInfoVO;

import java.util.List;

public interface IExcelBatchImportContextV3<T> {

    /**
     * 执行文件导入逻辑
     *
     * @param list Excel读取的记录
     * @return 执行结果
     */
    BaseImportDataResponseV3<T> dataImportExecute(ImportExcelRequestV3 request, List<T> list);

    /**
     * 通过uuid查询文件下载地址
     *
     * @param fileUuid  文件uuid
     * @param ownServer 空间名
     * @param validity  有效期
     * @return 文件下载地址
     */
    String findFileDownloadUrl(String fileUuid, String ownServer, String validity);

    /**
     * 上传文件
     *
     * @param excelUploadParamsVO 文件参数
     * @return 服务器上文件的信息
     */
    UploadFileInfoVO uploadFile(ExcelUploadParamsVO excelUploadParamsVO);

    /**
     * 新增导入记录
     *
     * @param importResultDTO 导入记录对象
     * @return 导入记录ID
     */
    Long createImportResult(ImportResultDTO importResultDTO);

    /**
     * 更新导入记录
     *
     * @param importResultDTO 更新记录对象
     */
    void updateImportResult(ImportResultDTO importResultDTO);

    /**
     * 导入类型
     */
    int getType();


}
