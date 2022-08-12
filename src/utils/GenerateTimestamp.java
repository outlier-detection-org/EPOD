package utils;

import dataStructure.Data;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenerateTimestamp {
    public static String generate(int s, int range, String datasetPath) throws IOException {
        String[] strings = datasetPath.split("/");
        String newPath = datasetPath.substring(0, datasetPath.lastIndexOf('/'))+"/Time_"+
                strings[strings.length-1];
        BufferedReader bfr = new BufferedReader(new FileReader(datasetPath));
        BufferedWriter bfw = new BufferedWriter(new FileWriter(newPath));
        String line ="";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        Random random =new Random();
        int count = random.nextInt(range)+1;
        int unit = s/count;
        int index = 0;
        int num = 0;
        while((line = bfr.readLine())!=null){
            calendar.add(Calendar.SECOND,unit);
            bfw.write(formatter.format(calendar.getTime())+",");
            bfw.write(line);
            bfw.write("\n");
            index++;
            num++;
            if (num==2000) break;
            if (index==count){
                count = random.nextInt(range)+1;
                unit = s/count;
                index = 0;
            }
        }
        bfr.close();
        bfw.close();
        return newPath;
    }

    public static void main(String[] args) throws Throwable {
//        String newPath = generate(5000,500,Constants.emFileName);
//        DataGenerator dataGenerator = DataGenerator.getInstance("EM",true);
//        Date currentRealTime = dataGenerator.getFirstTimeStamp(newPath);
//        for (int i=0;i<5;i++){
//            ArrayList<Data> data = dataGenerator.getTimeBasedIncomingData(currentRealTime,5000);
//            if (data.size()==0) break;
//            System.out.println(data.size());
//            System.out.println(data.get(0).arrivalRealTime);
//            System.out.println(data.get(data.size()-1).arrivalRealTime);
//            currentRealTime.setTime(currentRealTime.getTime()+5000*1000);
        }
}
