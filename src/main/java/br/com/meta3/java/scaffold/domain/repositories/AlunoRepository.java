package br.com.meta3.java.scaffold.domain.repositories;

import br.com.meta3.java.scaffold.domain.entities.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository replacing legacy AlunoDAO.
 * Provides CRUD operations for Aluno entity.
 */
@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    // TODO: (REVIEW) Add custom query methods if legacy inserirAluno logic requires specific JPQL/SQL
}
