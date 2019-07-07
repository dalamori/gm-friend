package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;

public interface CreatureService {

    Creature create(Creature creature) throws CreatureException;

    Creature read(Long id) throws CreatureException;
    Creature read(String name) throws CreatureException;

    boolean exists(Long id);
    boolean exists(String name);

    Creature update(Creature creature) throws CreatureException;

    void delete(Creature creature) throws CreatureException;

    Creature fromMobile(Mobile mobile) throws CreatureException;
}
