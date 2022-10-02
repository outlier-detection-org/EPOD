package main;

import RPC.RPCFrame;
import be.tarsos.lsh.Vector;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class EdgeNode extends RPCFrame implements Runnable {

    public Map<Integer,List<Integer>> localAggFingerprints;
    public Map<Integer,List<Integer>> reverseFingerprints;
    public Map<Integer,List<Integer>> result;
    public ArrayList<Integer> edgeDevice;
    public AtomicInteger count;


    public EdgeNode(int numberOfHashTables){
        this.port = new Random().nextInt(50000)+10000;
        this.localAggFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.reverseFingerprints = Collections.synchronizedMap(new HashMap<>());
        this.result = Collections.synchronizedMap(new HashMap<>());
        this.count= new AtomicInteger(0);
    }

    public void upload(ArrayList<Integer> aggFingerprints,Integer edgeDeviceHashCode) throws Throwable {
        count.compareAndSet(edgeDevice.size(),0);
        ArrayList<Thread> threads = new ArrayList<>();

        this.reverseFingerprints.put(edgeDeviceHashCode,aggFingerprints);
        for (Integer id:aggFingerprints){
            if (!this.localAggFingerprints.containsKey(id)){
              this.localAggFingerprints.put(id,Collections.synchronizedList(new ArrayList<Integer>()));
              this.result.put(id,Collections.synchronizedList(new ArrayList<Integer>()));
            }
            Thread.sleep(1);
            this.localAggFingerprints.get(id).add(edgeDeviceHashCode);
            this.result.get(id).add(edgeDeviceHashCode);
        }
        count.incrementAndGet();
        if (count.get()==edgeDevice.size()){
            //说明device已经完成上传数据，进入node互相交互阶段
            for (EdgeNode node: EdgeNodeNetwork.edgeNodes) {
                if (node==this)
                    continue;
                Thread t = new Thread(() -> {
//                System.out.println(Thread.currentThread().getName()+": "+this+" new thread for invoke compareAndSend to "+node);
                    try {
                        Object[] parameters = new Object[]{this.localAggFingerprints};
                        Map<Integer, ArrayList<Integer>> tmp = (Map<Integer, ArrayList<Integer>>)
                                invoke("localhost", node.port, EdgeNode.class.getMethod
                                        ("compareAndSend", Map.class), parameters);
                        for (Integer x : tmp.keySet()) {
                            if (!result.containsKey(x)) {
                                result.put(x, Collections.synchronizedList(new ArrayList<Integer>()));
                            }
                            result.get(x).addAll(tmp.get(x));
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
        }
    }

    public HashMap<Integer,List<Integer>> compareAndSend(Map<Integer,ArrayList<Integer>> aggFingerprints) throws Throwable {
        while (this.count.get() != this.edgeDevice.size()); //等待这个node的所有device数据上传完成
        HashMap<Integer, List<Integer>> result = new HashMap<>();
        HashSet<Integer> intersection = new HashSet<>(localAggFingerprints.keySet());
        intersection.retainAll(aggFingerprints.keySet());
        if (!intersection.isEmpty()) {
            for (Integer x : intersection) {
                result.put(x, localAggFingerprints.get(x));
            }
        }
        return result;
    }

    public void sendBackResult() throws Throwable {
//        System.out.println(Thread.currentThread().getName() + " " + this + "  sendBackResult");
        ArrayList<Thread> threads = new ArrayList<>();
        for (Integer edgeDeviceCode : this.edgeDevice) {
//            EdgeDevice edgeDevice  = EdgeNodeNetwork.edgeDeviceHashMap.get(edgeDeviceCode);
            Thread t = new Thread(() -> {
                HashMap<Integer, ArrayList<Integer>> dependent = new HashMap<>();
                for (Integer x : this.reverseFingerprints.get(edgeDeviceCode)) {
                    if (!result.containsKey(x)) continue;
                    for (Integer y : result.get(x)) {
                        if (y==edgeDeviceCode.hashCode())continue;
//                        if (Objects.equals(edgeDeviceCode, y))continue;
                        if (!dependent.containsKey(y)) {
                            dependent.put(y, new ArrayList<Integer>());
                        }
                        dependent.get(y).add(x);
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
