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
    private final int numberOfHashTables;
    public Index index;
    public Map<Integer,List<Vector>> aggFingerprints;
    public Map<Integer,List<Vector>> allRawDataList;
    public Map<Integer,ArrayList<Integer>> dependentDevice;
    public DataGenerator dataGenerator;
    public EdgeNode nearestNode;
    public Detector detector;
    public long itr;
    public HashSet<Vector> outlier;

    public EdgeDevice(Index index, int NumberOfHashTables,int deviceId) throws Throwable {
        this.port = new Random().nextInt(50000)+10000;
        this.deviceId = deviceId;
        this.detector = new NewNETS(0);
        this.numberOfHashTables = NumberOfHashTables;
        this.index = index;
        this.dependentDevice = Collections.synchronizedMap(new HashMap<>());
        this.aggFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.allRawDataList = Collections.synchronizedMap(new HashMap<>());
        this.dataGenerator = DataGenerator.getInstance(Constants.dataset,deviceId);
    }

    public void clearFingerprints(){
        this.aggFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.allRawDataList = Collections.synchronizedMap(new HashMap<>());
    }

    public Set<Vector> detectOutlier(long itr) throws Throwable {
        this.itr = itr;
        Date currentRealTime = dataGenerator.getFirstTimeStamp(Constants.datasetPathWithTime);
        currentRealTime.setTime(currentRealTime.getTime() + (long) Constants.S * 10 * 1000 * itr);
        generateAggFingerprints(
                dataGenerator.getTimeBasedIncomingData(currentRealTime, Constants.S*10,deviceId));
        sendAggFingerprints();
        return outlier;
    }

    public void generateAggFingerprints(List<Vector> data) {
        clearFingerprints();
        for (Vector datum : data) {
            for (int j = 0; j < this.numberOfHashTables; j++) {
                int bucketId = index.getHashTable().get(j).getHashValue(datum);
                if (!aggFingerprints.containsKey(bucketId)) {
                    aggFingerprints.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                    allRawDataList.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                }
                aggFingerprints.get(bucketId).add(datum);
                allRawDataList.get(bucketId).add(datum);
            }
        }
//        StringBuilder sb =  new StringBuilder();
//        sb.append(this.hashCode()).append(" ");
//        aggFingerprints.keySet().forEach(x->
//                sb.append(x).append(" ").append(aggFingerprints.get(x).size()).append(","));
//        System.out.println(sb.toString());
//        /***
//         * used for test
//         */
//        HashMap<Integer,Integer> xx = new HashMap<>();
//        aggFingerprints.keySet().forEach(a->{
//            xx.put(a,aggFingerprints.get(a).size());
//            synchronized (testNetwork.all){
//            testNetwork.all.put(a,
//                    testNetwork.all.getOrDefault(a, 0)+aggFingerprints.get(a).size());}
//        } );
//        synchronized (testNetwork.buckets) {
//            testNetwork.buckets.add(xx);
//            testNetwork.index.put(this,testNetwork.buckets.size()-1);
//        }
//
//        HashSet<Vector> tmp = new HashSet<>();
//        for (List x: this.allRawDataList.values()){
//            tmp.addAll(x);
//        }
//        synchronized (testNetwork.total){
//            testNetwork.total += tmp.size();
//        }
//        System.out.println(Thread.currentThread().getName()+" "+this+" Original data size: "+tmp.size());
    }

    public void sendAggFingerprints() throws Throwable {
        Object[] parameters = new Object[]{new ArrayList<>(aggFingerprints.keySet()),this.hashCode()};
        invoke("localhost",this.nearestNode.port,
                EdgeNode.class.getMethod("upload", ArrayList.class, Integer.class),parameters);
    }

    public void getData() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Integer edgeDeviceCode :dependentDevice.keySet()){
            Thread t =new Thread(()->{
                Object[] parameters = new Object[]{dependentDevice.get(edgeDeviceCode)};
                try {
                    HashMap<Integer,List<Vector>> data = (HashMap<Integer, List<Vector>>)
                            invoke("localhost",
                            EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode).port,
                            EdgeDevice.class.getMethod("sendData", ArrayList.class),parameters);
                    for (Integer x:data.keySet()) {
                        try {
                            this.allRawDataList.get(x).addAll(data.get(x));
                        } catch (NullPointerException ignored) {
                        }
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
        for (List<Vector> x: this.allRawDataList.values()) {
            tmp.addAll(x);
        }
        outlier = detector.detectOutlier(new ArrayList<>(tmp),itr);
        System.out.println(this+" Final data size: "+tmp.size());
    }

    public HashMap<Integer,List<Vector>> sendData(ArrayList<Integer> bucketIds){
        HashMap<Integer,List<Vector>> data = new HashMap<>();
        for (Integer x:bucketIds){
            data.put(x, aggFingerprints.get(x));
        }
        return data;
    }

    public void setDependentDevice(HashMap<Integer, ArrayList<Integer>> dependentDevice) throws InterruptedException {
        this.dependentDevice = dependentDevice;
        getData();
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }

}
