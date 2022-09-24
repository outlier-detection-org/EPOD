package utils;

import be.tarsos.lsh.Vector;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class GenerateDeviceId {
    public static void generateDeviceId(int numberOfDevice, String datasetPath) throws Throwable {
        String[] strings = datasetPath.split("\\\\");
        String Path = datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\Timestamp_data\\Time_" +
                strings[strings.length - 1];
        DataGenerator dataGenerator = DataGenerator.getInstance("EM", true);
        Date currentRealTime = dataGenerator.getFirstTimeStamp(Path);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        String newPath =  datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\DeviceId_data\\Device_"+
                numberOfDevice +"_"+ strings[strings.length - 1];
        BufferedWriter bfw = new BufferedWriter(new FileWriter(newPath));
        Random random = new Random();

        int[] count = new int[numberOfDevice+1];
        int total = 0;
        for (int i = 0;; i++) {
            ArrayList<Vector> data = dataGenerator.getTimeBasedIncomingData(currentRealTime, 5000*10);
            if (data.size() == 0) break;
            for (Vector v: data){
                total ++;
                bfw.write(formatter.format(v.arrivalRealTime)+",");
                int deviceId = random.nextInt(numberOfDevice)+1;
                count[deviceId]++;

                bfw.write(deviceId+",");
                for (int j = 0; j< v.values.length; j++){
                    bfw.write(String.format("%.2f", v.values[j]));
                    if (j!= v.values.length-1){
                        bfw.write(",");
                    }
                }
                bfw.write("\n");
                bfw.flush();
            }
            currentRealTime.setTime(currentRealTime.getTime() + 50000 * 1000);
        }
        Arrays.stream(count).forEach(System.out::println);
        System.out.println("total: "+ total);
    }

    public static void main(String[] args) throws Throwable {
        generateDeviceId(6,Constants.emFileName);
    }
}
