package test;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.families.EuclidianHashFamily;
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
        int size = 6;
        int dimensions = 20;

        ArrayList<ArrayList<Vector>> buckets = new ArrayList<>();
        for (int i=0;i<size;i++){
            buckets.add(generateBucket(dimensions,25,3));
        }
        int n=buckets.stream().mapToInt(ArrayList::size).sum();

        double p1=0.8;
        double p2=0.4;
        double k = Math.log(n)/Math.log(1/p2);
        double L = Math.pow(n,Math.log(1/p1)/Math.log(1/p2));
        int numberOfHashes = (int) k;
        int numberOfHashTables = (int) L;
        System.out.println("k is "+numberOfHashes+" L is "+numberOfHashTables);
        EuclideanDistance euclideanDistance = new EuclideanDistance();

        double avg=0.0;
        for (Vector v:buckets.get(0)){
            avg+=euclideanDistance.distance(buckets.get(0).get(0),v);
        }
        Constants.R = avg/buckets.get(0).size();
        System.out.println("The avg distance of neighbor is "+ Constants.R);

        EuclidianHashFamily hashFamily;
        if ((int) (1 * Constants.R) == 0) {
            hashFamily = new EuclidianHashFamily(4, dimensions);
        } else {
            hashFamily = new EuclidianHashFamily((int) (10*Constants.R), dimensions);
        }
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(hashFamily, numberOfHashes, numberOfHashTables);
        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
        EdgeNodeNetwork.createNetwork(3,2, edgeDeviceFactory);
        EdgeNodeNetwork.startNetwork();
        System.out.println("started!");

//        ArrayList<Vector> outlier = DataGenerator.fullTransfer(buckets.get(0));
        ArrayList<Vector> outlier = DataGenerator.nonTransfer(buckets);
        EdgeNodeNetwork.stopNetwork();
    }

    public static ArrayList<Vector> generateBucket(int dimensions,int size,double radius){
        ArrayList<Vector> data = TestUtils.generate(dimensions,1,100);
        TestUtils.addNeighbours(data,size-1,radius);
        return data;
    }
}

