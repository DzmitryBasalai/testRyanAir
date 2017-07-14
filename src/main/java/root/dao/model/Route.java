package root.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Route implements Serializable {
    private String airportFrom;
    private String airportTo;
    private String newRoute;
    private String seasonalRoute;

    public String getAirportFrom() {
        return airportFrom;
    }

    public void setAirportFrom(String airportFrom) {
        this.airportFrom = airportFrom;
    }

    public String getAirportTo() {
        return airportTo;
    }

    public void setAirportTo(String airportTo) {
        this.airportTo = airportTo;
    }

    public String getNewRoute() {
        return newRoute;
    }

    public void setNewRoute(String newRoute) {
        this.newRoute = newRoute;
    }

    public String getSeasonalRoute() {
        return seasonalRoute;
    }

    public void setSeasonalRoute(String seasonalRoute) {
        this.seasonalRoute = seasonalRoute;
    }
}
