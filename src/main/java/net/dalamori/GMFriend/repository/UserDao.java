package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("userDao")
public interface UserDao extends CrudRepository<User, Long> {
    Optional<User> findByOwnerAndGame(String owner, String game);
    boolean existsByOwnerAndGame(String owner, String game);

    List<User> findAllByGame(String game);
    List<User> findAllByOwner(String owner);

    int deleteAllByGame(String game);
    int deleteAllByOwner(String owner);

    Optional<User> findFirstByOwnerAndGameInOrderByRoleDesc(String owner, List<String> games);
}
