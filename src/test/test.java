package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.util.TestUtils;
import java.util.ArrayList;

public class test {
    public static void main(String[] args) {
        Vector v = new Vector(-9.99,-9.99,-9.999);
        ArrayList<Vector> arrayList = new ArrayList<>();
        arrayList.add(v);
        TestUtils.addNeighbours(arrayList, 2000, 1);
        for (Vector x: arrayList){
            for (int i=0;i<x.values.length-1;i++){
                System.out.print(x.values[i]+",");
            }
            System.out.println(x.values[x.values.length-1]);
        }
    }
}
