package LexicalAnalysis;

/**
 * Created by yancychan on 17-6-18.
 */

import java.io.*;
import java.util.*;

public class Analysis {
    FileReader fileReader = null;
    BufferedReader reader = null;
    StringBuffer token = null;//缓冲区,缓冲临时未完全token

    char nowChar;//当前字符

    public char getNowChar() {
        return nowChar;
    }

    public void setNowChar(char nowChar) {
        this.nowChar = nowChar;
    }

    boolean isEnd = false;//判断是否到达文件尾

    public static String keyWorkStr[] = {
            "abstract", "assert", "boolean",
            "break", "byte", "case",
            "catch", "char", "const",
            "class", "continue", "default",
            "do", "double", "else",
            "enum", "extends", "final",
            "finally", "float", "for",
            "goto", "if", "implements",
            "import", "instanceof", "int",
            "interface", "long", "native",
            "new", "package", "private",
            "protected", "public", "return",
            "short", "static", "strictfp",
            "super", "switch", "synchronized",
            "this", "throw", "throws",
            "transient", "try", "void",
            "volatile", "while", "true",
            "false"
    };//关键字字符串

    public static String operaStr[] = {
            "+", "-", "*",
            "/", "++", "--",
            "<<", ">>", "<",
            ">", ">=", "<=",
            "==", "=", "*=",
            "+=", "-=", "/=",
            "%=", "&=", "|=",
            "^=", "&", "&&",
            "||", "!=", "~",
            "<<=", ">>=", "%"
    };//操作符字串
    public static String specialStr[] = {
            "(", ")", "[",
            "]", "!", ":",
            ".", ",", "{",
            "}", "#", ";",
            "@", "?"
    };//特殊符号字串

    public static Set<String> keyWork = new HashSet<String>(Arrays.asList(keyWorkStr));//关键字集合
    public static Set<String> special = new HashSet<String>(Arrays.asList(specialStr));//界符集合
    public static Set<String> opera = new HashSet<String>(Arrays.asList(operaStr));//操作符集合

    /**
     * 启动分析
     */
    public void start() {
        step0(nextChar());
    }

