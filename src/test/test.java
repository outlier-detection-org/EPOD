package test;

import Detector.MCOD;

import java.io.*;
import java.util.*;

public class test {
    public static void main(String[] args) throws IOException {
        MCOD mcod = new MCOD();
        //Vector(values:[-0.01, 73.15, 25.644], arrivalTime:42764, slideID:21)
        LinkedList<Double> list1 = new LinkedList<>();
        list1.add(-0.01);
        list1.add(73.15);
        list1.add(25.644);
        //Vector(values:[-0.01, 70.7, 25.698], arrivalTime:43209, slideID:21)
        LinkedList<Double> list2 = new LinkedList<>();
        list2.add(-0.01);
        list2.add(70.7);
        list2.add(25.698);
        // 0.01 && v.values.get(1) == 71.27 && v.values.get(2) == 25.708
        LinkedList<Double> list = new LinkedList<>();
        list.add(0.01);
        list.add(71.27);
        list.add(25.708);
        System.out.println(mcod.distance(list1,list));
        System.out.println(mcod.distance(list2,list));
    }
}
