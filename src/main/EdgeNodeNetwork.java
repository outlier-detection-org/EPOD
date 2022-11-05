package main;

import be.tarsos.lsh.Vector;
import dataStructure.Tuple;
import utils.Constants;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@SuppressWarnings("unchecked")
public class EdgeNodeNetwork {
    public static ArrayList<EdgeNode> edgeNodes = new ArrayList<>();
    public static HashMap<Integer,EdgeDevice> edgeDeviceHashMap = new HashMap<>();
    public static int numberOfHashTables;
    public static ArrayList<Thread> threads=new ArrayList<>();
    public static BufferedWriter[] outlierFw = new BufferedWriter[Constants.dn*Constants.nn];

    static {
//        try {
//            for (int i = 0;i<Constants.dn*Constants.nn;i++){
//                outlierFw[i] = new BufferedWriter(new FileWriter(new File(
//                        "src\\Result\\Result_"+
//                                Constants.dataset+
//                                "_NETS"+"_"
//                                +Constants.withTime+"_"
//                                +Constants.nn+"_"
//                                +Constants.dn+"_"
//                                +i+"_"
//                                + "outliers.txt")));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

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
        long time = 0;
        while (itr < Constants.nS+Constants.nW-1) {
            System.out.println("===============================");
            System.out.println("This is the "+itr + " slides.");
            if(itr>=Constants.nS-1) {
                System.out.println("Window " + (itr - Constants.nS + 1));
            }
            ArrayList<Thread> arrayList = new ArrayList<>();
            long start = System.currentTimeMillis();
            for (EdgeDevice device : edgeDeviceHashMap.values()) {
                int finalItr = itr;
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Set<Vector> outlier = device.detectOutlier(finalItr);
//                            if(finalItr>=Constants.nS-1) {
//                                outlierFw[device.deviceId].write("Window " +(finalItr-Constants.nS+1)+"\n");
//                                for (Vector v : outlier) {
//                                    StringBuilder sb =new StringBuilder();
////                                    sb.append(String.format("%d ",((Tuple)v).id));
//                                    for (Double d: ((Tuple)v).value) {
//                                        sb.append(String.format("%.2f ",d));
//                                    }
//                                    outlierFw[device.deviceId].write(sb.toString() +"\n");
//                                }
//                                outlierFw[device.deviceId].flush();
//                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                arrayList.add(t);
            }
            for (Thread t : arrayList) {
                t.join();
            }
            time += System.currentTimeMillis()-start;
            System.out.println("Time cost for this window is : "+(System.currentTimeMillis()-start));
            itr++;

//            System.out.println("total" + testNetwork.total);
//            for (EdgeDevice e : testNetwork.index.keySet()) {
//                AtomicInteger total1 = new AtomicInteger();
//                int index = testNetwork.index.get(e);
//                HashMap<Integer, Integer> hashMap = testNetwork.buckets.get(index);
//                hashMap.keySet().forEach(x -> {
//                    total1.addAndGet(testNetwork.all.get(x));
//                });
//                System.out.println(e + " " + total1);
//            }
//            testNetwork.total =0;
//            testNetwork.index = new HashMap<>();
//            testNetwork.buckets= new ArrayList<>();
//            testNetwork.all = new HashMap<>();
        }
//        for (BufferedWriter bufferedWriter: outlierFw){
//            bufferedWriter.close();
//        }
        stopNetwork();
        System.out.println("Average time cost is: "+time*1.0/(Constants.nS+Constants.nW-1));
    }

    public static void startNetworkForTest(boolean allTransfer) throws Throwable {
        System.out.println("allTransfer: "+allTransfer);
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
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("src/Result/TaoBucketing.txt"));
        ArrayList<ArrayList<Vector>> buckets = (ArrayList<ArrayList<Vector>>) objectInputStream.readObject();
        ArrayList<ArrayList<Vector>> data;
        if (allTransfer){
            data = new ArrayList<>();
            int lengthOfData = (int) Math.ceil(buckets.get(0).size()*1.0/edgeDeviceHashMap.values().size());
            for (int i=0;i<edgeDeviceHashMap.values().size();i++){
                int left = Math.min(i*lengthOfData,buckets.get(0).size());
                int right = Math.min((i+1)*lengthOfData,buckets.get(0).size());
                System.out.println(left);
                System.out.println(right);
                data.add(new ArrayList<>(buckets.get(0).subList(left,right)));
            }
        }else data = buckets;

        int itr=0;
        while (itr < 1) {
            System.out.println("This is the "+itr + " turn.");
            ArrayList<Thread> arrayList = new ArrayList<>();
            for (EdgeDevice device : edgeDeviceHashMap.values()) {
                int finalItr = itr;
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<Vector> tmp = data.remove(0);
                            System.out.println(tmp.size());
                            Set<Vector> outlier = device.detectOutlier(tmp);
                            if(finalItr>=Constants.nS-1) {
                                outlierFw[device.deviceId].write("Window " +(finalItr-Constants.nS+1)+"\n");
                                for (Vector v : outlier) {
                                    StringBuilder sb =new StringBuilder();
                                    for (Double d: ((Tuple)v).value) {
                                        sb.append(String.format("%.2f ",d));
                                    }
                                    outlierFw[device.deviceId].write(sb.toString() +"\n");
                                }
                                outlierFw[device.deviceId].flush();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                arrayList.add(t);
            }
            for (Thread t : arrayList) {
                t.join();
            }
            itr++;
        }
        for (BufferedWriter bufferedWriter: outlierFw){
            bufferedWriter.close();
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
        System.out.println("Ended!");
    }

}
