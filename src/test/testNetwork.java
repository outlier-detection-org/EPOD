package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.util.TestUtils;
import main.EdgeDeviceFactory;
import main.EdgeNodeNetwork;
import utils.Constants;
import java.util.ArrayList;

public class testNetwork {

    public static void main(String[] args) throws Throwable {
        double p1=0.8;
        double p2=0.4;
        double k = Math.log(Constants.W)/Math.log(1/p2);
        double L = Math.pow(Constants.W,Math.log(1/p1)/Math.log(1/p2));
        int numberOfHashes = (int) k;
        int numberOfHashTables = (int) L;
        System.out.println("k is "+numberOfHashes+" L is "+numberOfHashTables);

        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(numberOfHashes, numberOfHashTables);
        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
        EdgeNodeNetwork.createNetwork(Constants.nn,Constants.dn, edgeDeviceFactory);
        System.out.println("started!");
        EdgeNodeNetwork.startNetwork();

//        ArrayList<Vector> outlier = DataGenerator.fullTransfer(buckets.get(0));
//        ArrayList<Vector> outlier = DataGenerator.nonTransfer(buckets);
    }
}

