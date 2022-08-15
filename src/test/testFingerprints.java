package test;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.util.TestUtils;
import main.EdgeDevice;
import main.EdgeDeviceFactory;
import utils.Constants;

import java.util.*;


public class testFingerprints {
    public static void main(String[] args) throws Throwable {
        int numberOfHashes = 4;
        int numberOfHashTables = 40;
        int dimensions = 20;
        Constants.slide = 500;
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                dimensions, numberOfHashes, numberOfHashTables);
//        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
//        EdgeNodeNetwork.createNetwork(3, edgeDeviceFactory);
//        EdgeNodeNetwork.startNetwork();

        System.out.println("started!");
        EdgeDevice device = edgeDeviceFactory.createEdgeDevice();
        ArrayList<Vector> dataset = TestUtils.generate(dimensions, 1,100);

        ArrayList<Vector> data_one = (ArrayList<Vector>) dataset.clone();
        device.generateAggFingerprints(dataset);
        HashSet[] fingerprint0 = device.aggFingerprints;
        int numberOfTrue = 0;
        int numberOfFalse = 0;
        System.out.println("=================================================");
        TestUtils.addNeighbours(dataset, 50, 10);
        HashSet<Integer> intersection = new HashSet<>();
        for (Vector v:dataset){
            boolean flag =false;
            ArrayList<Vector> arrayList = new ArrayList<>();
            arrayList.add(v);
            device.generateAggFingerprints(arrayList);
            HashSet[] fingerprint =device.aggFingerprints;
            for (int i=0;i<numberOfHashTables;i++){
                intersection.addAll(fingerprint0[i]);
                intersection.retainAll(fingerprint[i]);
                if (!intersection.isEmpty()){
                    flag = true;
                    break;
                }
            }
            if (flag){
                numberOfTrue++;
            }else numberOfFalse++;
            device.clearFingerprints();
        }
        System.out.println("True: "+numberOfTrue);
        System.out.println("False: "+numberOfFalse);
        System.out.println("========================================================");
        numberOfTrue = 0;
        numberOfFalse = 0;
        dataset = data_one;
        TestUtils.addNeighbours(dataset, 50, 50);
        intersection = new HashSet<>();
        for (Vector v:dataset){
            boolean flag =false;
            ArrayList<Vector> arrayList = new ArrayList<>();
            arrayList.add(v);
            device.generateAggFingerprints(arrayList);
            HashSet[] fingerprint =device.aggFingerprints;
            for (int i=0;i<numberOfHashTables;i++){
                intersection.addAll(fingerprint0[i]);
                intersection.retainAll(fingerprint[i]);
                if (!intersection.isEmpty()){
                    flag = true;
                    break;
                }
            }
            if (flag){
                numberOfTrue++;
            }else numberOfFalse++;
            device.clearFingerprints();
        }
        System.out.println("True: "+numberOfTrue);
        System.out.println("False: "+numberOfFalse);
    }

}

