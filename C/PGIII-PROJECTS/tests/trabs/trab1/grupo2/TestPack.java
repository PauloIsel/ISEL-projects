package trabs.trab1.grupo2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestPack {
    @Test
    public void testoString() {
        Pack p1 = new Pack( "P2", 2, 3 );
        assertEquals( "P2: €6.0 (3 units)",  p1.toString() );
    }

    @Test
    public void testSameName() {
        Pack p1 = new Pack( "P2", 2, 3 );
        assertTrue( p1.sameName("P2") );
        assertTrue( p1.sameName(new String("P2")) );
    }

    @Test
    public void testPrice() {
        Pack p1 = new Pack( "P2", 2, 3 );
        assertEquals(600, (int)p1.getPrice()*100);
    }

}
