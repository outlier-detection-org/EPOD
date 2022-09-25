package main;

import utils.Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();
    public static HashMap<Integer,EdgeDevice> edgeDeviceHashMap = new HashMap<>();
    public static int numberOfHashTables;
    public static ArrayList<Thread> threads=new ArrayList<>();

    public static void setNumberOfHashTables(int number) {
        numberOfHashTables = number;
    }


    public static EdgeNode createEdgeNode(){
        EdgeNode edgeNode =  new EdgeNode(numberOfHashTables);
        edgeNodes.add(edgeNode);
        return edgeNode;
    }

    public static void createNetwork(int nn , int dn, EdgeDeviceFactory edgeDeviceFactory) throws Throwable {
        for (int i=0;i<nn;i++){
            EdgeNode node = createEdgeNode();
            ArrayList<Integer> devices = new ArrayList<>();
            for (int j=0;j<dn;j++){
                EdgeDevice device = edgeDeviceFactory.createEdgeDevice(i*dn+j);
                edgeDeviceHashMap.put(device.hashCode(),device);
                device.setNearestNode(node);
                devices.add(device.hashCode());
            }
            node.setEdgeDevice(devices);
        }
    }

    public static void startNetwork() throws Throwable {
        for (EdgeNode node : edgeNodes) {
            node.active = true;
            Thread t1 = new Thread(node);
            threads.add(t1);
            t1.start();
        }
        for (EdgeDevice device : edgeDeviceHashMap.values()) {
            device.active = true;
            Thread t2 = new Thread(device);
            threads.add(t2);
            t2.start();
        }

        int itr=0;
        while (itr < Constants.nS+Constants.nW-1){
            ArrayList<Thread> arrayList = new ArrayList<>();
            for (EdgeDevice device : edgeDeviceHashMap.values()) {
                int finalItr = itr;
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            device.detectOutlier(finalItr);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                arrayList.add(t);
            }
            for (Thread t:arrayList){
                t.join();
            }
            itr ++;
        }
        stopNetwork();
    }

    public static void stopNetwork() throws IOException, InterruptedException {
        for (EdgeNode node : edgeNodes) {
            node.close();
        }
        for (EdgeDevice device : edgeDeviceHashMap.values()) {
            device.close();
        }
    }

}
