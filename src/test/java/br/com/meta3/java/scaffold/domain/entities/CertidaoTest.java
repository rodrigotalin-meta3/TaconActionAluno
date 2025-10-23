package br.com.meta3.java.scaffold.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Certidao embeddable entity:
 * - Ensures that all fields are normalized to uppercase as per legacy behavior.
 * - Verifies that toString() output contains the expected uppercase field values.
 */
public class CertidaoTest {

    @Test
    void testUppercaseFields() {
        Certidao cert = new Certidao();
        cert.setNumeroCertidao("abc123");
        cert.setLivroCertidao("livro");
        cert.setFolhaCertidao("folha");
        cert.setMatriculaNascimento("mat123");

        // Expect fields to be stored in uppercase, matching legacy functionality
        assertEquals("ABC123", cert.getNumeroCertidao());
        assertEquals("LIVRO", cert.getLivroCertidao());
        assertEquals("FOLHA", cert.getFolhaCertidao());
        assertEquals("MAT123", cert.getMatriculaNascimento());
    }

    @Test
    void testToStringIncludesUppercaseValues() {
        Certidao cert = new Certidao();
        cert.setNumeroCertidao("num");
        cert.setLivroCertidao("liv");
        cert.setFolhaCertidao("fol");
        cert.setMatriculaNascimento("mat");

        String result = cert.toString();

        // toString should reflect the uppercase-normalized field values
        assertTrue(result.contains("numerodacertidao=NUM"), 
            "toString should contain uppercase numeroCertidao");
        assertTrue(result.contains("livrodacertidao=LIV"), 
            "toString should contain uppercase livroCertidao");
        assertTrue(result.contains("folhadacertidao=FOL"), 
            "toString should contain uppercase folhaCertidao");
        assertTrue(result.contains("matriculanascimento=MAT"), 
            "toString should contain uppercase matriculaNascimento");
    }
}
