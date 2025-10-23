package br.com.meta3.java.scaffold.domain.repositories;

import br.com.meta3.java.scaffold.domain.entities.Aluno;

/**
 * Placeholder interface representing the legacy AlunoDAO contract.
 *
 * Purpose:
 * - Provide a clear abstraction for the legacy insertion operation used by migrated services.
 * - Guide the infrastructure layer to implement the legacy behavior (e.g., calling an external legacy DB).
 *
 * NOTE:
 * - The legacy implementation should live in the infrastructure layer (e.g., br.com.meta3.java.scaffold.infrastructure.repositories).
 * - Keep this interface minimal to reflect the immediate legacy dependency required by the migrated code.
 *
 * Design decisions:
 * - Kept the signature compatible with the legacy method used in the migrated AlunoServiceImpl:
 *     inserirAluno(Aluno, String ipCliente, String tipo) -> int
 *   This preserves behaviour and eases replacing the legacy call with a new implementation later.
 *
 * - We intentionally limit this interface to the single method currently required by services to avoid
 *   introducing a large legacy surface area. Additional legacy methods (e.g., pesquisaCodigosetps) should
 *   be added here only when service code is refactored to depend on this abstraction instead of concrete implementations.
 *
 * TODOs and considerations:
 * - (REVIEW) Expand this interface with additional legacy methods (pesquisaCodigosetps, verificaCpf, etc.)
 *         if services are refactored to depend on this abstraction instead of AlunoDaoImpl directly.
 * - (SECURITY) Consider using richer result types or checked exceptions instead of primitive int return codes to
 *         better represent failures from the legacy system.
 * - (TEST) Provide a test double / mock implementation for unit tests that depend on legacy insertion behaviour.
 */
public interface AlunoLegacyDao {

    /**
     * Inserts an Aluno record into the legacy system.
     *
     * @param aluno     domain Aluno object containing values to be inserted (may be partially populated)
     * @param ipCliente IP address of the client that triggered the insertion (legacy stored field)
     * @param tipo      operation type (e.g., "INCLUSAO", "EXCLUSAO")
     * @return legacy-specific integer result code (legacy returns 1 on success). Consider mapping to boolean or exceptions later.
     */
    int inserirAluno(Aluno aluno, String ipCliente, String tipo);
}