package test;

import be.tarsos.lsh.Index;
import be.tarsos.lsh.LSH;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.families.HashFamily;
import be.tarsos.lsh.util.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class testLsh {

    public static void main(String[] args) {
        int dimensions = 256;
        ArrayList<Vector> dataset = TestUtils.generate(dimensions, 100,512);
        TestUtils.addNeighbours(dataset, 4, 10);

        int radiusEuclidean = (int) LSH.determineRadius(dataset, new EuclideanDistance(), 20);
        HashFamily hashFamily = new EuclidianHashFamily(radiusEuclidean,dimensions);
        int NumberOfHashTables = 4;
        int NumberOfHashes = 8;
        Index index = new Index(hashFamily,NumberOfHashes,NumberOfHashTables);

        HashSet[] aggFingerprints = new HashSet[NumberOfHashTables];
        for (int i = 0; i < NumberOfHashTables; i++) {
            aggFingerprints[i] = new HashSet<Integer>();
        }
        for (int i=0;i<dataset.size();i++){
            for (int j=0; j<NumberOfHashTables;j++){
                int bucketId = index.getHashTable().get(j).getHashValue(dataset.get(i));
                aggFingerprints[j].add(bucketId);
            }
        }

//        HashMap[] aggFingerprints = new HashMap[NumberOfHashTables];
//        for (int i = 0; i < NumberOfHashTables; i++) {
//            aggFingerprints[i] = new HashMap<Integer,Integer>();
//        }
//        for (int i =0;i<dataset.size();i++){
//            for (int j=0;j<NumberOfHashes;j++){
//                int bucketId = index.getHashTable().get(j).getHashValue(dataset.get(i));
//                if (!aggFingerprints[j].containsKey(bucketId)){
//                    aggFingerprints[j].put(bucketId,0);
//                }
//                aggFingerprints[j].put(bucketId,(int)aggFingerprints[j].get(bucketId)+1);
//            }
//        }
    }
}
