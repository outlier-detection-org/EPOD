package main;

import be.tarsos.lsh.Index;
import be.tarsos.lsh.families.EuclidianHashFamily;
import be.tarsos.lsh.families.HashFamily;
import utils.DataGenerator;

public class EdgeDeviceFactory {
    public HashFamily hashFamily;
    public Index index;
    public int NumberOfHashTables;
    public int NumberOfHashes;

    public EdgeDeviceFactory(HashFamily hashFamily, int numberOfHashes, int numberOfHashTables){
        this.hashFamily = hashFamily;
        this.NumberOfHashes = numberOfHashes;
        this.NumberOfHashTables = numberOfHashTables;
        this.index = new Index(hashFamily,NumberOfHashes,NumberOfHashTables);
    }

    public EdgeDevice createEdgeDevice(){
        EdgeDevice edgeDevice = new EdgeDevice(index,NumberOfHashes,NumberOfHashTables);
        DataGenerator.register(edgeDevice);
        return edgeDevice;
    }




}
