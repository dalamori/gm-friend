package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Mobile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("mobileDao")
public interface MobileDao extends CrudRepository<Mobile, Long> {
}
