package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public static double sum = 0;
    public static String[] methods = new String[]{"NETS", "MCOD", "NETS_CENTRALIZE", "MCOD_CENTRALIZE", "NETS_P2P", "MCOD_P2P"};

    public static void runTestNetwork() throws Throwable {
        PrintStream ps;
        DeviceFactory edgeDeviceFactory;
//        File f = new File(Constants.resultPrefix);
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//        ps = new PrintStream(new FileOutputStream(Constants.resultPrefix + "_total.txt"));
        edgeDeviceFactory = new DeviceFactory();

//        System.setOut(ps);
        EdgeNodeNetwork.createNetwork(Constants.nn, Constants.dn, edgeDeviceFactory);
//        System.out.println("started!");
        EdgeNodeNetwork.startNetwork();
//        EdgeNodeNetwork.resetEdgeNetwork();
//        Thread.sleep(2000);
    }

    public static void main(String[] args) throws Throwable {
        testing = new BufferedWriter(new FileWriter("src/Result/testing_R"));
        for (String s : methods) {
            Constants.methodToGenerateFingerprint = s;
            testing.write("Method: " + Constants.methodToGenerateFingerprint + "\n");
            for (double R = 1.1; R < 1.7; R += 0.1) {
                Constants.R = R;
                // K = 10
                for (int K = 10; K <= 25; K = K + 5) {
                    Constants.K = K;
                    testing.write("R = " + Constants.R + "\n");
                    testing.write("K = " + Constants.K + "\n");
                    sum = 0;
                    for (int i = 0; i < 3; i++) {
                        runTestNetwork();
                    }
                    testNetwork.testing.write("Average time cost per slide is: " + sum / 3.0 + "\n");
                    testNetwork.testing.write("===============================\n");
                    testNetwork.testing.flush();
                }
                testNetwork.testing.write("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
                testNetwork.testing.flush();
            }
            testing.write("###########################################################################\n\n");
            testNetwork.testing.flush();
        }
        testing.flush();
        testing.close();
    }
}

