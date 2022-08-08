package main;

import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.families.HashFamily;
import utils.DataGenerator;

public class EdgeDeviceFactory {
    public HashFamily hashFamily;
    public int NumberOfHashTables;
    public int NumberOfHashes;

    public EdgeDeviceFactory(int w, int dimensions, int numberOfHashes, int numberOfHashTables){
        this.hashFamily = new EuclidianHashFamily(w,dimensions);
        this.NumberOfHashes = numberOfHashes;
        this.NumberOfHashTables = numberOfHashTables;
    }

    public EdgeDevice createEdgeDevice(){
        EdgeDevice edgeDevice = new EdgeDevice(hashFamily,NumberOfHashes,NumberOfHashTables);
        DataGenerator.register(edgeDevice);
        return edgeDevice;
    }




}
