package main;

import Detector.Detector;
import Detector.NewNETS;
import RPC.RPCFrame;
import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;
import utils.Constants;
import utils.DataGenerator;

import javax.lang.model.util.ElementScanner6;
import java.util.*;

@SuppressWarnings("unchecked")
public class EdgeDevice extends RPCFrame implements Runnable {
    public int deviceId;
    private final int numberOfHashTables;
    public Index index;
    /*this field is used for debugging*/
    public List<Vector> rawData=new ArrayList<>();

    public Map<Long,List<Vector>> aggFingerprints;
    public Map<Long,List<Vector>> allRawDataList;
    public Map<Integer,ArrayList<Integer>> dependentDevice;
    public DataGenerator dataGenerator;
    public EdgeNode nearestNode;
    public Detector detector;
    public long itr;
    public HashSet<Vector> outlier;

    public static HashMap<Long,ArrayList<Short>>hashBucket;

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
        if (hashBucket==null){
            hashBucket = new HashMap<>();
        }
    }

    public void clearFingerprints(){
        this.aggFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.allRawDataList = Collections.synchronizedMap(new HashMap<>());
    }

    public Set<Vector> detectOutlier(long itr) throws Throwable {
        this.itr = itr;
        Date currentRealTime = dataGenerator.getFirstTimeStamp(Constants.datasetPathWithTime);
        currentRealTime.setTime(currentRealTime.getTime() + (long) Constants.S * 10 * 1000 * itr);
        this.rawData = dataGenerator.getTimeBasedIncomingData(currentRealTime, Constants.S*10,deviceId);
        generateAggFingerprints(this.rawData);
        sendAggFingerprints();
        return outlier;
    }

    public Set<Vector> detectOutlier(ArrayList<Vector> data) throws Throwable {
        generateAggFingerprints(data);
        sendAggFingerprints();
        return outlier;
    }

    public void generateAggFingerprints(List<Vector> data) {
        clearFingerprints();

        if (Objects.equals(Constants.methodToGenerateFingerprint, "CELLID")) {
            for (Vector t : data) {
                ArrayList<Short> fullDimCellIdx = new ArrayList<Short>();
                for (int j = 0; j < Constants.dim; j++) {
                    short dimIdx = (short) ((t.values[j] - ((NewNETS) this.detector).minValues[j]) /
                            ((NewNETS) this.detector).dimLength[j]);
                    fullDimCellIdx.add(dimIdx);
                }
                Long bucketId = (long) fullDimCellIdx.hashCode();
                if (!hashBucket.containsKey(bucketId)) {
                    hashBucket.put(bucketId, fullDimCellIdx);
                }
                if (!aggFingerprints.containsKey(bucketId)) {
                    aggFingerprints.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                    allRawDataList.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                }
                aggFingerprints.get(bucketId).add(t);
                allRawDataList.get(bucketId).add(t);
            }
        } else if (Objects.equals(Constants.methodToGenerateFingerprint, "LSH")) {
            for (Vector datum : data) {
                for (int j = 0; j < this.numberOfHashTables; j++) {
                    long bucketId = index.getHashTable().get(j).getHashValue(datum);
                    if (!aggFingerprints.containsKey(bucketId)) {
                        aggFingerprints.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                        allRawDataList.put(bucketId, Collections.synchronizedList(new ArrayList<Vector>()));
                    }
                    aggFingerprints.get(bucketId).add(datum);
                    allRawDataList.get(bucketId).add(datum);
                }
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
        /*
        HashMap<Long,Integer> hashMap = new HashMap<>();
        for (Long x: aggFingerprints.keySet()){
            hashMap.put(x,aggFingerprints.get(x).size());
        }
        StringBuilder sb= new StringBuilder();
        for (Long x: hashMap.keySet()) {
            sb.append(x).append(" ").append(hashMap.get(x)).append(",");
        }
        sb.append("end");
        System.out.println(sb);
*/
        invoke("localhost",this.nearestNode.port,
                EdgeNode.class.getMethod("upload", ArrayList.class, Integer.class),parameters);
    }

    public void getData() throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Integer edgeDeviceCode :dependentDevice.keySet()){
            Thread t =new Thread(()->{
                Object[] parameters = new Object[]{dependentDevice.get(edgeDeviceCode)};
                try {
                    /*use for measurement*/
                    HashSet<Vector> dataSet = new HashSet<>();
                    for (Vector a: this.rawData){
                        for (Vector b: EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode).rawData){
                            if (new EuclideanDistance().distance(a,b)<=Constants.R){
                                dataSet.add(b);
                            }
                        }
                    }
                    /*end*/
                    HashMap<Long,List<Vector>> data = (HashMap<Long, List<Vector>>)
                            invoke("localhost",
                            EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode).port,
                            EdgeDevice.class.getMethod("sendData", ArrayList.class),parameters);

                    /*use for measurement*/
                    HashSet<Vector> dataSet1 = new HashSet<>();
                    for (Long x:data.keySet()) {
                        try {
                            dataSet1.addAll(data.get(x));
                            this.allRawDataList.get(x).addAll(data.get(x));
                        } catch (NullPointerException ignored) {
                        }
                    }
                    System.out.println("neighbor = " + dataSet.size() + " transferred = "+ dataSet1.size() +
                            " neighbor / transferred data  = " + dataSet.size()*1.0/dataSet1.size());
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

    public HashMap<Long,List<Vector>> sendData(ArrayList<Long> bucketIds){
        HashMap<Long,List<Vector>> data = new HashMap<>();
        for (Long x:bucketIds){
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
