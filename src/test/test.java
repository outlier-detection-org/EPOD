package test;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class test {
    public static void main(String[] args) throws IOException {
        //将double数组转化成Double的arraylist
        BufferedReader bufferedReader = new BufferedReader(new FileReader
                ("C:\\Users\\Lenovo\\Desktop\\outlier_detection\\EPOD\\Datasets\\gas.txt"));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                "C:\\Users\\Lenovo\\Desktop\\outlier_detection\\EPOD\\Datasets\\gas1.txt"));
        String line = bufferedReader.readLine();
        line = bufferedReader.readLine();
        while(line!=null){
            String[] strings = line.split(" +");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 2; i < strings.length - 1; i++) {
                stringBuilder.append(strings[i]);
                stringBuilder.append(",");
            }
            stringBuilder.append(strings[strings.length - 1]);
            stringBuilder.append("\n");
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        bufferedWriter.close();
    }
}
