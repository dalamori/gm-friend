package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.MobileException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;

import java.util.List;

public interface MobileService extends SimpleCrudeService<Mobile> {

    Mobile create(Mobile mobile) throws MobileException;

    Mobile read(Long id) throws MobileException;
    Mobile read(String name) throws MobileException;

    boolean exists(Long id);
    boolean exists(String name);

    Mobile update(Mobile mobile) throws MobileException;

    void delete(Mobile mobile) throws MobileException;

    Mobile fromCreature(Creature creature) throws MobileException;

    List<Mobile> initiativeList() throws MobileException;

}
