package DataStructure;

import RPC.Vector;
import utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class MCO extends Vector {
    //全部点
    public boolean isInFilledCluster;
    public boolean isCenter;
    public MCO center;
    public int ev; //记录min数量的最早preceding的exp的slide ID , 如果safe了记为0
    public int last_calculate_time; //指的是slideID

    public ArrayList<Integer> exps;
    public int numberOfSucceeding;

    public LinkedList<Vector> succeeding =new LinkedList<>();


    public void updateSucceeding(Vector mco){
        if (mco.values.equals(this.values)){
            int a =1;
        }
        numberOfSucceeding++;
        succeeding.add(mco);
    }
    public MCO(Vector d) {
        super();
        this.slideID = Constants.currentSlideID;
        this.arrivalTime = d.arrivalTime;
        this.values = d.values;
        center = null;
//            Rmc = new ArrayList<>();
        isInFilledCluster = false;
        isCenter = false;
        ev = 0;
        last_calculate_time = -1;
        exps = new ArrayList<>();
        numberOfSucceeding = 0;
    }
}
