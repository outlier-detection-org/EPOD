package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;
import main.EdgeDevice;
import main.EdgeDeviceFactory;
import utils.Constants;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class testDataset {
    static ArrayList<ArrayList<Vector>> buckets;

    public static void main(String[] args) throws Throwable {
//        ArrayList<ArrayList<Vector>> buckets = DataGenerator.bucketing("TAO");
//        buckets.sort((o1, o2) -> o2.size()-o1.size());
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("src/Result/TaoBucketing.txt"));
//        objectOutputStream.writeObject(buckets);
//        objectOutputStream.close();
//        System.out.println(buckets.stream().mapToInt(ArrayList::size).sum());
//        DistanceMeasure measure = new EuclideanDistance();
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(0).get(1)));
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(1).get(0)));
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("src/Result/TaoBucketing.txt"));
        ArrayList<ArrayList<Vector>> buckets = (ArrayList<ArrayList<Vector>>) objectInputStream.readObject();
        int numberOfHashes = 5;
        int numberOfHashTables = 10;
        System.out.println("k is "+numberOfHashes+" L is "+numberOfHashTables);
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(numberOfHashes, numberOfHashTables);
        EdgeDevice edgeDevice =edgeDeviceFactory.createEdgeDevice(0);

        ArrayList<Vector> e = buckets.get(1);
//            System.out.println("This is the "+index+++" turn.");
        System.out.println(e.size());
        HashMap<Long,Integer> hashMap =new HashMap<>();
        for (Vector v: e){
            edgeDevice.index.index(v);
        }
        List<Vector> r = edgeDevice.index.query(buckets.get(1).get(2), 50);
        System.out.println(r.size());
        int index=0;
        for (Vector v:r){
            if (new EuclideanDistance().distance(v,buckets.get(1).get(2))<Constants.R){
                index++;
            }
        }
        System.out.println(index);
//                StringBuilder sb = new StringBuilder();
//                for (int i=0;i<numberOfHashTables;i++) {
//                    Long hash = edgeDevice.index.getHashTable().get(i).getHashValue(v);
//                    if (!hashMap.containsKey(hash)){
//                        hashMap.put(hash,0);
//                    }
//                    hashMap.put(hash,hashMap.get(hash)+1);
//                }
//            }
//            for (Long l:hashMap.keySet()){
//                System.out.println(l+" "+hashMap.get(l));
//            }
    }

}
