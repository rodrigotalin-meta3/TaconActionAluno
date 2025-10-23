package br.com.meta3.java.scaffold.api.controllers;

import br.com.meta3.java.scaffold.api.dtos.AlunoRequest;
import br.com.meta3.java.scaffold.api.dtos.AlunoResponse;
import br.com.meta3.java.scaffold.api.dtos.AlunoSearchResponse;
import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.domain.services.AlunoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing Aluno operations.
 */
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    /**
     * Endpoint to create a new Aluno record.
     * Extracts client IP, invokes the domain service, and returns result.
     *
     * @param alunoRequest DTO with Aluno creation data
     * @param request      HTTP servlet request to extract client IP
     * @return AlunoResponse indicating success and reset flag
     */
    @PostMapping
    public ResponseEntity<AlunoResponse> criarAluno(
            @Valid @RequestBody AlunoRequest alunoRequest,
            HttpServletRequest request) {

        // Extract client IP address (may need adjustment behind proxies)
        String ipCliente = request.getRemoteAddr();

        // Retrieve email from AlunoRequest to pass into service
        String email = alunoRequest.getEmail();
        // TODO: (REVIEW) Consider passing additional fields (e.g., nome) if service signature is updated
        int resultado = alunoService.inserirAluno(ipCliente, "INCLUSAO", email);

        boolean sucesso = (resultado == 1);
        boolean resetar = true; // Always reset client state as in legacy flow

        AlunoResponse response = new AlunoResponse(sucesso, resetar);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a date string and age constraints, ported from legacy verificaData.
     *
     * @param data date in 'dd/MM/yyyy' format
     * @return 1 if valid; 0 otherwise
     */
    @GetMapping("/verify-date")
    public ResponseEntity<Integer> verificaData(@RequestParam("data") String data) {
        int result = alunoService.verificaData(data);
        return ResponseEntity.ok(result);
    }

    /**
     * Validate a CPF number for format and checksum, ported from legacy verificaCpf.
     *
     * @param cpf string with exactly 11 digits
     * @return true if CPF is valid; false otherwise
     */
    @GetMapping("/verify-cpf")
    public ResponseEntity<Boolean> verificaCpf(@RequestParam("cpf") String cpf) {
        boolean valid = alunoService.verificaCpf(cpf);
        return ResponseEntity.ok(valid);
    }

    /**
     * Search by codigoSetps, populating Aluno attributes.
     * Ported from legacy pesquisaCodigosetps.
     *
     * @param codigosetps  identifier of the dependent
     * @param codigoEscola titular (school) code for filtering
     * @return the same codigosetps if found; "-1" on error
     */
    @GetMapping("/{codigosetps}")
    public ResponseEntity<String> getByCodigosetps(
            @PathVariable("codigosetps") String codigosetps,
            @RequestParam("codigoEscola") String codigoEscola) {

        String result = alunoService.pesquisaCodigosetps(codigosetps, codigoEscola);
        return ResponseEntity.ok(result);
    }

    /**
     * Search students by name, optional birth date, cpf, and mother's name.
     * Now includes 'serie' and 'grau' fields in the results.
     *
     * @param nomeAluno       full or partial student name
     * @param dataNascimento  birth date filter (optional)
     * @param cpf             CPF filter (optional)
     * @param nomeMae         mother's name filter (optional)
     * @return list of matching AlunoSearchResponse DTOs including 'serie' and 'grau'
     */
    @GetMapping("/search")
    public ResponseEntity<List<AlunoSearchResponse>> searchByName(
            @RequestParam("nomeAluno") String nomeAluno,
            @RequestParam(value = "dataNascimento", required = false) String dataNascimento,
            @RequestParam(value = "cpf", required = false) String cpf,
            @RequestParam(value = "nomeMae", required = false) String nomeMae) {

        List<Aluno> lista = alunoService.pesquisarNome(nomeAluno, dataNascimento, cpf, nomeMae);
        // Map domain Aluno to API DTO, including 'serie' and 'grau'
        List<AlunoSearchResponse> responseList = lista.stream()
                .map(a -> {
                    AlunoSearchResponse dto = new AlunoSearchResponse(
                            a.getCodigoSetps(),
                            a.getMatricula(),
                            a.getNomeAluno(),
                            a.getNomeMae(),
                            a.getDataNascimento()
                    );
                    // Map series/period from domain model
                    dto.setSerie(a.getSerie());
                    // Map grade level (legacy 'grau') from domain model
                    // TODO: (REVIEW) Ensure 'grau' is populated by DAO/service layer
                    dto.setGrau(a.getGrau());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    /**
     * General search across multiple fields populating an Aluno.
     * Now includes 'serie' and 'grau' fields in the result.
     *
     * @param rg               identity RG (optional)
     * @param numeroCertidao   birth certificate number (optional)
     * @param matricula        student registration (optional)
     * @param cpf              CPF filter (optional)
     * @param nomeMae          mother's name filter (optional)
     * @param nomeAluno        student name filter (optional)
     * @param dataNascimento   birth date filter (optional)
     * @return AlunoSearchResponse DTO including 'serie' and 'grau'
     */
    @GetMapping("/search/general")
    public ResponseEntity<AlunoSearchResponse> generalSearch(
            @RequestParam(value = "rg", required = false) String rg,
            @RequestParam(value = "numeroCertidao", required = false) String numeroCertidao,
            @RequestParam(value = "matricula", required = false) String matricula,
            @RequestParam(value = "cpf", required = false) String cpf,
            @RequestParam(value = "nomeMae", required = false) String nomeMae,
            @RequestParam(value = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(value = "dataNascimento", required = false) String dataNascimento) {

        Aluno a = alunoService.pesquisarGeral(rg, numeroCertidao, matricula, cpf, nomeMae, nomeAluno, dataNascimento);
        if (a == null) {
            return ResponseEntity.ok(null);
        }
        AlunoSearchResponse dto = new AlunoSearchResponse(
                a.getCodigoSetps(),
                a.getMatricula(),
                a.getNomeAluno(),
                a.getNomeMae(),
                a.getDataNascimento()
        );
        // Map series/period from domain model
        dto.setSerie(a.getSerie());
        // Map grade level (legacy 'grau') from domain model
        // TODO: (REVIEW) Ensure 'grau' is populated by DAO/service layer
        dto.setGrau(a.getGrau());
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves list of 'apto' students for the previous year not yet sent.
     * Ported from legacy pesquisarAlunoApto.
     *
     * @param initials        comma-separated initials of student names
     * @param codigoEscola    titular code
     * @param anoVigencia     year of validity
     * @param dataNascimento  birth date filter (optional)
     * @return list of apto Aluno entities
     */
    @GetMapping("/apto")
    public ResponseEntity<List<Aluno>> listApto(
            @RequestParam("initials") String initials,
            @RequestParam("codigoEscola") String codigoEscola,
            @RequestParam("anoVigencia") String anoVigencia,
            @RequestParam(value = "dataNascimento", required = false) String dataNascimento) {

        String[] initialArray = initials.split(",");
        List<Aluno> lista = alunoService.pesquisarAlunoApto(initialArray, codigoEscola, anoVigencia, dataNascimento);
        return ResponseEntity.ok(lista);
    }

    /**
     * Flags an Aluno as excluded (legacy 'E' flag) in the system.
     * Ported from legacy excluirAluno.
     *
     * @param aluno domain Aluno entity to exclude
     * @return no content on success
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAluno(@RequestBody Aluno aluno) {
        alunoService.excluirAluno(aluno);
        return ResponseEntity.noContent().build();
    }

    // TODO: (REVIEW) Implement endpoints for listSent (/sent) and listExcluded (/excluded)
    // once corresponding service methods are added to AlunoService interface and implementation.
}
