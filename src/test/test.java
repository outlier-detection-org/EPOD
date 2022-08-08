package test;

import main.EdgeDeviceFactory;
import utils.Constants;
import utils.Data;
import utils.DataGenerator;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) {

        DataGenerator dataGenerator = DataGenerator.getInstance("ForestCover");
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                55,8,4);
        for (int i=0;i<5;i++){
            edgeDeviceFactory.createEdgeDevice();
        }

        int currentTime = 0;
        ArrayList<Data> data = dataGenerator.getIncomingData(currentTime,50);

    }
}
