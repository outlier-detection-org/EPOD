package Detector;

import RPC.Vector;
import DataStructure.Cell;
import DataStructure.Tuple;
import Framework.DeviceImpl;
import utils.Constants;

import java.util.*;

@SuppressWarnings("unchecked")
public class NewNETS extends Detector {
    public boolean subDimFlag;
    public double neighCellIdxDist;//the distance between two neighbor cells in the subspace
    public double neighCellFullDimIdxDist;
    public double[] maxValues;
    public double[] minValues;
    public int random; // whether random choose the subspace
    private List<Integer> priorityList;
    public double[] dimLength;
    public double[] subDimLength;

    public HashMap<Integer, ArrayList<Short>> idxDecoder;
    public HashMap<ArrayList<Short>, Integer> idxEncoder;
    public HashMap<Integer, Integer> slideDelta; //sub cell id -> cell delta number
    public HashSet<Tuple> outliers;
    public HashMap<Integer, Integer> windowCnt; //sub cell id -> cell point cnt
    public HashMap<Integer, Integer> fullDimCellWindowCnt;
    public HashMap<Integer, Cell> slideIn; //sub cell id -> cell
    public HashMap<Integer, Cell> slideOut;
    public LinkedList<HashMap<Integer, Cell>> slides;
    public HashMap<Integer, Integer> fullDimCellSlideInCnt; //full cell id -> cell point cnt
    public HashMap<Integer, Integer> fullDimCellSlideOutCnt;
    public Queue<HashMap<Integer, Integer>> fullDimCellSlidesCnt;
    public HashSet<Integer> influencedCells;

    public int candidateCellsTupleCnt = 0;

    public NewNETS(int random) {
        super();
        this.subDimFlag = Constants.dim != Constants.subDim;
        this.random = random;
        determineMinMax();
        this.windowCnt = new HashMap<>();
        this.slides = new LinkedList<>();
        this.slideOut = new HashMap<>();
        this.idxDecoder = new HashMap<>();
        this.idxEncoder = new HashMap<>();
        this.fullDimCellWindowCnt = new HashMap<>();
        this.fullDimCellSlidesCnt = new LinkedList<>();
        this.fullDimCellSlideOutCnt = new HashMap<>();
        this.outliers = new HashSet<>();
        this.neighCellIdxDist = Math.sqrt(Constants.subDim) * 2;
        this.neighCellFullDimIdxDist = Math.sqrt(Constants.dim) * 2;

        //TODO: all dimension weigh equal
        /* Cell size calculation for all dim*/
        double minDimSize = Integer.MAX_VALUE;
        double[] dimSize = new double[Constants.dim];
        for (int i = 0; i < Constants.dim; i++) {
            dimSize[i] = maxValues[i] - minValues[i];
            if (dimSize[i] < minDimSize) minDimSize = dimSize[i];
        }

        double dimWeightsSum = 0;
        int[] dimWeights = new int[Constants.dim];
        for (int i = 0; i < Constants.dim; i++) {
            //dimWeights[i] = dimSize[i]/minDimSize; //relative-weight
            dimWeights[i] = 1; //equal-weight
            dimWeightsSum += dimWeights[i];
        }

        dimLength = new double[Constants.dim];
        double[] gapCount = new double[Constants.dim];
        for (int i = 0; i < Constants.dim; i++) {
            dimLength[i] = Math.sqrt(Constants.R * Constants.R * dimWeights[i] / dimWeightsSum);
            gapCount[i] = Math.ceil(dimSize[i] / dimLength[i]);
            dimSize[i] = gapCount[i] * dimLength[i];
        }

        /* Cell size calculation for sub dim*/
        if (subDimFlag) {
            double minSubDimSize = Integer.MAX_VALUE;
            double[] subDimSize = new double[Constants.subDim];
            for (int i = 0; i < Constants.subDim; i++) {
                subDimSize[i] = maxValues[i] - minValues[i];
                if (subDimSize[i] < minSubDimSize) minSubDimSize = subDimSize[i];
            }

            double subDimWeightsSum = 0;
            int[] subDimWeights = new int[Constants.subDim];
            for (int i = 0; i < Constants.subDim; i++) {
                //subDimWeights[i] = subDimSize[i]/minSubDimSize; //relative-weight
                subDimWeights[i] = 1; //equal-weight
                subDimWeightsSum += subDimWeights[i];
            }

            subDimLength = new double[Constants.subDim];
            double[] subDimGapCount = new double[Constants.subDim];
            for (int i = 0; i < Constants.subDim; i++) {
                subDimLength[i] = Math.sqrt(Constants.R * Constants.R * subDimWeights[i] / subDimWeightsSum);
                subDimGapCount[i] = Math.ceil(subDimSize[i] / subDimLength[i]);
                subDimSize[i] = subDimGapCount[i] * subDimLength[i];
            }
        }

    }

