package dev.razafindratelo.unfaked.repository;

import dev.razafindratelo.unfaked.repository.model.JToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<JToken, String> {
  Optional<JToken> findByToken(String token);
}
