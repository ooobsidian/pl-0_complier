import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

public class Main {
    //从文件中读pl_0程序
    public static String readLine(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String buffer = "";
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                buffer += tempString;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer;
    }

    //找出子串在主串中出现了几次
    public static int countStr(String str, String sToFind) {
        int num = 0;
        while (str.contains(sToFind)) {
            str = str.substring(str.indexOf(sToFind) + sToFind.length());
            num++;
        }
        return num;
    }

    public static void countIdentifier(String filePath,int index) {
        String[] reserved_word = {"begin", "call", "const", "do", "end", "if", "odd", "procedure", "read", "then", "var", "while", "write"};
        String[] operate_word = {":=", "+", "-", "*", "/", "=", "#", "<", "<=", ">", ">="};
        String[] limited_word = {"(", ")", ",", ";", ".","'","\""};
        String buffer = readLine(filePath);
        HashSet<String> result = new HashSet<>();

        //对读出来的程序段进行字符串处理
        buffer.replace("\n", " ");
        buffer = buffer.toLowerCase();
        for (int i = 0; i < reserved_word.length; i++) {
            buffer = buffer.replace(reserved_word[i], " ");
        }
        for (int i = 0; i < operate_word.length; i++) {
            buffer = buffer.replace(operate_word[i], " ");
        }
        for (int i = 0; i < limited_word.length; i++) {
            buffer = buffer.replace(limited_word[i], " ");
        }
        String tmp = "";
        //四种情况
        for (int i = 0; i < buffer.length() - 1; i++) {
            if (buffer.charAt(i) != ' ' && buffer.charAt(i + 1) != ' ') {
                tmp += buffer.charAt(i);
            } else if (buffer.charAt(i) != ' ' && buffer.charAt(i + 1) == ' ') {
                tmp += buffer.charAt(i);
                result.add(tmp);
                tmp = "";
            } else if (buffer.charAt(i) == ' ' && buffer.charAt(i + 1) == ' ') {
                continue;
            } else if (buffer.charAt(i) == ' ' && buffer.charAt(i + 1) != ' ') {
                continue;
            }
        }
        //遍历Set集合
        Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str.charAt(0) >= '0' && str.charAt(0) <= '9') {
                iterator.remove();
            }
        }

        //输出结果
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out" + index + ".txt", true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (String str : result) {
            int num = countStr(buffer, str);
            try {
                System.out.println("(" + str + ": " + num + ")");
                out.write("(" + str + ": " + num + ")"+"\r\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        String filePath1 = "in1.txt";
        String filePath2 = "in2.txt";
        String filePath3 = "in3.txt";
        String filePath4 = "in4.txt";
        String filePath5 = "in5.txt";
        countIdentifier(filePath1,1);
        countIdentifier(filePath2,2);
        countIdentifier(filePath3,3);
        countIdentifier(filePath4,4);
        countIdentifier(filePath5,5);
    }
}