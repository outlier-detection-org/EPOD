package utils;

import Detector.NewNETS;
import RPC.Vector;
import java.io.*;
import java.util.*;

public class CompareResult {
    public static int nW=Constants.nW;
    public static int W=Constants.W;
    public static String dataset = Constants.dataset;
    public static BufferedWriter outlierFw;
    public static HashMap<Integer,List<Vector>> allDataByTime = new HashMap<>();

    public static HashSet<Vector> detectOutliersNaive(List<Vector> data, int itr) throws IOException {
        System.out.println("Detecting outliers using Naive method..");
        allDataByTime.put(itr,data);
        if (itr >= Constants.nS - 1){
            if (itr >= Constants.nS) allDataByTime.remove(itr - Constants.nS);
            List<Vector> allData = new ArrayList<>();
            allDataByTime.values().forEach(allData::addAll);
            HashSet<Vector> outliers = new HashSet<Vector>();
            for (Vector v : allData) {
                int numOfNeighbors = 0;

                NewNETS newNETS = new NewNETS(0);
                int nn = 0;
                int outSafe = 0;
                int outUnsafe = 0;
                ArrayList<Short> fullDimCellIdx0 = new ArrayList<>();
                for (int j = 0; j < Constants.dim; j++) {
                    short dimIdx = (short) ((v.values.get(j) - newNETS.minValues[j]) / newNETS.dimLength[j]);
                    fullDimCellIdx0.add(dimIdx);
                }

                for (Vector v2 : allData) {
                    if (v.equals(v2)) continue;
                    if (distance(v, v2) <= Constants.R * Constants.R) {
                        numOfNeighbors++;

                        ArrayList<Short> fullDimCellIdx = new ArrayList<>();
                        for (int j = 0; j < Constants.dim; j++) {
                            short dimIdx = (short) ((v2.values.get(j) - newNETS.minValues[j]) / newNETS.dimLength[j]);
                            fullDimCellIdx.add(dimIdx);
                        }
                        if (fullDimCellIdx.equals(fullDimCellIdx0)){
                            nn++;
                        }else {
                            if (v.slideID>v2.slideID){
                                outUnsafe++;
                            }
                            else {
                                outSafe++;
                            }
                        }

                    }
                }
                if(v.values.get(0) == 8.6693){
                    System.out.println("NAIVE neighbor: " + numOfNeighbors);

                    System.out.println("NETS nn: " + (nn));
                    System.out.println("NETS SafeOut: " + outSafe);
                    System.out.println("NETS UnSafeOut: " + outUnsafe);

                }
                if (numOfNeighbors < Constants.K) {
                    outliers.add(v);
                }
            }
            return outliers;
        }
        return new HashSet<>();
    }

    public static double distance(Vector v1, Vector v2){
        double ss = 0;
        for (int i = 0; i < v2.values.size(); i++) {
            ss += Math.pow((v1.values.get(i) - v2.values.get(i)), 2);
        }
        return ss;
    }
    public static void main(String[] args) throws IOException{
        outlierFw = new BufferedWriter(new FileWriter(
                "src\\Result\\"+"compare_result_"+dataset+".txt"));
        double[] result = compare("src/Result/49_79"+".txt");
        outlierFw.write("Dataset is "+dataset+"\n");
        outlierFw.write("Precision: "+result[0]/nW+"\n");
        outlierFw.write("Recall: "+result[1]/nW+"\n");
        outlierFw.write("F1: "+result[2]/nW);
        outlierFw.close();
    }
    
    public static double[] compare(String filename1) throws IOException{
        int number = 2;
        BufferedReader[] approx = new BufferedReader[number];
        String[] fileNames = new String[]{
                "src/Result/49_Result_Naive_TAO_outliers.txt",
                "src/Result/79_Result_NETS_CENTRALIZE_TAO_outliers.txt",
        };
        for (int i=0;i<number;i++){
            approx[i] = new BufferedReader(new FileReader(fileNames[i]));
            approx[i].readLine();
        };

        BufferedReader exact = new BufferedReader(new FileReader(filename1));
        exact.readLine();

        double[] precisions = new double[nW];
        double[] recalls = new double[nW];
        double[] f1s = new double[nW];

        for (int j=0;j<Constants.nW;j++) {
            outlierFw.write("Window "+j+"\n");
            HashSet<String> approxValues = new HashSet<>();
            HashSet<String> exactValues = new HashSet<>();

            String line = "";
            for (int i =0;i<number;i++) {
                while ((line = approx[i].readLine()) != null && !line.startsWith("Window")) {
                    approxValues.add(line.trim());
                }
            }
            line = "";
            while ((line = exact.readLine()) != null&& !line.startsWith("Window")) {
                exactValues.add(line.trim());
            }

            double precision = 0;
            double recall = 0;
            double F1 = 0;

            for (String i : approxValues) {
                if (exactValues.contains(i))
                    precision++;
                else {
                    System.out.println(i);
                }
            }
            for (String i : exactValues) {
                if (approxValues.contains(i))
                    recall++;
            }

            //print confusion matrix
            double cf2 = precision/exactValues.size(); //��ʵ��ȷ�б�Ԥ����ȷ��
            double cf1 = 1 - cf2;//��ʵ��ȷ�б�Ԥ������
            double cf4 = (approxValues.size() - precision) /(W - exactValues.size());//��ʵ�����б�Ԥ����ȷ��
            double cf3 = 1 - cf4;//��ʵ����Ԥ������
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
                outlierFw.write(x +" ");
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
