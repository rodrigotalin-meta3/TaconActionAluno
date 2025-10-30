package br.com.meta3.java.scaffold.api.controllers;

import br.com.meta3.java.scaffold.api.dtos.ArquivoDTO;
import br.com.meta3.java.scaffold.api.dtos.ListArquivosRequestDTO;
import br.com.meta3.java.scaffold.application.services.ArquivoService;
import br.com.meta3.java.scaffold.domain.entities.Arquivo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing Arquivo resources.
 */
@RestController
@RequestMapping("/arquivos")
@Validated
public class ArquivoController {

    private final ArquivoService arquivoService;

    public ArquivoController(ArquivoService arquivoService) {
        this.arquivoService = arquivoService;
    }

    /**
     * GET  /arquivos : List uploaded arquivos filtered by school code and date range.
     *
     * @param requestDTO the filtering criteria bound from query parameters
     * @return the list of ArquivoDTO
     */
    @GetMapping
    public List<ArquivoDTO> listArquivos(
            @Valid
            @ModelAttribute ListArquivosRequestDTO requestDTO) { 
        // Using @ModelAttribute to bind GET query params into ListArquivosRequestDTO
        List<Arquivo> arquivos = arquivoService.listBySchoolAndDateRange(
                requestDTO.getCodigoEscola(),
                requestDTO.getInicialData(),
                requestDTO.getFinalData()
        );

        // Map domain entities to DTOs for API response
        return arquivos.stream()
                .map(ArquivoDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
