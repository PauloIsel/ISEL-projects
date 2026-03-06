package trabs.trab1.grupo1;

public class Time {
    private final int hour, minute, second;

    public Time(int h, int m, int s) {
        this.hour = h;
        this.minute = m;
        this.second = s;
    }

    public Time() {
        this(0,0,0);
    }

    public Time(int h, int m) {
        this(h, m, 0);
    }

    public Time(int svalue) {
        this((svalue / 3600) % 24, (svalue % 3600) / 60, svalue % 60);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Time time)) return false;
        return hour == time.hour && minute == time.minute && second == time.second;
    }

    public int getHours() {
        return this.hour;
    }

    public int getMinutes() {
        return this.minute;
    }

    public int getSeconds() {
        return this.second;
    }

    public int compareTo(Time other) {
        int h = this.hour - other.hour;

        if (h == 0) {
            int m = this.minute - other.minute;

            if (m == 0) {
                return this.second - other.second;
            }

            return m;
        }

        return h;
    }

    public String toString () {
        return String.format("%02d:%02d:%02d", this.hour, this.minute, this.second);
    }

    public Time plusMinute() {
        int h = this.hour;
        int m = this.minute;
        m++;

        if(m == 60) {
            h++;
            m = 0;
            if(h == 24) h = 0;
        }

        return new Time(h, m, this.second);
    }

    private int totalSeconds() {
        return (this.hour * 3600) + (this.minute * 60) + this.second;
    }

    public Time plusSeconds(int s) {
        return new Time(this.totalSeconds() + s);
    }

    public int minus(Time t) {
        return Math.abs(this.totalSeconds() - t.totalSeconds());
    }

    public static Time toTime(String str) {
        int hour, minute = 0, second = 0;
        String[] parts = str.split(":");

        hour = Integer.parseInt(parts[0]);

        if (parts.length > 1) {
            minute = Integer.parseInt(parts[1]);

            if(parts.length > 2) {
                second = Integer.parseInt(parts[2]);
            }
        }

        return new Time(hour, minute, second);
    }

    public static Time getMaxTime(Time... times) {
        if(times == null || times.length == 0) return null;
        Time MaxTime = times[0];

        for(Time time : times) {
            if(time.compareTo(MaxTime) > 0) {
                MaxTime = time;
            }
        }

        return MaxTime;
    }
}
