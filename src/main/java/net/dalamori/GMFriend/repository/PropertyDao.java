package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Property;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyDao extends CrudRepository<Property, Long> {

    List<Property> getAllPropertiesByOwner(String owner);

}
