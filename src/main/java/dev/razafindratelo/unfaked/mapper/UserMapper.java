package dev.razafindratelo.unfaked.mapper;

import dev.razafindratelo.unfaked.model.User;
import dev.razafindratelo.unfaked.repository.model.JUser;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toCoreModel(JUser jUser);

  JUser toPersistence(User user);

  List<User> toCoreModel(List<JUser> jUsers);

  List<JUser> toPersistence(List<User> users);
}
