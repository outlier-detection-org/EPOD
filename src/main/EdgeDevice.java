package main;

import Detector.Detector;
import Detector.NewNETS;
import RPC.RPCFrame;
import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.HashFamily;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

@SuppressWarnings("unchecked")
public class EdgeDevice extends RPCFrame implements Runnable {
    public ArrayList<Vector> rawData;
    public ArrayList<Vector> allRawDataList = new ArrayList<>();

    private final int numberOfHashTables;
    public Index index;
    public int[] fingerprints;
    public HashSet[] aggFingerprints;

    public EdgeNode nearestNode;

    public Detector detector;

    public EdgeDevice(HashFamily hashFamily, int NumberOfHashes, int NumberOfHashTables){
        this.port = new Random().nextInt(50000)+10000;
        this.detector = new NewNETS(0);
        this.numberOfHashTables = NumberOfHashTables;
        this.index = new Index(hashFamily,NumberOfHashes,NumberOfHashTables);
        this.fingerprints = new int[NumberOfHashTables];
        this.aggFingerprints = new HashSet[NumberOfHashTables];
        for (int i = 0; i < NumberOfHashTables; i++) {
            aggFingerprints[i] = new HashSet<Integer>();
        }
    }
    public void clearFingerprints(){
        this.aggFingerprints = new HashSet[numberOfHashTables];
        for (int i = 0; i < numberOfHashTables; i++) {
            aggFingerprints[i] = new HashSet<Integer>();
        }
    }
    public List<Vector> detectOutlier(long currentTime) throws Throwable {
        System.out.println(Thread.currentThread().getName()+" "+this+": receive data and detect outlier: "+this.rawData.size());
        generateAggFingerprints(rawData);
        System.out.println("zxy: "+this.allRawDataList.size());
        sendAggFingerprints();
        System.out.println("zxy: "+this.allRawDataList.size());
//        detector.detectOutlier(allRawDataList,currentTime);
        return rawData;
    }

    public void generateAggFingerprints(List<Vector> data){
        for (Vector datum : data) {
            for (int j = 0; j < this.numberOfHashTables; j++) {
                int bucketId = index.getHashTable().get(j).getHashValue(datum);
                aggFingerprints[j].add(bucketId);
            }
        }
        System.out.println(Thread.currentThread().getName()+": "+this+"generateAggFingerprints "+ aggFingerprints.length);
    }

    public void sendAggFingerprints() throws Throwable {
        System.out.println(Thread.currentThread().getName()+": "+this+" sendAggFingerprints, invoke upload");
        Object[] parameters = new Object[]{aggFingerprints};
        List<Vector> result = (List<Vector>) invoke("localhost",this.nearestNode.port,EdgeNode.class.getMethod("upload", HashSet[].class),parameters);
        if(!result.isEmpty()){
            this.allRawDataList.addAll(result);
        }
        this.allRawDataList.addAll(this.rawData);
    }

    public List<Vector> sendData(){
        return rawData;
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }

    public void setRawData(ArrayList<Vector> rawData) {
        this.rawData = rawData;
    }
}
