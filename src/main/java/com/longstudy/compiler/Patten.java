package com.longstudy.compiler;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.*;
import lombok.Data;

/**
 * @anthor longzx
 * @create 2021 04 16 22:12
 * @Description
 **/
@Data
@ContentRowHeight(25)
@HeadRowHeight(25)
@ColumnWidth(25)
// 头字体设置成20
@HeadFontStyle(fontHeightInPoints = 18)
@ContentFontStyle(fontHeightInPoints = 15)
public class Patten{//输出格式
    @ColumnWidth(10)
    @ExcelProperty(value ="行号",index = 0)
    private int line;
    @ColumnWidth(15)
    @ExcelProperty(value ="种别码",index = 2)
    private int code;
    @ExcelProperty(value ="token",index = 3)
    private String token;
    @ExcelProperty(value ="类型",index = 1)
    private String type;//种类
    public Patten(int line, int code,String token, String type) {
        this.line = line;
        this.code = code;
        this.token = token;
        this.type = type;
    }
    @Override
    public String toString() {
        return "line=" + line +"    <"+ type  + " , " +  "  "  + code+" ,  "  +  token+"  >";
    }
}
