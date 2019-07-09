package net.dalamori.GMFriend.services;

import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.interfaces.HasNotes;

import java.util.List;

public interface NoteService extends SimpleCrudeService<Note> {
    Note create(Note note) throws NoteException;

    Note read(Long id) throws NoteException;
    Note read(String name) throws NoteException;

    boolean exists(Long id);
    boolean exists(String name);

    Note update(Note note) throws NoteException;

    void delete(Note note) throws NoteException;

    void attachToGlobalContext(Note note) throws NoteException;

    void attachToLocation(Note note, Location location) throws NoteException;

    void detachFromGlobalContext(Note note) throws NoteException;

    void detachFromLocation(Note note, Location location) throws NoteException;

    List<Note> getGlobalNotes() throws NoteException;

    List<Note> getLocationNotes(Location location) throws NoteException;

    boolean validateNotes(HasNotes subject);

}
