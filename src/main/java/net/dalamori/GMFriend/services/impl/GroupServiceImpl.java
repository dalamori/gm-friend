package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Data
@Service("groupService")
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupDao groupDao;

    @Override
    public Group create(Group group) throws GroupException {

        if (group.getId() instanceof Long) {
            log.debug("GroupServiceImpl::create - already has Id");
            throw new GroupException("group to create already has an ID set");
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

        return groupDao.save(group);
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

        return groupDao.save(group);
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

        groupDao.deleteById(group.getId());
    }
}
