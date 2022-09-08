package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import utils.DataGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;

@SuppressWarnings("unchecked")
public class testDataset {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ArrayList<ArrayList<Vector>> buckets = bucketing("TAO");
//        buckets.sort((o1, o2) -> o2.size()-o1.size());
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("src/Result/Bucketing.txt"));
//        objectOutputStream.writeObject(buckets);
//        objectOutputStream.close();

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("src/Result/Bucketing.txt"));
        ArrayList<ArrayList<Vector>> buckets = (ArrayList<ArrayList<Vector>>) objectInputStream.readObject();
//        System.out.println(buckets.stream().mapToInt(x->x.size()).sum());
//        DistanceMeasure measure = new EuclideanDistance();
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(0).get(1)));
//        System.out.println(measure.distance(buckets.get(0).get(0),buckets.get(1).get(0)));
    }
    public static ArrayList<ArrayList<Vector>> bucketing(String type) throws IOException {
        DataGenerator.getInstance(type, false);
        ArrayList<Vector> dataset = new ArrayList<>(DataGenerator.dataQueue);
        double r = 1.9;
        int[] flag = new int[dataset.size()];
        ArrayList<ArrayList<Vector>> buckets = new ArrayList<>();
        DistanceMeasure measure = new EuclideanDistance();
        for (int i = 0; i < dataset.size(); i++) {
            if (flag[i] == 1) continue;
            else flag[i] = 1;
            Vector v = dataset.get(i);
            ArrayList<Vector> bucket = new ArrayList<>();
            bucket.add(v);
            DistanceComparator dc = new DistanceComparator(v, measure);
            PriorityQueue<Vector> pq = new PriorityQueue<Vector>(dc);
            pq.addAll(dataset);
            Vector neighbor = pq.peek();
            while (measure.distance(v, neighbor) <= r) {
                if (flag[dataset.indexOf(neighbor)]!=1){
                    flag[dataset.indexOf(neighbor)] = 1;
                    bucket.add(pq.poll());
                }else pq.poll();
                neighbor = pq.peek();
            }
            buckets.add(bucket);
        }
        return buckets;
    }

}
