package no4mat.no4mat.agenda;

import java.io.Serializable;
import java.util.Calendar;

public class AgendaEntry implements Serializable {
    int id;
    public String name;
    public String lastName;
    public String category;
    public String phoneNumber;
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;


    public AgendaEntry () {
        Calendar calendar = Calendar.getInstance();
        id = -1;
        name = "";
        lastName = "";
        category = "";
        phoneNumber = "1234567890";
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
    }

    public String getDateFormat() {
        return day + "/" + month + "/" + year;
    }

    public String getTimeFormat () {
        return hour  + ":" + minute;
    }
}
