package dev.razafindratelo.misinformation.repository.model;

import dev.razafindratelo.misinformation.model.classifier.UserRole;
import dev.razafindratelo.misinformation.model.classifier.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class JUser {
  @Id private String id;

  @Column(nullable = false)
  private String email;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "clerk_id", nullable = false)
  private String clerkId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "user_role", nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "user_status", nullable = false)
  private UserStatus status;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
