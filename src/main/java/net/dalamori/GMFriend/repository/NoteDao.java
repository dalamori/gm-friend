package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Note;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("noteDao")
public interface NoteDao extends CrudRepository<Note, Long> {

    Optional<Note> findByTitle(String title);

    boolean existsByTitle(String title);

    List<Note> findAllByOwner(String owner);
}
