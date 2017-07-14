package root.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import root.dao.model.Route;
import root.dao.model.Schedule;
import java.util.Arrays;
import java.util.List;

@Component
public class FlightsInfoDaoImpl implements FlightsInfoDao {
    private static final String ROUTES_URL = "https://api.ryanair.com/core/3/routes";
    private static final String SCHEDULE_URL = "https://api.ryanair.com/timetable/3/schedules/{departure}/{arrival}/years/{year}/months/{month}";
    private RestTemplate restTemplate = new RestTemplate();

    private List<Route> routeList;

    public FlightsInfoDaoImpl() {
        try {
            Route[] route = restTemplate.getForObject(ROUTES_URL, Route[].class);
            routeList = Arrays.asList(route);
        }
        catch (Exception ex){
            routeList = null;
        }
    }

    public List<Route> getRoutesList(){
        return routeList;
    }

    public Schedule getSchedule(String departure, String arrival, int year, int month){
        Schedule schedule;
        try {
            String scheduleResponse = restTemplate.getForObject(SCHEDULE_URL, String.class, departure, arrival, year, month);
            ObjectMapper objectMapper = new ObjectMapper();
            schedule = objectMapper.readValue(scheduleResponse, Schedule.class);
        } catch (Exception ex) {
            ex.printStackTrace();
           return null;
        }

        return schedule;

    }
}
