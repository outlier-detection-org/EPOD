package utils;

public class Constants {
    public static boolean withTime = true;
    public static boolean withDeviceId = true;
    public static int nn = 2;
    public static int dn = 3;

    public static double R = 1.9;
    // distance threshold, default=6.5(HPC), 115(EM), 1.9(TAO), 0.45(STK), 0.028(GAU), 525(FC), 2.75(GAS)
    public static int K = 50; // neighborhood threshold, default = 50
    public static int dim = 3; // dimension, default = 7(HPC), 16(EM), 55(FC), 3(TAO), 10(GAS)
    public static int S = 500; // sliding size, default = 500(FC, TAO), 5000(Otherwise)
    public static int W = 10000; // sliding size, default = 10000(FC, TAO), 100000(Otherwise)
    public static int nS = W / S;
    public static int nW = 1;

    public static String dataset = "TAO";
    public static String datasetPath;
    public static String datasetPathWithTime;
    //    public static String prefix = "/home/xinyingzheng/Desktop/outlier_detection";
    public static String prefix = "C:\\Users\\14198\\Desktop\\outlier_detection";
    public static String forestCoverFileName = prefix + "\\Datasets\\fc.data";
    public static String taoFileName = prefix + "\\Datasets\\tao.txt";
    public static String emFileName = prefix + "\\Datasets\\ethylene.txt";
    public static String stockFileName = prefix + "\\Datasets\\stock.txt";
    public static String gaussFileName = prefix + "\\Datasets\\gaussian.txt";
    public static String hpcFileName = prefix + "\\Datasets\\household2.txt";
    public static String gasFileName = prefix + "\\Datasets\\gas.txt";
}

