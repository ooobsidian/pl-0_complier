package Compiler;

import java.io.*;
import java.util.*;

/*
 * @program: Compiler
 * @description: 实验三-语法分析 实验四-语义分析（表达式）
 * @author: xw
 * @create: 2019-04-11 14:57
 **/
public class GrammaAnalysis {

    //    public static String[] reserved_words = {"begin","call","const","do","end","if","odd","procedure","read","then","var","while","write"};
//    public static String[] operator_words = {"+","-","*","/","#","<=",">=",":=","<",">","="};
//    public static String[] margin_words = {"(",")",",",";","."};
    // 所有的保留字或运算符对应的编码
    private static HashMap<String, String> encode = new HashMap<String, String>() {{
        put("begin", "beginsym");
        put("call", "callsym");
        put("const", "constsym");
        put("do", "dosym");
        put("end", "endsym");
        put("if", "ifsym");
        put("odd", "oddsym");
        put("procedure", "proceduresym");
        put("read", "readsym");
        put("then", "thensym");
        put("var", "varsym");
        put("while", "whilesym");
        put("write", "writesym");
        put("+", "plus");
        put("-", "minus");
        put("*", "times");
        put("/", "slash");
        put("=", "eql");
        put("#", "neq");
        put("<", "lss");
        put("<=", "leq");
        put(">", "gtr");
        put(">=", "geq");
        put(":=", "becomes");
        put("(", "lparen");
        put(")", "rparen");
        put(",", "comma");
        put(";", "semicolon");
        put(".", "period");
    }};
    private static Map<String, String> predictmap;
    private static ArrayList<String> tokenList = new ArrayList<>();   //存储词法分析结果得到的编码
    private static String sym;  //用于遍历输入的词法分析结果的每个编码
    private static int sym_index = 0;     //遍历tokenList的索引
    private static int bracketsFlag = 0; //用于判断左右括号是否搭配：当flag>0右括号缺失；当flag<0右括号缺失
    private static List<ArrayList<String>> MorphologyWordList = new ArrayList<>();

    private GrammaAnalysis() {
        tokenList = new ArrayList<>();
        sym = "";
        predictmap = new HashMap<>();
        //basecode表示基本字
        predictmap.put("beginsym", "basecode");
        predictmap.put("callsym", "basecode");
        predictmap.put("constsym", "basecode");
        predictmap.put("dosym", "basecode");
        predictmap.put("endsym", "basecode");
        predictmap.put("ifsym", "basecode");
        predictmap.put("oddsym", "basecode");
        predictmap.put("proceduresym", "basecode");
        predictmap.put("readsym", "basecode");
        predictmap.put("thensym", "basecode");
        predictmap.put("varsym", "basecode");
        predictmap.put("whilesym", "basecode");
        predictmap.put("writesym", "basecode");
        //ident表示标识符
        predictmap.put("ident", "ident");
        //number表示常数
        predictmap.put("number", "number");
        //opt表示运算符
        predictmap.put("plus", "opt");
        predictmap.put("minus", "opt");
        predictmap.put("times", "opt");
        predictmap.put("becomes", "opt");
        predictmap.put("slash", "opt");
        predictmap.put("eql", "opt");
        predictmap.put("neq", "opt");
        predictmap.put("lss", "opt");
        predictmap.put("leq", "opt");
        predictmap.put("gtr", "opt");
        predictmap.put("geq", "opt");
        //delimiter表示界符
        predictmap.put("lparen", "delimiter");
        predictmap.put("rparen", "delimiter");
        predictmap.put("comma", "delimiter");
        predictmap.put("semicolon", "delimiter");
        predictmap.put("period", "delimiter");
    }

    //    private static final String cur_directory = "D:\\编译原理\\Compiler\\src\\TestInstance\\";
    private static final String cur_directory = "/Users/obsidian/source/pl-0_complier/src/test/case06.txt";
    //    private static final String input_filename="InputText\\input2.txt";
//    private static final String conditionTest_filename="ConditionTest\\test1.txt";
//    private static final String expressionTest_filename="ExpressionTest\\test1.txt";
//    private static final String identExpressionTest_filename="IdentExpressionTest\\test1.txt";
//    private static final String output_filename="output1.txt";
    //private File inputfile = new File(cur_directory+input_filename);
    //条件表达式测试
//    private File inputfile = new File(cur_directory+conditionTest_filename);
    private File inputfile = new File(cur_directory);

    private BufferedWriter writer = null;

