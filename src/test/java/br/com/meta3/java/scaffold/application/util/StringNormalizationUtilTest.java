package br.com.meta3.java.scaffold.application.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringNormalizationUtil character normalization logic.
 * Covers:
 *  - null input
 *  - empty string
 *  - accented character replacement
 *  - apostrophe removal
 *  - mixed cases
 */
public class StringNormalizationUtilTest {

    @Test
    void testNormalize_NullInput() {
        // Expect null input to return null
        assertNull(StringNormalizationUtil.normalize(null),
                "normalize(null) should return null");
    }

    @Test
    void testNormalize_EmptyString() {
        // Empty string should be returned unchanged
        assertEquals("",
                StringNormalizationUtil.normalize(""),
                "normalize(\"\") should return empty string");
    }

    @Test
    void testNormalize_AccentedCharacters() {
        // Legacy mappings: Ç->C, ÁÀÃÂÄ->A, ÉÈÊË->E, ÍÌÎÏ->I, ÓÒÕÖÔ->O, ÚÙÛÜ->U
        String input = "ÇÁÀÃÂÄÉÈÊËÍÌÎÏÓÒÕÖÔÚÙÛÜ";
        String expected = "CAAAAAEEEEIIIIOOOOOUUUU";
        assertEquals(expected,
                StringNormalizationUtil.normalize(input),
                "All accented uppercase chars should be replaced by unaccented equivalents");
    }

    @Test
    void testNormalize_ApostropheRemoval() {
        // Apostrophes should be stripped, other characters remain
        String input = "DON'T PANIC";
        String expected = "DONT PANIC";
        assertEquals(expected,
                StringNormalizationUtil.normalize(input),
                "Apostrophes should be removed but spaces preserved");
    }

    @Test
    void testNormalize_MixedContent() {
        // Mixed accents, apostrophes and normal letters
        String input = "ÇA'BÄ D'ÉF";
        // Breakdown:
        // Ç->C, Ä->A, É->E, apostrophes removed, others preserved
        String expected = "CABA DEF";
        assertEquals(expected,
                StringNormalizationUtil.normalize(input),
                "Mixed accents and apostrophes should be normalized correctly");
    }

    // TODO: (REVIEW) Consider adding tests for lowercase accented letters if utility is extended
}
