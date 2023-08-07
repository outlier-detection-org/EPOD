package Detector;

import RPC.Vector;

import java.util.*;

public abstract class Detector {
    public Set<? extends Vector> outlierVector; // This field is only used to return to the global network
    public Map<Integer, Map<List<Double>, List<Vector>>> externalData;
    public Map<List<Double>, Integer> status;
    public Map<List<Double>, Map<Integer, Integer>> historyRecord;

    public Map<List<Double>, Integer> fullCellDelta; //fingerprint
    public Detector(){
        this.externalData = Collections.synchronizedMap(new HashMap<>());
        this.fullCellDelta = new HashMap<>();
        this.historyRecord = Collections.synchronizedMap(new HashMap<>());
    }
    public abstract void detectOutlier(List<Vector> data);

    public abstract void processOutliers();

    public void clearFingerprints() {
        this.fullCellDelta = new HashMap<>();
    }

    public abstract Map<List<Double>,List<Vector>> sendData(Set<List<Double>> bucketIds, int deviceHashCode);
}
