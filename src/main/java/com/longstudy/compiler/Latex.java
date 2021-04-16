package com.longstudy.compiler;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @anthor longzx
 * @create 2021 04 15 20:53
 * @Description
 **/
/*
public class Testclass{
    static int numb = 100;
    char value = 'p';

    String str = "mystring";

    float f= 3.1245;
    int a += numb;

    char c ='4545';
}
 */

/**
 * 其他种别码
 *  十进制 无符号数  1
 *  八进制 无符号数  2
 *  十六进制 无符号数  3
 * 无符号浮点数  4
 * 字符串 5
 * 字符  6
 * 标识符 7
 */
public class Latex {

    static Map<String,Integer> keyWorldMap ;//关键字
    static Map<String,Integer> operationMap ; //运算符
    static Map<String,Integer> doubleOperationMap ; //两位运算符
    static Map<String,Integer> symbolMap ;  //界符
    static StringBuilder text = new StringBuilder();
    static List<Patten> ansList = new ArrayList<>();
    static List<Err> error = new ArrayList<>();//存储错误信息
    public static void main(String[] args) {
        //初始化关键字
        inint();
        //for (int i = 1; i <= 5; i++) {
            //读取文件
            readFile("src\\main\\java\\com\\longstudy\\compiler\\test\\code"+"1"+".txt");
            //testNum();
            analyze();
            for (Patten obj : ansList){
                System.out.println(obj.toString());
            }
            for (Err info : error){
                System.out.println(info.toString());
            }
            writerFile("src\\main\\java\\com\\longstudy\\compiler\\result\\result"+"1"+".xlsx");
       // }

    }

