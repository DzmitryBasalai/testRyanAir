package root.service;

import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import root.dao.FlightsInfoDao;
import root.dao.model.*;
import root.util.DateTime;
import root.util.FlightInfo;
import root.util.Flight;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FlightsService {

    private FlightsInfoDao flightsInfoDao;
    private static final int NUMBER_OF_HOURS_FOR_TRANSFER = 2;
    private static final int ONE_DAY_IN_MINUTES = 24 * 60;

    @Autowired
    public void setFlightsInfoDao(FlightsInfoDao flightsInfoDao) {
        this.flightsInfoDao = flightsInfoDao;
    }

    private List<Route> routeList;

    public List<Flight> getFlightList(FlightInfo searchFlight) {
        List<Flight> flightList = new ArrayList<>();

        routeList = flightsInfoDao.getRoutesList();
        if (routeList != null) {
            List<Flight> directFlightsList = getDirectFlights(searchFlight);
            List<Flight> interconnectedFlightsList = getInterconnectedFlights(searchFlight);

            if (directFlightsList != null && directFlightsList.size() > 0) {
                flightList.addAll(directFlightsList);
            }
            if (interconnectedFlightsList != null && interconnectedFlightsList.size() > 0) {
                flightList.addAll(interconnectedFlightsList);
            }
        }
        return flightList;
    }

    private List<Flight> getDirectFlights(FlightInfo searchFlight) {

        List<Flight> flightList = new ArrayList<>();

        DateTime searchDateTimeDep = getDateTime(searchFlight.getDepartureDateTime());
        int searchDepYear = searchDateTimeDep.getYear();
        int searchDepMonth = searchDateTimeDep.getMonth();
        int searchDepDay = searchDateTimeDep.getDay();
        LocalTime searchDepTime = searchDateTimeDep.getTime();

        DateTime searchDateTimeArriv = getDateTime(searchFlight.getArrivalDateTime());
        int searchArrivYear = searchDateTimeArriv.getYear();
        int searchArrivMonth = searchDateTimeArriv.getMonth();
        int searchArrivDay = searchDateTimeArriv.getDay();
        LocalTime searchArrivTime = searchDateTimeArriv.getTime();

        for (Route route : routeList) {
            if (searchFlight.getDepartureAirport().equals(route.getAirportFrom()) &&
                    searchFlight.getArrivalAirPort().equals(route.getAirportTo())) {

                long searchDepDateInMinutes = dateToMinutes(searchDepYear, searchDepMonth, searchDepDay);
                long searchArrivDateInMinutes = dateToMinutes(searchArrivYear, searchArrivMonth, searchArrivDay);
                while (searchArrivDateInMinutes >= searchDepDateInMinutes) {
                    Date d = new Date(searchDepDateInMinutes * 60000);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int yyyy = cal.get(Calendar.YEAR);
                    int mm = cal.get(Calendar.MONTH) + 1;
                    int dd = cal.get(Calendar.DAY_OF_MONTH);

                    Schedule schedule = flightsInfoDao.getSchedule(searchFlight.getDepartureAirport(),
                            searchFlight.getArrivalAirPort(), yyyy, mm);

                    if (schedule != null) {
                        for (Days day : schedule.getDays()) {
                            if (day.getDay() == dd) {
                                for (Flights flights : day.getFlights()) {
                                    long searchDateTimeDepInMinutes = dateToMinutes(searchDepYear, searchDepMonth, searchDepDay) + searchDepTime.getHourOfDay() * 60 + searchDepTime.getMinuteOfHour();
                                    long searchDateTimeArrivInMinutes = dateToMinutes(searchArrivYear, searchArrivMonth, searchArrivDay) + searchArrivTime.getHourOfDay() * 60 + searchArrivTime.getMinuteOfHour();

                                    LocalTime depTime = LocalTime.parse(flights.getDepartureTime());
                                    LocalTime arrivTime = LocalTime.parse(flights.getArrivalTime());
                                    long dateTimeDepInMinutes = dateToMinutes(yyyy, mm, dd) + depTime.getHourOfDay() * 60 + depTime.getMinuteOfHour();
                                    long dateTimeArrivInMinutes = dateToMinutes(yyyy, mm, dd) + arrivTime.getHourOfDay() * 60 + arrivTime.getMinuteOfHour();

                                    if (dateTimeDepInMinutes >= searchDateTimeDepInMinutes && dateTimeArrivInMinutes <= searchDateTimeArrivInMinutes) {

                                        FlightInfo flightInfo = new FlightInfo();
                                        flightInfo.setDepartureAirport(route.getAirportFrom());
                                        flightInfo.setArrivalAirPort(route.getAirportTo());
                                        flightInfo.setDepartureDateTime(dateTimeBuilder(yyyy, mm, dd, depTime.getHourOfDay(), depTime.getMinuteOfHour()));
                                        flightInfo.setArrivalDateTime(dateTimeBuilder(yyyy, mm, dd, arrivTime.getHourOfDay(), arrivTime.getMinuteOfHour()));

                                        List<FlightInfo> flightInfoList = new ArrayList<>();
                                        flightInfoList.add(flightInfo);

                                        Flight flight = new Flight();
                                        flight.setStop(0);
                                        flight.setLegs(flightInfoList);

                                        flightList.add(flight);
                                    }
                                }
                            }
                        }
                    }
                    searchDepDateInMinutes += ONE_DAY_IN_MINUTES;
                }
            }
        }
        return flightList;
    }

    private List<Flight> getInterconnectedFlights(FlightInfo searchFlight) {
        List<Flight> flightList = new ArrayList<>();

        DateTime searchDateTimeDep = getDateTime(searchFlight.getDepartureDateTime());
        DateTime searchDateTimeArriv = getDateTime(searchFlight.getArrivalDateTime());

        int searchDepYear = searchDateTimeDep.getYear();
        int searchDepMonth = searchDateTimeDep.getMonth();
        int searchDepDay = searchDateTimeDep.getDay();
        LocalTime searchDepTime = searchDateTimeDep.getTime();

        int searchArrivYear = searchDateTimeArriv.getYear();
        int searchArrivMonth = searchDateTimeArriv.getMonth();
        int searchArrivDay = searchDateTimeArriv.getDay();
        LocalTime searchArrivTime = searchDateTimeArriv.getTime();


        List<Route> routeList1 = new ArrayList<>();
        List<Route> routeList2 = new ArrayList<>();

        for (Route route : routeList) {
            if (route.getAirportFrom().equals(searchFlight.getDepartureAirport()) && !route.getAirportTo().equals(searchFlight.getArrivalAirPort())) {
                routeList1.add(route);
            }
            if (!route.getAirportFrom().equals(searchFlight.getDepartureAirport()) && route.getAirportTo().equals(searchFlight.getArrivalAirPort())) {
                routeList2.add(route);
            }
        }
        for (Route route1 : routeList1) {
            for (Route route2 : routeList2) {
                if (route1.getAirportTo().equals(route2.getAirportFrom())) {


                    List<Map<String, Object>> dayRoute1Flights_list = new ArrayList<>();
                    List<Map<String, Object>> dayRoute2Flights_list = new ArrayList<>();

                    long searchDepDateInMinutes = dateToMinutes(searchDepYear, searchDepMonth, searchDepDay);
                    long searchArrivDateInMinutes = dateToMinutes(searchArrivYear, searchArrivMonth, searchArrivDay);

                    while (searchArrivDateInMinutes >= searchDepDateInMinutes) {
                        Date d = new Date(searchDepDateInMinutes * 60000);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(d);
                        int searchDepYYYY = cal.get(Calendar.YEAR);
                        int searchDepMM = cal.get(Calendar.MONTH) + 1;
                        int searchDepDD = cal.get(Calendar.DAY_OF_MONTH);

                        Schedule schedule1 = flightsInfoDao.getSchedule(route1.getAirportFrom(),
                                route1.getAirportTo(), searchDepYYYY, searchDepMM);

                        if (schedule1 != null) {
                            Map<String, Object> mapRoute1Flights = new LinkedHashMap<>();
                            for (Days days : schedule1.getDays()) {
                                if (days.getDay() == searchDepDD) {
                                    mapRoute1Flights.put("route", route1);
                                    mapRoute1Flights.put("flights", days.getFlights());
                                    mapRoute1Flights.put("year", searchDepYYYY);
                                    mapRoute1Flights.put("months", searchDepMM);
                                    mapRoute1Flights.put("day", searchDepDD);
                                    dayRoute1Flights_list.add(mapRoute1Flights);
                                    break;
                                }
                            }
                        }

                        Schedule schedule2 = flightsInfoDao.getSchedule(route2.getAirportFrom(),
                                route2.getAirportTo(), searchDepYYYY, searchDepMM);
                        if (schedule2 != null) {
                            Map<String, Object> mapRoute2Flights = new LinkedHashMap<>();
                            for (Days days : schedule2.getDays()) {
                                if (days.getDay() == searchDepDD) {
                                    mapRoute2Flights.put("route", route2);
                                    mapRoute2Flights.put("flights", days.getFlights());
                                    mapRoute2Flights.put("year", searchDepYYYY);
                                    mapRoute2Flights.put("months", searchDepMM);
                                    mapRoute2Flights.put("day", searchDepDD);

                                    dayRoute2Flights_list.add(mapRoute2Flights);
                                    break;
                                }
                            }
                        }
                        searchDepDateInMinutes += ONE_DAY_IN_MINUTES;
                    }

                    if (dayRoute1Flights_list.size() < 0 || dayRoute2Flights_list.size() < 0) {
                        continue;
                    }

                    for (Map<String, Object> dayRoute1Flights : dayRoute1Flights_list) {
                        int yyyy_1 = (int) dayRoute1Flights.get("year");
                        int mm_1 = (int) dayRoute1Flights.get("months");
                        int dd_1 = (int) dayRoute1Flights.get("day");
                        Route route_1 = (Route) dayRoute1Flights.get("route");
                        Flights[] flights_1Array = (Flights[]) dayRoute1Flights.get("flights");

                        for (Flights flights1 : flights_1Array) {
                            LocalTime depTime1 = LocalTime.parse(flights1.getDepartureTime());
                            LocalTime arrivTime1 = LocalTime.parse(flights1.getArrivalTime());

                            long dateTimeDep_1_InMinutes = dateToMinutes(yyyy_1, mm_1, dd_1) + depTime1.getHourOfDay() * 60 + depTime1.getMinuteOfHour();
                            long dateTimeArriv_1_InMinutes = dateToMinutes(yyyy_1, mm_1, dd_1) + arrivTime1.getHourOfDay() * 60 + arrivTime1.getMinuteOfHour();
                            long searchDateTimeDepInMinutes = dateToMinutes(searchDepYear, searchDepMonth, searchDepDay) + searchDepTime.getHourOfDay() * 60 + searchDepTime.getMinuteOfHour();

                            if (dateTimeDep_1_InMinutes >= searchDateTimeDepInMinutes) {

                                for (Map<String, Object> dayRoute2Flights : dayRoute2Flights_list) {
                                    int yyyy_2 = (int) dayRoute2Flights.get("year");
                                    int mm_2 = (int) dayRoute2Flights.get("months");
                                    int dd_2 = (int) dayRoute2Flights.get("day");
                                    Route route_2 = (Route) dayRoute2Flights.get("route");
                                    Flights[] flights_2Array = (Flights[]) dayRoute2Flights.get("flights");

                                    for (Flights flights2 : flights_2Array) {
                                        LocalTime depTime2 = LocalTime.parse(flights2.getDepartureTime());
                                        LocalTime arrivTime2 = LocalTime.parse(flights2.getArrivalTime());

                                        long dateTimeDep_2_InMinutes = dateToMinutes(yyyy_2, mm_2, dd_2) + depTime2.getHourOfDay() * 60 + depTime2.getMinuteOfHour();
                                        long dateTimeArrive_2_InMinutes = dateToMinutes(yyyy_2, mm_2, dd_2) + arrivTime2.getHourOfDay() * 60 + arrivTime2.getMinuteOfHour();

                                        long searchDateTimeArrivInMinutes = searchArrivDateInMinutes + searchArrivTime.getHourOfDay() * 60 + searchArrivTime.getMinuteOfHour();

                                        if ((dateTimeDep_2_InMinutes - dateTimeArriv_1_InMinutes > NUMBER_OF_HOURS_FOR_TRANSFER * 60)
                                                && (searchDateTimeArrivInMinutes > dateTimeArrive_2_InMinutes)) {

                                            FlightInfo flightInfo1 = new FlightInfo();
                                            flightInfo1.setDepartureAirport(route_1.getAirportFrom());
                                            flightInfo1.setArrivalAirPort(route_1.getAirportTo());
                                            flightInfo1.setDepartureDateTime(dateTimeBuilder(yyyy_1, mm_1, dd_1, depTime1.getHourOfDay(), depTime1.getMinuteOfHour()));
                                            flightInfo1.setArrivalDateTime(dateTimeBuilder(yyyy_1, mm_1, dd_1, arrivTime1.getHourOfDay(), arrivTime1.getMinuteOfHour()));

                                            FlightInfo flightInfo2 = new FlightInfo();
                                            flightInfo2.setDepartureAirport(route_2.getAirportFrom());
                                            flightInfo2.setArrivalAirPort(route_2.getAirportTo());
                                            flightInfo2.setDepartureDateTime(dateTimeBuilder(yyyy_2, mm_2, dd_2, depTime2.getHourOfDay(), depTime2.getMinuteOfHour()));
                                            flightInfo2.setArrivalDateTime(dateTimeBuilder(yyyy_2, mm_2, dd_2, arrivTime2.getHourOfDay(), arrivTime2.getMinuteOfHour()));

                                            List<FlightInfo> flightInfoList = new ArrayList<>();
                                            flightInfoList.add(flightInfo1);
                                            flightInfoList.add(flightInfo2);

                                            Flight flight = new Flight();
                                            flight.setStop(1);
                                            flight.setLegs(flightInfoList);

                                            flightList.add(flight);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return flightList;
    }

    private DateTime getDateTime(String dateTimeStr) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date = new Date();
        try {
            date = formatter.parse(dateTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        DateTime dateTime = new DateTime();
        dateTime.setYear(cal.get(Calendar.YEAR));
        dateTime.setMonth(cal.get(Calendar.MONTH) + 1);
        dateTime.setDay(cal.get(Calendar.DAY_OF_MONTH));
        dateTime.setTime(new LocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

        return dateTime;
    }

    private String dateTimeBuilder(int year, int month, int day, int hour, int min) {
        StringBuilder dateTime = new StringBuilder();
        dateTime.append(year).append("-");
        dateTime.append(month).append("-");
        dateTime.append(day).append("T");
        dateTime.append(hour).append(":");
        dateTime.append(min);

        return dateTime.toString();
    }

    private long dateToMinutes(int year, int monts, int day) {
        long dateInMinutes;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(year + "-" + monts + "-" + day + " 00:00:00");
            dateInMinutes = date.getTime() / 60000;
        } catch (ParseException ex) {
            ex.printStackTrace();
            return 0;
        }
        return dateInMinutes;
    }


    private void testMethod(){

    }
    private void testMethod1(){

    }
}
