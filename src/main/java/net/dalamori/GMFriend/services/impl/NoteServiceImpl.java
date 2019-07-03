package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.NoteDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
@Service("noteService")
@Transactional(rollbackFor = {NoteException.class, GroupException.class})
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DmFriendConfig config;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public Note create(Note note) throws NoteException {
        if (note.getId() instanceof Long) {
            log.debug("NoteServiceImpl::create - already has Id");
            throw new NoteException("group to create already has an ID set");
        }

        if (noteDao.existsByTitle(note.getTitle())) {
            log.debug("NoteServiceImpl::create - duplicate title");
            throw new NoteException("group to create duplicates a title already in the DB");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();

        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        if (violations.size() > 0) {
            for (ConstraintViolation<Note> violation : violations) {
                log.debug("GroupServiceImpl::create validation violation for note {} : {}", note, violation.getMessage());
            }

            throw new NoteException("group to create failed validation");
        }

        try {
            return noteDao.save(note);
        } catch (Throwable ex) {
            log.info("NoteServiceImpl::create Record insert failed: {}", note, ex);
            throw new NoteException("SQL failed to insert", ex);
        }
    }

    @Override
    public Note read(Long id) throws NoteException {
        Optional<Note> result = noteDao.findById(id);

        if (!result.isPresent()) {
            log.debug("NoteServiceImpl::read - ID {} not found", id);
            throw new NoteException("Not Found");
        }

        return result.get();
    }

    @Override
    public Note read(String title) throws NoteException {
        Optional<Note> result = noteDao.findByTitle(title);

        if (!result.isPresent()) {
            log.debug("NoteServiceImpl::read - Title {} not found", title);
            throw new NoteException("Not Found");
        }

        return result.get();
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return noteDao.existsById(id);
        }

        return false;
    }

    @Override
    public boolean exists(String title) {
        if (title != null) {
            return noteDao.existsByTitle(title);
        }

        return false;
    }

    @Override
    public Note update(Note note) throws NoteException {
        if (note.getId() == null) {
            log.debug("NoteServiceImpl::update - cannot update note with null Id");
            throw new NoteException("note id cannot be null");
        }

        if (!noteDao.existsById(note.getId())) {
            log.debug("NoteServiceImpl::update - note with ID {} not found", note.getId());
            throw new NoteException("Note not found");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();

        Set<ConstraintViolation<Note>> violations = validator.validate(note);
        if (violations.size() > 0) {
            for (ConstraintViolation<Note> violation : violations) {
                log.debug("NoteServiceImpl::update validation violation : {}", violation.getMessage());
            }

            throw new NoteException("note to create failed validation");
        }

        try {
            return noteDao.save(note);
        } catch (Throwable ex) {
            log.info("NoteServiceImpl::update Record update failed: {}", note, ex);
            throw new NoteException("SQL failed to update", ex);
        }
    }

    @Override
    public void delete(Note note) throws NoteException {
        if (note.getId() == null) {
            log.debug("NoteServiceImpl::delete - cannot update note with null Id");
            throw new NoteException("note id cannot be null");
        }

        if (!noteDao.existsById(note.getId())) {
            log.debug("NoteServiceImpl::delete - Note Id {} not found", note.getId());
            throw new NoteException("Note not found");
        }

        try {
            noteDao.deleteById(note.getId());
        } catch (Throwable ex) {
            log.info("NoteServiceImpl::delete failed to delete {}", note, ex);
            throw new NoteException("SQL failed to delete");
        }
    }

    @Override
    public void attachToGlobalContext(Note note) throws NoteException {
        if (note.getId() == null) {
            log.debug("NoteServiceImpl::attachToGlobalContext - asked to attach unsaved note");
            throw new NoteException("can't attach unsaved note");
        }

        try {
            Group notes = resolveGlobalNoteGroup();

            notes.getContents().add(note.getId());

            groupService.update(notes);
        } catch (GroupException ex) {
            log.warn("NoteServiceImpl::attachToGlobalContext failed to attach note {}", note, ex);
            throw new NoteException("unable to attach note to global list", ex);
        }
    }

    @Override
    public void attachToLocation(Note note, Location location) throws NoteException {

        if (note.getId() == null) {
            log.debug("NoteServiceImpl::attachToLocation - asked to attach unsaved note");
            throw new NoteException("can't attach unsaved note");
        }

        if (location.getId() == null) {
            log.debug("NoteServiceImpl::attachToLocation - asked to attach to unsaved location");
            throw new NoteException("can't attach to unsaved location");
        }

        try {
            Group notes = resolveLocationNoteGroup(location);

            notes.getContents().add(note.getId());

            groupService.update(notes);
        } catch (GroupException ex) {
            log.warn("NoteServiceImpl::attachToLocation failed to attach note {} to location {}", note, location, ex);
            throw new NoteException("unable to attach note to location", ex);
        }
    }

    @Override
    public void detachFromGlobalContext(Note note) throws NoteException {
        if (note.getId() == null) {
            log.debug("NoteServiceImpl::detachFromGlobalContext - asked to detach unsaved note");
            throw new NoteException("can't detach unsaved note");
        }

        try {
            Group notes = resolveGlobalNoteGroup();
            Set<Long> contents = notes.getContents();

            if (contents.contains(note.getId())) {
                contents.remove(note.getId());

                groupService.update(notes);
            } else {
                log.debug("NoteServiceImpl::detachFromGlobalContext asked to detach unattached note");
                throw new NoteException("note not found in global context");
            }

        } catch (GroupException ex) {
            log.warn("NoteServiceImpl::detachFromGlobalContext failed to detach note {}", note, ex);
            throw new NoteException("unable to detach note to global list", ex);
        }
    }

    @Override
    public void detachFromLocation(Note note, Location location) throws NoteException {
        if (note.getId() == null) {
            log.debug("NoteServiceImpl::attachToLocation - asked to detach unsaved note");
            throw new NoteException("can't attach unsaved note");
        }

        if (location.getId() == null) {
            log.debug("NoteServiceImpl::attachToLocation - asked to detach from unsaved location");
            throw new NoteException("can't attach to unsaved location");
        }

        try {
            Group notes = resolveLocationNoteGroup(location);

            notes.getContents().remove(note.getId());

            groupService.update(notes);
        } catch (GroupException ex) {
            log.warn("NoteServiceImpl::detachFromLocation failed to detach note {} from location {}", note, location, ex);
            throw new NoteException("unable to detach note to location", ex);
        }
    }


    @Override
    public List<Note> getGlobalNotes() throws NoteException {
        List<Note> list = new ArrayList<>();

        try {
            Group notes = resolveGlobalNoteGroup();

            noteDao.findAllById(notes.getContents()).iterator().forEachRemaining(list::add);

            return list;
        } catch (GroupException ex) {
            throw new NoteException("unable to retrieve global Notes", ex);
        }
    }

    @Override
    public List<Note> getLocationNotes(Location location) throws NoteException {
        List<Note> list = new ArrayList<>();

        try {
            Group notes = resolveLocationNoteGroup(location);

            noteDao.findAllById(notes.getContents()).iterator().forEachRemaining(list::add);

            return list;

        } catch (GroupException ex) {
            throw new NoteException("Unable to retrieve Location Notes", ex);
        }
    }

    private Group resolveLocationNoteGroup(Location location) throws GroupException, NoteException {
        if (location.getId() == null) {
            log.debug("NoteServiceImpl::resolveLocationNoteGroup asked to resolve notes for unsaved location");
            throw new NoteException(" cant lookup for location with null id");
        }

        String name = String.format("%s%s%d",
                config.getSystemGroupPrefix(),
                config.getSystemGroupLocationNoteAction(),
                location.getId());

        return groupService.resolveSystemGroup(name, PropertyType.NOTE);
    }

    private Group resolveGlobalNoteGroup() throws GroupException {
        String name = config.getSystemGroupPrefix().concat(config.getSystemGroupGlobalNoteAction());
        return groupService.resolveSystemGroup(name, PropertyType.NOTE);
    }


}
