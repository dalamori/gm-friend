package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Note;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("noteDao")
public interface NoteDao extends CrudRepository<Note, Long> {

    List<Note> findAllByOwner(String owner);
}
