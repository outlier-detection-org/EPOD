package utils;

import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.DistanceComparator;
import be.tarsos.lsh.families.DistanceMeasure;
import be.tarsos.lsh.families.EuclideanDistance;
import main.EdgeDevice;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataGenerator {

    PriorityQueue<Vector> dataQueue;

    public static DataGenerator instance;

    public static ArrayList<EdgeDevice> listeners = new ArrayList<>();

    public static HashSet<Vector> outlierList = new HashSet<>();
    public static void register(EdgeDevice edgeDevice){
        listeners.add(edgeDevice);
    }

    public static HashSet<Vector> notifyDevices(ArrayList<Vector> data,long currentTime) throws InterruptedException {
        int lengthOfData = (int) Math.ceil(data.size()*1.0/listeners.size());
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i=0;i<listeners.size();i++){
            int finalI = i;
            Thread t = new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName()+": notify listener "+finalI);
                    int left = Math.min(finalI*lengthOfData,data.size());
                    int right = Math.min((finalI+1)*lengthOfData,data.size());
                    List<Vector> dataForDevice =data.subList(left,right);
                    listeners.get(finalI).setRawData(dataForDevice);
                    outlierList.addAll(listeners.get(finalI).detectOutlier(currentTime));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t:threads){
            t.join();
        }
        return outlierList;
    }

    public static DataGenerator getInstance(String type, boolean withTime) throws IOException {
        String name = "";
        boolean random = false;
        if (instance != null) {
            return instance;
        } else {
            switch (type){
                case "ForestCover":name = Constants.forestCoverFileName;break;
                case "TAO":name = Constants.taoFileName;break;
                case "EM":name = Constants.emFileName;break;
                case "STOCK":name = Constants.stockFileName;break;
                case "Gauss":name = Constants.gaussFileName;break;
                case "HPC":name = Constants.hpcFileName;break;
                default: random = true;break;
            }
            if (random){
                instance = new DataGenerator(false);
                instance.getRandomInput(1000, 10);
                return instance;
            }
            if (withTime){
                String newPath = GenerateTimestamp.generate(Constants.slide*10,Constants.slide,name);
                Constants.dataset = newPath;
                instance = new DataGenerator(true);
                instance.getData(newPath);
                return instance;
            }
            instance = new DataGenerator(false);
            instance.getData(name);
            return instance;
        }
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
    public HashSet<Vector> getIncomingData(int currentTime, int length) throws Throwable {
        ArrayList<Vector> results = new ArrayList<Vector>();
        Vector d = dataQueue.peek();
        while (d != null && d.arrivalTime > currentTime
                && d.arrivalTime <= currentTime + length) {
            results.add(d);
            dataQueue.poll();
            d = dataQueue.peek();

        }
        HashSet<Vector> outlier = notifyDevices(results,currentTime);
        return outlier;
    }

    public ArrayList<Vector> getTimeBasedIncomingData(Date currentTime, int lengthInSecond) throws Throwable {
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
//        HashSet<Data> outlier = notifyDevices(results,currentTime.getTime());
        return results;
    }


    /**
     * generate random data of the size length in the range [0,range]
     * @param length
     * @param range
     */
    public void getRandomInput(int length, int range) {

        Random r = new Random();
        for (int i = 1; i <= length; i++) {
            double d = r.nextInt(range);
            Vector data = new Vector(d);
            data.arrivalTime = i;
            dataQueue.add(data);
            dataQueue.add(data);

        }

    }

    /**
     * read data and store it in the dataQueue
     * @param filename
     */
    public void getData(String filename) {
        boolean hasTimestamp = false;
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));

            try {
                String line = bfr.readLine();
                try{
                    Double.parseDouble(line.split(",")[0]);
                }catch (NumberFormatException e){
                    hasTimestamp = true;
                }
                int time = 1;
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
                        double[] d = new double[atts.length-1];
                        for (int i = 1; i < d.length-1; i++) {
                            d[i] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
                        }
                        Vector data = new Vector(d);
                        data.arrivalRealTime = formatter.parse(atts[0]);
                        dataQueue.add(data);
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

    public HashMap<Integer,HashSet<Vector>> linearSearchReturnBuckets(ArrayList<Vector> data,Vector tmp) {
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
    public HashSet<Vector> linearSearch(ArrayList<Vector> data){
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
