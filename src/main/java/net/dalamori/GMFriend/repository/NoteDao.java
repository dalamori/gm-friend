package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Note;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteDao extends CrudRepository<Note, Long> {

    List<Note> findAllByOwner(String owner);
}
