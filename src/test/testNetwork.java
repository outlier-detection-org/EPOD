package test;

import Framework.DeviceFactory;
import Framework.EdgeNodeNetwork;
import org.apache.thrift.TApplicationException;
import utils.Constants;

import java.io.*;

public class testNetwork {
    public static BufferedWriter testing;
    public static double sum = 0;

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
        EdgeNodeNetwork.resetTime();
        EdgeNodeNetwork.startNetwork();
//        Thread.sleep(2000);
    }

    public static void recursive_run() throws IOException {
        try {
            while (Constants.R < 1.6) {
                if (Constants.K == 30) Constants.K = 10;
                while (Constants.K < 30) {
                    sum = 0;
                    testing.write("R = "+Constants.R+"\n");
                    testing.write("K = "+Constants.K+"\n");
                    for (int i = 0; i < 3; i++) {
                        runTestNetwork();
                    }
                    testing.write("Average time cost per slide is: " + sum / 3.0 + "\n");
                    testing.write("===============================\n");

                    testing.flush();
                    Constants.K += 5;
                }
                testing.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
                Constants.R += 0.1;
            }
        }catch (Throwable e){
            testing.write("Error\n");
            recursive_run();
        }finally {
            testing.flush();
            testing.close();
        }
    }

    public static void main(String[] args) throws Throwable {
        testing = new BufferedWriter(new FileWriter("src/Result/testing_R"));
        Constants.K = 10;
        Constants.R = 1.1;
        recursive_run();

//        for (double R = 1.1; R < 1.6; R += 0.1) {
//            Constants.R = R;
//            // K = 10
//            for (int K = 10; K <= 25; K = K + 5) {
//                sum = 0;
//                Constants.K = K;
//                for (int i = 0; i < 3; i++) {
//                    runTestNetwork();
////                if (cnt == 0) i -= 1;
////                cnt++;
//                }
//                testNetwork.testing.write("Average time cost per slide is: " + sum / 3.0 + "\n");
//                testNetwork.testing.write("===============================\n");
//                testNetwork.testing.flush();
//            }
//            testNetwork.testing.write("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
//            testNetwork.testing.flush();
//        }
    }
}