    @Override
    public void detectOutlier(List<Vector> data) {
        if (data.isEmpty()) return;
        ArrayList<Tuple> newSlide = preprocessData(data); //vector to tuple
        calcNetChange(newSlide, Constants.currentSlideID);
        findOutlier("NETS", Constants.currentSlideID);
    }

    public ArrayList<Tuple> preprocessData(List<Vector> data) {
        ArrayList<Tuple> newSlide = new ArrayList<Tuple>();
        for (Vector datum : data) {
            double[] value = new double[data.get(0).values.size()];
            int j = 0;
            //ά������
            for (int i : priorityList) {
                value[j] = datum.values.get(i);
                j++;
            }
            Tuple tuple = new Tuple(datum.arrivalTime, Constants.currentSlideID, value);
            newSlide.add(tuple);
        }
        return newSlide;
    }

    public void determineMinMax() {
        if (Constants.dataset.contains("HPC")) {
            this.maxValues = new double[]{10.67, 1.39, 252.14, 46.4, 80, 78, 31};
            this.minValues = new double[]{0.076, 0, 223.49, 0.2, 0, 0, 0};
            this.priorityList = Arrays.asList(2, 3, 0, 1, 6, 5, 4);
            sortPriority(random);
        } else if (Constants.dataset.contains("EM")) {
            maxValues = new double[]{2993.82, 9422.14, 5567.44, 6127.68, 4420.84, 5593.51, 4717.23, 5376.15, 4134.21, 3295.82, 4493.98, 4037.97, 4540.98, 5108.82, 4417.46, 3468.07};
            minValues = new double[]{-56.48, 1664.2, -47.78, -6.83, -12.68, -41.98, -15.28, -11.87, 2976.53, 2367.65, 789.55, 671.67, 460.37, 453.42, 862.61, 659.45};
            this.priorityList = Arrays.asList(8, 9, 10, 15, 11, 14, 2, 1, 3, 6, 7, 0, 4, 13, 12, 5);
            sortPriority(random);
        } else if (Constants.dataset.contains("TAO")) {
            this.maxValues = new double[]{75.39, 101.68, 30.191};
            this.minValues = new double[]{-9.99, -9.99, -9.999};
            this.priorityList = Arrays.asList(1, 2, 0);
            sortPriority(random);
        } else if (Constants.dataset.contains("STK")) {
            this.maxValues = new double[]{9930};
            this.minValues = new double[]{0};
            this.priorityList = Arrays.asList(new Integer[]{0});
            sortPriority(random);
        } else if (Constants.dataset.contains("GAU")) {
            this.maxValues = new double[]{100.81};
            this.minValues = new double[]{-3.5042};
            this.priorityList = Arrays.asList(new Integer[]{0});
            sortPriority(random);
        } else if (Constants.dataset.contains("FC")) {
            this.maxValues = new double[]{3858, 360, 66, 1397, 601, 7117, 254, 254, 254, 7173, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7};
            this.minValues = new double[]{1859, 0, 0, 0, -173, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
            this.priorityList = Arrays.asList(1, 5, 9, 8, 0, 3, 6, 2, 7, 4, 54, 10, 12, 42, 36, 45, 13, 46, 43, 25, 23, 35, 44, 37, 51, 26, 52, 33, 24, 11, 17, 53, 19, 15, 32, 30, 14, 29, 16, 39, 31, 18, 48, 22, 41, 34, 47, 40, 27, 50, 21, 49, 20, 28, 38);
            sortPriority(random);
        } else if (Constants.dataset.contains("GAS")) {
            this.maxValues = new double[]{13.7333, 11.3155, 11.3742, 12.7548, 378.75, 73.8178, 102.575, 99.8881, 30.3254, 77.6805};
            this.minValues = new double[]{5.43146, 1.82066, 1.6269, 2.28292, 1.90129, 5.58795, 1.22037, 1.43053, 24.4344, 44.6604};
            this.priorityList = Arrays.asList(8, 9, 3, 2, 1, 0, 5, 7, 6, 4);
            sortPriority(random);
        } else if (Constants.dataset.contains("RC")) {
            this.maxValues = new double[]{1228.7920230041038, 2368.1967782464408, 2525.0995344267344, 69.99999559016085, 1951.538090949156};
            this.minValues = new double[]{-1357.513967818367, -946.2981855322763, -261.7643504682149, -1787.6199065185856, -1613.3679145459894};
            //TODO: how to decide priority
            this.priorityList = Arrays.asList(1, 1, 1, 1, 1);
            sortPriority(random);
        }
    }

    public void sortPriority(int random) { //sort min and max arrays
        if (random > 0) Collections.shuffle(this.priorityList);
        double[] new_maxValues = new double[priorityList.size()];
        double[] new_minValues = new double[priorityList.size()];
        for (int i = 0; i < priorityList.size(); i++) {
            new_maxValues[i] = maxValues[this.priorityList.get(i)];
            new_minValues[i] = minValues[this.priorityList.get(i)];
        }
        this.maxValues = new_maxValues;
        this.minValues = new_minValues;
    }

    /**
     * @param slideTuples new coming slide
     * @description put new tuples in data structure slideIn fullDimCellSlideInCnt slides fullDimCellSlideCnt
     */
    public void indexingSlide(ArrayList<Tuple> slideTuples) {
        slideIn = new HashMap<>();
        fullDimCellSlideInCnt = new HashMap<>();
        for (Tuple t : slideTuples) {
            ArrayList<Short> fullDimCellIdx = new ArrayList<>();
            ArrayList<Short> subDimCellIdx = new ArrayList<>();
            for (int j = 0; j < Constants.dim; j++) {
                short dimIdx = (short) ((t.value[j] - minValues[j]) / dimLength[j]);
                fullDimCellIdx.add(dimIdx);
            }
            if (subDimFlag) {
                for (int j = 0; j < Constants.subDim; j++) {
                    short dimIdx = (short) ((t.value[j] - minValues[j]) / subDimLength[j]);
                    subDimCellIdx.add(dimIdx);
                }
            } else {
                subDimCellIdx = fullDimCellIdx;
            }

            t.fullDimCellIdx = fullDimCellIdx;
            t.subDimCellIdx = subDimCellIdx;

            if (!idxEncoder.containsKey(fullDimCellIdx)) {
                int id = idxEncoder.size();
                idxEncoder.put(fullDimCellIdx, id);
                idxDecoder.put(id, fullDimCellIdx);
            }
            if (!idxEncoder.containsKey(subDimCellIdx)) {
                int id = idxEncoder.size();
                idxEncoder.put(subDimCellIdx, id);
                idxDecoder.put(id, subDimCellIdx);
            }
            if (!slideIn.containsKey(idxEncoder.get(subDimCellIdx))) {
                double[] cellCenter = new double[Constants.subDim];
                if (subDimFlag) {
                    for (int j = 0; j < Constants.subDim; j++)
                        cellCenter[j] = minValues[j] + subDimCellIdx.get(j) * subDimLength[j] + subDimLength[j] / 2;
                } else {
                    for (int j = 0; j < Constants.dim; j++)
                        cellCenter[j] = minValues[j] + fullDimCellIdx.get(j) * dimLength[j] + dimLength[j] / 2;
                }
                slideIn.put(idxEncoder.get(subDimCellIdx), new Cell(subDimCellIdx, fullDimCellIdx, cellCenter, subDimFlag));
            }
            slideIn.get(idxEncoder.get(subDimCellIdx)).addTuple(t, subDimFlag);

            if (!fullDimCellSlideInCnt.containsKey(idxEncoder.get(fullDimCellIdx))) {
                fullDimCellSlideInCnt.put(idxEncoder.get(fullDimCellIdx), 0);
            }
            fullDimCellSlideInCnt.put(idxEncoder.get(fullDimCellIdx), fullDimCellSlideInCnt.get(idxEncoder.get(fullDimCellIdx)) + 1);
        }

        slides.add(slideIn);
        fullDimCellSlidesCnt.add(fullDimCellSlideInCnt);
    }

    /**
     * @param slideTuples the coming slide
     * @param itr         current slideId
     * @description calculate net change, ָfingerprint is calculated in this function
     */
    public void calcNetChange(ArrayList<Tuple> slideTuples, int itr) {
        this.indexingSlide(slideTuples);

        removeExpiredExternalData();
        /* Slide out */
        if (itr > Constants.nS - 1) {
            slideOut = slides.poll();
            fullDimCellSlideOutCnt = fullDimCellSlidesCnt.poll();
        }
        slideDelta = new HashMap<>(); //sub cellID -> Change Number

        /* Update sub-dimension window */
        for (Integer key : slideIn.keySet()) {
            if (!windowCnt.containsKey(key)) {
                windowCnt.put(key, 0);
                idxDecoder.put(key, slideIn.get(key).cellIdx);// TODO
            }
            windowCnt.put(key, windowCnt.get(key) + slideIn.get(key).getNumTuples());
            slideDelta.put(key, slideIn.get(key).getNumTuples());
        }

        for (Integer key : slideOut.keySet()) {
            windowCnt.put(key, windowCnt.get(key) - slideOut.get(key).getNumTuples());
            if (windowCnt.get(key) < 1) {
                windowCnt.remove(key);
            }

            if (slideDelta.containsKey(key)) {
                slideDelta.put(key, slideDelta.get(key) - slideOut.get(key).getNumTuples());
            } else {
                slideDelta.put(key, slideOut.get(key).getNumTuples() * -1);
            }
        }

        /* Update full dim cell window count */
        for (Integer key : fullDimCellSlideInCnt.keySet()) {
            if (!fullDimCellWindowCnt.containsKey(key)) {
                fullDimCellWindowCnt.put(key, 0);
            }
            fullDimCellWindowCnt.put(key, fullDimCellWindowCnt.get(key) + fullDimCellSlideInCnt.get(key));

            ArrayList<Double> fingerprint = convertShortToDouble(idxDecoder.get(key));
            if (!this.fullCellDelta.containsKey(fingerprint)) {
                this.fullCellDelta.put(fingerprint, 0);
            }
            this.fullCellDelta.put(fingerprint,
                    this.fullCellDelta.get(fingerprint) + fullDimCellSlideInCnt.get(key));
        }

        for (Integer key : fullDimCellSlideOutCnt.keySet()) {
            fullDimCellWindowCnt.put(key, fullDimCellWindowCnt.get(key) - fullDimCellSlideOutCnt.get(key));
            ArrayList<Double> fingerprint = convertShortToDouble(idxDecoder.get(key));
            if (!this.fullCellDelta.containsKey(fingerprint)) {
                this.fullCellDelta.put(fingerprint, 0);
            }
            this.fullCellDelta.put(fingerprint,
                    this.fullCellDelta.get(fingerprint) - fullDimCellSlideOutCnt.get(key));
            if (fullDimCellWindowCnt.get(key) < 1) {
                fullDimCellWindowCnt.remove(key);
                this.fullCellDelta.put(fingerprint, Integer.MIN_VALUE);
            }
        }
    }

    public void processOutliers() {
        System.out.printf("Thead %d processOutliers. \n", Thread.currentThread().getId());
        // after receive external data, we need to process outliers once again
        Iterator<Tuple> it = outliers.iterator();
        OutlierLoop:
        while (it.hasNext()) {
            Tuple outlier = it.next();
            ArrayList<Double> tmp = convertShortToDouble(outlier.fullDimCellIdx);
            if (status.get(tmp) == 2) {
                it.remove();
                continue OutlierLoop;
            }

            HashMap<Integer, HashMap<List<Double>, List<Vector>>> candNeighborByTime = new HashMap<>();
            if (status.get(tmp) == 1) {
                // undetermined point:
                // add up all points from the cell that is less than 3/2R, if small, then outlier
                // if large than K, then calculate distance concretely，not update nn，update unSafeout，safeout，add points to unSafeOutNeighbors，
                // last_calculated_time
                int sumOfNeighbor = 0;
                int sumOfNN = 0;
                //public Map<Integer, Map<ArrayList<?>, List<Vector>>> externalData;
                for (int time : this.externalData.keySet()) {
                    HashMap<List<Double>, List<Vector>> candNeighbor = new HashMap<>();
                    Map<List<Double>, List<Vector>> x = this.externalData.get(time);
                    for (List<Double> cellId : x.keySet()) {
                        double[] cellCenter = new double[Constants.dim];
                        for (int j = 0; j < Constants.dim; j++) {
                            cellCenter[j] = minValues[j] + cellId.get(j) * dimLength[j] + dimLength[j] / 2;
                        }
                        if (neighboringTupleSet(outlier.value, cellCenter, 1.5 * Constants.R)) {

                            if (listComparison(outlier.fullDimCellIdx, cellId)) {
                                sumOfNN += x.get(cellId).size();
                            } else {
                                if (!candNeighbor.containsKey(cellId)) {
                                    candNeighbor.put(cellId, new ArrayList<>());
                                }
                                candNeighbor.get(cellId).addAll(x.get(cellId));
                            }
                            sumOfNeighbor += x.get(cellId).size();
                        }
                    }
                    candNeighborByTime.put(time, candNeighbor);
                }
                if (sumOfNeighbor < Constants.K) {
                    continue OutlierLoop;
                }

                //calculate distance concretely
                int need = Constants.K - outlier.getNN() - sumOfNN;
                if (outlier.last_calculate_time == -1 || outlier.last_calculate_time <= Constants.currentSlideID - Constants.nS) {
                    outlier.last_calculate_time = Constants.currentSlideID - Constants.nS + 1;
                }
                while (outlier.last_calculate_time <= Constants.currentSlideID) {
                    HashMap<List<Double>, List<Vector>> candiNeighbors = candNeighborByTime.get(outlier.last_calculate_time);
                    for (List<Double> cellId : candiNeighbors.keySet()) {
                        List<Vector> cluster = candiNeighbors.get(cellId);
                        for (Vector v : cluster) {
                            if (neighboringTupleSet(v.values, outlier.value, Constants.R)) {
                                if (v.slideID < outlier.slideID) {
                                    outlier.nnUnSafeOut++;
                                    if (!outlier.unSafeOutNeighbors.containsKey(v.slideID)) {
                                        outlier.unSafeOutNeighbors.put(v.slideID, 0);
                                    }
                                    outlier.unSafeOutNeighbors.put(v.slideID, outlier.unSafeOutNeighbors.get(v.slideID) + 1);
                                } else {
                                    outlier.nnSafeOut++;
                                }
                                need--;
                            }
                        }
                    }
                    outlier.last_calculate_time++;
                    if (need <= 0) {
                        continue OutlierLoop;
                    }
                }
            }
        }
        this.outlierVector = outliers;
    }

    public void removeExpiredExternalData() {
        //public Map<Integer, Map<ArrayList<?>, List<Vector>>> externalData;
        //remove expired external data
        externalData.remove(Constants.currentSlideID - Constants.nS);

        Iterator<Integer> it_time = externalData.keySet().iterator();
        while (it_time.hasNext()) {
            Integer time = it_time.next();
            Map<List<Double>, List<Vector>> clusters = externalData.get(time);
            Iterator<List<Double>> it_cluster = clusters.keySet().iterator();
            while (it_cluster.hasNext()) {
                List<Double> key = it_cluster.next();
                clusters.get(key).removeIf(v -> v.slideID <= Constants.currentSlideID - Constants.nS);
                if (clusters.get(key).size() == 0) {
                    it_cluster.remove();
                }
            }
            if (externalData.get(time).size() == 0) {
                it_time.remove();
            }
        }
    }

    @Override
    //TODO: need to check whether transferFullIdToSubId() is right
    public Map<List<Double>, List<Vector>> sendData(Set<List<Double>> bucketIds, int lastSent) {
        Map<List<Double>, List<Vector>> data = new HashMap<>();
        for (int time = lastSent + 1; time <= Constants.currentSlideID; time++) {
            int index = time - Constants.currentSlideID + Constants.nS - 1;
            for (List<Double> id : bucketIds) {
                int n = idxEncoder.get(transferFullIdToSubId(id));
                Cell fullCell = slides.get(index).get(n).fullCells.get(id);
                ArrayList<Vector> tuples = new ArrayList<>(fullCell.tuples);
                if (!data.containsKey(id)) {
                    data.put(id, new ArrayList<>());
                }
                data.get(id).addAll(tuples);
            }
        }
        return data;
    }

    //TODO: check correctness
    public ArrayList<Short> transferFullIdToSubId(List<Double> fullId) {
        ArrayList<Short> subId = new ArrayList<Short>();
        for (int i = 0; i < Constants.subDim; i++) {
            Short id = (short) (Math.sqrt(Constants.subDim * 1.0 / Constants.dim) * (fullId.get(i) + minValues[i]) - minValues[i]);
            subId.add(id);
        }
        return subId;
    }

    /**
     * get all neighbor cells of the changed cell, except inlier cells
     */
    public void getInfCellIndices() {
        influencedCells = new HashSet<Integer>();
        for (Integer cellIdxWin : windowCnt.keySet()) {
            //verify if inlier cell
            if (!subDimFlag && windowCnt.get(cellIdxWin) > Constants.K) {
                continue;
            }
            for (Integer cellIdxSld : slideDelta.keySet()) {
                if (this.neighboringSet(idxDecoder.get(cellIdxWin), idxDecoder.get(cellIdxSld))) {
                    if (!influencedCells.contains(cellIdxWin)) {
                        influencedCells.add(cellIdxWin);
                    }
                    break;
                }
            }
        }
    }

    public ArrayList<Integer> getSortedCandidateCellIndices(Integer cellIdxInf) {
        ArrayList<Integer> candidateCellIndices = new ArrayList<Integer>();

        HashMap<Double, HashSet<Integer>> candidateCellIndicesMap = new HashMap<Double, HashSet<Integer>>();
        //key: distance between cell and neighbor, value: cell indices
        for (Integer cellIdxWin : windowCnt.keySet()) {
            double dist = neighboringSetDist(idxDecoder.get(cellIdxInf), idxDecoder.get(cellIdxWin));

            if (dist < neighCellIdxDist) {
                if (!candidateCellIndicesMap.containsKey(dist))
                    candidateCellIndicesMap.put(dist, new HashSet<Integer>());
                candidateCellIndicesMap.get(dist).add(cellIdxWin);
            }
        }

        Object[] keys = candidateCellIndicesMap.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            candidateCellIndices.addAll(candidateCellIndicesMap.get(key));
            for (Integer cellIdxWin : candidateCellIndicesMap.get(key)) {
                candidateCellsTupleCnt += windowCnt.get(cellIdxWin);
            }
        }

        return candidateCellIndices;
    }

