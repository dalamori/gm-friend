package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;

public interface SimpleCrudeService<T> {
    T create(T object) throws DmFriendGeneralServiceException;
    T read(Long id) throws DmFriendGeneralServiceException;
    T read(String name) throws DmFriendGeneralServiceException;
    T update(T object) throws DmFriendGeneralServiceException;
    void delete(T object) throws DmFriendGeneralServiceException;
    boolean exists(Long id);
    boolean exists(String name);
}
