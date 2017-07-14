package root.dao.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Days implements Serializable{
    private int day;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Flights[] getFlights() {
        return flights;
    }

    public void setFlights(Flights[] flights) {
        this.flights = flights;
    }

    private Flights[] flights;
}
