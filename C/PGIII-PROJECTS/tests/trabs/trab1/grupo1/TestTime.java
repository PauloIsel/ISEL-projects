package trabs.trab1.grupo1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestTime {
    @Test
    public void testConstructsHourMinutes( ) {
        Time t = new Time(4,50);
        assertEquals(4, t.getHours());
        assertEquals(50, t.getMinutes());
        assertEquals(0, t.getSeconds());
    }

    @Test
    public void testConstruct( ) {
        Time t = new Time();
        assertEquals(0, t.getHours());
        assertEquals(0, t.getMinutes());
        assertEquals(0, t.getSeconds());
    }
    @Test
    public void testConstructsHourMinutesSeconds( ) {
        Time t = new Time(5,25, 35);
        assertEquals(5, t.getHours());
        assertEquals(25, t.getMinutes());
        assertEquals(35, t.getSeconds());
    }
    @Test
    public void testConstructsSeconds( ) {
        Time t = new Time(4*3600+50*60+34);
        assertEquals(4, t.getHours());
        assertEquals(50, t.getMinutes());
        assertEquals(34, t.getSeconds());
    }
    @Test
    public void testToTimeOnlyHours( ) {
        Time t = Time.toTime("14");
        assertEquals(14, t.getHours());
        assertEquals(0, t.getMinutes());
        assertEquals(0, t.getSeconds());
    }
    @Test
    public void testToTimeHoursMinutes( ) {
        Time t = Time.toTime("04:50");
        assertEquals(4, t.getHours());
        assertEquals(50, t.getMinutes());
        assertEquals(0, t.getSeconds());
    }
    @Test
    public void testToTimeHoursMinutesSeconds( ) {
        Time t = Time.toTime("04:50:31");
        assertEquals(4, t.getHours());
        assertEquals(50, t.getMinutes());
        assertEquals(31, t.getSeconds());
    }
    @Test
    public void testEquals( ) {
        Time t1 = new Time(4, 50, 30);
        assertTrue(t1.equals(t1));
        Time t2 = new Time(4,50, 30);
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));

        assertFalse(t1.equals(null));
        assertFalse(t1.equals(new Time(4,5, 30)));
        assertFalse(t1.equals(new Time(5,50, 30)));
        assertFalse(t1.equals(new Time(4,50, 31)));
        assertEquals(t1, new Time(4,50, 30));
    }

    @Test
    public void testCompareTo( ) {
        Time t1 = new Time(5, 5, 5);
        assertEquals(0, t1.compareTo(t1));
        assertEquals(0, t1.compareTo(new Time(5,5, 5)));
        Time t2 = new Time(4,5, 5);
        assertTrue(t1.compareTo(t2)>0);
        assertTrue(t2.compareTo(t1) < 0);
        t2 = new Time(5,4, 5);
        assertTrue(t1.compareTo(t2)>0);
        assertTrue(t2.compareTo(t1) < 0);

        t2 = new Time(5,5, 4);
        assertTrue(t1.compareTo(t2)>0);
        assertTrue(t2.compareTo(t1) < 0);
    }

    @Test
    public void testplusMinute( ) {
        Time t1 = new Time(22, 58, 58);
        assertEquals(new Time(22,59, 58), t1=t1.plusMinute());
        assertEquals(new Time(23,0, 58), t1.plusMinute());
        assertEquals(new Time(22,59,58), t1);
        t1 = new Time(23, 59, 10);
        assertEquals(new Time(0,0, 10), t1.plusMinute());
        assertEquals(new Time(23,59,10), t1);
    }

    @Test
    public void testplusSeconds( ) {
        Time t1 = new Time(22, 58, 8);
        assertEquals(new Time(22,58, 58), t1.plusSeconds(50));
        assertEquals(new Time(23,0, 8), t1.plusSeconds(120));
        assertEquals(new Time(22,58,8), t1);
        t1 = new Time(23, 59, 10);
        assertEquals(new Time(0,0, 10), t1.plusSeconds(60));
        assertEquals(new Time(23,59,10), t1);
    }

    @Test
    public void testMinus( ) {
        Time t1 = new Time(22, 58, 30);
        assertEquals(0, t1.minus( t1 ));
        assertEquals(60, t1.plusMinute().minus( t1 ));
        assertEquals(60*60+2, t1.minus(new Time(21,58, 28)));
        t1 = new Time(23, 59);
        assertEquals((60*24-1)*60, t1.minus(new Time(0)));
    }

    @Test
    public void testGetMaxTime( ) {
        Time t1 = new Time(22, 56, 30);
        Time t2 = new Time(22, 58, 30);
        Time t3 = new Time(23, 58, 31);

        assertEquals(null, Time.getMaxTime());

        assertEquals(t1, Time.getMaxTime(t1));

        assertEquals(t2, Time.getMaxTime(t1, t2));
        assertEquals(t3, Time.getMaxTime(t3, t2));

        assertEquals(t3, Time.getMaxTime(t1, t2, t3));
        assertEquals(t3, Time.getMaxTime(t3, t2, t1));
        assertEquals(t3, Time.getMaxTime(t1, t3, t2));
        assertEquals(t3, Time.getMaxTime(t3, t1, t2));
        assertEquals(t3, Time.getMaxTime(t2, t3, t1));
        assertEquals(t3, Time.getMaxTime(t2, t1, t3));
    }

    @Test
    public void testToString(){
        assertEquals("00:00:00", new Time().toString());
        assertEquals("04:50:00", new Time(4,50).toString());
        assertEquals("23:05:10", new Time(23,5, 10).toString());
        assertEquals("00:05:08", new Time(0,5, 8).toString());
    }
}
