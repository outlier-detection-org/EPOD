package Framework;

import Detector.*;
import Handler.*;
import RPC.*;
import RPC.Vector;
import org.apache.thrift.TException;
import utils.Constants;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EdgeNodeImpl implements EdgeNodeService.Iface {
    public EdgeNode belongedNode;
    public Map<List<Double>, List<UnitInNode>> unitResultInfo; //primly used for pruning
    public Map<List<Double>, UnitInNode> unitsStatusMap; // used to maintain the status of the unit in a node
    public Handler handler;
    public Map<Integer, EdgeNodeService.Client> clientsForEdgeNodes;
    public Map<Integer, DeviceService.Client> clientsForDevices;
    /* this two fields are used for judge whether the node has complete uploading*/
    public AtomicInteger count;
    volatile Boolean flag = false;

    //=============================Centralize===============================
    public Detector detector;
    public List<Vector> allData;
    public List<Vector> rawData;

    public EdgeNodeImpl(EdgeNode edgeNode) {
        this.belongedNode = edgeNode;
        this.unitsStatusMap = Collections.synchronizedMap(new HashMap<>());
        this.unitResultInfo = Collections.synchronizedMap(new HashMap<>());
        this.allData = Collections.synchronizedList(new ArrayList<>());
        this.rawData = Collections.synchronizedList(new ArrayList<>());
        this.count = new AtomicInteger(0);
        if (Constants.methodToGenerateFingerprint.contains("NETS")) {
            this.handler = new NETSHandler(this);
        } else if (Constants.methodToGenerateFingerprint.contains("MCOD")) {
            this.handler = new MCODHandler(this);
        }

        if (Objects.equals(Constants.methodToGenerateFingerprint, "NETS_CENTRALIZE")){
            this.detector = new NewNETS(0);
        }else if (Objects.equals(Constants.methodToGenerateFingerprint, "MCOD_CENTRALIZE")){
            this.detector = new MCOD();
        }
    }

    public void setClients(Map<Integer, EdgeNodeService.Client> clientsForEdgeNodes, Map<Integer, DeviceService.Client> clientsForDevices) {
        this.clientsForDevices = clientsForDevices;
        this.clientsForEdgeNodes = clientsForEdgeNodes;
    }

    //该接口，多传 Map<List<Double>, List<Integer,Integer>>
    //500/5000  threadhold  threadhold + delta

    public void receiveAndProcessFP(Map<List<Double>, Integer> fingerprints, int edgeDeviceHashCode) {
//        System.out.printf("Thead %d receiveAndProcessFP. \n", Thread.currentThread().getId());
        for (List<Double> id : fingerprints.keySet()) {
            if (id.get(0) ==331.0){
                int a = 1;
            }
            int delta;
//            System.out.printf("Thead %d receiveAndProcessFP1. \n", Thread.currentThread().getId());
            if (fingerprints.get(id) < Constants.threadhold) {
//                System.out.printf("Thead %d receiveAndProcessFP2. \n", Thread.currentThread().getId());
                delta = fingerprints.get(id) - Constants.threadhold;
                unitsStatusMap.get(id).updateCount(delta);
                unitsStatusMap.get(id).updateDeltaCount(delta);
                unitsStatusMap.get(id).update();
                unitsStatusMap.get(id).belongedDevices.remove(edgeDeviceHashCode);
//                if (unitsStatusMap.get(id).belongedDevices.isEmpty()) {
//                    unitsStatusMap.remove(id);
//                    unitResultInfo.remove(id);
//                }
            }else {
                delta = fingerprints.get(id);
                if (!unitsStatusMap.containsKey(id)) {
                    unitsStatusMap.put(id, new UnitInNode(id, 0));
                }
                unitsStatusMap.get(id).updateCount(delta);
                unitsStatusMap.get(id).updateDeltaCount(delta);
                unitsStatusMap.get(id).update();
                unitsStatusMap.get(id).belongedDevices.add(edgeDeviceHashCode);
                System.out.print("1");
            }
        }
        ArrayList<Thread> threads = new ArrayList<>();
        count.incrementAndGet();
        boolean flag = count.compareAndSet(this.clientsForDevices.size(), 0);
        if (flag) {
            // node has finished collecting data, entering into the N-N phase, only one thread go into this loop
            this.flag = true; //indicate to other nodes I am ready
            for (UnitInNode unitInNode : unitsStatusMap.values()) {
                unitInNode.updateSafeness();
            }
            // �����unSafeUnits��������ЩunSafeUnits, ������Ҫ�ӱ���豸��Ѱ���ھ�
            List<List<Double>> unSafeUnits =
                    unitsStatusMap.keySet().stream().filter(key -> unitsStatusMap.get(key).isSafe != 2).toList();
            // ��һ�׶�: ������ͬһ��node�µ�����device�����ھ�, ���������
            HashSet<UnitInNode> needUpdate = new HashSet<>();
            for (List<Double> unsafeUnit : unSafeUnits) {
                List<UnitInNode> unitInNodeList = unitsStatusMap.values().stream().filter(x -> x.isUpdated.get(this.belongedNode.hashCode()) == 1)
                        .filter(x -> this.handler.neighboringSet(unsafeUnit, x.unitID)).toList();
//                unitInNodeList.forEach(x -> x.isUpdated.put(this.belongedNode.hashCode(), 0));//�����в�ͬ��unsafeUnit, Ҫһ���Ը��� @shimin
                needUpdate.addAll(unitInNodeList);
                if (!unitResultInfo.containsKey(unsafeUnit)) {
                    unitResultInfo.put(unsafeUnit, new ArrayList<>());
                }
//                System.out.printf("Thead %d: Node is ready4.\n",Thread.currentThread().getId());
                unitInNodeList.forEach(
                        x -> {
                            for (UnitInNode unitInNode : unitResultInfo.get(unsafeUnit)) {
                                if (unitInNode.unitID.equals(x.unitID)) {
                                    unitInNode.updateCount(x.deltaCnt);
                                    unitInNode.belongedDevices.addAll(x.belongedDevices);
                                    return;
                                }
                            }
                            UnitInNode unitInNode = new UnitInNode(x);
                            unitResultInfo.get(unsafeUnit).add(unitInNode);//TODO: check��ǳ����������
                        }
                );
//                System.out.printf("Thead %d: Node is ready5.\n",Thread.currentThread().getId());
            }
            needUpdate.forEach(x -> x.isUpdated.put(this.belongedNode.hashCode(), 0));
            pruning(1);
            unSafeUnits = unitsStatusMap.keySet().stream().filter(key -> unitsStatusMap.get(key).isSafe != 2).toList();
            //node - node
            for (int edgeNodeCode : this.clientsForEdgeNodes.keySet()) {
                while (!EdgeNodeNetwork.nodeHashMap.get(edgeNodeCode).handler.flag) {
                }
                List<List<Double>> finalUnSafeUnits = unSafeUnits;
                Thread t = new Thread(() -> {
                    try {
                        Map<List<Double>, List<UnitInNode>> result = this.clientsForEdgeNodes.get(edgeNodeCode).provideNeighborsResult(finalUnSafeUnits, this.belongedNode.hashCode());
//                        System.out.printf("Thead %d processResult. \n", Thread.currentThread().getId());
                        for (List<Double> unitID : result.keySet()) {
                            if (unitID.get(0) ==331.0 && Constants.currentSlideID == 20){
                                int a = 1;
                            }
                            List<UnitInNode> unitInNodeList = result.get(unitID);
                            if (!unitResultInfo.containsKey(unitID)) {
                                unitResultInfo.put(unitID, unitInNodeList);
                                return;
                            }
                            unitInNodeList.forEach(
                                    x -> {
                                        for (UnitInNode unitInNode : unitResultInfo.get(unitID)) {
                                            if (unitInNode.unitID.equals(x.unitID)) {
                                                if (unitInNode.unitID.get(0) ==331.0 && Constants.currentSlideID == 20){
                                                    int a = 1;
                                                }
                                                unitInNode.updateCount(x.deltaCnt);
                                                unitInNode.belongedDevices.addAll(x.belongedDevices);
                                                return;
                                            }
                                        }
                                        UnitInNode unitInNode = new UnitInNode(x);
                                        unitResultInfo.get(unitID).add(unitInNode);
                                    }
                            );
                            if (unitID.get(0) ==331.0 && Constants.currentSlideID == 20){
                                int a = 1;
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
                threads.add(t);
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //Pruning Phase
            pruning(2);
            //send result back to the belonged device;
            sendDeviceResult();
        }
    }

    public void pruning(int stage) {
        //update UnitInNode update
//        System.out.printf("Thead %d: pruning.\n",Thread.currentThread().getId());
        for (List<Double> UnitID : unitResultInfo.keySet()) {
            if (UnitID.get(0) == 331.0 && Constants.currentSlideID == 20){
                int a =1;
            }
            //add up all point count
            List<UnitInNode> list = unitResultInfo.get(UnitID);
            //ͬһ��cell�ĵ�������k����ô���cell����safe��
            Optional<UnitInNode> exist = list.stream().filter(x -> x.unitID.equals(UnitID) && (x.pointCnt > Constants.K)).findAny();
            if (exist.isPresent()) {
                unitsStatusMap.get(UnitID).isSafe = 2;
            }
            if (stage == 2) {
                int totalCnt = list.stream().mapToInt(x -> x.pointCnt).sum();
                if (totalCnt <= Constants.K) {
                    unitsStatusMap.get(UnitID).isSafe = 0;
                }
            }

        }
    }

    /**
     * @param unSateUnits:  units need to find neighbors in this node
     * @param edgeNodeHash: from which node
     * @description find whether there are neighbor unit in local node
     */
    @Override
    public Map<List<Double>, List<UnitInNode>> provideNeighborsResult(List<List<Double>> unSateUnits, int edgeNodeHash) {
//        System.out.printf("Thead %d provideNeighborsResult. \n", Thread.currentThread().getId());
        Map<List<Double>, List<UnitInNode>> result = new HashMap<>();
        HashSet<UnitInNode> needUpdate = new HashSet<>();
        for (List<Double> unit : unSateUnits) {
            if(unit.get(0) == 6.9762 && Constants.currentSlideID == 19)
            {
                int a=1;
            }
            List<UnitInNode> unitInNodeList = unitsStatusMap.values().stream()
                    .filter(x -> x.isUpdated.get(edgeNodeHash) == 1)
                    .filter(x -> this.handler.neighboringSet(unit, x.unitID)).toList();
            needUpdate.addAll(unitInNodeList);
            result.put(unit, unitInNodeList);
        }
        needUpdate.forEach(x -> x.isUpdated.put(edgeNodeHash, 0));
        return result;
    }

    public Set<Vector> result;
    AtomicInteger dataSize = new AtomicInteger(0);
    /*
    public Set<Vector> uploadAndDetectOutlier(List<Vector> data) throws InvalidException, TException {
//        System.out.printf("Thead %d uploadAndDetectOutlier.\n", Thread.currentThread().getId() );
        allData.addAll(data);
        count.incrementAndGet();
        boolean flag = count.compareAndSet(Constants.dn * Constants.nn, 0);
        // wait for all nodes to finish uploading && current slide after first window
        if (flag) {
            if (Constants.currentSlideID == 0||Constants.currentSlideID >= Constants.nS) {
                dataSize = new AtomicInteger(0);
            }
            dataSize.addAndGet(allData.size());
            if (Constants.currentSlideID >= Constants.nS - 1) {
                System.out.println("Each device get data size is " + dataSize);
            }
//            System.out.printf("Thead %d all nodes have finished uploading data.\n", Thread.currentThread().getId() );
            this.detector.detectOutlier(allData);
            if (Constants.currentSlideID >= Constants.nS - 1){
                result = new HashSet<>(this.detector.outlierVector);
            }
            else result = new HashSet<>();
            this.flag = true;
        }
        while (!this.flag){
        }
        allData.clear();
        return result;
    }*/
    public boolean ready = false;
    @Override
    public Set<Vector> uploadAndDetectOutlier(List<Vector> data) throws InvalidException, TException {
        rawData.addAll(data);
        dataSize.addAndGet(data.size());
        allData.addAll(data);
        count.incrementAndGet();
        boolean localFlag = count.compareAndSet(Constants.dn, 0);
        // wait for all nodes to finish uploading && current slide after first window
        if (localFlag) {
            this.ready = true;
            ArrayList<Thread> threads = new ArrayList<>();
            for (int edgeNodeCode : EdgeNodeNetwork.nodeHashMap.keySet()) {
                if (edgeNodeCode == this.belongedNode.hashCode()) continue;
                Thread thread = new Thread(() -> {
                    try {
                        List<Vector> eachData = clientsForEdgeNodes.get(edgeNodeCode).sendAllNodeData();
                        allData.addAll(eachData);
                        dataSize.addAndGet(eachData.size());
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                threads.add(thread);
            }
            for (Thread t: threads){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (Constants.currentSlideID >= Constants.nS - 1) {
                System.out.println("Each node get data size is " + (dataSize.get()));
                dataSize.set(0);
            }
            this.detector.detectOutlier(allData);
            if (Constants.currentSlideID >= Constants.nS - 1){
                result = new HashSet<>(this.detector.outlierVector);
            }
            else result = new HashSet<>();
            this.flag = true;
        }
        while (!this.flag){
        }
        allData.clear();
        rawData.clear();
        return result;
    }

    public void sendDeviceResult() {
        for (Integer edgeDeviceCode : this.clientsForDevices.keySet()) {
            Thread t = new Thread(() -> {
                List<UnitInNode> list = unitsStatusMap.values().stream().filter(
                        x -> x.belongedDevices.contains(edgeDeviceCode)).toList(); // ���device��ǰ�е�����unit
                HashMap<List<Double>, Integer> status = new HashMap<>();
                for (UnitInNode i : list) {
                    status.put(i.unitID, i.isSafe);
                }
                list = unitsStatusMap.values().stream().filter(
                        x -> (x.belongedDevices.contains(edgeDeviceCode) && (x.isSafe == 1))).toList();
                HashMap<Integer, Set<List<Double>>> result = new HashMap<>();
                //deviceHashCode: unitID
                for (UnitInNode unitInNode : list) {
                    unitResultInfo.get(unitInNode.unitID).forEach(
                            x -> {
                                Set<Integer> deviceList = x.belongedDevices;
                                for (Integer y : deviceList) {
                                    if (!result.containsKey(y)) {
                                        result.put(y, new HashSet<>());
                                    }
                                    result.get(y).add(x.unitID);
                                }
                            }
                    );
                }
                try {
                    this.clientsForDevices.get(edgeDeviceCode).getExternalData(status, result);
                } catch (TException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }
    }

    @Override
    public List<Vector> sendAllNodeData() throws InvalidException, TException {
        while(!this.ready){
        }
        return new ArrayList<>(this.rawData);
    }

}
