package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class testNetwork {

    public static void main(String[] args) throws Throwable {
        PrintStream ps;
        DeviceFactory edgeDeviceFactory;
        File f = new File(Constants.resultPrefix);
        if (f.exists()) {
            return;
        } else f.mkdirs();
        ps = new PrintStream(new FileOutputStream(Constants.resultPrefix + "_total.txt"));
        edgeDeviceFactory = new DeviceFactory();

        System.setOut(ps);
        EdgeNodeNetwork.createNetwork(Constants.nn,Constants.dn, edgeDeviceFactory);
        System.out.println("started!");
        EdgeNodeNetwork.startNetwork();
    }
}

