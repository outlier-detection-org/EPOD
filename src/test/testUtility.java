package test;
import main.EdgeNode;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class testUtility {
    public static void main(String[] args) throws IOException {
        String filePath = "C:\\Users\\14198\\Desktop\\outlier_detection\\Datasets\\gas_origin.txt";
        String filePath1 = "C:\\Users\\14198\\Desktop\\outlier_detection\\Datasets\\gas.txt";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath1));
        String x;
        double max[] = new double[10];
        double min[] = new double[10];
        for (int i=0;i<10;i++){
            min[i] = Double.MAX_VALUE;
            max[i] = Double.MIN_VALUE;
        }
        while ((x=br.readLine())!=null){
            String[] tmp = x.split(" ");
            StringBuilder y = new StringBuilder();
            int j=0;
            for (int i=4;i<tmp.length;i++){
                if (Objects.equals(tmp[i], ""))continue;
                y.append(tmp[i]).append(",");
                max[j] = Math.max(max[j],Double.parseDouble(tmp[i]));
                min[j] = Math.min(min[j],Double.parseDouble(tmp[i]));
                j++;
            }
            y.replace(y.length()-1,y.length(),"");
            y.append("\n");
            bw.write(y.toString());
        }
        Arrays.stream(max).forEach(System.out::println);
        Arrays.stream(min).forEach(System.out::println);
        bw.flush();
        br.close();
        bw.close();
    }
}