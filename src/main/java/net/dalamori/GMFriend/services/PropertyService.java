package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;

import java.util.List;

public interface PropertyService {
    Property create(Property property) throws PropertyException;

    Property read(Long id) throws PropertyException;

    Property update(Property property) throws PropertyException;

    void delete(Property property) throws PropertyException;

    void attachToMobile(Property propery, Mobile mobile) throws PropertyException;

    void attachToCreature(Property property, Creature creature) throws PropertyException;

    void attachToGlobalContext(Property property) throws PropertyException;

    List<Property> getGlobalProperties();

    List<Property> getCreatureProperties();

    List<Property> getMobileProperties();

}
