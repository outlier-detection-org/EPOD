package main;

import java.io.IOException;
import java.util.ArrayList;

public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();
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

    public static void createNetwork(int n , EdgeDeviceFactory edgeDeviceFactory){
        for (int i=0;i<n;i++){
            EdgeNode node = createEdgeNode();
            EdgeDevice device = edgeDeviceFactory.createEdgeDevice();
            node.setEdgeDevice(device);
            device.setNearestNode(node);
        }
    }

    public static void startNetwork() throws IOException {
        for (EdgeNode node: edgeNodes){
            node.active = true;
            node.edgeDevice.active = true;
            Thread t1 = new Thread(node);
            Thread t2 = new Thread(node.edgeDevice);
            threads.add(t1);
            threads.add(t2);
            t1.start();
            t2.start();
        }
    }

    public static void stopNetwork() throws IOException, InterruptedException {
        for (EdgeNode node: edgeNodes) {
            node.close();
            node.edgeDevice.close();
        }
    }

}
