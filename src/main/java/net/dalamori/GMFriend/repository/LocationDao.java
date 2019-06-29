package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LocationDao extends CrudRepository<Location, Long> {

    public List<Location> getAllByOwner(String owner);
}
