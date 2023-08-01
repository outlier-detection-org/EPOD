package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public void runTestNetwork() throws Throwable {
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
        Constants.R = 1.3;
    }
}

