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

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.*;


public class testFingerprints {
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
        DataGenerator.getInstance("TAO",false);
        int currentTime=0;
        Constants.W = 10000;
        Constants.slide = 500;
        DataGenerator.getIncomingData(0,Constants.slide);
        EdgeNodeNetwork.stopNetwork();
//        EdgeDevice device = edgeDeviceFactory.createEdgeDevice();
//        ArrayList<Vector> dataset = TestUtils.generate(dimensions, 1,100);

//        device.generateAggFingerprints(dataset);
//        HashSet[] fingerprint0 = device.aggFingerprints;
//        int numberOfTrue = 0;
//        int numberOfFalse = 0;
//        System.out.println("=================================================");
//        TestUtils.addNeighbours(dataset, 50, 10);
//        HashSet<Integer> intersection = new HashSet<>();
//        for (Vector v:dataset){
//            boolean flag =false;
//            ArrayList<Vector> arrayList = new ArrayList<>();
//            arrayList.add(v);
//            device.generateAggFingerprints(arrayList);
//            HashSet[] fingerprint =device.aggFingerprints;
//            for (int i=0;i<numberOfHashTables;i++){
//                intersection.addAll(fingerprint0[i]);
//                intersection.retainAll(fingerprint[i]);
//                if (!intersection.isEmpty()){
//                    flag = true;
//                    break;
//                }
//            }
//            if (flag){
//                numberOfTrue++;
//            }else numberOfFalse++;
//            device.clearFingerprints();
//        }
//        System.out.println("True: "+numberOfTrue);
//        System.out.println("False: "+numberOfFalse);
//        System.out.println("========================================================");

//        double p1 = 0.2;
//        double p2 = 0.8;
//        ArrayList<ArrayList<Vector>> buckets = bucketing("TAO");
//
//        buckets.stream().filter(v->v.size()>100).forEach(bucket->{
//            device.generateAggFingerprints(bucket);
//            HashSet[] fingerprint = device.aggFingerprints;
//            Arrays.stream(fingerprint).forEach(System.out::println);
//            System.out.println("=========================================");
//        });
//
    }
    public static ArrayList<ArrayList<Vector>> bucketing(String type) throws IOException {
        DataGenerator.getInstance(type,false);
        ArrayList<Vector> dataset = new ArrayList<>(DataGenerator.dataQueue);
        double r = 1.9;
        int[] flag = new int[10000];
        ArrayList<ArrayList<Vector>> buckets = new ArrayList<>();
        DistanceMeasure measure = new EuclideanDistance();
        for (int i=0;i<10000;i++){
            if (flag[i]==1)continue;
            else flag[i]=1;
            Vector v = dataset.get(i);
            ArrayList<Vector> bucket = new ArrayList<>();
            DistanceComparator dc = new DistanceComparator(v, measure);
            PriorityQueue<Vector> pq = new PriorityQueue<Vector>(10000,dc);
            pq.addAll(dataset.subList(0,10000));
            Vector neighbor = pq.peek();
            while (measure.distance(v,neighbor)<=r){
                flag[dataset.indexOf(neighbor)]=1;
                bucket.add(pq.poll());
                neighbor = pq.peek();
            }
            buckets.add(bucket);
        }
        return buckets;
    }
}

