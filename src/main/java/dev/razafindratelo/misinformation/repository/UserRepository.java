package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.JUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<JUser, String> {

  boolean existsByEmail(String email);

  boolean existsByClerkId(String clerkId);

  Optional<JUser> findByEmail(String email);

  Optional<JUser> findByClerkId(String clerkId);

  Optional<JUser> findByEmailAndClerkId(String email, String clerkId);
}
