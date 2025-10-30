package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.entities.Arquivo;
import br.com.meta3.java.scaffold.domain.repositories.ArquivoRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ArquivoRepository using JPA EntityManager and JPQL.
 */
@Repository
public class ArquivoRepositoryImpl implements ArquivoRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Arquivo> listBySchoolAndDateRange(Long codigoEscola, LocalDate inicialData, LocalDate finalData) {
        // Convert LocalDate range to LocalDateTime for inclusive filtering on timestamp column
        LocalDateTime startDateTime = inicialData.atStartOfDay();
        // inclusive end of day: set to 23:59:59.999999999
        LocalDateTime endDateTime = finalData.atTime(23, 59, 59, 999_999_999);

        String jpql = "SELECT a FROM Arquivo a " +
                      "WHERE a.codigoEscola = :codigoEscola " +
                      "  AND a.dataUpload BETWEEN :startDateTime AND :endDateTime";

        TypedQuery<Arquivo> query = em.createQuery(jpql, Arquivo.class);
        query.setParameter("codigoEscola", codigoEscola);
        query.setParameter("startDateTime", startDateTime);
        query.setParameter("endDateTime", endDateTime);

        return query.getResultList();
    }
}
