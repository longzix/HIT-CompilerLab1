package com.longstudy.compiler;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;
import lombok.ToString;
import org.apache.poi.ss.usermodel.FillPatternType;


/**
 * @anthor longzx
 * @create 2021 04 16 22:00
 * @Description
 **/
@Data
@ContentRowHeight(25)
@HeadRowHeight(25)
@ToString
@HeadStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
// 头字体设置成20
@HeadFontStyle(fontHeightInPoints = 18)
@ContentStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 10)
// 内容字体设置成20
@ContentFontStyle(fontHeightInPoints = 18)
public class Err{
    @ExcelProperty(value ="行号",index = 0)
    @ColumnWidth(10)
    private int line;
    @ExcelProperty(value ="错误提示",index = 1)
    @ColumnWidth(80)
    private String info;
    public Err(int line, String info) {
        this.line = line;
        this.info = info;
    }
}