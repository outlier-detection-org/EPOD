package main;

import java.io.IOException;
import java.util.ArrayList;

public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();
    public static int numberOfHashTables;

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
            node.start();
            node.edgeDevice.start();
        }
    }

}
