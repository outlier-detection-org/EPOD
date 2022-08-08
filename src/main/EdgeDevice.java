package main;

import be.tarsos.lsh.Index;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.HashFamily;
import utils.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class EdgeDevice {
    public List<Data> rawData;
    public SynchronousQueue<Data> allDataList;

    private int numberOfHashes;
    public Index index;
    public int[] fingerprints;
    public HashSet[] aggFingerprints;

    public EdgeNode nearestNode;

    public EdgeDevice(HashFamily hashFamily, int NumberOfHashes, int NumberOfHashTables){
        this.numberOfHashes = NumberOfHashes;
        this.index = new Index(hashFamily,NumberOfHashes,NumberOfHashTables);
        this.fingerprints = new int[NumberOfHashTables];
        this.aggFingerprints = new HashSet[NumberOfHashTables];
        for (int i = 0; i < NumberOfHashTables; i++) {
            aggFingerprints[i] = new HashSet<Integer>();
        }
    }
    public List<Data> detectOutlier(List<Data> data){
        rawData = data;
        //TODO:generate signature for data and upload to edgeNode
        generateAggFingerprints(rawData);
        //TODO:send to closest edge device
        //TODO:waiting to receive necessary data from others
        sendAggFingerprints();
        //TODO:aggregate data and run outlier detection algorithm
        return rawData;
    }

    public void generateAggFingerprints(List<Data> data){
        for (Data datum : data) {
            for (int j = 0; j < this.numberOfHashes; j++) {
                // TODO:convert data to vector
                Vector v = new Vector(datum.values, datum.arrivalTime);
                int bucketId = index.getHashTable().get(j).getHashValue(v);
                aggFingerprints[j].add(bucketId);
            }
        }
    }

    public void sendAggFingerprints(){


    }

    public void publish(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        while (true) {
            try {
                final Socket socket = server.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                                try {
                                    String methodName = input.readUTF();
                                    Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                                    Object[] arguments = (Object[]) input.readObject();
                                    try {
                                        Method method = this.getClass().getMethod(methodName, parameterTypes);
                                        Object result = method.invoke(this, arguments);
                                        output.writeObject(result);
                                    } catch (Throwable t) {
                                        output.writeObject(t);
                                    } finally {
                                        output.close();
                                    }
                                } finally {
                                    input.close();
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setNearestNode(EdgeNode nearestNode) {
        this.nearestNode = nearestNode;
    }
}
