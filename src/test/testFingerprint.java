package test;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.util.TestUtils;
import main.EdgeDevice;
import main.EdgeDeviceFactory;
import utils.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

@SuppressWarnings("unchecked")
public class testFingerprint extends JFrame {
     int n = 50;
     int dimensions = 20;
     int numberOfTrue = 0;
     int numberOfFalse = 0;
     private JTable table;
     String[][] kl = new String[10][10];
     String[][] tf = new String[10][10];
    
    public static void main(String[] args) {
        testFingerprint testFingerprint = new testFingerprint();
        testFingerprint.test();
        testFingerprint.createTable();
    }

    public void createTable(){

        this.table = new JTable();
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setColumnIdentifiers(new String[] {"(p1,p2)","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9"});
        for(int i=1 ; i<10 ; i++){
            model.addRow(new String[]{String.valueOf((i)/10.0),
//                    kl[i][1],kl[i][2],kl[i][3],kl[i][4],kl[i][5],kl[i][6],kl[i][7],kl[i][8],kl[i][9]
                    tf[i][1],tf[i][2],tf[i][3],tf[i][4],tf[i][5],tf[i][6],tf[i][7],tf[i][8],tf[i][9]
            });
        }
        this.table.setModel(model);
        JScrollPane cen_pan = new JScrollPane();
        cen_pan.setViewportView(this.table);
        this.add(cen_pan);

        this.setSize(5000 , 4000);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    public void test(){
        ArrayList<Vector> data = TestUtils.generate(dimensions, 1, 100);
        ArrayList<Vector> neighbor = (ArrayList<Vector>) data.clone();
        TestUtils.addNeighbours(neighbor, 50, 3);
        ArrayList<Vector> nonNeighbor = (ArrayList<Vector>) data.clone();
        TestUtils.addNeighbours(nonNeighbor, 50, 20);
        EuclideanDistance euclideanDistance = new EuclideanDistance();
        double avg = 0;
        for (Vector v:neighbor){
            avg+=euclideanDistance.distance(data.get(0),v);
        }
        System.out.println("The avg distance of neighbor is "+ avg / neighbor.size());
        avg = 0;
        for (Vector v:nonNeighbor){
            avg+=euclideanDistance.distance(data.get(0),v);
        }
        System.out.println("The avg distance of neighbor is "+ avg / nonNeighbor.size());
        for (int x=1;x<10;x++){
            for (int y=x;y>0;y-=1){
                double p1=x/10.0;
                double p2=y/10.0;
                double k = Math.log(n)/Math.log(1/p2);
                double L = Math.pow(n,Math.log(1/p1)/Math.log(1/p2));
                int numberOfHashes = (int) k;
                int numberOfHashTables = (int) L;
                EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                        dimensions, numberOfHashes, numberOfHashTables);
                double TP=0;
                double FP=0;
                double FN=0;
                double TN=0;
                double precious=0;
                double recall=0;
                double accuracy=0;
                double f1_score=0;
                kl[x][y]= String.format("(%d,%2d)",numberOfHashes,numberOfHashTables);
                StringBuilder stringBuilder = new StringBuilder();
                testKL(edgeDeviceFactory,neighbor,data.get(0));
                TP = numberOfTrue*1.0;
                FN = numberOfFalse*1.0;
//                stringBuilder.append(String.format("(%2d,%2d,",numberOfTrue,numberOfFalse));
                testKL(edgeDeviceFactory,nonNeighbor,data.get(0));
                FP = numberOfTrue*1.0;
                TN = numberOfFalse*1.0;
//                stringBuilder.append(String.format("%2d,%2d)",numberOfTrue,numberOfFalse));
                precious = TP/(TP+FP);
                recall = TP/(TP+FN);
                accuracy = (TP+TN)/(TP+TN+FP+FN);
                f1_score = (2*precious*recall)/(precious+recall);
                stringBuilder.append(String.format("(%.1f,%.1f,%.1f,%.1f)",precious,recall,accuracy,f1_score));
                tf[x][y]=stringBuilder.toString();
            }
        }
    }

    public void testKL(EdgeDeviceFactory edgeDeviceFactory,ArrayList<Vector> dataset,Vector data){
        EdgeDevice device = edgeDeviceFactory.createEdgeDevice();
        HashSet[] fingerprint0 = device.aggFingerprints;

        numberOfTrue=0;
        numberOfFalse=0;

        HashSet<Integer> intersection = new HashSet<>();
        for (Vector v : dataset) {
            if (v==data){
                continue;
            }
            boolean flag = false;
            ArrayList<Vector> arrayList = new ArrayList<>();
            arrayList.add(v);
            device.generateAggFingerprints(arrayList);
            HashSet[] fingerprint = device.aggFingerprints;
            for (int i = 0; i < edgeDeviceFactory.NumberOfHashTables; i++) {
                intersection.addAll(fingerprint0[i]);
                intersection.retainAll(fingerprint[i]);
                if (!intersection.isEmpty()) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                numberOfTrue++;
            } else numberOfFalse++;
            device.clearFingerprints();
        }
    }

}

