package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<Dummy, String> {}
