package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.models.Group;

public interface GroupService {
    Group create(Group group) throws GroupException;

    Group read(Long id) throws GroupException;
    Group read(String name) throws GroupException;

    boolean exists(Long id);
    boolean exists(String name);

    Group update(Group group) throws GroupException;

    void delete(Group group) throws GroupException;

}
