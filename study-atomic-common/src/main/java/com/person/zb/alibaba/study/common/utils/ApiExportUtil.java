package com.person.zb.alibaba.study.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
public class ApiExportUtil {
    private final static String DATETYPE = "java.util.Date";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static SXSSFWorkbook export(List list, String[] cellTitleName, String titleName) throws Exception {
        // 创建excel
        SXSSFWorkbook wb = new SXSSFWorkbook();
        // 创建sheet
        SXSSFSheet sheet = createSheet(wb, cellTitleName, titleName);
        // 创建第一行（标题行）
        Short height = 400;//  目的是想把行高设置成400px
        SXSSFRow rowTitle = createRowTitle(sheet, height);
        // 创建标题栏样式
        CellStyle styleTitle = createCellStyle(wb);

        batchWrite(list,wb,sheet,cellTitleName,rowTitle,styleTitle,0);
        log.info("生成SXSSFWorkbook结束");
        return wb;
    }

    // 创建sheet
    public static SXSSFSheet createSheet(SXSSFWorkbook wb,String[] cellTitleName,String titleName){

        SXSSFSheet sheet = wb.createSheet(titleName);
        // 自适应宽度
        /*for (int i=0;i<cellTitleName.length;i++){
            sheet.setColumnWidth(i,400);
        }*/
        for (int columnNum = 0; columnNum <= cellTitleName.length; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                SXSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }
                if (currentRow.getCell(columnNum) != null) {
                    SXSSFCell currentCell = currentRow.getCell(columnNum);

                    if (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(columnNum, columnWidth * 256*2);
        }
        return sheet;
    }
    // 创建第一行（标题行）
    public static SXSSFRow createRowTitle(SXSSFSheet sheet,short height){
        // 创建第一行（标题行）
        SXSSFRow rowTitle = sheet.createRow(0);
        rowTitle.setHeight(height); //  目的是想把行高设置成xxx px
        return rowTitle;
    }

    // 创建标题栏样式
    public static CellStyle createCellStyle(SXSSFWorkbook wb){
        CellStyle styleTitle = wb.createCellStyle();
        styleTitle.setAlignment(styleTitle.ALIGN_CENTER);// 水平居中
        styleTitle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直居中
        styleTitle.setFillForegroundColor((short) 13);      // 设置背景色
        styleTitle.setFillPattern(styleTitle.SOLID_FOREGROUND);
        styleTitle.setBorderBottom(styleTitle.BORDER_THIN);  //  下边框
        styleTitle.setBorderLeft(styleTitle.BORDER_THIN);    //  左边框
        styleTitle.setBorderTop(styleTitle.BORDER_THIN);     //  上边框
        styleTitle.setBorderRight(styleTitle.BORDER_THIN);   //  右边框
        // 设置标题字体
        Font fontTitle = wb.createFont();
        // 宋体加粗
        //fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontTitle.setFontName("宋体");
        fontTitle.setFontHeight((short) 300);
        fontTitle.setFontHeightInPoints((short) 12);        //  设置字体大小
        return styleTitle;
    }

    public static SXSSFWorkbook batchExport(List<List> recordList, String[] cellTitleName, String titleName) throws Exception {
        // 创建excel
        SXSSFWorkbook wb = new SXSSFWorkbook();
        // 创建sheet
        SXSSFSheet sheet = createSheet(wb, cellTitleName, titleName);
        // 创建第一行（标题行）
        Short height = 400;//  目的是想把行高设置成400px
        SXSSFRow rowTitle = createRowTitle(sheet, height);
        // 创建标题栏样式
        CellStyle styleTitle = createCellStyle(wb);

        //List<Object> list = recordList;
        if (CollectionUtils.isEmpty(recordList)){
            // 在第一行上创建标题列
            SXSSFCell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("暂无数据");
            log.info("生成SXSSFWorkbook结束");
            return wb;
        }
        int index = 0;
        for (List list:recordList) {
            batchWrite(list,wb,sheet,cellTitleName,rowTitle,styleTitle,index);
            index+=list.size();
        }
        log.info("生成SXSSFWorkbook结束");
        return wb;
    }

    public static void createTitleToFirstRow(SXSSFRow rowTitle,CellStyle styleTitle,String[] cellTitleNameNew,int cellInde){
        // 在第一行上创建标题列
        SXSSFCell cellTitle = null;

        for (String str : cellTitleNameNew) {
            cellTitle = rowTitle.createCell(cellInde);
            if (StringUtils.isEmpty(str)) {
                cellTitle.setCellValue("未定义");
            } else if (str.split("=").length == 1) {
                cellTitle.setCellValue(str.split("=")[0]);
            } else if (StringUtils.isEmpty(str.split("=")[1])) {
                cellTitle.setCellValue(str.split("=")[0]);
            } else {
                cellTitle.setCellValue(str.split("=")[1]);
            }
            cellTitle.setCellStyle(styleTitle);
            cellInde = cellInde + 1;
        }
    }
    public static void packgeRow(List list,SXSSFWorkbook wb,SXSSFSheet sheet,String[] cellTitleNameNew,int cellInde,int rowIndex) throws Exception {
        // 创建内容
        CellStyle styleCenter = wb.createCellStyle();
        styleCenter.setAlignment(CellStyle.ALIGN_RIGHT);    // 右对齐
        int listIndex = 0;
        for (int i = 0+rowIndex; i < list.size()+rowIndex; i++) {
            Object map = list.get(listIndex);
            Class<?> clazz = map.getClass();
            SXSSFRow row = sheet.createRow(i + 1);
            cellInde = 0;
            SXSSFCell cell = null;
            for (String str : cellTitleNameNew) {
                cell = row.createCell(cellInde);
                Field declaredField = clazz.getDeclaredField(str.split("=")[0]);
                declaredField.setAccessible(true);
                Class<?> type = declaredField.getType();
                if (declaredField.get(map) == null) {
                    cell.setCellValue("");
                } else {
                    Object cellValue = declaredField.get(map);
                    String cellResult = dataFormat(cellValue, type.getTypeName());
                    cell.setCellValue(cellResult);
                }
                cell.setCellStyle(styleCenter);
                cellInde = cellInde + 1;
            }
            listIndex = listIndex+1;
        }
    }

    public static void batchWrite(List list1,SXSSFWorkbook wb,SXSSFSheet sheet,String[] cellTitleName,SXSSFRow rowTitle,CellStyle styleTitle,int rowIndex) throws Exception {
        List<Object> list = (List<Object>) list1;
        if (list.size() > 0) {
            // 表头以自定义为准
            String[] cellTitleNameNew = cellTitleName;
            // 在第一行上创建标题列
            int cellInde = 0;
            if (rowIndex==0) {
                createTitleToFirstRow(rowTitle, styleTitle, cellTitleNameNew, cellInde);
            }
            packgeRow(list,wb,sheet,cellTitleNameNew,cellInde,rowIndex);
        } else {
            // 在第一行上创建标题列
            SXSSFCell cellTitle = rowTitle.createCell(0);
            cellTitle.setCellValue("暂无数据");
        }
    }
    /**
     * 时间格式化
     *
     * @param
     * @return
     */
    public static String dataFormat(Object cellValue,String type) {
        if (DATETYPE.equals(type)){
            return simpleDateFormat.format((Date) cellValue);
        }
        return cellValue.toString();
    }

    /**
     * 重新整理表头
     *
     * @param cellTitleName
     * @param listFirstMap
     * @return
     */
    public String[] getNewHead(String[] cellTitleName, Map<String, Object> listFirstMap) {
        String[] cellTitleNameNew = new String[listFirstMap.size()];        // 动态表头
        String[] cellTitleQuery = new String[listFirstMap.size()];
        Set set = listFirstMap.entrySet();
        int cellIndex = 0;
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry mapentry = (Map.Entry) iterator.next();
            cellTitleQuery[cellIndex] = mapentry.getKey().toString();
            cellIndex++;
        }

        int cellIndexNew = 0;
        List inserListt = new ArrayList();
        // 根据定义 获取动态表头 并安装固定顺序排序
        for (String str : cellTitleName) {
            for (String strQuery : cellTitleQuery) {
                if (!StringUtils.isEmpty(str) && !inserListt.contains(strQuery) && str.split("=")[0].equals(strQuery)) {
                    inserListt.add(strQuery);
                    if (str.split("=").length == 2) {
                        cellTitleNameNew[cellIndexNew] = strQuery + "=" + str.split("=")[1];
                    } else if (str.split("=").length == 1) {
                        cellTitleNameNew[cellIndexNew] = strQuery + "=" + str.split("=")[0];
                    }
                    cellIndexNew++;
                    break;
                }
            }
        }

        // 校验定义表头 添加返回结果中未在定义中的属性
        boolean tag;
        int cellIndexNull = listFirstMap.size() - 1;
        for (String strQuery : cellTitleQuery) {
            tag = false;
            for (int cellIndexNewValue = 0; cellIndexNewValue <= (cellIndexNew - 1); cellIndexNewValue++) {
                if (strQuery.equals(cellTitleNameNew[cellIndexNewValue].split("=")[0])) {
                    tag = true;
                    break;
                }
            }
            if (!tag) {
                cellTitleNameNew[cellIndexNull] = strQuery + "=未定义" + strQuery;
                cellIndexNull--;
            }
        }

        return cellTitleNameNew;
    }
}
