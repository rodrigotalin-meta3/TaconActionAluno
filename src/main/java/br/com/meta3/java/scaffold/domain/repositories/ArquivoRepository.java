package br.com.meta3.java.scaffold.domain.repositories;

import br.com.meta3.java.scaffold.domain.entities.Arquivo;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Arquivo entity.
 * Provides method to list loaded files by school code and date range.
 */
public interface ArquivoRepository {

    /**
     * Retrieves a list of Arquivo entities filtered by the given school code
     * and falling within the specified inclusive date range.
     *
     * @param codigoEscola the identifier of the school
     * @param inicialData  the start date (inclusive) of the period
     * @param finalData    the end date (inclusive) of the period
     * @return list of matching Arquivo entities
     */
    List<Arquivo> listBySchoolAndDateRange(Long codigoEscola, LocalDate inicialData, LocalDate finalData);
    
    // TODO: (IMPLEMENT) Provide Spring Data JPA or custom implementation in infrastructure layer
}
