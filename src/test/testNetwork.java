package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public static double sum = 0;
//    public static double cnt = 0;

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
        EdgeNodeNetwork.reOpenEdgeNetwork();
//        Thread.sleep(2000);
    }

    public static void main(String[] args) throws Throwable {
        testing = new BufferedWriter(new FileWriter("src/Result/testing_R"));
//        Constants.R = 1.5;
//        Constants.K = 20;
        for (double R = 1.6; R < 1.8; R += 0.1) {
            Constants.R = R;
            for (int K = 15; K <= 30; K = K + 5) {
                sum = 0;
                Constants.K = K;
                for (int i = 0; i < 3; i++) {
                    runTestNetwork();
//                if (cnt == 0) i -= 1;
//                cnt++;
                }
                testNetwork.testing.write("Average time cost per slide is: " + sum / 3.0 + "\n");
                testNetwork.testing.write("===============================\n");
                testNetwork.testing.flush();
            }
            testNetwork.testing.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
            testNetwork.testing.flush();
        }
//        runTestNetwork();
    }
}

