import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    private List<String> countReservedWord() throws IOException {
        List<String> res = new LinkedList<>();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream("in1.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("打开文件失败，错误信息为" + e.toString());
            return res;
        }
        BufferedReader buff = new BufferedReader(inputStreamReader);
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
                } else if (c == ' ' || c == '\n') {
                    flag = 0;
                    // 当前的nowWord碰到了+ - * / ; >= <=这种运算符
                } else {
                    if (flag != -1 && flag != 2 || c == ';') {
                        res.add(nowWord);
                        nowWord = "";
                    }
                    nowWord += c;
                    flag = 2;
                }
            }
        }
        buff.close();
        inputStreamReader.close();
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

    public static void main(String[] args) throws IOException, Exception {

        // 所有的保留字或运算符对应的编码
        HashMap<String, String > encode = new HashMap<String, String>(){{
            put("begin","beginsym"); put("call","callsym"); put("const","constsym");
            put("do", "dosym"); put("end", "endsym");
            put("if","ifsym"); put("odd","oddsym"); put("procedure","proceduresym");
            put("read","readsym"); put("then","thensym"); put("var","varsym");
            put("while","whilesym"); put("write","writesym"); put("+","plus");
            put("-","minus"); put("*","times"); put("/","slash");
            put("=","eql"); put("#","neq"); put("<","lss");
            put("<=","leq"); put(">","gtr"); put(">=","geq");
            put(":=","becomes"); put("(","lparen"); put(")","rparen");
            put(",","comma"); put(";","semicolon"); put(".","period");
        }};

        Main o = new Main();
        List<String> tmp = o.countReservedWord();
        for(String str : tmp) {
            if(encode.containsKey(str)) {
                // 仅供实验二参考
                System.out.printf("(" + "%-10s", encode.get(str) + ",");
            } else {
                if (o.isNumber(str)) {
                    System.out.print("(number,   ");
                } else if(o.isVariable(str)) {
                    System.out.print("(ident,    ");
                } else {
                    throw new Exception("格式错误:" + str);
                }
            }
            System.out.printf("%10s", str + ")\n");
        }
    }
}
