package br.com.meta3.java.scaffold.domain.services;

import br.com.meta3.java.scaffold.domain.entities.Arquivo;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Arquivo related operations.
 * Defines business operations for listing loaded Arquivo records
 * by school code and date range.
 */
public interface ArquivoService {

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

    // TODO: (IMPLEMENT) Provide the implementation of this interface in
    //       src/main/java/br/com/meta3/java/scaffold/application/services/
}
