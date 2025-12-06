package dev.razafindratelo.misinformation.repository;

import dev.razafindratelo.misinformation.repository.model.JImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<JImage, String> {
  Optional<JImage> findByBucketKey(String bucketKey);

  @Query("select img from JImage img join fetch img.owner")
  List<JImage> findAllWithOwner();
}
