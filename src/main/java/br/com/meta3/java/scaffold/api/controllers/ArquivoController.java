package br.com.meta3.java.scaffold.api.controllers;

import br.com.meta3.java.scaffold.api.dtos.ArquivoSecSmecResponse;
import br.com.meta3.java.scaffold.api.dtos.AlunoRgResponse;
import br.com.meta3.java.scaffold.domain.services.ArquivoService;
import br.com.meta3.java.scaffold.domain.entities.ArquivoSecSmec;
import br.com.meta3.java.scaffold.domain.entities.AlunoRgInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller exposing endpoints for:
 *  - listing files sent by SEC/SMEC
 *  - listing students older than 10 without RG
 *
 * Migration note:
 * - Previously this controller depended on inner DTO types declared inside the service implementation
 *   (ArquivoServiceImpl.ArquivoSecSmec and ArquivoServiceImpl.AlunoRgInfo). That creates a tight coupling
 *   to the implementation class. We now depend on domain-level entities:
 *     br.com.meta3.java.scaffold.domain.entities.ArquivoSecSmec
 *     br.com.meta3.java.scaffold.domain.entities.AlunoRgInfo
 *
 * TODO: (REVIEW) If domain entities evolve to use different field types (e.g., dates as LocalDate),
 * update mappings here to format them into strings expected by the API DTOs.
 */
@RestController
public class ArquivoController {

    private final ArquivoService arquivoService;

    public ArquivoController(ArquivoService arquivoService) {
        this.arquivoService = arquivoService;
    }

    /**
     * GET  /arquivos/sec-smec : List files sent by SEC/SMEC for a given month and year.
     *
     * @param mes            month in MM format
     * @param anoBase        year as YYYY
     * @param codigoTitular  '9999' for SEC, otherwise SMEC
     * @return list of file records matching criteria
     */
    @GetMapping("/arquivos/sec-smec")
    public ResponseEntity<List<ArquivoSecSmecResponse>> getArquivosSecSmec(
            @RequestParam("mes") String mes,
            @RequestParam("anoBase") String anoBase,
            @RequestParam("codigoTitular") String codigoTitular) {

        // Use domain-level ArquivoSecSmec returned by the service
        List<ArquivoSecSmec> arquivos = arquivoService.listarArquivosEnviadosSecSmec(mes, anoBase, codigoTitular);

        // Map domain entities to API response DTOs
        List<ArquivoSecSmecResponse> responseList = arquivos.stream()
                .map(a -> new ArquivoSecSmecResponse(
                        a.getNomeArquivo(),
                        a.getDataRecebimento(),
                        a.getQuantidadeAlunos()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    /**
     * GET  /alunos/sem-rg : List students older than 10 without RG.
     *
     * @param codigos        hyphen-separated list of dependent codes (e.g., "1-2-3")
     * @param codigoTitular  titular code
     * @return list of dependents missing RG
     */
    @GetMapping("/alunos/sem-rg")
    public ResponseEntity<List<AlunoRgResponse>> getAlunosSemRg(
            @RequestParam("codigos") String codigos,
            @RequestParam("codigoTitular") String codigoTitular) {

        // Use domain-level AlunoRgInfo returned by the service
        List<AlunoRgInfo> alunosInfo = arquivoService.verificaRgAlunos(codigos, codigoTitular);

        // Map domain DTOs to API response DTOs
        List<AlunoRgResponse> responseList = alunosInfo.stream()
                .map(info -> new AlunoRgResponse(
                        info.getCodDependente(),
                        info.getMatricula(),
                        info.getNomeDependente(),
                        info.getDataNascimento()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
