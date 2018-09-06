package de.hhu.bsinfo.dxutils;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void isNumeric() {
        String decimalSeperator = StringUtils.DECIMAL_SEPERATOR;

        assertTrue(StringUtils.isNumeric(String.format("1%s000%s000", decimalSeperator, decimalSeperator)));
        assertTrue(StringUtils.isNumeric(String.format("-1%s000%s000", decimalSeperator, decimalSeperator)));

        assertTrue(StringUtils.isNumeric("1"));
        assertTrue(StringUtils.isNumeric("-1"));

        assertFalse(StringUtils.isNumeric("2x"));
        assertFalse(StringUtils.isNumeric("a8"));

        assertFalse(StringUtils.isNumeric("abcd"));
        assertFalse(StringUtils.isNumeric("!?#$"));
    }
}