    public void findOutlier(String type, int itr) {
        // Remove expired or outdated outliers
        Iterator<Tuple> it = outliers.iterator();
        while (it.hasNext()) {
            Tuple outlier = it.next();
            if (outlier.slideID <= Constants.currentSlideID - Constants.nS) {
                it.remove();
            } else if (fullDimCellWindowCnt.get(idxEncoder.get(outlier.fullDimCellIdx)) > Constants.K) {
                // Remove outliers that are in inlier cells
                it.remove();
            }
        }

        if (Objects.equals(type, "NAIVE"))
            this.findOutlierNaive();
        else if (Objects.equals(type, "NETS"))
            this.findOutlierNETS(itr);
    }

    //TODO: baseline to find outliers
    public void findOutlierNaive() {
        HashSet<Tuple> allTuples = new HashSet<Tuple>();
        for (HashMap<Integer, Cell> slide : slides) {
            for (Cell cell : slide.values()) {
                allTuples.addAll(cell.tuples);
            }
        }
        outliers.clear();

        for (Tuple candTuple : allTuples) {
            boolean outlierFlag = true;
            candTuple.nn = 0;
            for (Tuple otherTuple : allTuples) {
                if ((candTuple.id != otherTuple.id) && (neighboringTuple(candTuple, otherTuple, Constants.R))) {
                    candTuple.nn += 1;
                }
                if (candTuple.nn >= Constants.K) {
                    outlierFlag = false;
                    break;
                }
            }
            if (outlierFlag) outliers.add(candTuple);
        }
    }

