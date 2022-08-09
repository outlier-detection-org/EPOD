package test;

import main.EdgeDeviceFactory;
import utils.Constants;
import dataStructure.Data;
import utils.DataGenerator;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) throws Throwable {

        DataGenerator dataGenerator = DataGenerator.getInstance("ForestCover");
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                55,8,4);
        for (int i=0;i<5;i++){
            edgeDeviceFactory.createEdgeDevice();
        }

        int currentTime = 0;

    }
}