    /*
    对程序进行分词处理
     */
    private List<String> countReservedWord() throws IOException {
        List<String> res = new LinkedList<>();
        //运算表达式测试
        //private File inputfile = new File(cur_directory+expressionTest_filename);
        //赋值表达式测试
        //private File inputfile = new File(cur_directory+identExpressionTest_filename);
        //private File outputfile = new File(cur_directory+output_filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputfile));
        } catch (IOException e) {
            System.out.println("打开文件失败，错误信息为" + e.toString());
            return res;
        }
        BufferedReader buff = new BufferedReader(reader);
        // line是从文件读取进来的一行
        // nowWord是读取的每行中的一个操作符或者一个单词
        String line, nowWord = "";
        // 标记目前统计的是什么符号类型
        int flag = -1;
        while ((line = buff.readLine()) != null) {
            // 全部小写
            line = line.toLowerCase();
            // 拿到长度
            int len = line.length();
            // 循环统计
            for (int i = 0; i < len; i++) {
                char c = line.charAt(i);
                // 当前的nowWord是单词开始的
                if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                    if (flag != -1 && flag != 1) {
                        res.add(nowWord);
                        nowWord = "";
                    }
                    nowWord += c;
                    flag = 1;
                    // 当前的nowWord碰到了空格或者换行
                } else if (c == ' ' || c == '\n' || c == '\t') {
                    flag = 0;
                    // 当前的nowWord碰到了+ - * / ; >= <=这种运算符
                } else {
                    if (flag != 2 || c == ';'
                            || c == '(' || c == ')'
                            || c == '+' || c == '-'
                            || c == '*' || c == '/') {
                        res.add(nowWord);
                        nowWord = "";
                    }
                    nowWord += c;
                    flag = 2;
                }
            }
            if (!nowWord.equals("")) {
                res.add(nowWord);
                nowWord = "";
                flag = -1;
            }
        }
        buff.close();
        reader.close();
        return res;
    }

    /*
        只要判断首位是不是数字，因为在countReservedWord函数中
        已经保证了输入的变量一定是字母数组下划线才能合成一个string
        所以首位是数字一定是数字串
     */
    boolean isNumber(String str) {
        return str.charAt(0) >= '0' && str.charAt(0) <= '9';
    }

    /*
        判断是否是变量名或者是数字，因为在countReservedWord函数中
        已经保证了输入的变量一定是字母数组下划线才能合成一个string
        所以只要检测到首位是_或者字母那么一定是变量
     */
    boolean isVariable(String str) {
        // 特判变量或者函数名的首位不能是数字
        return str.charAt(0) >= 'a' && str.charAt(0) <= 'z'
                || str.charAt(0) == '_';
    }

    /*
    对分词进行词法分析
     */
    public List<ArrayList<String>> getMorphologyWord(List<String> tmp) throws IOException, Exception {
        List<ArrayList<String>> resultList = new ArrayList<>();
        for (String str : tmp) {
            if (str != "") {
                ArrayList<String> morphologyMap = new ArrayList();
                if (encode.containsKey(str)) {
                    morphologyMap.add(str);
                    morphologyMap.add(encode.get(str));
                } else {
                    if (isNumber(str)) {
                        morphologyMap.add(str);
                        morphologyMap.add("number");
                    } else if (isVariable(str)) {
                        morphologyMap.add(str);
                        morphologyMap.add("ident");
                    } else {
                        throw new Exception("格式错误:" + str);
                    }
                }
                //System.out.print(morphologyMap + "\n");
                resultList.add(morphologyMap);
            }
        }
        return resultList;
    }


    //========================================================================
    //========================================================================
    //========================================================================
    //========================================================================
    // 语法分析

    //对标识符进行遍历 getNext
    private void getSym() {
        if (sym_index < tokenList.size()) {
            sym = tokenList.get(sym_index++);
        } else {
            System.out.println(sym_index);
            System.out.println("越界");
            System.exit(1);
        }
        System.out.println(sym);
    }


    // 分程序(整个程序的入口) ::= [<常量说明部分>][<变量说明部分>][<过程说明部分>]<语句>
    private int subprogram() {
        while (sym.equals("constsym")) {
            constExpression();
            if (sym_index == tokenList.size()) return 0;
            getSym();
        }
        while (sym.equals("varsym")) {
            varExpression();
            if (sym_index == tokenList.size()) return 0;
            getSym();
        }
        while (sym.equals("proceduresym")) {
            procedureExpression();
            if (sym_index == tokenList.size()) return 0;
            getSym();
        }
        sentence();
        return 0;
    }


    // <常量说明部分> const<常量定义>{,<常量定义>};
    private void constExpression() {
        if (sym.equals("constsym")) {
            getSym();
            constDefinition();
            getSym();
            while (sym.equals("comma")) {
                getSym();
                constDefinition();
                getSym();
            }
        } else {
            System.out.println("常量说明需要\'const\'进行声明");
            System.exit(1);
        }
        if (sym_index != tokenList.size()) {
            if (!sym.equals("semicolon")) {
                System.out.println("常量定义缺少分号结束");
                System.exit(1);
            }
            return;
        }
        if (!sym.equals("semicolon")) {
            System.out.println("常量定义缺少分号结束");
            System.exit(1);
        }
    }

    // <常量定义> ::= <id> = <integer>
    // 常量说明部分的儿子，属于不透明部分
    private void constDefinition() {
        // 判断是不是标识符
        if (sym.equals("ident")) {
            // 再读取一个词
            getSym();
            // 判断是不是等号
            if (sym.equals("eql")) {
                // 如果是等号，则在读取一个
                getSym();
                // 判断是不是数字
                if (!sym.equals("number")) {
                    System.out.println("常量定义语句缺少值");
                    System.exit(1);
                }
            } else {
                System.out.println("常量定义语句部分需要\'=\'");
                System.exit(1);
            }
        } else {
            System.out.println("常量定义语句起始部分需要有标识符");
            System.exit(1);
        }
    }


    // <变量说明部分> ::= var <id> {, <id>};
    private void varExpression() {
        // 如果第一个是var，读一个词
        if (sym.equals("varsym")) {
            getSym();
            if (!sym.equals("ident")) {
                System.out.println("变量说明语句缺少标识符");
                System.exit(1);
            }
            // 判断是不是comma
            getSym();
            while (sym.equals("comma")) {
                getSym();
                if (!sym.equals("ident")) {
                    System.out.println("缺少标识符");
                    System.exit(1);
                }
                getSym();
            }
        } else {
            System.out.println("变量说明需要\'var\'进行声明");
            System.exit(1);
        }
        if (sym_index == tokenList.size()) {
            if (!sym.equals("semicolon")) {
                System.out.println("变量说明缺少分号结束");
                System.exit(1);
            }
            return;
        }
        if (!sym.equals("semicolon")) {
            System.out.println("变量说明缺少分号结束");
            System.exit(1);
        }
    }


    // <过程说明部分> ::=<过程首部><分程序>{;<过程说明部分>};
    private void procedureExpression() {
        if (sym.equals("proceduresym")) {
            procedureHead();
            getSym();
            subprogram();
            getSym();
            while (sym.equals("semicolon")) {
                getSym();
                //如果这里过程说明部分是空的，那么刚好说明过程说明以分号结束
                //过程说明部分的定义成功结束，并且此时要把拿到的sym吐出来
                if (!sym.equals("proceduresym")) {
                    sym_index--;
                    return;
                }
                procedureExpression();
                getSym();
            }
            // 如果代码能执行到这里，说明过程说明缺少了分号
            System.out.println("过程说明部分缺少结束分号");
            System.exit(1);

        }
    }

    // <过程首部> ::= procedure<id>;
    // 过程说明部分的儿子
    private void procedureHead() {
        if (sym.equals("proceduresym")) {
            getSym();
            if (!sym.equals("ident")) {
                System.out.println("过程调用缺少调用的ident");
                System.exit(1);
            }
            getSym();
            if (!sym.equals("semicolon")) {
                System.out.println("过程调用缺少结束分号");
                System.exit(1);
            }
        }
    }

    // 语句
    /*
        ident     赋值语句
        if        条件语句
        while     当型循环语句
        procedure 过程首部语句
        call      过程调用语句
        read      读语句
        write     写语句
        begin     复合语句
        null      空语句
     */
    private void sentence() {
        switch (sym) {
            case "ident":
                // 赋值语句
                identExpression();
                break;
            case "ifsym":
                // 条件语句
                conditionExpression();
                break;
            case "whilesym":
                // 当型循环语句
                whileExpression();
                break;
            case "callsym":
                // 过程调用
                callExpression();
                break;
            case "readsym":
                // 读语句
                readExpression();
                break;
            case "writesym":
                // 写语句
                writeExpression();
                break;
            case "beginsym":
                // 复合语句
                beginExpression();
                break;
            // 默认的话就是空语句,那么当前这个内容应该被吐出来，然后回到上一个
            default:
                sym_index--;
        }
    }

    // 判断下一个开始的是不是一个语句
    private boolean isSentence() {
        return sym.equals("ident") || sym.equals("ifsym") || sym.equals("whilesym")
                || sym.equals("callsym") || sym.equals("readsym") || sym.equals("writesym")
                || sym.equals("beginsym");
    }

    // <赋值语句> ::= <标识符> := <表达式>
    private void identExpression() {
        if (sym.equals("ident")) {
            getSym();
            if (sym.equals("becomes")) {
                getSym();
                expression();
            } else {
                System.out.print("赋值表达式缺少赋值符");
                System.exit(1);
            }
        } else {
            System.out.print("赋值语句左部缺少标识符");
            System.exit(1);
        }
    }


    // <复合语句> ::= begin <语句> { ; <语句> } end
    private void beginExpression() {
        if (sym.equals("beginsym")) {
            getSym();
            sentence();
            getSym();
            while (sym.equals("semicolon")) {
                getSym();
                sentence();
                getSym();
            }
            if (!sym.equals("endsym")) {
                if (isSentence()) {
                    System.out.println("缺少分号");
                    System.exit(1);
                } else {
                    System.out.println("begin语句缺少end结尾");
                    System.exit(1);
                }
            }
        }
    }


    // <条件> ::= <表达式> <关系运算符> <表达式> |ODD<表达式>
    private void condition() {
        if (sym.equals("oddsym")) { //ODD<表达式>
            getSym();
            expression();
        } else {
            expression();
            getSym();
            if (relationalOperation() == 1) {
                getSym();
                expression();
            } else {
                System.out.print("条件表达式缺少关系运算符");
                System.exit(1);
            }
        }
    }

    // <表达式> ::= [+|-]<项>{<加法运算符><项>}
    // 外层，主要用于判断括号的问题
    private void expression() {
        int result = exp();
        System.out.println("最终结果为：" + result);
        if (bracketsFlag != 0) {
            System.out.println("括号出错");
            System.exit(1);
        }
    }

    // 内层
    private Integer exp() {
        String op;
        Integer arg1, arg2, result;
        if (sym.equals("plus") || sym.equals("minus")) { //处理 [+|-]
            getSym();
            if (predictmap.get(sym).equals("opt") || predictmap.get(sym).equals("delimiter")) {
                System.out.println("表达式开头[+|-]后错误");
                System.exit(1);
            }
        }
        arg1 = term();
        //{<加法运算符><项>}
        getSym();
        boolean intoWhile = false;
        while (sym.equals("plus") || sym.equals("minus")) {
            op = sym;
            intoWhile = true;
            if (sym_index >= tokenList.size()) {
                System.out.println("[+|-]后没有计算符号");
                System.exit(1);
            }
            getSym();
            arg2 = term();
            if (op.equals("plus")) {
                result = arg1 + arg2;
                arg1 = result;
            } else {
                result = arg1 - arg2;
                arg1 = result;
            }
            getSym();
            intoWhile = false;
        }
        if (!intoWhile) {
            sym_index--;
        }
        return arg1;
    }

    // <项> ::= <因子>{<乘法运算符><因子>}
    // 表达式的儿子
    private Integer term() {
        String op;
        Integer arg1, arg2, result;
        arg1 = factor();     //处理<因子>
        getSym();
        //{<乘法运算符><因子>}
        boolean intoWhile = false;
        while (sym.equals("times") || sym.equals("slash")) {
            op = sym;
            intoWhile = true;
            getSym();
            if (sym.equals("times") || sym.equals("slash") ||
                    sym.equals("plus") || sym.equals("minus")) {
                System.out.println("<项>里面出现多余的运算符号");
                System.exit(1);
            }
            arg2 = factor();
            if (op.equals("times")) {
                result = arg1 * arg2;
                arg1 = result;
            } else {
                if (arg2 == 0) {
                    System.out.print("除数不能为0");
                    System.exit(1);
                }
                result = arg1 / arg2;
                arg1 = result;
            }
            getSym();
            intoWhile = false;
        }
        if (!intoWhile) {
            sym_index--;
        }
        return arg1;
    }

    // <因子> ::= <标识符>|<无符号整数>| ‘(’<表达式>‘)’
    // 项的儿子
    private Integer factor() {
        Integer arg = 0;
        //<标识符>
        switch (sym) {
            case "ident":
                if (sym_index == tokenList.size()) return null;
                break;
            //<无符号整数>
            case "number":
                if (sym_index == tokenList.size()) return null;
                arg = Integer.parseInt(MorphologyWordList.get(sym_index - 1).get(0));
                break;
            //‘(’<表达式>‘)’
            case "lparen":   //‘(’
                bracketsFlag++;
                System.out.println(bracketsFlag);
                getSym();
                arg = exp();  //<表达式>
                getSym();
                if (sym.equals("rparen")) { //‘)’
                    System.out.println(sym_index + " " + bracketsFlag);
                    if (--bracketsFlag < 0) {
                        System.out.println("缺少右括号");
                    }
                } else {
                    System.out.println("缺少右括号");
                    System.exit(1);
                }
                if (sym_index == tokenList.size()) return null;
                break;
            case "rparen":
                System.out.println("错误使用右括号");
                System.exit(1);
            default:
                System.out.println("+ | -运算符后面未跟数字或标识符");
                System.exit(1);
        }
        return arg;
    }


    // <关系运算符> ::= =|#|<|<=|>|>=
    private int relationalOperation() {
        if (sym.equals("eql") || sym.equals("neq") || sym.equals("lss") ||
                sym.equals("leq") || sym.equals("gtr") || sym.equals("geq")) {
            return 1;
        } else {
            return 0;
        }
    }


    // <条件语句> ::= IF <条件表达式> THEN <语句>
    private void conditionExpression() {
        if (sym.equals("ifsym")) { //ODD<表达式>
            getSym();
            condition();
            getSym();
            if (sym.equals("thensym")) {
                getSym();
                sentence();
            } else {
                System.out.print("缺少THEN");
                System.exit(1);
            }
        }
    }


    // <过程调用语句> ::= call <id>
    private void callExpression() {
        if (sym.equals("callsym")) {
            getSym();
            if (!sym.equals("ident")) {
                System.out.println("过程调用缺少过程名");
                System.exit(1);
            }
        }
    }


    // <当型循环语句> ::= while <条件> do <语句>
    private void whileExpression() {
        if (sym.equals("whilesym")) {
            getSym();
            condition();
            getSym();
            if (sym.equals("dosym")) {
                // 判断是否是复合语句
                getSym();
                sentence();
            } else {
                System.out.println("while缺少do");
                System.exit(1);
            }
        }
    }


    // <读语句> ::= read '(' <id> {, <id>} ')'
    private void readExpression() {
        if (sym.equals("readsym")) {
            getSym();
            if (sym.equals("lparen")) { // (
                getSym();
                if (sym.equals("ident")) { // <id>
                    getSym();
                    while (sym.equals("comma")) { // {, <id>}
                        getSym();
                        if (!sym.equals("ident")) {
                            System.out.println("读语句括号内的逗号后的必须仍然是ident");
                            System.exit(1);
                        }
                        getSym();
                    }
                    if (!sym.equals("rparen")) { // )
                        System.out.println("读语句缺少右括号");
                        System.exit(1);
                    }
                } else {
                    System.out.println("读语句读取的必须是ident");
                    System.exit(1);
                }
            } else {
                System.out.println("读语句不能没有左括号");
                System.exit(1);
            }
        }
    }


    // <写语句> ::= write '(' <表达式> {, <表达式> } ')'
    private void writeExpression() {
        if (sym.equals("writesym")) {
            getSym();
            if (sym.equals("lparen")) { // (
                getSym();
                expression(); // <表达式>
                getSym();
                while (sym.equals("comma")) { // {, <表达式>}
                    getSym();
                    expression();
                    getSym();
                }
                if (!sym.equals("rparen")) { // )
                    System.out.println("写语句缺少右括号");
                    System.exit(1);
                }
            } else {
                System.out.println("写语句不能没有左括号");
                System.exit(1);
            }
        }
    }


    private void start() {
        getSym();
        subprogram();
        if (sym_index < tokenList.size() - 1) {
            System.out.println("分程序的语句只能有一条（如需多条请使用嵌套写法）");
            System.exit(1);
        }
        getSym();
        if (!sym.equals("period")) {
            System.out.println("程序结束缺少.");
            System.exit(1);
        }
        System.out.println(sym_index);
        System.out.println("语法正确无误");
    }

    //========================================================================
    public static void main(String[] args) throws Exception {
        GrammaAnalysis grammaAnalysis = new GrammaAnalysis();
        MorphologyWordList =
                grammaAnalysis.getMorphologyWord(grammaAnalysis.countReservedWord());
        //获得词法分析列表tokenList
//        System.out.println(MorphologyWordList.size());
//        for (ArrayList<String> a : MorphologyWordList) {
//            System.out.println(a);
//        }
        for (ArrayList<String> MorphologyWord : MorphologyWordList) {
            tokenList.add(MorphologyWord.get(1));
        }
//        System.out.print(tokenList);
        // 程序入口
        grammaAnalysis.start();

    }

}