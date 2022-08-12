package main;

import Detector.Detector;
import Detector.NewNETS;
import RPC.RPCFrame;
import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.HashFamily;
import dataStructure.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

@SuppressWarnings("unchecked")
public class EdgeDevice extends RPCFrame implements Runnable {
    public List<Data> rawData;
    public List<Data> allRawDataList = new ArrayList<>();

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
    public List<Data> detectOutlier(List<Data> data,long currentTime) throws Throwable {
        rawData = data;
        generateAggFingerprints(rawData);
        sendAggFingerprints();
        ///TODO:aggregate data and run outlier detection algorithm
        detector.detectOutlier(allRawDataList,currentTime);
        return rawData;
    }

    public void generateAggFingerprints(List<Data> data){
        for (Data datum : data) {
            for (int j = 0; j < this.numberOfHashTables; j++) {
                /// TODO:convert data to vector
                Vector v = new Vector(datum.values, datum.arrivalTime);
                int bucketId = index.getHashTable().get(j).getHashValue(v);
                aggFingerprints[j].add(bucketId);
            }
        }
    }

    public void sendAggFingerprints() throws Throwable {
        Object[] parameters = new Object[]{aggFingerprints};
        SynchronousQueue<Data> result = (SynchronousQueue<Data>) invoke("localhost",this.nearestNode.port,EdgeNode.class.getMethod("upload", HashSet[].class),parameters);
        while(!result.isEmpty()){
            this.allRawDataList.add(result.poll());
        }
    }

    public List<Data> sendData(){
        return rawData;
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }

    public void setRawData(List<Data> rawData) {
        this.rawData = rawData;
    }
}
