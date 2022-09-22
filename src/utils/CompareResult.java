package utils;

import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import javax.print.attribute.HashAttributeSet;

public class CompareResult {
    public static int nW=10;
    public static int W=100000;
    public static String dataset = "STK";
    public static BufferedWriter outlierFw;

    public static void main(String[] args) throws IOException{
        outlierFw = new BufferedWriter(new FileWriter(new File(
                "src\\Result\\"+"compare_result_"+dataset+".txt")));
        double[] result = compare(
                "src/Result/Result_NETS_"+dataset+"_outliers.txt",
                "src/Result/Result_NAIVE_"+dataset+"_outliers.txt");
        outlierFw.write("Dataset is "+dataset+"\n");
        outlierFw.write("Precision: "+result[0]/nW+"\n");
        outlierFw.write("Recall: "+result[1]/nW+"\n");
        outlierFw.write("F1: "+result[2]/nW);
        outlierFw.close();
    }
    
    public static double[] compare(String filename1, String filename2) throws IOException{
        
        BufferedReader approx = new BufferedReader(new FileReader(new File(filename1)));

        BufferedReader exact = new BufferedReader(new FileReader(new File(filename2)));

        approx.readLine();
        exact.readLine();
        double[] precisions = new double[nW];
        double[] recalls = new double[nW];
        double[] f1s = new double[nW];

        for (int j=0;j<nW;j++) {
            outlierFw.write("Window "+j+"\n");
            HashSet<Integer> approxValues = new HashSet<Integer>();
            HashSet<Integer> exactValues = new HashSet<Integer>();

            String line = "";
            while ((line = approx.readLine()) != null && !line.startsWith("Window")) {
                approxValues.add(Integer.valueOf(line.trim()));
            }
            line = "";
            while ((line = exact.readLine()) != null&& !line.startsWith("Window")) {
                exactValues.add(Integer.valueOf(line.trim()));
            }

            double precision = 0;
            double recall = 0;
            double F1 = 0;

            for (Integer i : approxValues) {
                if (exactValues.contains(i))
                    precision++;
            }
            for (Integer i : exactValues) {
                if (approxValues.contains(i))
                    recall++;
            }

            //print confusion matrix
            double cf2 = precision/exactValues.size(); //真实正确中被预测正确的
            double cf1 = 1 - cf2;//真实正确中被预测错误的
            double cf4 = (approxValues.size() - precision) /(W - exactValues.size());//真实错误中被预测正确的
            double cf3 = 1 - cf4;//真实错误被预测错误的
            outlierFw.write(String.format( "%.2f ", cf1 ) + " "+ String.format( "%.2f\n", cf2 ) );
            outlierFw.write(String.format( "%.2f ", cf3 ) + " "+ String.format( "%.2f\n", cf4 ) );
            outlierFw.write("===========================\n");
            precision = precision *1.0/ approxValues.size();
            recall = recall *1.0/ exactValues.size();
            F1 = (2 * precision * recall) / (precision + recall);
            precisions[j] = precision;
            recalls[j] = recall;
            f1s[j] = F1;
        }
        outlierFw.write("precisions array: ");
        Arrays.stream(precisions).forEach(x-> {
            try {
                outlierFw.write(String.valueOf(x)+" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outlierFw.write("\n");

        outlierFw.write("recalls array: ");
        Arrays.stream(recalls).forEach(x-> {
            try {
                outlierFw.write(String.valueOf(x)+" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outlierFw.write("\n");

        outlierFw.write("F1 array: ");
        Arrays.stream(f1s).forEach(x-> {
            try {
                outlierFw.write(String.valueOf(x)+" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outlierFw.write("\n");
         return new double[]{Arrays.stream(precisions).reduce(Double::sum).orElse(0),
                 Arrays.stream(recalls).reduce(Double::sum).orElse(0),
                 Arrays.stream(f1s).reduce(Double::sum).orElse(0),
                };
    }
}
