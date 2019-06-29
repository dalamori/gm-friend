package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("locationDao")
public interface LocationDao extends CrudRepository<Location, Long> {

    public List<Location> getAllByOwner(String owner);
}
