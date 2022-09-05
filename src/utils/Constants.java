package utils;

import java.util.HashMap;

public class Constants {

    public static double R = 525;
    public static int K = 50;
    public static int W = 1000;
    public static int numberWindow = -1;
    public static int numberSlidePerWindow= 0;
    public static int slide = 500;
    public static int dimensions = 3;
    public static int subDim = 0;
    public static boolean subDimFlag = false;

    public static String dataset = "TAO";
//    public static String prefix = "/home/xinyingzheng/Desktop/outlier_detection";
    public static String prefix = "C:\\Users\\14198\\Desktop\\outlier_detection\\";
    public static String forestCoverFileName = prefix+"\\Datasets\\fc.data";
    public static String taoFileName = prefix+"\\Datasets\\tao.txt";
    public static String emFileName = prefix+"\\Datasets\\ethylene.txt";
    public static String stockFileName = prefix+"\\Datasets\\stock.txt";
    public static String gaussFileName = prefix+"\\Datasets\\gaussian.txt";
    public static String hpcFileName = prefix+"\\Datasets\\household2.txt";

    public static String outputFile= prefix+"\\Algorithm\\output\\outputDefault.txt";

    public static HashMap<String, Integer> radiusEuclideanDict = new HashMap<>();

    static {
        radiusEuclideanDict.put(forestCoverFileName,30);
    }
}

