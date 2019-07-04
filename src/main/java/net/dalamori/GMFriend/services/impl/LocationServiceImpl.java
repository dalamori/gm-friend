package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.LocationException;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.repository.LocationDao;
import net.dalamori.GMFriend.repository.LocationLinkDao;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
@Data
@Slf4j
@Service("locationService")
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationDao locationDao;

    @Autowired
    private LocationLinkDao linkDao;

    @Autowired
    private NoteService noteService;

    @Override
    public Location create(Location location) throws LocationException {

    }

    @Override
    public Location read(Long id) throws LocationException {

    }

    @Override
    public Location read(String name) throws LocationException {

    }

    @Override
    public boolean exists(Long id) {

    }

    @Override
    public boolean exists(String name) {

    }

    @Override
    public Location update(Location location) throws LocationException {

    }

    @Override
    public void delete(Location location) throws LocationException {

    }

}
*/