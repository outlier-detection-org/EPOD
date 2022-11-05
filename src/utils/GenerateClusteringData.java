package utils;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.util.TestUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GenerateClusteringData {
    public static void generateClusteringData(int number) throws IOException {
        int dimension = 5;
        ArrayList<Vector> data = TestUtils.generate(dimension, 1, 100);
        TestUtils.addNeighbours(data, number-1, 3000);

        EuclideanDistance euclideanDistance = new EuclideanDistance();
        BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter("C:\\Users\\14198\\Desktop\\outlier_detection\\Datasets\\RandomCluster.txt"));
        ArrayList<ArrayList<Vector>> clusters = new ArrayList<>();
        for (int i=0;i<data.size();i++) {
            Vector v = data.get(i);
            ArrayList<Vector> cluster = new ArrayList<>();
            cluster.add(v);
            TestUtils.addNeighbours(cluster, 12000-1, 3);
            double avg = 0;
            for (Vector u : cluster) {
                avg += euclideanDistance.distance(cluster.get(0), u);
            }
            System.out.println("The avg distance of neighbor is " + avg / cluster.size());
            clusters.add(cluster);
        }
        for (int i=0;i<20+5-1;i++){
            for (int j=0;j<clusters.size();j++){
                for (int k=0;k<500/clusters.size();k++){
                    bufferedWriter.write(j+",");
                    Vector v = clusters.get(j).get(i*500/clusters.size()+k);
                    for (int x=0;x<v.values.length-1;x++){
                        bufferedWriter.write(v.values[x]+",");
                    }
                    bufferedWriter.write(v.values[v.values.length-1]+"\n");
                }
            }
            bufferedWriter.flush();
        }
        bufferedWriter.close();
    }
    public static void main(String[] args) throws IOException {
        generateClusteringData(4);
    }
}
