package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.JText;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TextRepository extends JpaRepository<JText, String> {

  @Query("select txt from JText txt join fetch txt.owner")
  List<JText> findAllWithOwner();
}
