package root.dao;

import root.dao.model.Route;
import root.dao.model.Schedule;
import java.util.List;

public interface FlightsInfoDao {
    List<Route> getRoutesList();
    Schedule getSchedule(String departure, String arrival, int year, int month);
}
