package main;

import utils.Data;

import java.util.HashSet;
import java.util.List;

public class EdgeNode {

    private List<Data> localData;
    private HashSet[] localAggFingerprints;
    public EdgeDevice edgeDevice;

    public void upload(HashSet[] aggFingerprints){
        this.localAggFingerprints = aggFingerprints;
        //todo: upload fingerprints to all node in network
        // RPC compare
    }

    public void setEdgeDevice(EdgeDevice edgeDevice) {
        this.edgeDevice = edgeDevice;
    }

    public List<Data> getData(){
        if(localData==null){
            //TODO: ask device to upload data
        }
        return localData;
    }
}
