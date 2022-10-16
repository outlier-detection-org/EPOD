package main;

import be.tarsos.lsh.Index;
import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.families.HashFamily;
import utils.Constants;

public class EdgeDeviceFactory {
    public HashFamily hashFamily;
    public Index index;
    public int NumberOfHashTables;
    public int NumberOfHashes;


    public EdgeDeviceFactory( int numberOfHashes, int numberOfHashTables){
        EuclidianHashFamily hashFamily;
        if ((int) (1 * Constants.R) == 0) {
            hashFamily = new EuclidianHashFamily(4, Constants.dim);
        } else {
            hashFamily = new EuclidianHashFamily((int) (10*Constants.R), Constants.dim);
        }
        this.hashFamily = hashFamily;
        this.NumberOfHashes = numberOfHashes;
        this.NumberOfHashTables = numberOfHashTables;
        this.index = new Index(hashFamily,NumberOfHashes,NumberOfHashTables);
    }

    public EdgeDevice createEdgeDevice(int deviceId) throws Throwable {
        EdgeDevice edgeDevice = new EdgeDevice(index,NumberOfHashTables,deviceId);
//        DataGenerator.register(edgeDevice);
        return edgeDevice;
    }




}