    //扫描分析核心代码
    public static void analyze(){

        String[] texts = text.toString().split("\n");
        boolean hasNote = false;//判断是否存在多行注释
        for(int m = 0; m < texts.length; m++) {
            String str = texts[m];
            if (str.equals(" ") ||str.equals("") )//跳过空行
                continue;

            char[] strline = str.toCharArray();
            int index =0;
            if(hasNote){//如果有多行注释  找到多行注释的结尾
                for (; index < strline.length; index++) {
                    if(strline[index] == '*'){
                        if(index<strline.length-1 && strline[index] == '/'){
                            //找到
                            index +=2;//跳过两个
                            hasNote =false;
                            break;
                        }
                    }
                }
            }
            else {//开始分析
                //将字符串转化为字符串数组
                for(int i = index; i < strline.length; i++){
                    //遍历strline中的每个字符
                    char ch = strline[i];
                    //初始化token字符串为空
                    StringBuilder token = new StringBuilder();
                    //判断标识和关键字 //以字母开头
                    if (isAlpha(ch)){
                        // 判断关键字和标识符
                        do {
                            token.append(ch);
                            i++;
                            if(i >= strline.length) break;
                            ch = strline[i];
                        } while (ch != '\0' && (isAlpha(ch) || isDigit(ch)));//连续的字母，下划线和数字
                        --i; //由于指针加1,指针回退
                        //是关键字
                        if (isMatchKeyword(token.toString()))//是关键字
                        {
                            ansList.add(new Patten(m+1,keyWorldMap.get(token.toString()),token.toString(),"关键字") );

                        }
                        else{//是标识符
                            ansList.add(new Patten(m+1,7,token.toString(),"标识符") );
                        }
                    }
                    //判断数字常量 包括 8进制 和16进制
                    else if(isDigit(ch)){
                       int state = 0;
                        //boolean isfloat = false;
                       if(ch == '0'){//以0开头
                           state=7;
                           i++;
                           if (i >= strline.length) {
                               ansList.add(new Patten(m+1,1,"0","整型常量") );
                               break;
                           }
                           ch = strline[i];
                           while ( (ch !=' ')&&(ch != '\0') && !isSingleOp(ch) && !symbolMap.containsKey(String.valueOf(ch)) && state !=11){
                               switch (state){
                                   case 7:
                                       if(ch>='0' &&ch<='7'){
                                           token.append(ch);
                                           state=8;
                                       }else if(ch=='x' || ch=='X'){
                                           state =9;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 8:
                                       if(ch>='0' &&ch<='7') {
                                           token.append(ch);
                                           state = 8;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 9:
                                       if(ch>='0'&&ch<='9' || ch >='a' && ch <='f' || ch >= 'A'&& ch<='F'){
                                           state = 10;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 10:
                                       if(ch>='0'&&ch<='9' || ch >='a' && ch <='f' || ch >= 'A'&& ch<='F'){
                                           state = 10;
                                       }else {
                                           state =11;
                                       }
                                       break;

                               }
                               i++;
                               if (i >= strline.length) {
                                   ansList.add(new Patten(m+1,1,"0","整型常量") );
                                   break;
                               }
                               ch = strline[i];
                           }
                          /* //下一个是分界符或者操作符,或者0是最后一个
                           if(i+1==strline.length || i+1 <strline.length &&( isSingleOp(strline[i+1]) || symbolMap.containsKey(String.valueOf(strline[i+1])))){
                               //识别为整数0
                               ansList.add(new Patten(m+1,1,"0","整型常量") );
                               //i++;
                               continue;
                           }else if(i+1 <strline.length && (strline[i+1] >= '0' && strline[i+1] <='7') || strline[i+1] =='x'||strline[i+1] =='X'){
                               state =7;
                           }else {
                               state =11;//错
                           }*/

                       }else {
                           //初始化进入1状态 ,往十进制方向识别
                           state = 1;
                           while ( (ch !=' ')&&(ch != '\0') && !isSingleOp(ch) && !symbolMap.containsKey(String.valueOf(ch)) && state !=11) {//
//                               if (ch == '.' || ch == 'e') {
//                                   isfloat = true;
//                               }
                               switch (state) {
                                   case 1://状态1，读入 . 进入状态2，读入e 进入状态4
                                       if(isDigit(ch)){//是数字
                                           token.append(ch);
                                       }else if(ch == '.'){
                                           token.append(ch);
                                           state = 2;//进入状态2
                                       }else if(ch =='e' || ch=='E'){
                                           token.append(ch);
                                           state = 4;//进入状态4
                                       }else{//匹配出错
                                           state =11;
                                       }
                                       break;
                                   case 2://状态2，读入数字d 进入状态3
                                       if(isDigit(ch)){
                                           token.append(ch);
                                           state =3;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 3://状态3，读入数字d 进入状态3 读入e进入状态4
                                       if (isDigit(ch)){
                                           token.append(ch);
                                           state =3;
                                       }else if(ch =='e' || ch=='E'){
                                           token.append(ch);
                                           state =4;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 4://状态4，读入 -,+ 进入状态5，读入数字进入状态6
                                       if(ch=='-' || ch=='+'){
                                           token.append(ch);
                                           state =5;
                                       }else if(isDigit(ch)){
                                           token.append(ch);
                                           state =6;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 5://状态5， 度输入数字进入 状态 6
                                       if(isDigit(ch)){
                                           token.append(ch);
                                           state =6;
                                       }else {
                                           state =11;
                                       }
                                       break;
                                   case 6://状态6， 读入数字进入状态6
                                       if(isDigit(ch)){
                                           token.append(ch);
                                           state =6;
                                       }else {
                                           state =11;
                                       }
                                       break;
                               }
                               if (state ==11) { break;}//错误状态
                               //遍历符号先前移动
                               i++;
                               if (i >= strline.length) break;
                               ch = strline[i];
                           }
                       }
                       Boolean haveMistake = false;
                       if (state == 2 || state == 4 || state == 5 || state == 9 || state ==11)//非终结状态或出错状态
                       {
                           haveMistake = true;
                       }
                       //错误处理
                       if (haveMistake)
                       {
                           //一直到“可分割”的字符结束,和操作符
                           while (ch != '\0' && ch != ',' && ch != ';' && ch != ' '&&!isSingleOp(ch))
                           {
                               token.append(ch);
                               i++;
                               if(i >= strline.length) break;
                               ch = strline[i];
                           }
                           error.add(new Err(m+1,"Lexical error at Line ["+String.valueOf(m+1)+"]: [确认无符号常数输入正确]."));
                       }
                       else {
                           if (state==3 || state ==6) {
                               ansList.add(new Patten(m+1,4,token.toString(),"浮点型常量") ); }
                           else if(state ==1){
                               ansList.add(new Patten(m+1,1,token.toString(),"整型常量") ); }
                           else if(state ==8){
                               ansList.add(new Patten(m+1,2,token.toString(),"8进制整型常量") ); }
                           else {
                               ansList.add(new Patten(m+1,3,token.toString(),"16进制整型常量") ); }
                       }
                       i--;
                    }

                    //识别字符常量
                    else if(ch == '\''){
                        //初始化进入1状态
                        int state = 1;
                        token.append(ch);
                        for (int j = 0; j <3 ; j++) {//往后识别两个字符
                            i++;
                            //char c ='\\';
                            if(i>=strline.length){break;}
                            if(state==3 || state ==6|| state ==4){break;}
                            ch = strline[i];
                            switch (state){
                                case 1:
                                    if(ch == '\''){
                                        state =6;//空字符错误
                                    }
                                    if(ch == '\\'){//识别到了转义符
                                        state =5;
                                        token.append(ch);
                                    }else {
                                        state =2;
                                        token.append(ch);
                                    }
                                    break;
                                case 2:
                                    if(ch =='\''){
                                        token.append(ch);
                                        state =3;//终结状态
                                        ansList.add(new Patten(m+1,7,token.toString(),"字符常量") );
                                    }else {
                                        state=4;//字符超出一个,且未封闭
                                    }
                                    break;
                                case 5:
                                    if(ch == '\\'||ch == 'n' || ch =='b' || ch =='r'||ch =='t'){//几个常见的转义符
                                        token.append(ch);
                                        state =2;
                                    }else {
                                        state =4;//错误
                                    }
                                    break;
                            }
                        }

                        if (state ==4){//字符个数超出一个，
                            error.add(new Err(m+1,"Lexical error at Line ["+String.valueOf(m+1)+"]: [字符常量格式不正确].") );
                            //一直跳过直到引号封闭或者有空格为止
                            while (i<strline.length && strline[i] != '\'' && strline[i] != ' '){
                                i++;
                            }
                        }
                        if (state ==6){
                            error.add(new Err(m+1,"Lexical error at Line ["+String.valueOf(m+1)+"]: [空字符错误]."));
                        }
                        if (state ==1) {
                            //ansList.add(new Object[] {token, "字符常量", "302", m+1});
                            error.add(new Err(m+1,"Lexical error at Line ["+String.valueOf(m+1)+"]: [单引号未封闭]."));
                        }

                    }
                    //识别字符串常量
                    else if (ch == '"'){
                        //初始化进入1状态
                        int state = 1;
                       // String s ="";
                        token.append(ch);
                        i++;
                        while(i<strline.length && state !=2){
                            ch = strline[i];
                            token.append(ch);
                            if(ch == '"'){
                                state =2;//结束状态
                            }
                            i++;
                        }
                        if(state==1){//字符串为封闭
                            //ansList.add(new Object[] {m+1, token + " 字符串常量引号未封闭"});
                            error.add(new Err(m+1,"Lexical error at Line ["+String.valueOf(m+1)+"]: [字符串常量引号未封闭]."));
                            //已经读完一整行 ，不用再回退
                        }else {
                            //ansList.add(new Object[] {token, "字符串常量", "303", m+1});
                            ansList.add(new Patten(m+1,5,token.toString(),"字符串常量") );
                        }
                    }
                    //运算符,单个和多个， 这里不包括 除号 / ，/ 在注释里识别
                    else if (ch != '/' && isSingleOp(ch)){
                        StringBuilder s = new StringBuilder();
                        s.append(ch);
                        boolean isTow = false;
                        //判断是否是两位的操作符
                        if(i+1<strline.length){
                            s.append(strline[i+1]);
                        }
                        if(doubleOperationMap.containsKey(s.toString())){
                            i++;
                            isTow = true;
                        }
                        if (isTow) {//两位操作符
                            token.append(s.toString());
                            ansList.add(new Patten(m+1,doubleOperationMap.get(token.toString()),token.toString(),"操作符") );
                        } else {
                            token.append(ch);
                            ansList.add(new Patten(m+1,operationMap.get(token.toString()),token.toString(),"操作符") );
                        }

                        /// 和界符
                    }else if(symbolMap.containsKey(String.valueOf(ch))){
                        token.append(ch);
                        //ansList.add(new Object[] {token, "界符", "304", m+1});
                        ansList.add(new Patten(m+1,symbolMap.get(token.toString()),token.toString(),"界符") );
                    }
                    //识别注释 单行//  /* 多行  并且包括除号/
                    else if (ch == '/'){
                        token.append(ch);
                        if(i< strline.length-1 && strline[i+1] =='/' ){//单行注释
                            token.append('/');
                            //忽略该行的所有内容
                            break;
                            //ansList.add(new Object[] {token, "单行注释", "306", m+1});
                        }else if(i< strline.length-1 && strline[i+1] =='*' ) {//多行注释
                            hasNote = true;
                            break;
                        }else {//是除号
                            //ansList.add(new Object[] {token, "操作符", "/", m+1});
                            ansList.add(new Patten(m+1,operationMap.get(token.toString()),token.toString(),"操作符") );
                        }
                    }
                    //不合法字符
                    else{
                        
                    }
                }

            }
        }
    }


    //初始化关键字 ,用Set哈希表存储，是的查询为时间 O1
    static void inint(){
        symbolMap = new HashMap<String, Integer>(){
            { put( ",",11);put( ";",12);put( ":",13);put( "(",14);put( ")",15);put( "{",16);put( "}",17); }
        };
        operationMap = new HashMap<String, Integer>(){
            { put("+",20);put("-",21);put("*",22);put("/",23);put("%",24);put("&",25);put("|",26);put("^",27);put("~",28);put(">",29);put("<",30);put("=",31);put("!",32);put(".",33); }
        };
        doubleOperationMap = new HashMap<String, Integer>(){
            { put("++",40);put("--",41);put("-=",42);put("+=",43);put("*=",44);put("/=",45);put("<<",46);put(">>",47);put("==",48);put("!=",49);put(">=",50);put("<=",51);put("&&",52);put("||",53); }
        };
        keyWorldMap = new HashMap<String, Integer>(){
            {
                put("private",100);put("protected",101);put("public",102);put("abstract",103);put("class",104);put("extends",105);put("final",106);put("implements",107);put("interface",108);
                put("native",109);put("new",110);put("static",111);put("break",112);put("continue",113);put("return",114);put("do",115);put("while",116);put("if",117);put("else",118);put("for",119);
                put("instanceof",120);put("switch",121);put("case",122);put("default",123);put("boolean",124);put("byte",125);put("char",126);put("double",127);put("float",128);put("int",129);
                put("long",130);put("short",131);put("String",132);put("null",133);put("true",134);put("false",135);put("void",136);put("this",137);put("goto",138);
            }
        };
    }
    //读文件
    public static void readFile(String path){
       try {
            File file = new File(path);
            FileReader filereader = new FileReader(file);
            BufferedReader bufferreader = new BufferedReader(filereader);
            String aline;
            while ((aline = bufferreader.readLine()) != null){
                text.append(aline + "\n");
            }
            filereader.close();
            bufferreader.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    //将结果写入到excel文件
    public static void writerFile(String path){
        String fileName = path;
        ExcelWriter excelWriter = EasyExcel.write(fileName, Patten.class).build();
        try {
            excelWriter = EasyExcel.write(fileName, Patten.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(1, "分析结果").head(Patten.class).build();
            excelWriter.write(ansList, writeSheet);
            writeSheet = EasyExcel.writerSheet(2, "错误结果").head(Err.class).build();
            excelWriter.write(error, writeSheet);
        } finally {
            // 千万别忘记finish 会帮忙关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    //判断字母及下划线
    public static Boolean isAlpha(char ch)
    {
        return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_');
    }
    public static Boolean isDigit(char ch)
    {
        return (ch >= '0' && ch <= '9');
    }
    //判断是否是关键字
    public static Boolean isMatchKeyword(String str) {
        return keyWorldMap.containsKey(str);
    }
    //判断是否是运算符
    public static Boolean isSingleOp(char ch)
    {
        return operationMap.containsKey(String.valueOf(ch));
    }
    //测试识别数字功能
    static void testNum(){
        String[] texts = text.toString().split("\n");
        String str = texts[0];
        char[] strline = str.toCharArray();
        int i=0;
        char ch = strline[i];
        //初始化token字符串为空
        StringBuilder token = new StringBuilder();
        //初始化进入1状态
        int state = 1;
        boolean isfloat = false;
        while ( (ch !=' ')&&(ch != '\0') && (isDigit(ch) || ch == '.' || ch == 'e' || ch == 'E'|| ch == '+'|| ch == '-')) {
            if (ch == '.' || ch == 'e') {
                isfloat = true;
            }
            switch (state) {
                case 1://状态1，读入 . 进入状态2，读入e 进入状态4
                    if(isDigit(ch)){//是数字
                        token.append(ch);
                    }else if(ch == '.'){
                        token.append(ch);
                        state = 2;//进入状态2
                    }else if(ch =='e' || ch=='E'){
                        token.append(ch);
                        state = 4;//进入状态4
                    }else{//匹配出错
                        state =7;
                    }
                    break;
                case 2://状态2，读入数字d 进入状态3
                    if(isDigit(ch)){
                        token.append(ch);
                        state =3;
                    }else {
                        state =7;
                    }
                    break;
                case 3://状态3，读入数字d 进入状态3 读入e进入状态4
                    if (isDigit(ch)){
                        token.append(ch);
                        state =3;
                    }else if(ch =='e' || ch=='E'){
                        token.append(ch);
                        state =4;
                    }else {
                        state =7;
                    }
                    break;
                case 4://状态4，读入 -,+ 进入状态5，读入数字进入状态6
                    if(ch=='-' || ch=='+'){
                        token.append(ch);
                        state =5;
                    }else if(isDigit(ch)){
                        token.append(ch);
                        state =6;
                    }else {
                        state =7;
                    }
                    break;
                case 5://状态5， 度输入数字进入 状态 6
                    if(isDigit(ch)){
                        token.append(ch);
                        state =6;
                    }else {
                        state =7;
                    }
                    break;
                case 6://状态6， 读入数字进入状态6
                    if(isDigit(ch)){
                        token.append(ch);
                        state =6;
                    }else {
                        state =7;
                    }
                    break;
            }
            if (state ==7) {break;}//错误状态
            //遍历符号先前移动
            i++;
            if (i >= strline.length) break;
            ch = strline[i];
        }
        Boolean haveMistake = false;
        if (state == 2 || state == 4 || state == 5 || state ==7)//非终结状态或出错状态
        {
            haveMistake = true;
        }
        //错误处理
        if (haveMistake)
        {
            //一直到“可分割”的字符结束
            while (ch != '\0' && ch != ',' && ch != ';' && ch != ' ')
            {
                token.append(ch);
                i++;
                if(i >= strline.length) break;
                ch = strline[i];
            }
            System.out.println(token + " 确认无符号常数输入正确");

        }
        else {
            if (isfloat)
            {
                System.out.println(token+"浮点型常量"+"300");

            }
            else {
                System.out.println(token+"整型常量"+"301");
            }
        }
        i--;
    }

}





