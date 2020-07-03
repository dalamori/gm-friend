package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.UserException;
import net.dalamori.GMFriend.models.User;
import net.dalamori.GMFriend.repository.UserDao;
import net.dalamori.GMFriend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public User create(User user) throws UserException {
        if (user.getId() != null) {
            log.debug("UserServiceImpl::create asked to create a user which already has an id");
            throw new UserException("asked to create user which already has id");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.size() > 0) {
            for (ConstraintViolation<User> violation : violations) {
                log.debug("MobileServiceImpl::create validation violation for user {} : {}", user, violation.getMessage());
            }

            throw new UserException("asked to create invalid user");
        }

        return userDao.save(user);
    }

    @Override
    public User read(String owner, String game) throws UserException {
        Optional<User> result = userDao.findByOwnerAndGame(owner, game);
        
        if (!result.isPresent()){
            log.debug("UserServiceImpl::read - owner {} and game {} not found", owner, game);
            throw new UserException("not found");
        }
        
        return result.get();
    }

    @Override
    public User update(User user) throws UserException {
        if (user.getId() == null) {
            log.debug("UserServiceImpl::update asked to create a user which has null id");
            throw new UserException("asked to update user which has no id");
        }

        if (!userDao.existsById(user.getId())) {
            log.debug("UserServiceImpl::update user with id # {} not found", user.getId());
            throw new UserException("asked to update user which is not found");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.size() > 0) {
            for (ConstraintViolation<User> violation : violations) {
                log.debug("UserServiceImpl::update validation violation for user {} : {}", user, violation.getMessage());
            }

            throw new UserException("asked to update invalid user");
        }
        
        return userDao.save(user);
    }

    @Override
    public void delete(User user) throws UserException {
        if (user.getId() == null) {
            log.debug("UserServiceImpl::delete asked to delete user without an id");
            throw new UserException("asked to save user without id");
        }

        if (!userDao.existsById(user.getId())) {
            log.debug("UserServiceImpl::delete user not found");
            throw new UserException("user not found");
        }

        userDao.deleteById(user.getId());
    }

    @Override
    public boolean exists(String owner, String game) {
        return userDao.existsByOwnerAndGame(owner, game);
    }

    @Override
    public void deleteAllByOwner(String owner) {
        userDao.deleteAllByOwner(owner);
    }

    @Override
    public void deleteAllByGame(String game) {
        userDao.deleteAllByGame(game);
    }

    @Override
    public User forGame(String owner, String game) {

        List<String> games = new ArrayList<>();
        games.add(game);
        games.add(User.GLOBAL_GAME_ID);

        Optional<User> result = userDao.findFirstByOwnerAndGameInOrderByRoleAsc(owner, games);

        if (result.isPresent()) {
            return result.get();
        }

        User user = new User();
        user.setOwner(owner);
        user.setGame(game);

        return user;
    }
}
