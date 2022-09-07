package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import utils.DataGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class testDataset {
    public static ArrayList<ArrayList<Vector>> bucketing(String type) throws IOException {
        DataGenerator.getInstance(type, false);
        ArrayList<Vector> dataset = new ArrayList<>(DataGenerator.dataQueue);
        double r = 1.9;
        int[] flag = new int[10000];
        ArrayList<ArrayList<Vector>> buckets = new ArrayList<>();
        DistanceMeasure measure = new EuclideanDistance();
        for (int i = 0; i < 10000; i++) {
            if (flag[i] == 1) continue;
            else flag[i] = 1;
            Vector v = dataset.get(i);
            ArrayList<Vector> bucket = new ArrayList<>();
            DistanceComparator dc = new DistanceComparator(v, measure);
            PriorityQueue<Vector> pq = new PriorityQueue<Vector>(10000, dc);
            pq.addAll(dataset.subList(0, 10000));
            Vector neighbor = pq.peek();
            while (measure.distance(v, neighbor) <= r) {
                flag[dataset.indexOf(neighbor)] = 1;
                bucket.add(pq.poll());
                neighbor = pq.peek();
            }
            buckets.add(bucket);
        }
        return buckets;
    }

}
