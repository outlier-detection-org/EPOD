package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public static void runTestNetwork() throws Throwable {
        PrintStream ps;
        DeviceFactory edgeDeviceFactory;
        File f = new File(Constants.resultPrefix);
        if (!f.exists()) {
            f.mkdirs();
        }
        ps = new PrintStream(new FileOutputStream(Constants.resultPrefix + "_total.txt"));
        edgeDeviceFactory = new DeviceFactory();

        System.setOut(ps);
        EdgeNodeNetwork.createNetwork(Constants.nn, Constants.dn, edgeDeviceFactory);
        System.out.println("started!");
        EdgeNodeNetwork.startNetwork();
        EdgeNodeNetwork.reOpenEdgeNetwork();
        Thread.sleep(100);
    }

    public static void main(String[] args) throws Throwable {
//        PrintStream ps;
//        DeviceFactory edgeDeviceFactory;
//        File f = new File(Constants.resultPrefix);
//        if (!f.exists()) {
//            f.mkdirs();
//        }
//        ps = new PrintStream(new FileOutputStream(Constants.resultPrefix + "_total.txt"));
//        edgeDeviceFactory = new DeviceFactory();
//
//        System.setOut(ps);
//        EdgeNodeNetwork.createNetwork(Constants.nn,Constants.dn, edgeDeviceFactory);
//        System.out.println("started!");
//        EdgeNodeNetwork.startNetwork();

        testing = new BufferedWriter(new FileWriter("src/Result/testing_R"));
        for (double i = 1.1; i < 2.2; i = i + 0.1) {
            Constants.R = i;
            Constants.K = 30;
            runTestNetwork();
        }
    }
}

