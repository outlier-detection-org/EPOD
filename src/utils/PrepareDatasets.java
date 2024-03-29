package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;


public class PrepareDatasets {
    public static String datasetPath;

    public static int deviceNum = Constants.max_dn * Constants.max_nn;
    public static int s = Constants.S;
    public static String deviceIdPrefix = Constants.deviceIdPrefix;
    public static String timePrefix = Constants.timePrefix;

    static {
        switch (Constants.dataset) {
            case "FC" -> datasetPath = Constants.forestCoverFileName;
            case "TAO" -> datasetPath = Constants.taoFileName;
            case "EM" -> datasetPath = Constants.emFileName;
            case "STK" -> datasetPath = Constants.stockFileName;
            case "GAU" -> datasetPath = Constants.gaussFileName;
            case "HPC" -> datasetPath = Constants.hpcFileName;
            case "GAS" -> datasetPath = Constants.gasFileName;
            default -> {
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        //Step1 : Generate deviceID;
//        generateDeviceId();
        //Step2 : Generate timestamp
        s = Constants.timelineSpeed;
        deviceIdPrefix = Constants.deviceIdPrefix;
        timePrefix = Constants.timePrefix;
        generateTimestamp();
    }

    public static void generateDeviceId() throws Throwable {
        File f = new File(deviceIdPrefix);
        if (f.exists()) {
            return;
        } else f.mkdirs();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(datasetPath));
        BufferedWriter[] bfws = new BufferedWriter[deviceNum];
        for (int i = 0; i < deviceNum; i++) {
            bfws[i] = new BufferedWriter(new FileWriter(Constants.deviceIdPrefix + "/" + i + ".txt"));
        }
        String line;
        int deviceId = 0;
        while ((line = bufferedReader.readLine()) != null) {
            bfws[deviceId].write(deviceId+","+line);
            bfws[deviceId].write("\n");
            bfws[deviceId].flush();
            deviceId = (deviceId+1)%deviceNum;
        }
        for (int i=0;i<deviceNum;i++){
            bfws[i].close();
        }
    }

    public static void generateTimestamp() throws IOException {
        File f = new File(timePrefix);
        if (f.exists()) {
            return;
        } else f.mkdirs();
        String metaPath = timePrefix + "/meta.txt";
        BufferedWriter metaBfw = new BufferedWriter(new FileWriter(metaPath));
        BufferedReader[] bfrs = new BufferedReader[deviceNum];
        for (int i = 0; i < deviceNum; i++) {
            bfrs[i] = new BufferedReader(new FileReader(deviceIdPrefix + "/" + i + ".txt"));
        }
        BufferedWriter[] bfws = new BufferedWriter[deviceNum];
        for (int i = 0; i < deviceNum; i++) {
            bfws[i] = new BufferedWriter(new FileWriter(timePrefix + "/" + i + ".txt"));
        }

        String line;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SS");
        Random random = new Random();

        for (int i=0;i<deviceNum;i++){
            int count; //count per slide
            if (s == 500) {
                count = 490 + random.nextInt(10) + 1;
            } else if (s == 5000) {
                count = 4900 + random.nextInt(100) + 1;
            } else if (s == 1000) {
                count = 990 + random.nextInt(10) + 1;
            } else {
                count = s - 10 + random.nextInt(10) + 1;
            }
            metaBfw.write("device " + i + " " + count + "\n");
            int unit = 1000 / count;
            int index = 0;
            while ((line = bfrs[i].readLine()) != null){
                if (index == count - 1) {
                    calendar.add(Calendar.MILLISECOND, 1000 - unit * (count - 1));
                } else calendar.add(Calendar.MILLISECOND, unit);
                bfws[i].write(formatter.format(calendar.getTime()) + "," + line + "\n");
                index++;
                if (index == count) {
                    if (s == 500) {
                        count = 490 + random.nextInt(10) + 1;
                    } else if (s == 5000) {
                        count = 4900 + random.nextInt(100) + 1;
                    } else if (s == 1000) {
                        count = 990 + random.nextInt(10) + 1;
                    } else {
                        count = s -10 + random.nextInt(10) + 1;
                    }
                    metaBfw.write("device " + i + " " + count + "\n");
                    unit = 1000 / count;
                    index = 0;
                }

            }
        }

        for (int i = 0; i < deviceNum; i++) {
            bfrs[i].close();
        }
        for (int i = 0; i < deviceNum; i++) {
            bfws[i].close();
        }
        metaBfw.close();
    }
}
