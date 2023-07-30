package utils;

import RPC.Vector;

import java.io.*;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataGenerator {

    public PriorityQueue<Vector> dataQueue;
    public Date firstTimeStamp;

    // we use this to uniquely identify each data point
    // there are cases that two data points have the same arrivalTime in each device
    // but are different points
    public static AtomicInteger arrivalTime = new AtomicInteger(-1);

    public DataGenerator(int deviceId) {
        int nodeId = deviceId / Constants.dn;
        int device = deviceId % Constants.dn;
        int deviceNumber = nodeId * Constants.max_dn + device;
        this.getData(Constants.timePrefix + deviceNumber + ".txt");
        assert this.dataQueue.peek() != null;
        this.firstTimeStamp = this.dataQueue.peek().arrivalRealTime;
    }


    public ArrayList<Vector> getTimeBasedIncomingData(Date currentTime, int lengthInSecond) {
        ArrayList<Vector> results = new ArrayList<>();
        Date endTime = new Date();
        endTime.setTime(currentTime.getTime() + lengthInSecond * 1000L);
        Vector d = dataQueue.peek();
        while (d != null && d.arrivalRealTime.compareTo(currentTime) >= 0
                && d.arrivalRealTime.compareTo(endTime) < 0) {
            d.slideID = Constants.currentSlideID;
            d.arrivalTime = arrivalTime.incrementAndGet();
            results.add(d);
            dataQueue.poll();
            d = dataQueue.peek();
        }
        return results;
    }


    public void getData(String filename) {
        Comparator<Vector> comparator = new DataComparatorWithTimestamp();
        dataQueue = new PriorityQueue<>(comparator);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SS");
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(filename));
            try {
                String line = bfr.readLine();
                while (line != null) {
                    String[] atts = line.split(",");
                    ArrayList<Double> d = new ArrayList<>();
                    for (int i = 2; i < atts.length; i++) {
                        d.add(Double.parseDouble(atts[i]));
                    }
                    Vector data = new Vector(d);
                    data.arrivalRealTime = formatter.parse(atts[0]);
                    dataQueue.add(data);
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

}

class DataComparatorWithTimestamp implements Comparator<Vector> {
    @Override
    public int compare(Vector x, Vector y) {
        return x.arrivalRealTime.compareTo(y.arrivalRealTime);
    }
}
