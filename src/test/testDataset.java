package test;

import be.tarsos.lsh.Vector;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class testDataset {
    static ArrayList<ArrayList<Vector>> buckets;

    static {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("src/Result/Bucketing.txt"));
            ArrayList<ArrayList<Vector>> buckets = (ArrayList<ArrayList<Vector>>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ArrayList<ArrayList<Vector>> buckets = bucketing("TAO");
//        buckets.sort((o1, o2) -> o2.size()-o1.size());
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("src/Result/Bucketing.txt"));
//        objectOutputStream.writeObject(buckets);
//        objectOutputStream.close();
//        System.out.println(buckets.stream().mapToInt(x->x.size()).sum());
//        DistanceMeasure measure = new EuclideanDistance();
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(0).get(1)));
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(1).get(0)));
//        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("src/Result/Bucketing.txt"));
//        ArrayList<ArrayList<Vector>> buckets = (ArrayList<ArrayList<Vector>>) objectInputStream.readObject();
        int a=1;
    }

}
