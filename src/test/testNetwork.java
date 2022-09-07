package test;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.util.TestUtils;
import main.EdgeDevice;
import main.EdgeDeviceFactory;
import main.EdgeNodeNetwork;
import utils.Constants;
import utils.DataGenerator;

import java.io.IOException;
import java.util.*;


public class testNetwork {
    public static void main(String[] args) throws Throwable {
        int numberOfHashes = 4;
        int numberOfHashTables = 40;
        int dimensions = 20;
        Constants.slide = 500;
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                dimensions, numberOfHashes, numberOfHashTables);
        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
        EdgeNodeNetwork.createNetwork(3, edgeDeviceFactory);
        EdgeNodeNetwork.startNetwork();

        System.out.println("started!");
        DataGenerator.getInstance("TAO", false);
        int currentTime = 0;
        Constants.W = 10000;
        Constants.slide = 500;
        DataGenerator.getIncomingData(0, Constants.slide);
        EdgeNodeNetwork.stopNetwork();
    }
}

