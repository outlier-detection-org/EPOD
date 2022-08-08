package utils;

import be.tarsos.lsh.LSH;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;

import java.util.ArrayList;

public class DetermineW {
    public static void main(String[] args) {
        ArrayList<Vector> dataset = LSH.readDataset(Constants.taoFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.taoFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
        dataset = LSH.readDataset(Constants.emFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.emFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
        dataset = LSH.readDataset(Constants.hpcFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.hpcFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
        dataset = LSH.readDataset(Constants.gaussFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.gaussFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
        dataset = LSH.readDataset(Constants.forestCoverFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.forestCoverFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
        dataset = LSH.readDataset(Constants.stockFileName,Integer.MAX_VALUE,",");
        System.out.println(Constants.stockFileName);
        LSH.determineRadius(dataset,new EuclideanDistance(),10);
    }
}
