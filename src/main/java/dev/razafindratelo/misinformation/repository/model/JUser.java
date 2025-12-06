package dev.razafindratelo.misinformation.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class JUser {
  @Id private String id;

  @Column(nullable = false)
  private String email;

  @Column(name = "clerk_id", nullable = false)
  private String clerkId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
