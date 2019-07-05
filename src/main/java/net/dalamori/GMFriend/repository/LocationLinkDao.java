package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository("linkDao")
public interface LocationLinkDao extends CrudRepository<LocationLink, Long> {

    Set<LocationLink> findAllByOrigin(Location origin);


}
