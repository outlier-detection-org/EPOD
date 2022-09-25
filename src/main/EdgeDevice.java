package main;

import Detector.Detector;
import Detector.NewNETS;
import RPC.RPCFrame;
import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import utils.Constants;
import utils.DataGenerator;

import java.util.*;

@SuppressWarnings("unchecked")
public class EdgeDevice extends RPCFrame implements Runnable {
    public int deviceId;
    public ArrayList<Vector> rawData;
    private final int numberOfHashTables;
    public Index index;
    public HashMap<Integer,ArrayList<Vector>> aggFingerprints;
    public HashMap<Integer,List<Vector>> allRawDataList;
    public HashMap<Integer,ArrayList<Integer>> dependentDevice;
    public DataGenerator dataGenerator;
    public EdgeNode nearestNode;
    public Detector detector;

    public EdgeDevice(Index index, int NumberOfHashTables,int deviceId) throws Throwable {
        this.port = new Random().nextInt(50000)+10000;
        this.deviceId = deviceId;
        this.detector = new NewNETS(0);
        this.numberOfHashTables = NumberOfHashTables;
        this.index = index;
        this.dependentDevice = new HashMap<>();
        this.aggFingerprints = new HashMap<>();
        this.allRawDataList =new HashMap<>();
        this.dataGenerator = DataGenerator.getInstance(Constants.dataset,deviceId);
    }

    public void clearFingerprints(){
        this.aggFingerprints = new HashMap<>();
    }

    public Set<Vector> detectOutlier(long itr) throws Throwable {
//        System.out.println(Thread.currentThread().getName()+" "+this+": receive data and detect outlier: "+this.rawData.size());
        Date currentRealTime = dataGenerator.getFirstTimeStamp(Constants.datasetPathWithTime);
        currentRealTime.setTime(currentRealTime.getTime() + (long) Constants.S * 10 * 1000 * itr);
        this.rawData = dataGenerator.getTimeBasedIncomingData(currentRealTime, Constants.S*10,deviceId);
        System.out.println(Thread.currentThread().getName()+" "+deviceId+": "+rawData.size());
//        generateAggFingerprints(rawData);
//        sendAggFingerprints();
        return new HashSet<>();
    }

    public void generateAggFingerprints(List<Vector> data) {
//        System.out.println("raw data size: "+data.size());
//        System.out.println(Thread.currentThread().getName()+": "+this+" generateAggFingerprints");
        for (Vector datum : data) {
            for (int j = 0; j < this.numberOfHashTables; j++) {
                int bucketId = index.getHashTable().get(j).getHashValue(datum);
//                System.out.println(Thread.currentThread().getName()+": "+bucketId);
                if (!aggFingerprints.containsKey(bucketId)) {
                    aggFingerprints.put(bucketId, new ArrayList<Vector>());
                    allRawDataList.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                }
                aggFingerprints.get(bucketId).add(datum);
                allRawDataList.get(bucketId).add(datum);
            }
        }
        HashSet<Vector> tmp = new HashSet<>();
        for (List x: this.allRawDataList.values()){
            tmp.addAll(x);
        }
        System.out.println(Thread.currentThread().getName()+" Original data size: "+tmp.size());
    }

    public void sendAggFingerprints() throws Throwable {
//        System.out.println(Thread.currentThread().getName()+": "+this+" sendAggFingerprints");
        Object[] parameters = new Object[]{new ArrayList<>(aggFingerprints.keySet()),this.hashCode()};
        invoke("localhost",this.nearestNode.port,
                EdgeNode.class.getMethod("upload", ArrayList.class, Integer.class),parameters);
    }

    public void getData() throws InterruptedException {
//        System.out.println(Thread.currentThread().getName()+": "+this+" getData");
        ArrayList<Thread> threads = new ArrayList<>();
//        System.out.println(this+" dependent device size "+dependentDevice.keySet().size());
        for (Integer edgeDeviceCode :dependentDevice.keySet()){
            Thread t =new Thread(()->{
                Object[] parameters = new Object[]{dependentDevice.get(edgeDeviceCode)};
                try {
                    HashMap<Integer,ArrayList<Vector>> data = (HashMap<Integer, ArrayList<Vector>>) invoke("localhost",
                            EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode).port,
                            EdgeDevice.class.getMethod("sendData", ArrayList.class),parameters);
                    for (Integer x:data.keySet()){
                        this.allRawDataList.get(x).addAll(data.get(x));
//                        System.out.println(Thread.currentThread().getName()+" "+this+" get data from "
//                                +EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode)+" total size is "+
//                                this.allRawDataList.values().stream().mapToInt(List::size).sum());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t:threads){
            t.join();
        }
        HashSet<Vector> tmp = new HashSet<>();
        for (List x: this.allRawDataList.values()){
            tmp.addAll(x);
        }
        System.out.println(Thread.currentThread().getName()+" Final data size: "+tmp.size());
    }

    public HashMap<Integer,ArrayList<Vector>> sendData(ArrayList<Integer> bucketIds){
        HashMap<Integer,ArrayList<Vector>> data = new HashMap<>();
        for (Integer x:bucketIds){
            data.put(x, (ArrayList<Vector>) aggFingerprints.get(x));
        }
//        System.out.println(Thread.currentThread().getName()+": "+this+" sendData, size is "+
//                data.values().stream().mapToInt(ArrayList::size).sum());
        return data;
    }

    public void setDependentDevice(HashMap<Integer, ArrayList<Integer>> dependentDevice) throws InterruptedException {
//        System.out.println(Thread.currentThread().getName()+": "+this+" setDependentDevice");
        this.dependentDevice = dependentDevice;
        getData();
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }

    public void setRawData(ArrayList<Vector> rawData) {
        this.rawData = rawData;
    }
}
