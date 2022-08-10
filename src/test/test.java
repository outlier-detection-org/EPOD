package test;

import main.EdgeDeviceFactory;
import utils.Constants;
import dataStructure.Data;
import utils.DataGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class test {
    public static void main(String[] args) throws Throwable {

//        DataGenerator dataGenerator = DataGenerator.getInstance("ForestCover");
//        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
//                55,8,4);
//        for (int i=0;i<5;i++){
//            edgeDeviceFactory.createEdgeDevice();
//        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        System.out.println(formatter.format(calendar.getTime()));
        calendar.add(Calendar.SECOND,10);
        System.out.println(formatter.format(calendar.getTime()));
        int currentTime = 0;

    }
}
