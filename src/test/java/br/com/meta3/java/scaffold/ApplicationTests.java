package br.com.meta3.java.scaffold;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import br.com.meta3.java.scaffold.domain.entities.Aluno;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    /**
     * Unit test for verifying the series (serie) getter and setter in Aluno entity.
     */
    @Test
    void testGetSerie() {
        // Arrange: create Aluno and set a known series value
        Aluno aluno = new Aluno();
        String expectedSerie = "3A";
        aluno.setSerie(expectedSerie);

        // Act: retrieve the series via getter
        String actualSerie = aluno.getSerie();

        // Assert: ensure the getter returns exactly what was set
        assertEquals(expectedSerie, actualSerie, "getSerie() should return the value set by setSerie()");
    }

    // TODO: (REVIEW) Add more unit tests for other getters/setters and business logic in Aluno
}
