package trabs.trab1.grupo2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSale {
    @Test
    public void testoString() {
        Product p1 = new Product( "P1", 4 );
        Reduction r = new Sale(p1, 50);
        assertEquals( "P1: €4.0",  r.getItemBase().toString() );
        assertEquals( 50,  r.getPercentage() );
        assertEquals( "P1: €4.0 com desconto de 50%: €2.0",  r.toString() );
    }

    @Test
    public void testSameName() {
        Sale s = new Sale(new Product( "P1", 4 ), 50);
        assertTrue( s.sameName("desconto de 50%") );
        s = new Sale(s, 25);
        assertTrue( s.sameName("desconto de 75%") );

    }

    @Test
    public void testToOtherSale() {
        Sale s = new Sale(new Product( "P1", 4 ), 50);
        assertEquals( "P1: €4.0 com desconto de 50%: €2.0",  s.toString() );
        Reduction r = new Sale(s, 25);
        assertEquals( "P1: €4.0 com desconto de 75%: €1.0",  r.toString() );
        assertEquals( "P1: €4.0",  r.getItemBase().toString() );
        assertEquals( 75,  r.getPercentage() );
    }
    @Test
    public void testPrice() {
        Item s = new Sale(new Product( "P1", 4 ), 50);
        assertEquals(200, (int)s.getPrice()*100);
    }

}
