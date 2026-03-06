package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.spreadsheet0.exceptions.InvalidCoordinatesException;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicCalcSheetTests {

    @Test
    void testInitialSize() {
        CalcSheet sheet = new CalcSheet(3, 3);
        assertEquals(3, sheet.getRows());
        assertEquals(3, sheet.getCols());
    }

    @Test
    void testAddRow() {
        CalcSheet sheet = new CalcSheet(2, 2);
        sheet.addRow();
        assertEquals(3, sheet.getRows());
    }

    @Test
    void testRemoveRow() {
        CalcSheet sheet = new CalcSheet(3, 3);
        sheet.removeRow();
        assertEquals(2, sheet.getRows());
    }

    @Test
    void testAddColumn() {
        CalcSheet sheet = new CalcSheet(3, 3);
        sheet.addColumn();
        assertEquals(4, sheet.getCols());
    }

    @Test
    void testRemoveColumn() {
        CalcSheet sheet = new CalcSheet(3, 3);
        sheet.removeColumn();
        assertEquals(2, sheet.getCols());
    }

    @Test
    void testExpandUpToZ() {
        CalcSheet sheet = new CalcSheet(2, 25); // Até 'Y'
        sheet.addColumn(); // Deve ser 'Z'
        assertEquals(26, sheet.getCols());
    }

    @Test
    void addMoreThanZ() {
        CalcSheet sheet = new CalcSheet(2, 26); // Até 'Y'
        assertThrows(IllegalArgumentException.class, sheet::addColumn);
    }


    @Test
    void testValidCellAccess() {
        CalcSheet sheet = new CalcSheet(3, 3);
        assertNotNull(sheet.getCellAt(2, 2));
    }

    @Test
    void testInvalidCellAccess() {
        CalcSheet sheet = new CalcSheet(3, 3);
        assertThrows(InvalidCoordinatesException.class, () -> sheet.getCellAt(3, 3));
    }
}
