package alexvetter.timetrackr.utils;

import android.text.Spanned;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputFilterMinMaxTest {

    private static final String VALID = null;
    private static final String INVALID = "";

    private Spanned createEmptySpanned() {
        return createSpanned("");
    }

    private Spanned createSpanned(String content) {
        Spanned spanned = mock(Spanned.class);
        when(spanned.toString()).thenReturn(content);
        return spanned;
    }

    @Test
    public void testFilter() throws Exception {
        InputFilterMinMax filter = new InputFilterMinMax(1, 12);

        assertEquals(VALID, filter.filter("1", 0, 1, createEmptySpanned(), 0, 0));
        assertEquals(VALID, filter.filter("5", 0, 1, createEmptySpanned(), 0, 0));
        assertEquals(VALID, filter.filter("12", 0, 1, createEmptySpanned(), 0, 0));

        assertEquals(INVALID, filter.filter("0", 0, 1, createEmptySpanned(), 0, 0));
        assertEquals(INVALID, filter.filter("13", 0, 1, createEmptySpanned(), 0, 0));
    }

    @Test
    public void testFilterWithInvalidNumber() {
        InputFilterMinMax filter = new InputFilterMinMax(1, 12);

        try {
            filter.filter("a", 1, 1, createSpanned("6"), 1, 1);
            fail("Shouldn't happen");
        } catch (IllegalArgumentException nfe) {
            assertEquals(NumberFormatException.class, nfe.getCause().getClass());
        } catch (Exception e) {
            fail("Should be a NumberFormatException");
        }

        try {
            filter.filter("1.", 1, 1, createSpanned("0"), 1, 1);
            fail("Shouldn't happen");
        } catch (IllegalArgumentException nfe) {
            assertEquals(NumberFormatException.class, nfe.getCause().getClass());
        } catch (Exception e) {
            fail("Should be a NumberFormatException");
        }
    }

    @Test
    public void testFilterWithInvalidRange() {
        try {
            new InputFilterMinMax(13, 12);
            fail("Max must be greater than Min.");
        } catch(IllegalArgumentException e) {
            //
        }
    }
}