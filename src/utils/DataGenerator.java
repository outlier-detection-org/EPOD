package utils;

import dataStructure.Data;
import main.EdgeDevice;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataGenerator {

    PriorityQueue<Data> dataQueue;

    public static DataGenerator instance;

    public static ArrayList<EdgeDevice> listeners = new ArrayList<>();

    public static HashSet<Data> outlierList = new HashSet<>();
    public static void register(EdgeDevice edgeDevice){
        listeners.add(edgeDevice);
    }

    public static HashSet<Data> notifyDevices(ArrayList<Data> data,long currentTime) throws Throwable {
        int lengthOfData = (int) Math.ceil(data.size()*1.0/listeners.size());
        for (int i=0;i<listeners.size();i++){
            int left = Math.min(i*lengthOfData,data.size());
            int right = Math.min((i+1)*lengthOfData,data.size());
            List<Data> dataForDevice =data.subList(left,right);
            listeners.get(i).setRawData(dataForDevice);
            outlierList.addAll(listeners.get(i).detectOutlier(dataForDevice,currentTime));
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
                String newPath = GenerateTimestamp.generate(Constants.slide*10,Constants.slide,Constants.emFileName);
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

//    /**
//     *  return a list of data which is in the period [currentTime,currentTime+length]
//     * @param currentTime
//     * @param length
//     * @param filename
//     * @return
//     */
//    public ArrayList<Data> getIncomingData(int currentTime, int length, String filename) {
//
//        ArrayList<Data> results = new ArrayList<>();
//        try {
//            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));
//
//            String line = "";
//            int time = 0;
//            try {
//                while ((line = bfr.readLine()) != null) {
//                    time++;
//                    if (time > currentTime && time <= currentTime + length) {
//                        String[] atts = line.split(",");
//                        double[] d = new double[atts.length];
//                        for (int i = 0; i < d.length; i++) {
//                            d[i] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
//                        }
//                        Data data = new Data(d);
//                        data.arrivalTime = time;
//                        results.add(data);
//                    }
//                }
//                bfr.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return results;
//    }

    public Date getFirstTimeStamp(String filename) throws FileNotFoundException, IOException, ParseException {
        BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));

        String line = "";
        line = bfr.readLine();
        String[] atts = line.split(",");
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return formatter.parse(atts[0].trim());
    }

//    /**
//     * return a list of data which is in the period [currentTime,currentTime+length] but each has a likely probability
//     * to return
//     * @param currentTime
//     * @param length
//     * @param filename
//     * @param likely
//     * @return
//     */
//    public ArrayList<Data> getRandomIncomingData(int currentTime, int length, String filename, double likely) {
//        Random r = new Random();
//        ArrayList<Data> results = new ArrayList<>();
//        try {
//            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));
//
//            String line = "";
//            int time = 0;
//            try {
//                while ((line = bfr.readLine()) != null) {
//                    time++;
//                    if (time > currentTime && time <= currentTime + length) {
//                        String[] atts = line.split(",");
//                        double[] d = new double[atts.length];
//                        for (int i = 0; i < d.length; i++) {
//
//                            d[i] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
//                        }
//                        Data data = new Data(d);
//                        data.arrivalTime = time;
//
//                        if (likely > r.nextDouble()) {
//                            results.add(data);
//                        }
//                    }
//                }
//                bfr.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return results;
//
//    }
//
//    /**
//     * return time based Incoming data, each data line begins with a timestamp like: [2022/8/6 data]
//     * @param currentTime
//     * @param lengthInSecond
//     * @param filename
//     * @return
//     */
//    public HashSet<Data> getTimeBasedIncomingData(Date currentTime, int lengthInSecond, String filename) throws Throwable {
//        ArrayList<Data> results = new ArrayList<>();
//        try {
//            BufferedReader bfr = new BufferedReader(new FileReader(new File(filename)));
//
//            String line = "";
//            int time = 0;
//            Date endTime = new Date();
//            endTime.setTime(currentTime.getTime() + lengthInSecond * 1000L);
//            try {
//                while ((line = bfr.readLine()) != null) {
//                    String[] atts = line.split(",");
//                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//
//                    try {
//                        Date data_time = formatter.parse(atts[0].trim());
//                        if (data_time.after(currentTime) && data_time.before(endTime)) {
//
//                            double[] d = new double[atts.length - 1];
//                            for (int i = 1; i < d.length; i++) {
//
//                                d[i - 1] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
//                            }
//                            Data data = new Data(d);
//                            data.arrivalTime = time;
//
//                            results.add(data);
//
//                        }
//                    } catch (ParseException ex) {
//                        Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//
//                }
//                bfr.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        HashSet<Data> outlier = notifyDevices(results,currentTime.getTime());
//        return outlier;
//
//    }

    /**
     * get incoming data in the period [currentTime,currentTime+length]
     * @param currentTime
     * @param length
     * @return
     */
    public HashSet<Data> getIncomingData(int currentTime, int length) throws Throwable {
        ArrayList<Data> results = new ArrayList<Data>();
        Data d = dataQueue.peek();
        while (d != null && d.arrivalTime > currentTime
                && d.arrivalTime <= currentTime + length) {
            results.add(d);
            dataQueue.poll();
            d = dataQueue.peek();

        }
        HashSet<Data> outlier = notifyDevices(results,currentTime);
        return outlier;
    }

    public ArrayList<Data> getTimeBasedIncomingData(Date currentTime, int lengthInSecond) throws Throwable {
        ArrayList<Data> results = new ArrayList<>();
        Date endTime = new Date();
        endTime.setTime(currentTime.getTime() + lengthInSecond * 1000L);
        Data d = dataQueue.peek();
        while (d!=null&&d.arrivalRealTime.compareTo(currentTime)>=0
                &&d.arrivalRealTime.compareTo(endTime)<0){
            results.add(d);
            dataQueue.poll();
            d=dataQueue.peek();
        }
        HashSet<Data> outlier = null;
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
            Data data = new Data(d);
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
                        Data data = new Data(d);
                        data.arrivalTime = time;
                        dataQueue.add(data);
                        time++;
                    }
                    else {
                        double[] d = new double[atts.length-1];
                        for (int i = 1; i < d.length-1; i++) {
                            d[i] = Double.parseDouble(atts[i]) + (new Random()).nextDouble() / 10000000;
                        }
                        Data data = new Data(d);
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
            Comparator<Data> comparator = new DataComparatorWithTimestamp();
            dataQueue = new PriorityQueue<>(comparator);
        } else {
            Comparator<Data> comparator = new DataComparator();
            dataQueue = new PriorityQueue<Data>(comparator);
        }
    }
}

class DataComparator implements Comparator<Data> {

    @Override
    public int compare(Data x, Data y) {
        if (x.arrivalTime < y.arrivalTime) {
            return -1;
        } else if (x.arrivalTime > y.arrivalTime) {
            return 1;
        } else {
            return 0;
        }

    }

}

class DataComparatorWithTimestamp implements Comparator<Data> {

    @Override
    public int compare(Data x, Data y) {
        return x.arrivalRealTime.compareTo(y.arrivalRealTime);
    }

}
