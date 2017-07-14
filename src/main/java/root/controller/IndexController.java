package root.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import root.service.FlightsService;
import root.util.FlightInfo;
import root.util.Flight;

import java.util.*;

@RestController
public class IndexController {

   private FlightsService flightsService;
    @Autowired
    public void setFlightsService(FlightsService flightsService){
        this.flightsService = flightsService;
    }

    @GetMapping(value = {"/interconnections"}, params = {"departure", "arrival", "departureDateTime", "arrivalDateTime"})
    public List<Flight> welcomePage(@RequestParam("departure") String departure,
                                    @RequestParam("arrival") String arrival,
                                    @RequestParam("departureDateTime") String departureDateTime,
                                    @RequestParam("arrivalDateTime") String arrivalDateTime) {

        FlightInfo searchFlight = new FlightInfo();
        searchFlight.setDepartureAirport(departure);
        searchFlight.setArrivalAirPort(arrival);
        searchFlight.setDepartureDateTime(departureDateTime);
        searchFlight.setArrivalDateTime(arrivalDateTime);

        return flightsService.getFlightList(searchFlight);
    }
}
