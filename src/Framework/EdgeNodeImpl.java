package Framework;

import Detector.*;
import Handler.*;
import RPC.*;
import RPC.Vector;
import org.apache.thrift.TException;
import utils.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

//    //=========================fro testing=========================
//    public static int new_center_cnt = 0;
    //========================for multiple query========================
    public int minK = Integer.MAX_VALUE;
    public int maxK = Integer.MIN_VALUE;
    public double minR = Double.MAX_VALUE;
    public double maxR = Double.MIN_VALUE;

    public EdgeNodeImpl(EdgeNode edgeNode) {
        this.belongedNode = edgeNode;
        this.unitsStatusMap = new ConcurrentHashMap<>();
//        this.unitsStatusMap = Collections.synchronizedMap(new HashMap<>());
//        this.unitResultInfo = Collections.synchronizedMap(new HashMap<>());
        this.unitResultInfo = new ConcurrentHashMap<>();
        this.allData = Collections.synchronizedList(new ArrayList<>());
        this.rawData = Collections.synchronizedList(new ArrayList<>());
        this.count = new AtomicInteger(0);
        if (Constants.methodToGenerateFingerprint.contains("NETS")) {
            this.handler = new NETSHandler(this);
        } else if (Constants.methodToGenerateFingerprint.contains("MCOD")) {
            this.handler = new MCODHandler(this);
        }

        // for baseline
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

    public void receiveAndProcessFP(Map<List<Double>, Integer> fingerprints, int edgeDeviceHashCode) {
//        System.out.printf("Thead %d receiveAndProcessFP. \n", Thread.currentThread().getId());
        for (List<Double> id : fingerprints.keySet()) {

//            if (id.get(0) == 11.757){
//                int a =1;
//            }
            int delta;
//            System.out.printf("Thead %d receiveAndProcessFP1. \n", Thread.currentThread().getId());
            // cluster remove
            if (fingerprints.get(id) < Constants.threadhold) {
//                System.out.printf("Thead %d receiveAndProcessFP2. \n", Thread.currentThread().getId());
                delta = fingerprints.get(id) - Constants.threadhold;

//                unitsStatusMap.get(id).updateCount(delta);
//                unitsStatusMap.get(id).belongedDevices.remove(edgeDeviceHashCode);
//                if (unitsStatusMap.get(id).belongedDevices.isEmpty()) {
//                    unitsStatusMap.remove(id);
//                }

                unitsStatusMap.compute(id, (key, value) -> {
                    value.updateCount(delta);
                    value.belongedDevices.remove(edgeDeviceHashCode);
                    //替换到while flag一开始的地方
//                    if (value.belongedDevices.isEmpty()) {
//                        unitsStatusMap.remove(id);
//                    }
                    return value;
                });

            }else {
                delta = fingerprints.get(id);
//                if (!unitsStatusMap.containsKey(id)) {
//                    unitsStatusMap.put(id, new UnitInNode(id, 0));
//                }
//                unitsStatusMap.get(id).updateCount(delta);
//                unitsStatusMap.get(id).belongedDevices.add(edgeDeviceHashCode);
//                System.out.print("1");

                unitsStatusMap.compute(id, (key, value) -> {
                    if (value == null) {
                        //for testing
//                        new_center_cnt++;

                        value = new UnitInNode(id, 0);

                    }

                    value.updateCount(delta);
                    value.belongedDevices.add(edgeDeviceHashCode);
                    return value;
                });
            }
        }
        ArrayList<Thread> threads = new ArrayList<>();
        count.incrementAndGet();
        boolean flag = count.compareAndSet(this.clientsForDevices.size(), 0);
        if (flag) {
            Iterator<Map.Entry<List<Double>, UnitInNode>> iterator = unitsStatusMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<List<Double>, UnitInNode> entry = iterator.next();
                UnitInNode unitInNode = entry.getValue();
                if (unitInNode.belongedDevices.isEmpty()) {
                    iterator.remove();
                }
            }

            unitResultInfo.clear();
            // node has finished collecting data, entering into the N-N phase, only one thread go into this loop
            this.flag = true; //indicate to other nodes I am ready
            for (UnitInNode unitInNode : unitsStatusMap.values()) {
                unitInNode.updateSafeness();
            }

            //自己有的clients汇总答案信息
            List<List<Double>> unSafeUnits =
                    unitsStatusMap.keySet().stream().filter(key -> unitsStatusMap.get(key).isSafe != 2).toList();
            for (List<Double> unsafeUnit : unSafeUnits) {
                List<UnitInNode> unitInNodeList = unitsStatusMap.values().stream()
                        .filter(x -> this.handler.neighboringSet(unsafeUnit, x.unitID)).toList();
                if (!unitResultInfo.containsKey(unsafeUnit)) {
                    unitResultInfo.put(unsafeUnit, Collections.synchronizedList(new ArrayList<>()));
                }
//                System.out.printf("Thead %d: Node is ready4.\n",Thread.currentThread().getId());
                unitInNodeList.forEach(
                        x -> {
                            UnitInNode unitInNode = new UnitInNode(x);
                            unitResultInfo.get(unsafeUnit).add(unitInNode);
                        }
                );
//                if (unsafeUnit.get(0) == 11.757){
//                    int a =1;
//                }
//                System.out.printf("Thead %d: Node is ready5.\n",Thread.currentThread().getId());
            }

            for (int edgeNodeCode : this.clientsForEdgeNodes.keySet()) {
                while (!EdgeNodeNetwork.nodeHashMap.get(edgeNodeCode).handler.flag) {
                    // Waiting for flag to be set
                }

                Thread t = new Thread(() -> {
                    try {
                        Map<List<Double>, List<UnitInNode>> result = this.clientsForEdgeNodes.get(edgeNodeCode).provideNeighborsResult(unSafeUnits, this.belongedNode.hashCode());
                        for (List<Double> unitID : result.keySet()) {
                            List<UnitInNode> unitInNodeList = result.get(unitID);
                            unitResultInfo.compute(unitID, (key, value) -> {
                                if (value == null) {
                                    return Collections.synchronizedList(unitInNodeList);
                                } else {
                                    unitInNodeList.forEach(x -> {
                                        for (UnitInNode unitInNode : value) {
                                            if (unitInNode.unitID.equals(x.unitID)) {
                                                unitInNode.updateCount(x.pointCnt);
                                                unitInNode.belongedDevices.addAll(x.belongedDevices);
                                                return;
                                            }
                                        }
                                        UnitInNode unitInNode = new UnitInNode(x);
                                        value.add(unitInNode);
                                    });
                                    return value;
                                }
                            });
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


//            //node - node
//            for (int edgeNodeCode : this.clientsForEdgeNodes.keySet()) {
//                while (!EdgeNodeNetwork.nodeHashMap.get(edgeNodeCode).handler.flag) {
//                }
////                List<List<Double>> finalUnSafeUnits = unSafeUnits;
//                Thread t = new Thread(() -> {
//                    try {
//                        Map<List<Double>, List<UnitInNode>> result = this.clientsForEdgeNodes.get(edgeNodeCode).provideNeighborsResult(unSafeUnits, this.belongedNode.hashCode());
////                        System.out.printf("Thead %d processResult. \n", Thread.currentThread().getId());
//                        for (List<Double> unitID : result.keySet()) {
////                            if (unitID.get(0) ==331.0 && Constants.currentSlideID == 20){
////                                int a = 1;
////                            }
//                            List<UnitInNode> unitInNodeList = result.get(unitID);
//                            if (!unitResultInfo.containsKey(unitID)) {
//                                unitResultInfo.put(unitID, Collections.synchronizedList(unitInNodeList));
//                                continue;
//                            }
//                            // todo: 偶尔有并发错误，但好像换成concurrentMap之后好了
//                            unitInNodeList.forEach(
//                                    x -> {
//                                        // todo: 再check一下并发错误
//                                        for (UnitInNode unitInNode : unitResultInfo.get(unitID)) {
//                                            if (unitInNode.unitID.equals(x.unitID)) {
////                                                if (unitInNode.unitID.get(0) ==331.0 && Constants.currentSlideID == 20){
////                                                    int a = 1;
////                                                }
//                                                unitInNode.updateCount(x.pointCnt);
//                                                unitInNode.belongedDevices.addAll(x.belongedDevices);
//                                                return;
//                                            }
//                                        }
//                                        UnitInNode unitInNode = new UnitInNode(x);
//                                        unitResultInfo.get(unitID).add(unitInNode);
//                                    }
//                            );
//                            if (unitID.get(0) ==6.9106){
//                                int a = 1;
//                            }
//                        }
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                });
//                threads.add(t);
//                t.start();
//            }
//            for (Thread t : threads) {
//                try {
//                    t.join();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            //Pruning Phase
            pruning();
            //send result back to the belonged device;
            sendDeviceResult();
        }
    }

//    public void pruning(int stage) {
//        //update UnitInNode update
////        System.out.printf("Thead %d: pruning.\n",Thread.currentThread().getId());
//        for (List<Double> UnitID : unitResultInfo.keySet()) {
//            if (UnitID.get(0) == 331.0 && Constants.currentSlideID == 20){
//                int a =1;
//            }
//            //add up all point count
//            List<UnitInNode> list = unitResultInfo.get(UnitID);
//            Optional<UnitInNode> exist = list.stream().filter(x -> x.unitID.equals(UnitID) && (x.pointCnt > Constants.K)).findAny();
//            if (exist.isPresent()) {
//                unitsStatusMap.get(UnitID).isSafe = 2;
//            }
//            if (stage == 2) {
//                int totalCnt = list.stream().mapToInt(x -> x.pointCnt).sum();
//                if (totalCnt <= Constants.K) {
//                    unitsStatusMap.get(UnitID).isSafe = 0;
//                }
//            }
//        }
//    }

    public void pruning() {
//        System.out.printf("Thead %d: pruning.\n",Thread.currentThread().getId());
        for (List<Double> UnitID : unitResultInfo.keySet()) {
            List<UnitInNode> list = unitResultInfo.get(UnitID);
            //add up all point count
            Optional<UnitInNode> exist = list.stream().filter(x -> x.unitID.equals(UnitID) && (x.pointCnt > Constants.K)).findAny();
//            if (UnitID.get(0) == 434.0 && Constants.currentSlideID == 20){
//                int a =1;
//            }
            if (exist.isPresent()) {
                unitsStatusMap.get(UnitID).isSafe = 2;
            }
            int totalCnt = list.stream().mapToInt(x -> x.pointCnt).sum();
            if (totalCnt <= Constants.K) {
                unitsStatusMap.get(UnitID).isSafe = 0;
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
        for (List<Double> unit : unSateUnits) {
            if(unit.get(0) == 8.674 && Constants.currentSlideID == 20)
            {
                int a=1;
            }
            List<UnitInNode> unitInNodeList = unitsStatusMap.values().stream()
                    .filter(x -> this.handler.neighboringSet(unit, x.unitID)).toList();
            result.put(unit, unitInNodeList);
        }
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
    public volatile boolean ready = false;
    @Override
    public Set<Vector> uploadAndDetectOutlier(List<Vector> data) throws InvalidException, TException {
//        System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 361");
        rawData.addAll(data);
        dataSize.addAndGet(data.size());
        allData.addAll(data);
        count.incrementAndGet();
//        System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 366");
        boolean localFlag = count.compareAndSet(Constants.dn, 0);
        // wait for all nodes to finish uploading && current slide after first window
        if (localFlag) {
//            System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 370");
            this.ready = true;
            ArrayList<Thread> threads = new ArrayList<>();
            for (int edgeNodeCode : EdgeNodeNetwork.nodeHashMap.keySet()) {
                if (edgeNodeCode == this.belongedNode.hashCode()) continue;
                Thread thread = new Thread(() -> {
                    try {
//                        System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 377");
                        List<Vector> eachData = clientsForEdgeNodes.get(edgeNodeCode).sendAllNodeData();
//                        System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 379");
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
//            System.out.println("Thead " + Thread.currentThread().getId() + " uploadAndDetectOutlier 394");
            if (Constants.currentSlideID >= Constants.nS - 1) {
                System.out.println("Each node get data size is " + (dataSize.get()));
                dataSize.set(0);
            }
            this.detector.detectOutlier(allData);
            if (Constants.currentSlideID >= Constants.nS - 1){
                result = new HashSet<>(this.detector.outlierVector);
            }
            else result = new HashSet<>();
            this.flag = true; // 同步device
            allData.clear();
            rawData.clear();
        }
        while (!this.flag){
        }
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
//                    if (unitInNode.unitID.get(0) == 434.0 && Constants.currentSlideID == 20){
//                        int a =1;
//                    }
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
//                    int a = 1;
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
//        System.out.println("Thead " + Thread.currentThread().getId() + " sendAllNodeData 457");
        while(!this.ready){
        }
        return new ArrayList<>(this.rawData);
    }

    public double synchronizeRK(){
        // 1.等待所有自己的device上传RK，找到local的 minR minK maxR maxK
        // 2.调用其他node的sendRK, 找到global的 minR minK maxR maxK
        // 3.储存并返回minR给自己的各device
        return 0;
    }

    public ArrayList<Double> sendRK(){
        //返回自己的minR minK maxR maxK
        return null;
    }
}
