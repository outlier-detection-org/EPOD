package Framework;

import RPC.DeviceService;
import RPC.EdgeNodeService;
import RPC.Vector;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import utils.CompareResult;
import utils.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EdgeNodeNetwork {
    public static HashMap<Integer, Device> deviceHashMap = new HashMap<>();
    public static HashMap<Integer, EdgeNode> nodeHashMap = new HashMap<>();
    public static Set<? extends Vector> outliers; //only used to print out outlier
    public static BufferedWriter outlierFw;

    static {
        try {
            outlierFw = new BufferedWriter(
                    new FileWriter("src\\Result\\" +new Random().nextInt(100)+"_Result_"+Constants.methodToGenerateFingerprint+"_"+ Constants.dataset + "_outliers.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static EdgeNode createEdgeNode() {
        EdgeNode edgeNode = new EdgeNode();
        nodeHashMap.put(edgeNode.hashCode(), edgeNode);
        return edgeNode;
    }

    public static void createNetwork(int nn, int dn, DeviceFactory edgeDeviceFactory) throws Throwable {
        for (int i = 0; i < nn; i++) {
            EdgeNode node = createEdgeNode();
            ArrayList<Integer> devicesCodes = new ArrayList<>();
            for (int j = 0; j < dn; j++) {
                Device device = edgeDeviceFactory.createEdgeDevice(i * dn + j);
                deviceHashMap.put(device.hashCode(), device);
                device.setNearestNode(node.hashCode());
                devicesCodes.add(device.hashCode());
            }
            node.setDevices(devicesCodes);
        }
        for (Device device : deviceHashMap.values()) {
            device.handler.setHistoryRecord();
        }
    }

    public static void startNetwork() throws Throwable {
        //Print Logs
        System.out.println("# Dataset: " + Constants.dataset);
        System.out.println("Method: " + Constants.methodToGenerateFingerprint);
        System.out.println("Dim: " + Constants.dim);
        System.out.println("dn/nn: " + Constants.dn + "/" + Constants.nn);
        System.out.println("R/K/W/S: " + Constants.R + "/" + Constants.K + "/" + Constants.W + "/" + Constants.S);
        System.out.println("# of windows: " + (Constants.nW));
        for (EdgeNode node : nodeHashMap.values()) {
            node.begin();
        }
        for (Device device : deviceHashMap.values()) {
            device.begin();
        }

        //set clients for nodes and devices
        for (EdgeNode node : nodeHashMap.values()) {
            setClientsForEdgeNodes(node);
        }
        for (Device device : deviceHashMap.values()) {
            setClientsForDevices(device);
        }

        int itr = 0;
        long time = 0;
        while (itr < Constants.nS + Constants.nW - 1) {
            //per slide
            System.out.println("===============================");
            System.out.println("This is the " + itr + " slides.");
            if (itr >= Constants.nS - 1) {
                System.out.println("Window " + (itr - Constants.nS + 1));
            }
            ArrayList<Thread> arrayList = new ArrayList<>();
            long start = System.currentTimeMillis();
            for (Device device : deviceHashMap.values()) {
                int finalItr = itr;
                Thread t = new Thread(() -> {
                    try {
                        if (Constants.methodToGenerateFingerprint.contains("CENTRALIZE")){
                            outliers = device.handler.detectOutlier_Centralize(finalItr);
                        }else if (Constants.methodToGenerateFingerprint.contains("P2P")){
                            outliers = device.handler.detectOutlier_P2P(finalItr);
                        }else {
                            outliers = device.handler.detectOutlier(finalItr);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                t.start();
                arrayList.add(t);
            }
            for (Thread t : arrayList) {
                t.join();
            }
            time += System.currentTimeMillis() - start;
            System.out.println("Time cost for this slide is : " + (System.currentTimeMillis() - start));
            itr++;
            printOutliers();

            //========================== NAIVE ================================
            List<Vector> allData = new ArrayList<>();
            for (Device device : deviceHashMap.values()) {
                allData.addAll(device.handler.rawData);
            }
            CompareResult.detectOutliersNaive(allData, itr);

        }
        stopNetwork();
        System.out.println("Average time cost is: " + time * 1.0 / (Constants.nS + Constants.nW - 1));
        outlierFw.close();
    }

    public static void printOutliers() throws IOException {
        for (Vector outlier : outliers) {
            outlierFw.write(outlier.toString() + "\n");
        }
        outlierFw.flush();
    }
    public static void stopNetwork() {
        for (EdgeNode node : nodeHashMap.values()) {
            node.stop();
        }
        for (Device device : deviceHashMap.values()) {
            device.stop();
        }
        System.out.println("Ended!");
    }

    public static void setClientsForEdgeNodes(EdgeNode node) throws TTransportException {
        Map<Integer, EdgeNodeService.Client> clientsForEdgeNodes = new HashMap<>();
        Map<Integer, DeviceService.Client> clientsForDevices = new HashMap<>();

        for (Integer nodeHashCode : nodeHashMap.keySet()) {
            if (nodeHashCode == node.hashCode()) continue;
            TTransport transport = new TSocket("127.0.0.1", nodeHashMap.get(nodeHashCode).port);
            transport.open();
            node.transports.add(transport);
            TProtocol protocol = new TBinaryProtocol(transport);
            EdgeNodeService.Client client = new EdgeNodeService.Client(protocol);
            clientsForEdgeNodes.put(nodeHashCode, client);
        }

        for (Integer deviceCode : node.devicesCodes) {
            TTransport transport = new TSocket("127.0.0.1", deviceHashMap.get(deviceCode).port);
            transport.open();
            node.transports.add(transport);
            TProtocol protocol = new TBinaryProtocol(transport);
            DeviceService.Client client = new DeviceService.Client(protocol);
            clientsForDevices.put(deviceCode, client);
        }
        node.handler.setClients(clientsForEdgeNodes, clientsForDevices);
    }


    public static void setClientsForDevices(Device device) throws TTransportException {
        TTransport transport = new TSocket("127.0.0.1", nodeHashMap.get(device.nearestNodeCode).port);
        transport.open();
        device.transports.add(transport);
        TProtocol protocol = new TBinaryProtocol(transport);
        EdgeNodeService.Client clientForNearestNode = new EdgeNodeService.Client(protocol);

        Map<Integer, DeviceService.Client> clientsForDevices = new HashMap<>();
        for (Integer deviceHashCode : deviceHashMap.keySet()) {
            if (deviceHashCode == device.hashCode()) continue;
            TTransport transport1 = new TSocket("127.0.0.1", deviceHashMap.get(deviceHashCode).port);
            transport1.open();
            device.transports.add(transport1);
            TProtocol protocol1 = new TBinaryProtocol(transport1);
            DeviceService.Client client = new DeviceService.Client(protocol1);
            clientsForDevices.put(deviceHashCode, client);
        }
        device.handler.setClients(clientForNearestNode, clientsForDevices);
    }
}
