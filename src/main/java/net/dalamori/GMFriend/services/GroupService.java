package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.enums.PropertyType;

public interface GroupService {
    Group create(Group group) throws GroupException;

    Group read(Long id) throws GroupException;
    Group read(String name) throws GroupException;

    boolean exists(Long id);
    boolean exists(String name);

    Group update(Group group) throws GroupException;

    void delete(Group group) throws GroupException;

    Group resolveSystemGroup(String name, PropertyType groupType) throws GroupException;
}
