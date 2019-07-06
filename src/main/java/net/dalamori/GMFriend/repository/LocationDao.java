package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("locationDao")
public interface LocationDao extends CrudRepository<Location, Long> {

    boolean existsByName(String name);

    Optional<Location> findByName(String name);

    List<Location> findAllByOwner(String owner);
}
