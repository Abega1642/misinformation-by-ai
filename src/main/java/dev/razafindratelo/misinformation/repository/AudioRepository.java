package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.JAudio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioRepository extends JpaRepository<JAudio, String> {}
