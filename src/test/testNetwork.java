package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public static BufferedWriter testingCSV;
    public static double sum = 0;
    public static String[] methods = new String[]{"NETS", "MCOD", "NETS_CENTRALIZE", "MCOD_CENTRALIZE", "NETS_P2P", "MCOD_P2P"};
    public static double[] accuracys = new double[]{0.025, 0.05, 0.1, 0.15};

    public static void runTestNetwork() throws Throwable {
        DeviceFactory edgeDeviceFactory;
        edgeDeviceFactory = new DeviceFactory();
        EdgeNodeNetwork.createNetwork(Constants.nn, Constants.dn, edgeDeviceFactory);
        EdgeNodeNetwork.startNetwork();
//        EdgeNodeNetwork.resetEdgeNetwork();
    }

    public static void main(String[] args) throws Throwable {
//        simple_run();
//        change_nd();
        change_nn();
    }

    public static void simple_run() throws Throwable {
        File f = new File(Constants.resultPrefix);
        if (!f.exists()) {
            f.mkdirs();
        }
        runTestNetwork();
    }

    public static void change_nd() throws Throwable {
        testingCSV = new BufferedWriter(new FileWriter("src/Result/testing_CSV"));
        for (int m = 0; m < methods.length; m++) {
            Constants.methodToGenerateFingerprint = methods[m];
            testingCSV.write(Constants.methodToGenerateFingerprint + "\n");
            testingCSV.write("nd,4,5,6,7,8,9,10\n");
            testingCSV.write("nn = 4,");
            for (int dn = 4; dn <= 10; dn += 1) {
                Constants.dn = dn;
                Constants.resultPrefix = "src/Result/" + Constants.methodToGenerateFingerprint + "_" + Constants.nn + "*" + dn + "_" + Constants.dataset + "/";
                Constants.getDataInfoCSV = Constants.resultPrefix + "get_data_info.csv";
                Constants.supportDeviceInfoCSV = Constants.resultPrefix + "support_device_info.csv";
                File f = new File(Constants.resultPrefix);
                if (!f.exists()) {
                    f.mkdirs();
                }
                sum = 0;
                for (int i = 0; i < 3; i++) {
                    runTestNetwork();
                }
                testingCSV.write((int) (sum / 3) + ",");
                testingCSV.flush();
            }
            testingCSV.write("\n");
            testingCSV.flush();
        }
        testingCSV.flush();
    }

    public static void change_nn() throws Throwable {
        testingCSV = new BufferedWriter(new FileWriter("src/Result/testing_CSV"));
        for (int m = 3; m < methods.length; m++) {
            Constants.methodToGenerateFingerprint = methods[m];
            testingCSV.write(Constants.methodToGenerateFingerprint + "\n");
            testingCSV.write("nn,2,3,4,5,6\n");
            testingCSV.write("nd = 8,");
            for (int nn = 2; nn <= 6; nn += 1) {
                Constants.nn = nn;
                Constants.resultPrefix = "src/Result/" + Constants.methodToGenerateFingerprint + "_" + Constants.nn + "*" + Constants.dn + "_" + Constants.dataset + "/";
                Constants.getDataInfoCSV = Constants.resultPrefix + "get_data_info.csv";
                Constants.supportDeviceInfoCSV = Constants.resultPrefix + "support_device_info.csv";
                File f = new File(Constants.resultPrefix);
                if (!f.exists()) {
                    f.mkdirs();
                }
                sum = 0;
                for (int i = 0; i < 3; i++) {
                    runTestNetwork();
                }
                testingCSV.write((int) (sum / 3) + ",");
                testingCSV.flush();
            }
            testingCSV.write("\n\n");
            testingCSV.flush();
        }
        testingCSV.flush();
    }


    public static void default_measurement() throws Throwable {
        PrintStream ps;
        ps = new PrintStream(new FileOutputStream("src/Result/testing"));
        System.setOut(ps);
        System.out.println("dn/nn: " + Constants.dn + "/" + Constants.nn);
        System.out.println("R/K/W/S: " + Constants.R + "/" + Constants.K + "/" + Constants.W + "/" + Constants.S);
        System.out.println("# of windows: " + (Constants.nW));
        System.out.println("============================");

        for (int m = 0; m < methods.length; m++) {
            Constants.methodToGenerateFingerprint = methods[m];
            Constants.resultPrefix = "src/Result/" + Constants.methodToGenerateFingerprint + "_4*8_STK_default/";
            Constants.getDataInfoCSV = Constants.resultPrefix + "get_data_info.csv";
            Constants.supportDeviceInfoCSV = Constants.resultPrefix + "support_device_info.csv";
            EdgeNodeNetwork.getDataInfoCSV = new BufferedWriter(new FileWriter(Constants.getDataInfoCSV));
            EdgeNodeNetwork.supportDeviceInfoCSV = new BufferedWriter(new FileWriter(Constants.supportDeviceInfoCSV));
            File f = new File(Constants.resultPrefix);
            if (!f.exists()) {
                f.mkdirs();
            }
            System.out.println("Method: " + Constants.methodToGenerateFingerprint);
            runTestNetwork();
        }
    }

    public static void various_accuracy_R_K_methods() throws Throwable {
        testing = new BufferedWriter(new FileWriter("src/Result/testing"));
        testingCSV = new BufferedWriter(new FileWriter("src/Result/testing_CSV"));
        for (int a = 1; a < accuracys.length; a++) {
            Constants.mix_rate_node = accuracys[a];
            testing.write("Accuracy: " + Constants.mix_rate_node + "\n");

            for (String s : methods) {
                Constants.methodToGenerateFingerprint = s;
                testing.write("Method: " + Constants.methodToGenerateFingerprint + "\n");
                testingCSV.write(Constants.methodToGenerateFingerprint + " " + Constants.mix_rate_node + "\n");
                testingCSV.write("R \\ K,10,15,20,25\n");
                for (double R = 0.3; R < 0.9; R += 0.1) {
                    Constants.R = R;
                    testingCSV.write(Constants.R + ",");
                    for (int K = 10; K <= 25; K = K + 5) {
                        Constants.K = K;
                        testing.write("R = " + Constants.R + "\n");
                        testing.write("K = " + Constants.K + "\n");
                        sum = 0;
                        for (int i = 0; i < 3; i++) {
                            runTestNetwork();
                        }
                        testing.write("Average time cost per slide is: " + (int) (sum / 3) + "\n");
                        testing.write("===============================\n");
                        testing.flush();
                        testingCSV.write((int) (sum / 3) + ",");
                        testingCSV.flush();
                    }
                    testing.write("++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
                    testing.flush();
                    testingCSV.write("\n");
                    testingCSV.flush();
                }
                testing.write("###########################################################################\n\n");
                testing.flush();
                testingCSV.write("\n");
                testingCSV.flush();
            }
            testing.write("_________________________________________________________________________________\n\n\n");
            testing.flush();
        }
        testing.flush();
        testing.close();
        testingCSV.flush();
    }
}

