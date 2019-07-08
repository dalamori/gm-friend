package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Mobile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("mobileDao")
public interface MobileDao extends CrudRepository<Mobile, Long> {

    Optional<Mobile> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT COUNT(m) FROM Mobile m WHERE m.name LIKE CONCAT(:name ,'%')")
    int countByNameBeginning(@Param("name") String name);

}
