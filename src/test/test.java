package test;

import main.EdgeDeviceFactory;
import main.EdgeNodeNetwork;
import utils.Constants;
import utils.DataGenerator;

import java.util.Date;


public class test {
    public static void main(String[] args) throws Throwable {
        int numberOfHashes = 8;
        int numberOfHashTables = 4;
        int dimensions = 55;
        int nS = 1;
        Constants.slide = 500;
        EdgeDeviceFactory edgeDeviceFactory = new EdgeDeviceFactory(Constants.radiusEuclideanDict.get(Constants.forestCoverFileName),
                dimensions,numberOfHashes,numberOfHashTables);
        EdgeNodeNetwork.setNumberOfHashTables(numberOfHashTables);
        EdgeNodeNetwork.createNetwork(3,edgeDeviceFactory);
        EdgeNodeNetwork.startNetwork();

        System.out.println("started!");
        DataGenerator dataGenerator = DataGenerator.getInstance("ForestCover",true); //put data into data queue
        Date date = dataGenerator.getFirstTimeStamp(Constants.dataset);
        for (int i=nS;i>=0;i--){
            dataGenerator.getTimeBasedIncomingData(date,5000);
        }
//        EdgeNodeNetwork.stopNetwork();
    }
}
