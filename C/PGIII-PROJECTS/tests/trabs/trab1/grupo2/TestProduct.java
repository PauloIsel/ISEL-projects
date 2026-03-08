package trabs.trab1.grupo2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestProduct {
    @Test
    public void testoString() {
        Product p1 = new Product( "P1", 2 );
        assertEquals( "P1: €2.0",  p1.toString() );
    }

    @Test
    public void testSameName() {
        Product p1 = new Product( "P1", 2 );
        assertTrue( p1.sameName("P1") );
        assertTrue( p1.sameName(new String("P1")) );
        assertFalse( p1.sameName(new String("F1")) );
        assertFalse( p1.sameName(new String("P2")) );
    }

    @Test
    public void testPrice() {
        Product p1 = new Product( "P1", 2 );
        assertEquals(200, (int)p1.getPrice()*100);
    }

}
