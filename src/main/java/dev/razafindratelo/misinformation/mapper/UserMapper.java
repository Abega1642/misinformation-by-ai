package dev.razafindratelo.misinformation.mapper;

import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.repository.model.JUser;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class UserMapper {

  public User toCoreModel(JUser jUser) {
    return new User(
        jUser.getId(),
        jUser.getEmail(),
        jUser.getFullName(),
        jUser.getClerkId(),
        jUser.getCreatedAt());
  }

  public JUser toPersistenceModel(User user) {
    return new JUser(user.id(), user.email(), user.fullName(), user.clerkId(), user.createdAt());
  }
}
