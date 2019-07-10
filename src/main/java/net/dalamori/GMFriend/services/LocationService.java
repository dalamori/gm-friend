package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.LocationException;
import net.dalamori.GMFriend.models.Location;

public interface LocationService extends SimpleCrudeService<Location> {

    Location create(Location location) throws LocationException;

    Location read(Long id) throws LocationException;
    Location read(String name) throws LocationException;

    boolean exists(Long id);
    boolean exists(String name);

    Location update(Location location) throws LocationException;

    void delete(Location location) throws LocationException;

}
