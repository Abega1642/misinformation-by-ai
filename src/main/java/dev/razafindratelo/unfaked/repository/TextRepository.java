package dev.razafindratelo.unfaked.repository;

import dev.razafindratelo.unfaked.repository.model.JText;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TextRepository extends JpaRepository<JText, String> {
  @Query("select txt from JText txt where txt.owner.email = :email")
  Page<JText> findAllByOwnerEmail(@Param("email") String email, Pageable pageable);
}
