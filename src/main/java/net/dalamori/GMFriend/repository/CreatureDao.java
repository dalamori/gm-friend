package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Creature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("creatureDao")
public interface CreatureDao extends CrudRepository<Creature, Long> {
}
