package utils;

import java.util.Random;

public class Constants {
//    public static boolean withTime = true;

    // Configuration
    public static int currentSlideID = -1;

    public static int threadhold = -1000000;
    public static int nn = 4;
    public static int dn = 8;
    public static int nW = 10;
    public static String methodToGenerateFingerprint = "NETS"; //"NETS" "MCOD" "NETS_CENTRALIZE" "MCOD_CENTRALIZE" "NETS_P2P" "MCOD_P2P"
    public static String dataset = "STK"; //"FC"(ignore) "TAO" "GAS" "STK" "GAU" "EM" "HPC"

    //calculated automatically

    public static double R = -1;// distance threshold, default=6.5(HPC), 115(EM), 1.9(TAO), 0.45(STK), 0.028(GAU), 525(FC), 2.75(GAS), 5(RC)
    public static int dim = -1; // dimension, default = 7(HPC), 16(EM), 55(FC), 3(TAO), 10(GAS),1(STK)
    public static int subDim = -1;
    public static int S = -1; // sliding size, default = 500(FC, TAO), 5000(Otherwise)
    public static int W = -1; // Window size, default = 10000(FC, TAO), 100000(Otherwise)
    public static int nS = -1;

    //fixed
    public static int K = 50; // neighborhood threshold, default = 50
    public static String prefix = "Datasets/";

    //Paths
    //    public static String prefix = "/home/xinyingzheng/Desktop/outlier_detection";
    public static double mix_rate_node = 0.5;
//    public static String deviceIdPrefix = Constants.prefix + "/DeviceId_data/Device_" + nn * dn + "_" + dataset + "/";
    public static String deviceIdPrefix = Constants.prefix + "/DeviceId_data/Node_" + nn + "_Device_" + dn + "_" + dataset + "_K_" + mix_rate_node + "/";

    public static String timePrefix = Constants.prefix + "/Timestamp_data/Node_" + nn + "_Device_" + dn + "_" + dataset + "_K_" + mix_rate_node + "/";
//    public static String timePrefix = Constants.prefix + "/Timestamp_data/Device_" + nn * dn + "_" + dataset + "/";
    public static String variable = "mix0.5";
    public static String resultPrefix = "src/Result/"+methodToGenerateFingerprint +"_"+ nn + "*" + dn + "_" + dataset + "_"+ variable + "/";

    public static String resultFile = resultPrefix +
            "_Result_"+Constants.methodToGenerateFingerprint+ "_outliers.txt";
    public static String resultNaiveFile = resultPrefix+
            "_Result_Naive_" + "_outliers.txt";
    public static String naiveInfo = resultPrefix+
            "_Result_Naive_info" + "_outliers.txt";
    public static String getDataInfo = resultPrefix+"get_data_info";
    public static String supportDeviceInfo = resultPrefix+"support_device_info";
    public static String ratioInfo = resultPrefix+"ratio_info";


    public static String forestCoverFileName = prefix + "fc.txt";
    public static String taoFileName = prefix + "tao.txt";
    public static String emFileName = prefix + "ethylene.txt";
    public static String stockFileName = prefix + "stock.txt";
    public static String gaussFileName = prefix + "gaussian.txt";
    public static String hpcFileName = prefix + "household2.txt";
    public static String gasFileName = prefix + "gas.txt";
    public static String randomClusterFileName = prefix + "RandomCluster.txt";

    //========================for multiple query========================
    public static double[] Rs = new double[nn*dn];
    public static int[] Ks = new int[nn*dn];
    static Random random = new Random();
    public static boolean isVariousR = false;
    public static boolean isVariousK = false;
    public static boolean isMultipleQuery = isVariousR || isVariousK;

    static {
        if (dataset.contains("TAO") || dataset.contains("FC") || dataset.contains("RC")) {
            S = 500;
            W = S * 20;
        } else {
            S = 500;
            W = 10000;
        }
        nS = W / S;

        if (dataset.contains("FC")) {
            R = 525;
            dim = 55;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 3;
            }
        } else if (dataset.contains("TAO")) {
            R = 1.9;
            dim = 3;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 3;
            }
        } else if (dataset.contains("EM")) {
            R = 115;
            dim = 16;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 4;
            }
        } else if (dataset.contains("STK")) {
            R = 0.45;
            dim = 1;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 1;
            }
        } else if (dataset.contains("GAU")) {
            R = 0.028;
            dim = 1;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 1;
            }
        } else if (dataset.contains("HPC")) {
            R = 6.5;
            dim = 7;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 7;
            }
        } else if (dataset.contains("GAS")) {
            R = 2.75;
            dim = 10;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 10;
            }
        } else if (dataset.contains("RC")){
            R = 4;
            dim = 5;
            if (methodToGenerateFingerprint.contains("NETS")){
                subDim = 1;
            }
        }

        //========================for multiple query========================
        for (int i = 0; i < nn*dn; i++) {
            if (isVariousR) {
                Rs[i] = R + random.nextDouble() * R * 0.5 * Math.pow(-1, i);
            }
            else Rs[i] = R;

            if (isVariousK) {
                Ks[i] = K + random.nextInt(10) * (int) Math.pow(-1, i);
            }
            else Ks[i] = K;
        }
    }
}


