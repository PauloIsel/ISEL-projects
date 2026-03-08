package trabs.trab1.grupo2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestGroup {
    @Test
    public void testoString() {
        Product p1 = new Product( "P1", 2 );
        Pack p2 = new Pack( "P2", 2, 3 );
        Group g = new Group( "G1", p1, p2);
        assertEquals( """
           G1: €8.0
             P1: €2.0
             P2: €6.0 (3 units)""",  g.toString() );
    }

    @Test
    public void testSameName() {
        Product p1 = new Product( "P1", 2 );
        Pack p2 = new Pack( "P2", 2, 3 );
        Group g = new Group( "G1", p1, p2);
        assertTrue( g.sameName("G1") );
    }

    @Test
    public void testPrice() {
        Product p1 = new Product( "P1", 2 );
        Pack p2 = new Pack( "P2", 2, 3 );
        Group g = new Group( "G1", p1, p2);
        assertEquals(800, (int)g.getPrice()*100);
    }

    @Test
    public void testGroups() {
        Product p1 = new Product( "P1", 2 );
        Pack p2 = new Pack( "P2", 2, 3 );
        Group g1 = new Group( "G1", p1, p2);
        Group g2 = new Group("G2", new Product("P3", 5 ), g1 );
        assertEquals( """
          G2: €13.0
            P3: €5.0
            G1: €8.0
              P1: €2.0
              P2: €6.0 (3 units)""",  g2.toString() );

    }

    @Test
    public void testPutInSale() {
        Product p1 = new Product( "P1", 2 );
        Pack p2 = new Pack( "P2", 2, 3 );
        Group g = new Group( "G1", p1, p2);
        g.putInSale("P1", 50);
        assertEquals(700, (int)g.getPrice()*100);
        assertEquals( """
           G1: €7.0
             P1: €2.0 com desconto de 50%: €1.0
             P2: €6.0 (3 units)""",  g.toString() );

    }

}
