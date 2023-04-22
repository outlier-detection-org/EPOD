package Framework;

import Detector.Detector;
import Detector.MCOD;
import Detector.NewNETS;
import RPC.DeviceService;
import RPC.EdgeNodeService;
import RPC.Vector;
import utils.Constants;
import utils.DataGenerator;

import java.util.*;

@SuppressWarnings("unchecked")
public class DeviceImpl implements DeviceService.Iface {

    //=============================For All===============================
    public int deviceId;
    public List<Vector> rawData = new ArrayList<>();
    public DataGenerator dataGenerator;
    public Detector detector;
    volatile public boolean ready = false;

    //=============================EPOD===============================
    public Map<List<Double>, Integer> fullCellDelta; //fingerprint
    public HashMap<Integer, Integer> historyRecord; //������¼ÿ��device���ϴη��͵���ʷ��¼��deviceID->slideID
    public Map<Integer, DeviceService.Client> clientsForDevices; //And for P2P
    public EdgeNodeService.Client clientsForNearestNode;

    //=============================P2P===============================
    public List<Vector> AllData = new ArrayList<>();

    public DeviceImpl(int deviceId) {
        this.deviceId = deviceId;
        this.dataGenerator = new DataGenerator(deviceId);
        if (Objects.equals(Constants.methodToGenerateFingerprint, "NETS")) {
            this.detector = new NewNETS(0, this);
        } else if (Objects.equals(Constants.methodToGenerateFingerprint, "MCOD")) {
            this.detector = new MCOD(this);
        }
        this.fullCellDelta = new HashMap<>();
        this.historyRecord = new HashMap<>();
        for (int deviceHashCode : EdgeNodeNetwork.deviceHashMap.keySet()) {
            this.historyRecord.put(deviceHashCode, 0);
        }
    }

    public void setClients(EdgeNodeService.Client clientsForNearestNode, Map<Integer, DeviceService.Client> clientsForDevices) {
        this.clientsForDevices = clientsForDevices;
        this.clientsForNearestNode = clientsForNearestNode;
    }

    public void getRawData(int itr) {
        Date currentRealTime = new Date();
        currentRealTime.setTime(dataGenerator.firstTimeStamp.getTime() + (long) Constants.S * 10 * 1000 * itr);
        this.rawData = dataGenerator.getTimeBasedIncomingData(currentRealTime, Constants.S * 10);
    }

    public Set<? extends Vector> detectOutlier(int itr) throws Throwable {
        this.ready = false;
        //get initial data
        Constants.currentSlideID = itr;
        getRawData(itr);

        //step1: ����ָ�� + �����ȼ���outliers
        if (itr > Constants.nS - 1) {
            clearFingerprints();
        }
        this.detector.detectOutlier(this.rawData);

        //step2: �ϴ�ָ��
        if (itr >= Constants.nS - 1) {
            this.clientsForNearestNode.receiveAndProcessFP(fullCellDelta, this.hashCode());
        } else return new HashSet<>();

        //���ػ�ȡ���� + ����outliers
        while (!this.ready) {
        }
        this.detector.processOutliers();
        return this.detector.outlierVector;
    }


    public void clearFingerprints() {
        this.fullCellDelta = new HashMap<>();
    }

    public Map<List<Double>, List<Vector>> sendData(Set<List<Double>> bucketIds, int deviceHashCode) {
        //������ʷ��¼����������
        int lastSent = Math.max(this.historyRecord.get(deviceHashCode), Constants.currentSlideID - Constants.nS);
        this.historyRecord.put(deviceHashCode, Constants.currentSlideID);
        return this.detector.sendData(bucketIds, lastSent);
    }

    public void getExternalData(Map<List<Double>, Integer> status, Map<Integer, Set<List<Double>>> result) {
        this.detector.status = status; //�����ж�outliers�Ƿ���Ҫ���¼��㣬����processOutliers()��
        ArrayList<Thread> threads = new ArrayList<>();
        for (Integer deviceCode : result.keySet()) {
            //HashMap<Integer,HashSet<ArrayList<?>>>
            Thread t = new Thread(() -> {
                try {
                    Map<List<Double>, List<Vector>> data = this.clientsForDevices.get(deviceCode).sendData(result.get(deviceCode), this.hashCode());
                    if (!this.detector.externalData.containsKey(Constants.currentSlideID)) {
                        this.detector.externalData.put(Constants.currentSlideID, Collections.synchronizedMap(new HashMap<>()));
                    }
                    Map<List<Double>, List<Vector>> map = this.detector.externalData.get(Constants.currentSlideID);//TODO: Check ����������
                    data.keySet().forEach(
                            x -> {
                                if (!map.containsKey(x)) {
                                    map.put(x, Collections.synchronizedList(new ArrayList<>()));
                                }
                                map.get(x).addAll(data.get(x));
                            }
                    );
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.ready = true;
    }

    public Set<? extends Vector> detectOutlier_P2P(int itr) throws Throwable {
        this.ready = false;
        Constants.currentSlideID = itr;
        getRawData(itr);
        //step1: �Լ�ȡdata
        if (itr > Constants.nS - 1) {
            AllData.clear();
        }
        AllData.addAll(rawData);

        //step2: �ռ�����device��data
        if (itr >= Constants.nS - 1) {
            ArrayList<Thread> threads = new ArrayList<>();
            for (DeviceService.Client client : clientsForDevices.values()) {
                Thread t = new Thread(() -> {
                    try {
                        List<Vector> data = client.sendAllLocalData();
                        AllData.addAll(data);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
                threads.add(t);
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.ready = true;
        }

        //step3: detectOutlier
        while (!this.ready) {}
        this.detector.detectOutlier(AllData);
        return this.detector.outlierVector;
    }

    //����slide�ĵ�
    @Override
    public List<Vector> sendAllLocalData() {
        return this.rawData;
    }
}