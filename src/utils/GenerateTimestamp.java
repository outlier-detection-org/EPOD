package utils;

import be.tarsos.lsh.Vector;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenerateTimestamp {
    public static String generate(int s, String datasetPath) throws IOException {
        String[] strings = datasetPath.split("\\\\");
        String newPath = datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\Time_" +
                strings[strings.length - 1];
        String newPath1 = datasetPath.substring(0, datasetPath.lastIndexOf('\\')) + "\\Meta_" +
                strings[strings.length - 1];
        BufferedReader bfr = new BufferedReader(new FileReader(datasetPath));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(newPath));
        BufferedWriter bfw1 = new BufferedWriter(new FileWriter(newPath1));
        String line = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Random random = new Random();
        int count = 0;
        if (s == 500) {
            count = 490 + random.nextInt(10) + 1;
        } else if (s == 5000) {
            count = 4900 + random.nextInt(100) + 1;
        }
        bfw1.write(strings[strings.length - 1] + "\n");
        bfw1.write(count + "\n");
        int unit = s * 10 / count;
        int index = 0;
        while ((line = bfr.readLine()) != null) {
            if (index == count - 1) {
                calendar.add(Calendar.SECOND, s * 10 - unit * (count - 1));
            } else calendar.add(Calendar.SECOND, unit);
            bfw.write(formatter.format(calendar.getTime()) + ",");
            bfw.write(line);
            bfw.write("\n");
            index++;
            if (index == count) {
                if (s == 500) {
                    count = 490 + random.nextInt(10) + 1;
                } else if (s == 5000) {
                    count = 4900 + random.nextInt(100) + 1;
                }
                bfw1.write(count + "\n");
                unit = s * 10 / count;
                index = 0;
            }
        }
        bfr.close();
        bfw.close();
        bfw1.close();
        return newPath;
    }

    public static void main(String[] args) throws Throwable {
//        String newPath = generate(5000, Constants.emFileName);
//        DataGenerator dataGenerator = DataGenerator.getInstance("EM", true);
//        Date currentRealTime = dataGenerator.getFirstTimeStamp(newPath);
//        for (int i = 0; i < 5; i++) {
//            ArrayList<Vector> data = dataGenerator.getTimeBasedIncomingData(currentRealTime, 5000*10);
//            if (data.size() == 0) break;
//            System.out.println(data.size());
//            System.out.println(data.get(0).arrivalRealTime);
//            System.out.println(data.get(data.size() - 1).arrivalRealTime);
//            currentRealTime.setTime(currentRealTime.getTime() + 5000 * 10 * 1000);
//        }
    }
}
