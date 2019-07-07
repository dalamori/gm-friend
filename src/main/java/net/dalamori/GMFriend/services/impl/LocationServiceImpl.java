package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.LocationException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.repository.LocationDao;
import net.dalamori.GMFriend.repository.LocationLinkDao;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@Slf4j
@Service("locationService")
@Transactional(rollbackFor = {GroupException.class, LocationException.class, NoteException.class})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationDao locationDao;

    @Autowired
    private LocationLinkDao linkDao;

    @Autowired
    private NoteService noteService;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public Location create(Location location) throws LocationException {

        if (location.getId() != null) {
            log.debug("LocationServiceImpl::create asked to create a location which already has an id");
            throw new LocationException("asked to create location which already has id");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Location>> violations = validator.validate(location);
        if (violations.size() > 0) {
            for (ConstraintViolation<Location> violation : violations) {
                log.debug("LocationServiceImpl::create validation violation for location {} : {}", location, violation.getMessage());
            }

            throw new LocationException("asked to create invalid location");
        }

        if (!validateLocationLinkEndpoints(location)) {
            log.debug("LocationServiceImpl::create all location links must originate at this location");
            throw new LocationException("all location links must originate at this location");
        }

        if (!validateLocationNotes(location)) {
            log.debug("LocationServiceImpl::create all location note must already be saved");
            throw new LocationException("all location notes must already be saved");
        }

        Location savedLocation = locationDao.save(location);
        for (LocationLink link : location.getLinks()) {
            savedLocation.getLinks().add(linkDao.save(link));
        }

        for (Note note : location.getNotes()) {
            try {
                noteService.attachToLocation(note, location);
                savedLocation.getNotes().add(note);
            } catch(NoteException ex) {
                log.warn("LocationServiceImpl::create failed to attach note #{} to Location #{}", note.getId(), location.getId(), ex);
                throw new LocationException("create failed to attach notes");
            }
        }

        return savedLocation;
    }

    @Override
    public Location read(Long id) throws LocationException {

        Optional<Location> result = locationDao.findById(id);

        if (!result.isPresent()) {
            log.debug("LocationServiceImpl::read - Id # {} not found", id);
            throw new LocationException("not found");
        }
        Location location = result.get();

        location.getLinks().addAll(linkDao.findAllByOrigin(location));

        try {
            List<Note> notes = noteService.getLocationNotes(location);
            location.getNotes().addAll(notes);
        } catch (NoteException ex) {
            log.warn("LocationServiceImpl::Read got error while trying to look up location notes");
            throw new LocationException("failed to look up location notes", ex);
        }

        return location;
    }

    @Override
    public Location read(String name) throws LocationException {
        Optional<Location> result = locationDao.findByName(name);

        if (!result.isPresent()) {
            log.debug("LocationServiceImpl::read - name not found: {}", name);
            throw new LocationException("not found");
        }
        Location location = result.get();

        location.getLinks().addAll(linkDao.findAllByOrigin(location));

        try {
            List<Note> notes = noteService.getLocationNotes(location);
            location.getNotes().addAll(notes);
        } catch (NoteException ex) {
            log.warn("LocationServiceImpl::Read got error while trying to look up location notes");
            throw new LocationException("failed to look up location notes", ex);
        }

        return location;
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return locationDao.existsById(id);
        }

        return false;
    }

    @Override
    public boolean exists(String name) {
        if (name != null) {
            return locationDao.existsByName(name);
        }

        return false;
    }

    @Override
    public Location update(Location location) throws LocationException {

        // STEP 1: Input validation
        if (location.getId() == null) {
            log.debug("LocationServiceImpl::update asked to create a location which does not have an id");
            throw new LocationException("asked to create location which doesn't have an id");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Location>> violations = validator.validate(location);
        if (violations.size() > 0) {
            for (ConstraintViolation<Location> violation : violations) {
                log.debug("LocationServiceImpl::update validation violation for location {} : {}", location, violation.getMessage());
            }

            throw new LocationException("asked to update invalid location");
        }

        if (!validateLocationLinkEndpoints(location)) {
            log.debug("LocationServiceImpl::update all location links must originate at this location");
            throw new LocationException("all location links must originate at this location");
        }

        if (!validateLocationNotes(location)) {
            log.debug("LocationServiceImpl::update all location note must already be saved");
            throw new LocationException("all location notes must already be saved");
        }

        // STEP 2: Save location to DAO
        Location savedLocation = locationDao.save(location);

        // STEP 3: Sync notes to noteService
        List<Note> currentNotes;
        try {
            currentNotes = noteService.getLocationNotes(location);

        } catch (NoteException ex) {
            log.error("LocationServiceImpl::update failed to look up location notes for location #{}", location.getId(), ex);
            throw new LocationException("failed to lookup location notes", ex);
        }

        // calc changes
        Set<Note> newNotes = new HashSet<>();
        newNotes.addAll(location.getNotes());
        newNotes.removeAll(currentNotes);

        Set<Note> removeNotes = new HashSet<>();
        removeNotes.addAll(currentNotes);
        removeNotes.removeAll(location.getNotes());

        // apply changes
        try {
            for (Note note : removeNotes) {
                noteService.detachFromLocation(note, location);
            }
            for (Note note : newNotes) {
                noteService.attachToLocation(note, location);
            }
        } catch (NoteException ex) {
            log.error("LocationServiceImpl::update unable to sync notes.", ex);
            throw new LocationException("unable to sync notes");
        }

        // add to retval
        savedLocation.getNotes().addAll(location.getNotes());

        // STEP 4: Sync Links to Dao
        Set<LocationLink> currentLinks = linkDao.findAllByOrigin(location);

        // construct a reference map by ids
        Map<Long, LocationLink> currentLinkIds = new HashMap<>();
        for (LocationLink link : currentLinks) {
            currentLinkIds.put(link.getId(), link);
        }

        // iterate over new items, comparing IDs to isolate a set of links to delete, and saving all others to retval
        Set<LocationLink> linksToRemove = new HashSet<>();
        linksToRemove.addAll(currentLinks);
        for (LocationLink link : location.getLinks()) {

            // link is used, remove from from the nukelist
            if (currentLinkIds.containsKey(link.getId())) {
                linksToRemove.remove(currentLinkIds.get(link.getId()));
            }
            savedLocation.getLinks().add(linkDao.save(link));
        }

        // all others get nuked
        linkDao.deleteAll(linksToRemove);

        // STEP 5: return retval
        return savedLocation;

    }

    @Override
    public void delete(Location location) throws LocationException {
        if (location.getId() == null) {
            log.debug("LocationServiceImpl::delete - cannot update location with null Id");
            throw new LocationException("location id cannot be null");
        }

        if (!locationDao.existsById(location.getId())) {
            log.debug("LocationServiceImpl::delete - Location Id {} not found", location.getId());
            throw new LocationException("Location not found");
        }

        // LocationLinks should cascade when we delete the location

        // Unlink Location Notes
        try {
            for (Note note : noteService.getLocationNotes(location)) {
                noteService.detachFromLocation(note, location);
            }
        } catch (NoteException ex) {
            log.error("LocationServiceImpl::delete was unable to unlink notes for location {}", location, ex);
            throw new LocationException("unable to unlink notes", ex);
        }

        try {
            locationDao.deleteById(location.getId());
        } catch (Throwable ex) {
            log.info("LocationServiceImpl::delete failed to delete {}", location, ex);
            throw new LocationException("SQL failed to delete");
        }
    }

    private boolean validateLocationLinkEndpoints(Location location) {
        List<LocationLink> links = location.getLinks();
        for (LocationLink link : links) {
            if (link.getOrigin() != location) {
                return false;
            }

            // dest must exist
            if (link.getDestination() == null || link.getDestination().getId() == null) {
                return false;
            }
        }

        return true;
    }

    private boolean validateLocationNotes(Location location) {
        return noteService.validateNotes(location);
    }
}
