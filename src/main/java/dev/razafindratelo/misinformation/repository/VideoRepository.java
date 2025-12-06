package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.JVideo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<JVideo, String> {
  @Query("select jv from JVideo jv where jv.owner.email = :email")
  Page<JVideo> findAllByOwnerEmail(@Param("email") String email, Pageable pageable);

  @Query("select v from JVideo v join fetch v.owner")
  List<JVideo> findAllWithOwner();
}
