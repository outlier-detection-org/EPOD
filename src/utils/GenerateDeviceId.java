package utils;

import java.io.*;
import java.util.Random;

public class GenerateDeviceId {
    public static String generateDeviceId(int numberOfDevice, String datasetPath) throws Throwable {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.datasetPathWithTime));
        String[] strings = datasetPath.split("\\\\");
        String newPath = datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\DeviceId_data\\Device_" +
                numberOfDevice + "_" + strings[strings.length - 1].split("\\.")[0];
        File f = new File(newPath);
        if (f.exists()) {
            return newPath;
        } else f.mkdirs();
        BufferedWriter[] bfws = new BufferedWriter[numberOfDevice];
        for (int i = 0; i < numberOfDevice; i++) {
            bfws[i] = new BufferedWriter(new FileWriter(
                    newPath + "\\" + i + "." + strings[strings.length - 1].split("\\.")[1]));
        }
        Random random = new Random();

        int[] count = new int[numberOfDevice + 1];
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            int deviceId = random.nextInt(numberOfDevice);
            String[] s = line.split(",");
            bfws[deviceId].write(s[0] + ","+deviceId+line.substring(line.indexOf(","),line.length()));
            count[deviceId]++;
            bfws[deviceId].write("\n");
            bfws[deviceId].flush();
        }

        for (int i=0;i< numberOfDevice;i++){
            bfws[i].close();
        }
//        Arrays.stream(count).forEach(System.out::println);
        return newPath;
    }

    public static String DivideByDeviceId(int numberOfDevice,String datasetPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.datasetPathWithTime));
        String[] strings = datasetPath.split("\\\\");
        String newPath = datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\DeviceId_data\\Device_" +
                numberOfDevice + "_" + strings[strings.length - 1].split("\\.")[0];
        File f = new File(newPath);
        if (f.exists()) {
            return newPath;
        } else f.mkdirs();
        BufferedWriter[] bfws = new BufferedWriter[numberOfDevice];
        for (int i = 0; i < numberOfDevice; i++) {
            bfws[i] = new BufferedWriter(new FileWriter(
                    newPath + "\\" + i + "." + strings[strings.length - 1].split("\\.")[1]));
        }

        int[] count = new int[numberOfDevice + 1];
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            int deviceId  = Integer.parseInt(line.split(",")[1]);
            String[] s = line.split(",");
            bfws[deviceId].write(s[0] +line.substring(line.indexOf(","),line.length()));
            count[deviceId]++;
            bfws[deviceId].write("\n");
            bfws[deviceId].flush();
        }

        for (int i=0;i< numberOfDevice;i++){
            bfws[i].close();
        }
//        Arrays.stream(count).forEach(System.out::println);
        return newPath;
    }
    public static void main(String[] args) throws Throwable {

    }
}
