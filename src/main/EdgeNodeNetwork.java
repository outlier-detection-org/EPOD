package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();
    public static HashMap<Integer,EdgeDevice> edgeDeviceHashMap = new HashMap<>();
    public static int numberOfHashTables;
    public static int nn;
    public static int dn;
    public static ArrayList<Thread> threads=new ArrayList<>();

    public static void setNumberOfHashTables(int number) {
        numberOfHashTables = number;
    }


    public static EdgeNode createEdgeNode(){
        EdgeNode edgeNode =  new EdgeNode(numberOfHashTables);
        edgeNodes.add(edgeNode);
        return edgeNode;
    }

    public static void createNetwork(int nn , int dn, EdgeDeviceFactory edgeDeviceFactory){
        for (int i=0;i<nn;i++){
            EdgeNode node = createEdgeNode();
            ArrayList<Integer> devices = new ArrayList<>();
            for (int j=0;j<dn;j++){
                EdgeDevice device = edgeDeviceFactory.createEdgeDevice();
                edgeDeviceHashMap.put(device.hashCode(),device);
                device.setNearestNode(node);
                devices.add(device.hashCode());
            }
            node.setEdgeDevice(devices);
        }
    }

    public static void startNetwork() throws IOException {
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
