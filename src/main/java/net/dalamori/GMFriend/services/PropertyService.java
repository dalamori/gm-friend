package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.interfaces.HasProperties;

import java.util.List;
import java.util.Map;

public interface PropertyService {
    Property copy(Property property) throws PropertyException;

    Property create(Property property) throws PropertyException;

    Property read(Long id) throws PropertyException;

    boolean exists(Long id);

    Property update(Property property) throws PropertyException;

    void delete(Property property) throws PropertyException;

    void attachToMobile(Property property, Mobile mobile) throws PropertyException;

    void attachToCreature(Property property, Creature creature) throws PropertyException;

    void attachToGlobalContext(Property property) throws PropertyException;

    void detachFromCreature(Property property, Creature creature) throws PropertyException;

    void detachFromMobile(Property property, Mobile mobile) throws PropertyException;

    void detachFromGlobalContext(Property property) throws PropertyException;

    Map<String, Property> getGlobalProperties() throws PropertyException;

    List<Property> getCreatureProperties(Creature creature) throws PropertyException;

    List<Property> getMobileProperties(Mobile mobile) throws PropertyException;

    boolean validatePropertyMapNames(HasProperties subject);

}
