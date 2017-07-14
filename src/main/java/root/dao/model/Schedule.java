package root.dao.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule implements Serializable {
    private int month;
    private Days[] days;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Days[] getDays() {
        return days;
    }

    public void setDays(Days[] days) {
        this.days = days;
    }


}
