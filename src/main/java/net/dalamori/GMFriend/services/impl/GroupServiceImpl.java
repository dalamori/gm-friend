package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
@Service("groupService")
@Transactional(rollbackFor = NoteException.class)
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupDao groupDao;

    @Override
    public Group create(Group group) throws GroupException {

        if (group.getId() instanceof Long) {
            log.debug("GroupServiceImpl::create - already has Id");
            throw new GroupException("group to create already has an ID set");
        }

        if (groupDao.existsByName(group.getName())) {
            log.debug("GroupServiceImpl::create - duplicate name");
            throw new GroupException("group to create duplicates a name already in the DB");
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        if (violations.size() > 0) {
            for (ConstraintViolation<Group> violation : violations) {
                log.debug("GroupServiceImpl::create validation violation : {}", violation.getMessage());
            }

            throw new GroupException("group to create failed validation");
        }

        try {
            return groupDao.save(group);
        } catch (Throwable ex) {
            log.info("GroupServiceImpl::create Record insert failed: {}", group, ex);
            throw new GroupException("SQL failed to insert", ex);
        }
    }

    @Override
    public Group read(Long id) throws GroupException {
        Optional<Group> result = groupDao.findById(id);

        if (!result.isPresent()) {
            log.debug("GroupServiceImpl::read - ID {} not found", id);
            throw new GroupException("Not Found");
        }

        return result.get();
    }

    @Override
    public Group read(String name) throws GroupException {
        Optional<Group> result = groupDao.findByName(name);

        if (!result.isPresent()) {
            log.debug("GroupServiceImpl::read - Name {} not found", name);
            throw new GroupException("Not Found");
        }

        return result.get();
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return groupDao.existsById(id);
        }

        return false;
    }

    @Override
    public boolean exists(String name) {
        if (name != null) {
            return groupDao.existsByName(name);
        }

        return false;
    }

    @Override
    public Group update(Group group) throws GroupException {
        if (group.getId() == null) {
            log.debug("GroupServiceImpl::update - cannot update group with null Id");
            throw new GroupException("group id cannot be null");
        }

        if (!groupDao.existsById(group.getId())) {
            log.debug("GroupServiceImpl::update - group with ID {} not found", group.getId());
            throw new GroupException("Group not found");
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Group>> violations = validator.validate(group);
        if (violations.size() > 0) {
            for (ConstraintViolation<Group> violation : violations) {
                log.debug("GroupServiceImpl::update validation violation : {}", violation.getMessage());
            }

            throw new GroupException("group to create failed validation");
        }

        try {
            return groupDao.save(group);
        } catch (Throwable ex) {
            log.info("GroupServiceImpl::update Record update failed: {}", group, ex);
            throw new GroupException("SQL failed to update", ex);
        }
    }

    @Override
    public void delete(Group group) throws GroupException {
        if (group.getId() == null) {
            log.debug("GroupServiceImpl::delete - cannot update group with null Id");
            throw new GroupException("group id cannot be null");
        }

        if (!groupDao.existsById(group.getId())) {
            log.debug("GroupServiceImpl::delete - Group Id {} not found", group.getId());
            throw new GroupException("Group not found");
        }

        try {
            groupDao.deleteById(group.getId());
        } catch (Throwable ex) {
            log.info("GroupServiceImpl::delete failed to delete {}", group, ex);
            throw new GroupException("SQL failed to delete");
        }
    }
}
