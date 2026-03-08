package pt.isel.mpd;

import java.util.Date;

abstract class Flight {
    private static Integer flightNumber;
    private static Date departureTime;

    public static Integer getFlightNumber() {
        return flightNumber;
    }

    public static void setFlightNumber(Integer flightNumber) {
        Flight.flightNumber = flightNumber;
    }

    public static Date getDepartureTime() {
        return departureTime;
    }

    public static void setDepartureTime(Date departureTime) {
        Flight.departureTime = departureTime;
    }

    public abstract void fly();
}