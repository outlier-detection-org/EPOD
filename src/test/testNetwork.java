package test;

import main.EdgeDevice;
import main.EdgeDeviceFactory;
import main.EdgeNodeNetwork;
import utils.Constants;
import java.util.ArrayList;
import java.util.HashMap;

public class testNetwork {
    public static Integer total =0;
    public static HashMap<EdgeDevice,Integer> index = new HashMap<>();
    public static ArrayList<HashMap<Integer,Integer>> buckets= new ArrayList<>();
    public static HashMap<Integer,Integer> all = new HashMap<>();

    public static void main(String[] args) throws Throwable {
        double p1=0.8;
        double p2=0.4;
        double k = Math.log(Constants.W)/Math.log(1/p2);
        double L = Math.pow(Constants.W,Math.log(1/p1)/Math.log(1/p2));
        int numberOfHashes = 8;
        int numberOfHashTables = 1;
        System.out.println("k is "+numberOfHashes+" L is "+numberOfHashTables);

        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(numberOfHashes, numberOfHashTables);
        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
        EdgeNodeNetwork.createNetwork(Constants.nn,Constants.dn, edgeDeviceFactory);
        System.out.println("started!");
        EdgeNodeNetwork.startNetwork();
    }
}

