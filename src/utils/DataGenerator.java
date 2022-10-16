package utils;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import be.tarsos.lsh.util.TestUtils;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataGenerator {

    public PriorityQueue<Vector> dataQueue;

    public static DataGenerator getInstance(String type, int deviceId) throws Throwable {
        switch (type) {
            case "ForestCover":
                Constants.datasetPath = Constants.forestCoverFileName;
                break;
            case "TAO":
                Constants.datasetPath = Constants.taoFileName;
                break;
            case "EM":
                Constants.datasetPath = Constants.emFileName;
                break;
            case "STOCK":
                Constants.datasetPath = Constants.stockFileName;
                break;
            case "GAU":
                Constants.datasetPath = Constants.gaussFileName;
                break;
            case "HPC":
                Constants.datasetPath = Constants.hpcFileName;
                break;
            case "GAS":
                Constants.datasetPath = Constants.gasFileName;
                break;
            default:
                break;
        }
        if (Constants.withTime && !Constants.withDeviceId) {
            String[] strings = Constants.datasetPath.split("\\\\");
            String newPath = Constants.datasetPath.substring(0, Constants.datasetPath.lastIndexOf('\\')) + "\\Timestamp_data\\Time_" +
                    strings[strings.length - 1];
            DataGenerator instance = new DataGenerator(true);
            instance.getData(newPath);
            Constants.datasetPathWithTime = newPath;
            return instance;
        } else if (Constants.withTime) {
            String[] strings = Constants.datasetPath.split("\\\\");
            String newPath = Constants.datasetPath.substring(0, Constants.datasetPath.lastIndexOf('\\')) + "\\Timestamp_data\\Time_" +
                    strings[strings.length - 1];
            Constants.datasetPathWithTime = newPath;
            newPath = GenerateDeviceId.generateDeviceId(Constants.dn * Constants.nn, Constants.datasetPath)+
                    "\\" + deviceId + ".txt";
            DataGenerator instance = new DataGenerator(true);
            instance.getData(newPath);
            return instance;
        }
        //TODO: withTime = false that is count based window.
        DataGenerator instance = new DataGenerator(false);
        instance.getData(Constants.datasetPath);
        return instance;
    }

    public boolean hasNext() {
        return !dataQueue.isEmpty();
    }


    public Date getFirstTimeStamp(String filename) throws FileNotFoundException, IOException, ParseException {
        BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));

        String line = "";
        line = bfr.readLine();
        String[] atts = line.split(",");
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return formatter.parse(atts[0].trim());
    }

    /**
     * get incoming data in the period [currentTime,currentTime+length]
     * @param currentTime
     * @param length
     * @return
     */
    public ArrayList<Vector> getIncomingData(int currentTime, int length) throws Throwable {
        ArrayList<Vector> results = new ArrayList<Vector>();
        Vector d = dataQueue.peek();
        while (d != null && d.arrivalTime > currentTime
                && d.arrivalTime <= currentTime + length) {
            results.add(d);
            dataQueue.poll();
            d = dataQueue.peek();

        }
        return results;
    }

    public ArrayList<Vector> getTimeBasedIncomingData(Date currentTime, int lengthInSecond, int deviceId) throws Throwable {
        ArrayList<Vector> results = new ArrayList<>();
        Date endTime = new Date();
        endTime.setTime(currentTime.getTime() + lengthInSecond * 1000L);
        Vector d = dataQueue.peek();
        while (d!=null&&d.arrivalRealTime.compareTo(currentTime)>=0
                &&d.arrivalRealTime.compareTo(endTime)<0){
            results.add(d);
            dataQueue.poll();
            d=dataQueue.peek();
        }
        return results;
    }

    /**
     * read data and store it in the dataQueue
     * @param filename
     */
    public void getData(String filename) {
        boolean hasTimestamp = Constants.withTime;
        boolean hasDeviceId = Constants.withDeviceId;
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));

            try {
                String line = bfr.readLine();

                int time = 0;
                while (line != null) {
                    String[] atts = line.split(",");
                    if (!hasTimestamp){
                        double[] d = new double[atts.length];
                        for (int i = 0; i < d.length; i++) {
                            d[i] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
                        }
                        Vector data = new Vector(d);
                        data.arrivalTime = time;
                        dataQueue.add(data);
                        time++;
                    }
                    else {
                        if (!hasDeviceId){
                            double[] d = new double[atts.length-1];
                            for (int i = 1; i < atts.length; i++) {
                                d[i-1] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
                            }
                            Vector data = new Vector(d);
                            data.deviceId=0;
                            data.arrivalRealTime = formatter.parse(atts[0]);
                            data.arrivalTime = time;
                            time ++;
                            dataQueue.add(data);
                        } else {
                            double[] d = new double[atts.length-2];
                            for (int i = 2; i < atts.length; i++) {
                                d[i-2] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
                            }
                            Vector data = new Vector(d);
                            data.deviceId = Integer.parseInt(atts[1]);
                            data.arrivalRealTime = formatter.parse(atts[0]);
                            data.arrivalTime = time;
                            time++;
                            dataQueue.add(data);
                        }
                    }
                    line = bfr.readLine();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public DataGenerator(boolean hasTime) {
        if (hasTime) {
            Comparator<Vector> comparator = new DataComparatorWithTimestamp();
            dataQueue = new PriorityQueue<>(comparator);
        } else {
            Comparator<Vector> comparator = new DataComparator();
            dataQueue = new PriorityQueue<Vector>(comparator);
        }
    }

    public static HashMap<Integer,HashSet<Vector>> linearSearchReturnBuckets(ArrayList<Vector> data,Vector tmp) {
        HashMap<Integer, HashSet<Vector>> buckets = new HashMap<>();
        HashSet<Vector> outlier = new HashSet<>();
        DistanceMeasure measure = new EuclideanDistance();

        Vector data1 = tmp;
        ArrayList<Vector> neighbor = new ArrayList<>();
        HashSet<Vector> hashSet = new HashSet<>();
//            hashSet.add(data1);
        DistanceComparator dc = new DistanceComparator(data1, measure);
        PriorityQueue<Vector> priorityQueue = new PriorityQueue<>(data.size(), dc);
        priorityQueue.addAll(data);
        Vector d = priorityQueue.peek();
        double distance = measure.distance(d, data1);
        while (distance <= Constants.R) {
            neighbor.add(d);
            hashSet.add(d);
            priorityQueue.poll();
            d = priorityQueue.peek();
            distance = measure.distance(d, data1);
        }
        buckets.put(1, hashSet);
        if (neighbor.size() < Constants.K) {
            outlier.add(data1);
        }
        return buckets;
    }

    public static ArrayList<Vector> generateBucket(int dimensions,int size,double radius){
        ArrayList<Vector> data = TestUtils.generate(dimensions,1,100);
        TestUtils.addNeighbours(data,size-1,radius);
        return data;
    }

    public static HashSet<Vector> linearSearch(ArrayList<Vector> data){
        HashSet<Vector> outlier = new HashSet<>();
        DistanceMeasure measure = new EuclideanDistance();
        ArrayList<Vector> neighbor = new ArrayList<>();
        for (Vector data1:data){
            DistanceComparator dc = new DistanceComparator(data1,measure);
            PriorityQueue<Vector> priorityQueue = new PriorityQueue<>(data.size(),dc);
            priorityQueue.addAll(data);
            Vector d= priorityQueue.peek();
            double distance = measure.distance(d,data1);
            while(distance<=Constants.R){
                neighbor.add(d);
                priorityQueue.poll();
                d= priorityQueue.peek();
                distance = measure.distance(d,data1);
            }
            if (neighbor.size()<Constants.K){
                outlier.add(data1);
            }
        }
        return outlier;
    }

    public static ArrayList<ArrayList<Vector>> bucketing(String type) throws Throwable {
        DataGenerator dataGenerator = DataGenerator.getInstance(type, 0);
        ArrayList<Vector> dataset = new ArrayList<>(dataGenerator.dataQueue);
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

class DataComparator implements Comparator<Vector> {

    @Override
    public int compare(Vector x, Vector y) {
        if (x.arrivalTime < y.arrivalTime) {
            return -1;
        } else if (x.arrivalTime > y.arrivalTime) {
            return 1;
        } else {
            return 0;
        }

    }
}

class DataComparatorWithTimestamp implements Comparator<Vector> {

    @Override
    public int compare(Vector x, Vector y) {
        return x.arrivalRealTime.compareTo(y.arrivalRealTime);
    }

}
