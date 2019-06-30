package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;

import java.util.List;

public interface NoteService {
    Note create(Note note) throws NoteException;

    Note read(Long id) throws NoteException;

    Note update(Note note) throws NoteException;

    void delete(Note note) throws NoteException;

    void attachToGlobalContext(Note note);

    void attachToLocation(Note note, Location location) throws NoteException;

    List<Note> getGlobalNotes();

    List<Note> getLocationNotes(Location location);


}