    /**
     * 读取下一个字符,并且更新当前字符
     * 如果读取异常抛出异常时返回空字符
     * 读取相应输入文件,没有取默认文件
     * 如果到达文件尾则将isEnd标志为true
     */
    public char nextChar() {
        if (reader == null) {
            try {
                fileReader = new FileReader("/home/yancychan/IdeaProjects/CompilePrinciple/src/LexicalAnalysis/test.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            reader = new BufferedReader(fileReader);
        }
        try {
            if (reader.ready()) {
                setNowChar((char) reader.read());
                return getNowChar();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isEnd = true;
        return '`';//结束
    }

    /**
     * 接收一个字符判断:
     * 如果该字符是换行,空字符,制表符则忽略取下一个字符
     * 程序总体把分析分成6种种类去分析,并由初始模块根据第一字符原则判断进入哪个类别,.分别是
     * 1.   字符串:双引号关联起来的字串
     * 2.   单个字符:单引号关联起来的字符
     * 3.   注释:行注释以及块注释
     * 4.   数字:自然数以及小数
     * 5.   文字(letter):标识符以及关键字
     * 6.   符号:操作符以及特殊符号或非法字符
     */
    public void step0(char c) {
        while (c == '\n' || c == ' ' || c == '\r') {
            c = nextChar();
        }
        if (isEnd) {
            System.out.println("分析结束");
            try {
                fileReader.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (c == '\"') {
            stringStep();
        } else if (c == '\'') {
            charStep();
        } else if (c == '/') {
            noteStep();
        } else if (Character.isDigit(c)) {
            digitStep();
        } else if (Character.isLetter(c) || c == '_') {
            letterStep();
        } else {
            signStepOrIllegal();
        }
    }

    /**
     * 进入字符串步骤
     * 过滤第一种缺陷情况就是"\"",但是不能过滤第二重转义字符出现的缺陷.
     * 因为这种情况不多,所以忽略
     */
    private void stringStep() {
        token = new StringBuffer(String.valueOf('\"'));
        char c;
        do {
            c = nextChar();
            token.append(c);
            if (c == '\"' && !token.toString().equals("\"\\\"")) {
                break;
            }
        } while (true);
        System.out.println(token + " :字符串");
        step0(nextChar());
    }

    /**
     * 进入单字符步骤
     * 可能出现转义字符打印错误,故提供一重修复.
     * 鉴于深层情况少见,忽略
     */
    private void charStep() {
        token = new StringBuffer(String.valueOf('\''));

        char c;
        do {
            c = nextChar();
            token.append(c);
            if (c == '\'' && !token.toString().equals("'\\'")) {
                break;
            }
        } while (true);
        System.out.println(token + " :单字符");
        step0(nextChar());
    }

    /**
     * 进入注释步骤
     * 行注释以及块注释
     */
    private void noteStep() {
        token = new StringBuffer(String.valueOf('/'));
        char c = nextChar();

        if (c == '/') {//行注释
            token.append('/');
            token.append(getRemainLine());
        } else if (c == '*') {//块注释
            token.append('*');
            token.append(getRemainBlock());
        } else {
            System.out.println("注释代码未知情况");
        }
        System.out.println(token + " :注释");
        step0(nextChar());
    }

    /**
     * 返回注释块字符串
     * 策略是一直扫描直到扫描到*和/符号
     *
     * @return String
     */
    private String getRemainBlock() {
        StringBuffer buffer = new StringBuffer();
        char c;
        char c2;
        while (true) {
            c = nextChar();
            if (c == '\t')
                continue;
            buffer.append(c);
            if (c == '*') {
                c2 = nextChar();
                if (c2 == '\t')
                    continue;

                if (c2 == '/') {//如果继*后的符号是斜杠,那么就退出循环
                    buffer.append(c2);
                    break;
                }
                buffer.append(c2);
            }
        }
        return buffer.toString();
    }

    /**
     * 返回行注释的字符串
     * 扫描策略是直接扫描直到换行符
     *
     * @return
     */
    private String getRemainLine() {
        StringBuffer buffer = new StringBuffer();
        char c;
        while (true) {
            c = nextChar();
            if (c == '\n' || c == '\r') {
                break;
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    /**
     * 进入数字过程
     * 不包括正负号
     * 最多只能出现一个点.
     */
    private void digitStep() {
        boolean dot = false;
        token = new StringBuffer(String.valueOf(getNowChar()));//把当前数字加入

        char c = nextChar();
        while (c == '.' || Character.isDigit(c)) {
            if (c == '.') {
                if (dot) {//就是点已经出现过了
                    break;
                }
                dot = true;
            }
            token.append(c);//点或者数字都加入字串
            c = nextChar();
        }
        System.out.println(token + " :数字");

        step0(getNowChar());
    }

    /**
     * 进入文字过程
     */
    private void letterStep() {
        token = new StringBuffer(String.valueOf(getNowChar()));//吧当前的字符串加入
        char c = nextChar();
        while (c == '_' || Character.isDigit(c) || Character.isLetter(c)) {
            token.append(c);
            c = nextChar();
        }

        if (keyWork.contains(token.toString())) {
            System.out.println(token + " :关键字");
        } else {
            System.out.println(token + " :标识符");
        }

        step0(getNowChar());
    }

    /**
     * 进入符号阶段
     * 在这里分别区分非法字符
     * 操作符,特殊字符
     */
    private void signStepOrIllegal() {
        token = new StringBuffer();
        char c = getNowChar();
        StringBuffer buffer = new StringBuffer(String.valueOf(getNowChar()));
        while (opera.contains(buffer.toString()) || special.contains(buffer.toString())) {
            token.append(c);
            c = nextChar();
            buffer.append(c);
        }
        if (token.length() != 0) {//非法字符
            if (special.contains(token.toString())) {
                System.out.println(token + " :界符");
            } else {
                System.out.println(token + " :操作符");
            }
        } else {
            System.out.println(getNowChar() + " :非法字符");
        }
        step0(getNowChar());
    }

    public static void main(String[] args) {
        Analysis test = new Analysis();
        test.start();
    }
}
