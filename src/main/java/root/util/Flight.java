package root.util;

import java.util.List;

public class Flight {
    private int stop;
    private List<FlightInfo> legs;

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public List<FlightInfo> getLegs() {
        return legs;
    }

    public void setLegs(List<FlightInfo> legs) {
        this.legs = legs;
    }


}
