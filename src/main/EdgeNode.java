package main;
import RPC.RPCFrame;
import utils.Constants;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class EdgeNode extends RPCFrame implements Runnable {

    public Map<Long,List<Integer>> localAggFingerprints;
    public Map<Integer,List<Long>> reverseFingerprints;
    public Map<Long,List<Integer>> result;
    public ArrayList<Integer> edgeDevice;
    public AtomicInteger count;
    volatile Boolean flag = false;

    public EdgeNode(int numberOfHashTables){
        this.port = new Random().nextInt(50000)+10000;
        this.localAggFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.reverseFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.result = Collections.synchronizedMap(new HashMap<>());
        this.count= new AtomicInteger(0);
    }

    public void upload(ArrayList<Long> aggFingerprints,Integer edgeDeviceHashCode) throws Throwable {
        this.flag = false;
        ArrayList<Thread> threads = new ArrayList<>();
        this.reverseFingerprints.put(edgeDeviceHashCode,aggFingerprints);
        for (Long id:aggFingerprints){
            if (!this.localAggFingerprints.containsKey(id)){
              this.localAggFingerprints.put(id,Collections.synchronizedList(new ArrayList<Integer>()));
            }
            this.localAggFingerprints.get(id).add(edgeDeviceHashCode);
            if (!this.result.containsKey(id)){
                this.result.put(id,Collections.synchronizedList(new ArrayList<Integer>()));
            }
            this.result.get(id).add(edgeDeviceHashCode);
        }
        count.incrementAndGet();
        boolean flag = count.compareAndSet(edgeDevice.size(),0);
         if (flag){
            //说明device已经完成上传数据，进入node互相交互阶段
             this.flag = true;
            for (EdgeNode node: EdgeNodeNetwork.edgeNodes) {
                if (node==this)
                    continue;
                while (!node.flag){};
                Thread t = new Thread(() -> {
//                System.out.println(Thread.currentThread().getName()+": "+this+" new thread for invoke compareAndSend to "+node);
                    try {
                        Object[] parameters = new Object[]{this.localAggFingerprints};
                        Map<Long, ArrayList<Integer>> tmp = (Map<Long, ArrayList<Integer>>)
                                invoke("localhost", node.port, EdgeNode.class.getMethod
                                        ("compareAndSend", Map.class), parameters);
                        //TODO: java.lang.NullPointerException
                        if (tmp!=null) {
                            for (Long x : tmp.keySet()) {
                                if (!result.containsKey(x)) {
                                    result.put(x, Collections.synchronizedList(new ArrayList<Integer>()));
                                }
                                result.get(x).addAll(tmp.get(x));
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
                threads.add(t);
                t.start();
            }
            for (Thread t: threads){
                t.join();
            }
            //已经向网络中所有的node请求过数据，开始把数据发还给device
            sendBackResult();
            this.localAggFingerprints = Collections.synchronizedMap(new HashMap<>());
            this.reverseFingerprints = Collections.synchronizedMap(new HashMap<>());
            this.result = Collections.synchronizedMap(new HashMap<>());
        }
    }

    public HashMap<Long,List<Integer>> compareAndSend(Map<Long,ArrayList<Integer>> aggFingerprints) throws Throwable {
         //等待这个node的所有device数据上传完成

        HashMap<Long, List<Integer>> result = new HashMap<>();
        //cellID 比较的过程

        if (Objects.equals(Constants.methodToGenerateFingerprint, "CELLID")){
            for (Long id:aggFingerprints.keySet()){
                ArrayList<Short> dimId = EdgeDevice.hashBucket.get(id);
                for (Long id0:localAggFingerprints.keySet()){
                    ArrayList<Short> dimId0 = EdgeDevice.hashBucket.get(id0);
                    if (neighboringSet(dimId,dimId0))
                        result.put(id0, localAggFingerprints.get(id0));
                }
            }
        }
        else if (Objects.equals(Constants.methodToGenerateFingerprint, "LSH")){
            HashSet<Long> intersection = new HashSet<>(localAggFingerprints.keySet());
            intersection.retainAll(aggFingerprints.keySet());
            if (!intersection.isEmpty()) {
                for (Long x : intersection) {
                    result.put(x, localAggFingerprints.get(x));
                }
            }
        }
        return result;
    }

    public boolean neighboringSet(ArrayList<Short> c1, ArrayList<Short> c2) {
        double ss = 0;
        double cellIdxDist = Math.sqrt(Constants.dim)*2;
        double threshold =cellIdxDist*cellIdxDist;
        for(int k = 0; k<c1.size(); k++) {
            ss += Math.pow((c1.get(k) - c2.get(k)),2);
            if (ss >= threshold) return false;
        }
        return true;
    }

    public void sendBackResult() throws Throwable {
//        System.out.println(Thread.currentThread().getName() + " " + this + "  sendBackResult");
        ArrayList<Thread> threads = new ArrayList<>();
        for (Integer edgeDeviceCode : this.edgeDevice) {
            Thread t = new Thread(() -> {
                HashMap<Integer, ArrayList<Long>> dependent = new HashMap<>();
                for (Long x : this.reverseFingerprints.get(edgeDeviceCode)) {
                    //CellId
                    if (Objects.equals(Constants.methodToGenerateFingerprint, "CELLID")) {
                        for (Long id : result.keySet()) {
                            if (neighboringSet(EdgeDevice.hashBucket.get(x), EdgeDevice.hashBucket.get(id))) {
                                for (Integer y : result.get(id)) {
                                    if (y == edgeDeviceCode.hashCode()) continue;
                                    if (!dependent.containsKey(y)) {
                                        dependent.put(y, new ArrayList<Long>());
                                    }
                                    dependent.get(y).add(id);
                                }
                            }
                        }
                    }
                    else if (Objects.equals(Constants.methodToGenerateFingerprint, "LSH")) {
                        //LSH
                        if (!result.containsKey(x)) continue;
                        for (Integer y : result.get(x)) {
                            if (y == edgeDeviceCode.hashCode()) continue;
                            if (!dependent.containsKey(y)) {
                                dependent.put(y, new ArrayList<Long>());
                            }
                            dependent.get(y).add(x);
                        }
                    }
                }
                Object[] parameters = new Object[]{dependent};
                try {
                    invoke("localhost", EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode).port, EdgeDevice.class.getMethod
                            ("setDependentDevice", HashMap.class), parameters);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    public void setEdgeDevice(ArrayList<Integer> edgeDevice) {
        this.edgeDevice = edgeDevice;
    }
}
