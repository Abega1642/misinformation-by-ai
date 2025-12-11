package dev.razafindratelo.unfaked.repository;

import dev.razafindratelo.unfaked.repository.model.Dummy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyRepository extends JpaRepository<Dummy, String> {}
