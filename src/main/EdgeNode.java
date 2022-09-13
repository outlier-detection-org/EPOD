package main;

import RPC.RPCFrame;
import be.tarsos.lsh.Vector;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

@SuppressWarnings("unchecked")
public class EdgeNode extends RPCFrame implements Runnable {

    private List<Vector> localData = new ArrayList<>();
    public List<Vector> allData = Collections.synchronizedList(new ArrayList<>());
    private final int numberOfHashTables;
    private HashSet<Integer> localAggFingerprints;
    public EdgeDevice edgeDevice;

    public EdgeNode(int numberOfHashTables){
        this.numberOfHashTables = numberOfHashTables;
        this.port = new Random().nextInt(50000)+10000;
        this.localAggFingerprints = new HashSet<Integer>();
    }

    public List<Vector> upload(HashSet<Integer> aggFingerprints) throws InterruptedException {
//        System.out.println(Thread.currentThread().getName()+": "+this+" node upload");
        ArrayList<Thread> threads = new ArrayList<>();
        this.localAggFingerprints = aggFingerprints;
        this.allData.clear();
        for (EdgeNode node: EdgeNodeNetwork.edgeNodes) {
            if (node==this)
                continue;
            Thread t = new Thread(() -> {
//                System.out.println(Thread.currentThread().getName()+": "+this+" new thread for invoke compareAndSend to "+node);
                List<Vector> data;
                try {
                    Object[] parameters = new Object[]{aggFingerprints,true};
                    data = (List<Vector>) invoke("localhost", node.port, EdgeNode.class.getMethod("compareAndSend", HashSet.class,boolean.class), parameters);
                    System.out.println("result size is :"+data.size());
                    allData.addAll(data);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });

            threads.add(t);
            t.start();
        }
        for (Thread t: threads){
            t.join();
        }
        return this.allData;
    }

    public List<Vector> compareAndSend(HashSet<Integer> aggFingerprints,boolean allData) throws Throwable {
        if (allData) {
            HashSet<Integer> intersection = new HashSet<>();
//            System.out.println(Thread.currentThread().getName() + " " + this + ": compareAndSend :)");
            intersection.addAll(localAggFingerprints);
            intersection.retainAll(aggFingerprints);
            if (!intersection.isEmpty()) {
                return getData();
            }

            return new ArrayList<>();
        }else {
            return getData();
        }
    }

    public void setEdgeDevice(EdgeDevice edgeDevice) {
        this.edgeDevice = edgeDevice;
    }

    public List<Vector> getData() throws Throwable {
        if(localData.size()==0){
            localData = (List<Vector>) invoke("localhost",this.edgeDevice.port, EdgeDevice.class.getMethod("sendData"),null);
        }
        return localData;
    }
}
