package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Property;

public interface PropertyService {
    Property create(Property property) throws PropertyException;

    Property read(Long id) throws PropertyException;

    Property update(Property property) throws PropertyException;

    void delete(Long id) throws PropertyException;


}
