package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.LocationLink;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("linkDao")
public interface LocationLinkDao extends CrudRepository<LocationLink, Long> {

}
