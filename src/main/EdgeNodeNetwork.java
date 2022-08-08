package main;

import java.util.ArrayList;

public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();

    public static EdgeNode createEdgeNode(){
        EdgeNode edgeNode =  new EdgeNode();
        edgeNodes.add(edgeNode);
        return edgeNode;
    }

}