    public void findOutlierNETS(int itr) {
        // Will not return inlier cells and not influenced cells
        getInfCellIndices();

        // for each influenced sub cell
        InfCellLoop:
        for (Integer infCellIdx : influencedCells) {
            //find neighbor cells
            candidateCellsTupleCnt = 0;
            ArrayList<Integer> candCellIndices = getSortedCandidateCellIndices(infCellIdx);
            //verify if outlier cell
            if (candidateCellsTupleCnt < Constants.K + 1) {
                for (HashMap<Integer, Cell> slide : slides) {
                    if (!slide.containsKey(infCellIdx)) continue;
                    outliers.addAll(slide.get(infCellIdx).tuples);
                }
                continue InfCellLoop;
            }

            //For each non-determined influenced cells, we check tuples one by one
            HashSet<Tuple> candOutlierTuples = new HashSet<Tuple>();
            for (HashMap<Integer, Cell> slide : slides) {
                if (!slide.containsKey(infCellIdx)) continue;
                for (Tuple t : slide.get(infCellIdx).tuples) {
                    if (t.safeness) {
                        continue;
                    }
                    t.nnIn = fullDimCellWindowCnt.get(idxEncoder.get(t.fullDimCellIdx)) - 1;
                    t.removeOutdatedNNUnsafeOut(itr, Constants.nS);
                    if (t.getNN() < Constants.K) {
                        candOutlierTuples.add(t);
                    } else if (outliers.contains(t)) {
                        outliers.remove(t);
                    }
                }
            }

            //for each non-determined tuple, we check its neighbors
            TupleLoop:
            for (Tuple tCand : candOutlierTuples) {
                Iterator<HashMap<Integer, Cell>> slideIterator = slides.descendingIterator();//�����µ�slide��ʼ
                int currentSlideID = itr + 1;

                //we check by the order of the slide, newest first
                SlideLoop:
                while (slideIterator.hasNext()) {
                    HashMap<Integer, Cell> currentSlide = slideIterator.next();
                    currentSlideID--;
                    if (tCand.unSafeOutNeighbors.containsKey(currentSlideID)) {
                        continue SlideLoop;
                    } else {
                        tCand.unSafeOutNeighbors.put(currentSlideID, 0);
                    }

                    //only check candidate cells
                    CellLoop:
                    for (Integer otherCellIdx : candCellIndices) {
                        if (!currentSlide.containsKey(otherCellIdx)
                                || !neighboringTupleSet(tCand.value, currentSlide.get(otherCellIdx).cellCenter, 1.5 * Constants.R))
                            continue CellLoop;

                        HashSet<Tuple> otherTuples = new HashSet<Tuple>();
                        if (subDimFlag) {
                            //reduce search space using sub-dims
                            for (Cell allIdxCell : currentSlide.get(otherCellIdx).fullCells.values()) {
                                if (!allIdxCell.cellIdx.equals(tCand.fullDimCellIdx)
                                        && neighboringTupleSet(tCand.value, allIdxCell.cellCenter, 1.5 * Constants.R))//TODO: CHECK һ���ܲ���������
                                    otherTuples.addAll(allIdxCell.tuples);
                            }
                        } else {
                            otherTuples = currentSlide.get(otherCellIdx).tuples;
                        }

                        for (Tuple tOther : otherTuples) {
                            if (neighboringTuple(tCand, tOther, Constants.R)) {
                                if (tCand.slideID <= tOther.slideID) {
                                    tCand.nnSafeOut += 1;
                                } else {
                                    tCand.nnUnSafeOut += 1;
                                    tCand.unSafeOutNeighbors.put(currentSlideID, tCand.unSafeOutNeighbors.get(currentSlideID) + 1);
                                }
                                if (tCand.nnSafeOut >= Constants.K) {
                                    if (outliers.contains(tCand)) outliers.remove(tCand);
                                    tCand.safeness = true;
                                    continue TupleLoop;
                                }
                            }
                        }
                    }
                    if (tCand.getNN() >= Constants.K) {
                        if (outliers.contains(tCand)) outliers.remove(tCand);
                        continue TupleLoop;
                    }
                }
                outliers.add(tCand);
            }
        }
    }

