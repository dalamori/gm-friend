package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Group;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("groupDao")
public interface GroupDao extends CrudRepository<Group, Long> {

    List<Group> findAllByOwner(String owner);

}

