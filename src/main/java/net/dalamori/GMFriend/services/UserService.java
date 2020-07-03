package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.UserException;
import net.dalamori.GMFriend.models.User;

public interface UserService {
    User create(User user) throws UserException;
    User read(String owner, String game) throws UserException;
    User update(User user) throws UserException;
    void delete(User user) throws UserException;
    boolean exists(String owner, String game);

    void deleteAllByOwner(String owner);
    void deleteAllByGame(String game);

    User forGame(String owner, String game) throws UserException;
}