    public boolean neighboringTuple(Tuple t1, Tuple t2, double threshold) {
        double ss = 0;
        threshold *= threshold;
        for (int i = 0; i < Constants.dim; i++) {
            ss += Math.pow((t1.value[i] - t2.value[i]), 2);
            if (ss > threshold) return false;
        }
        return true;
    }

    public boolean neighboringTupleSet(double[] v1, double[] v2, double threshold) {

        double ss = 0;
        threshold *= threshold;
        for (int i = 0; i < v2.length; i++) {
            ss += Math.pow((v1[i] - v2[i]), 2);
            if (ss > threshold) return false;
        }
        return true;
    }

    public boolean neighboringTupleSet(List<Double> v1, double[] v2, double threshold) {

        double ss = 0;
        threshold *= threshold;
        for (int i = 0; i < v2.length; i++) {
            ss += Math.pow((v1.get(i) - v2[i]), 2);
            if (ss > threshold) return false;
        }
        return true;
    }

    public double neighboringSetDist(ArrayList<Short> c1, ArrayList<Short> c2) {
        double ss = 0;
        double cellIdxDist = (c1.size() == Constants.dim ? neighCellFullDimIdxDist : neighCellIdxDist);
        double threshold = cellIdxDist * cellIdxDist;
        for (int k = 0; k < c1.size(); k++) {
            ss += Math.pow((c1.get(k) - c2.get(k)), 2);
            if (ss >= threshold) return Double.MAX_VALUE;
        }
        return Math.sqrt(ss);
    }

    /**
     * @description check if two cells are less far from 2R
     */
    public boolean neighboringSet(ArrayList<Short> c1, ArrayList<Short> c2) {
        double ss = 0;
        double cellIdxDist = (c1.size() == Constants.dim ? neighCellFullDimIdxDist : neighCellIdxDist);
        double threshold = cellIdxDist * cellIdxDist;
        for (int k = 0; k < c1.size(); k++) {
            ss += Math.pow((c1.get(k) - c2.get(k)), 2);
            if (ss >= threshold) return false;
        }
        return true;
    }

    public boolean listComparison(List<Short> shortList, List<Double> doubleList){
        for (int i = 0; i<shortList.size();i++){
            double tmp = doubleList.get(i);
            if (shortList.get(i) != (short) tmp ){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Double> convertShortToDouble(ArrayList<Short> shortArrayList){
        ArrayList<Double> fingerprint = new ArrayList<>();
        shortArrayList.forEach(idx -> fingerprint.add((double)idx));
        return fingerprint;
    }
}
