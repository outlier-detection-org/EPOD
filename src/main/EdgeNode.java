package main;

import RPC.RPCFrame;
import dataStructure.Data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

@SuppressWarnings("unchecked")
public class EdgeNode extends RPCFrame implements Runnable {

    private List<Data> localData = new ArrayList<>();
    public SynchronousQueue<Data> allData = new SynchronousQueue<>();
    private final int numberOfHashTables;
    private HashSet[] localAggFingerprints;
    public EdgeDevice edgeDevice;

    public EdgeNode(int numberOfHashTables){
        this.numberOfHashTables = numberOfHashTables;
        this.port = new Random().nextInt(50000)+10000;
        this.localAggFingerprints = new HashSet[numberOfHashTables];
        for (int i = 0; i < numberOfHashTables; i++) {
            localAggFingerprints[i] = new HashSet<Integer>();
        }
    }
    public SynchronousQueue<Data> upload(HashSet[] aggFingerprints) throws Throwable {
        this.localAggFingerprints = aggFingerprints;
        this.allData.clear();
        for (EdgeNode node: EdgeNodeNetwork.edgeNodes) {
            new Thread(() -> {
                List<Data> data = null;
                try {
                    Object[] parameters = new Object[]{aggFingerprints};
                    data = (List<Data>) invoke("localhost", node.port, EdgeNode.class.getMethod("compareAndSend", HashSet[].class), parameters);
                    for (Data d : data) {
                        allData.put(d);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
        return this.allData;
    }

    public List<Data> compareAndSend(HashSet[] aggFingerprints) throws Throwable {
        HashSet<Integer> intersection = new HashSet<>();
        for (int i=0;i<numberOfHashTables;i++){
            intersection.addAll(localAggFingerprints[i]);
            intersection.retainAll(aggFingerprints[i]);
            if (!intersection.isEmpty()){
                return getData();
            }
        }
        return new ArrayList<>();

    }
    public void setEdgeDevice(EdgeDevice edgeDevice) {
        this.edgeDevice = edgeDevice;
    }

    public List<Data> getData() throws Throwable {
        if(localData==null){
            localData = (List<Data>) invoke("localhost",this.edgeDevice.port, EdgeDevice.class.getMethod("sendData"),new Object[]{});
        }
        return localData;
    }
}
