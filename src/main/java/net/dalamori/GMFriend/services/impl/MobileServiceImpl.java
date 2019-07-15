package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.MobileException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.MobileDao;
import net.dalamori.GMFriend.services.MobileService;
import net.dalamori.GMFriend.services.PropertyService;
import org.apache.commons.lang3.StringUtils;
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

@Slf4j
@Data
@Service("mobileService")
@Transactional(rollbackFor = MobileException.class)
public class MobileServiceImpl implements MobileService {

    @Autowired
    private DmFriendConfig config;

    @Autowired
    private MobileDao mobileDao;

    @Autowired
    private PropertyService propertyService;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public Mobile create(Mobile mobile) throws MobileException {
        if (mobile.getId() != null) {
            log.debug("MobileServiceImpl::create asked to create a mobile which already has an id");
            throw new MobileException("asked to create mobile which already has id");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Mobile>> violations = validator.validate(mobile);
        if (violations.size() > 0) {
            for (ConstraintViolation<Mobile> violation : violations) {
                log.debug("MobileServiceImpl::create validation violation for mobile {} : {}", mobile, violation.getMessage());
            }

            throw new MobileException("asked to create invalid mobile");
        }

        if (mobileDao.existsByName(mobile.getName())) {
            log.debug("MobileServiceImpl::create name collision detected for {}", mobile.getName());
            throw new MobileException("a mobile by that name already exists");
        }

        if (!propertyService.validatePropertyMapNames(mobile)) {
            log.debug("MobileServiceImpl::create propertyMap validation for mobile {}", mobile);
            throw new MobileException("propertyMap validation failed");
        }

        Mobile savedMobile = mobileDao.save(mobile);

        // copy properties
        try {
            for (String key : mobile.getPropertyMap().keySet()) {
                Property property = mobile.getPropertyMap().get(key);
                Property savedProperty;
                if (property.getId() == null) {
                    savedProperty = propertyService.create(property);
                } else {
                    savedProperty = property;
                }
                propertyService.attachToMobile(savedProperty, savedMobile);
                savedMobile.getPropertyMap().put(key, savedProperty);
            }
        } catch (PropertyException ex) {
            log.debug("MobileServiceImpl::create failed to create properties, ex");
            throw new MobileException("failed to create properties", ex);
        }

        return savedMobile;
    }

    @Override
    public Mobile read(Long id) throws MobileException {
        Optional<Mobile> result = mobileDao.findById(id);

        if (!result.isPresent()) {
            log.debug("MobileServiceImpl::read - Id # {} not found", id);
            throw new MobileException("not found");
        }

        Mobile mobile = result.get();
        try {
            List<Property> properties = propertyService.getMobileProperties(mobile);

            for (Property property : properties) {
                mobile.getPropertyMap().put(property.getName(), property);
            }
        } catch (PropertyException ex) {
            log.debug("MobileServiceImpl::read - couldn't read properties for mobile id #", id, ex);
            throw new MobileException("error reading properties", ex);
        }

        return mobile;
    }

    @Override
    public Mobile read(String name) throws MobileException {
        if (StringUtils.isNumeric(name)) {
            return read(Long.valueOf(name));
        }

        Optional<Mobile> result = mobileDao.findByName(name);

        if (!result.isPresent()) {
            log.debug("MobileServiceImpl::read - Id # {} not found", name);
            throw new MobileException("not found");
        }

        Mobile mobile = result.get();
        try {
            List<Property> properties = propertyService.getMobileProperties(mobile);

            for (Property property : properties) {
                mobile.getPropertyMap().put(property.getName(), property);
            }
        } catch (PropertyException ex) {
            log.debug("MobileServiceImpl::read - couldn't read properties for mobile named ", name, ex);
            throw new MobileException("error reading properties", ex);
        }

        return mobile;
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return mobileDao.existsById(id);
        }

        return false;
    }

    @Override
    public boolean exists(String name) {
        if (name != null) {
            if (StringUtils.isNumeric(name)) {
                return mobileDao.existsById(Long.valueOf(name));
            }
            return mobileDao.existsByName(name);
        }

        return false;
    }

    @Override
    public Mobile update(Mobile mobile) throws MobileException {
        if (mobile.getId() == null) {
            log.debug("MobileServiceImpl::update asked to create a mobile which has null id");
            throw new MobileException("asked to update mobile which has no id");
        }

        if (!mobileDao.existsById(mobile.getId())) {
            log.debug("MobileServiceImpl::update mobile with id # {} not found", mobile.getId());
            throw new MobileException("asked to update mobile which is not found");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Mobile>> violations = validator.validate(mobile);
        if (violations.size() > 0) {
            for (ConstraintViolation<Mobile> violation : violations) {
                log.debug("MobileServiceImpl::update validation violation for mobile {} : {}", mobile, violation.getMessage());
            }

            throw new MobileException("asked to update invalid mobile");
        }

        if (!propertyService.validatePropertyMapNames(mobile)) {
            log.debug("MobileServiceImpl::update propertyMap validation for mobile {}", mobile);
            throw new MobileException("propertyMap validation failed");
        }

        Mobile savedMobile = mobileDao.save(mobile);

        // sync properties
        try {
            List<Property> originalProperties = propertyService.getMobileProperties(mobile);
            Map<Long, Property> propertyIdMap = new HashMap<>();
            Set<Property> propertiesToRemove = new HashSet<>();

            propertiesToRemove.addAll(originalProperties);

            // populate id map
            for (Property property : originalProperties) {
                propertyIdMap.put(property.getId(), property);
            }

            // check against map for properties to delete
            for (String key : mobile.getPropertyMap().keySet()) {
                Property property = mobile.getPropertyMap().get(key);

                // props without an id need created.
                if (property.getId() == null) {
                    property = propertyService.create(property);
                    propertyService.attachToMobile(property, savedMobile);
                }

                if (propertyIdMap.containsKey(property.getId())) {
                    propertiesToRemove.remove(propertyIdMap.get(property.getId()));
                }

                savedMobile.getPropertyMap().put(property.getName(), property);
            }

            // remove
            for (Property property : propertiesToRemove) {
                propertyService.detachFromMobile(property, savedMobile);
            }

        } catch(PropertyException ex) {
            log.debug("MobileServiceImpl::update failed to sync properties");
            throw new MobileException("failed to sync properties", ex);
        }

        return savedMobile;
    }

    @Override
    public void delete(Mobile mobile) throws MobileException {
        if (mobile.getId() == null) {
            log.debug("MobileServiceImpl::delete asked to delete mobile without an id");
            throw new MobileException("asked to save mobile without id");
        }

        if (!mobileDao.existsById(mobile.getId())) {
            log.debug("MobileServiceImpl::delete mobile not found");
            throw new MobileException("mobile not found");
        }

        try {
            List<Property> properties = propertyService.getMobileProperties(mobile);
            for (Property property : properties) {
                propertyService.detachFromMobile(property, mobile);
            }
        } catch (PropertyException ex) {
            log.debug("MobileServiceImpl::delete unable to unlink mobile properties");
            throw new MobileException("failed to unlink mobile properties", ex);
        }

        mobileDao.deleteById(mobile.getId());
    }

    @Override
    public Mobile fromCreature(Creature creature) throws MobileException {

        if (creature.getId() == null) {
            log.debug("MobileServiceImpl::fromCreature creature id not set");
            throw new MobileException("can't convert unsaved creature");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Creature>> violations = validator.validate(creature);
        if (violations.size() > 0) {
            for (ConstraintViolation<Creature> violation : violations) {
                log.debug("MobileServiceImpl::create validation violation for creature {} : {}", creature, violation.getMessage());
            }
            throw new MobileException("can't convert invalid creature");
        }

        if (!propertyService.validatePropertyMapNames(creature)) {
            log.debug("MobileServiceImpl::fromCreature propertyMap names didn't validate");
            throw new MobileException("propertyMap names inconsistent");
        }


        Mobile mobile = new Mobile();

        mobile.setCreatureId(creature.getId());
        mobile.setName(resolveMobName(creature.getName()));
        mobile.setOwner(creature.getOwner());

        if (creature.getPropertyMap().containsKey(config.getCreaturePropertyMaxHpName())) {
            Property value = creature.getPropertyMap().get(config.getCreaturePropertyMaxHpName());

            if (value.getType() == PropertyType.INTEGER) {
                mobile.setMaxHp(Integer.parseInt(value.getValue()));
                mobile.setHp(mobile.getMaxHp());
            } else {
                log.debug("MobileServiceImpl::fromCreature maxHP was wrong property Type");
            }
        }

        Mobile savedMobile = mobileDao.save(mobile);

        // copy properties
        try {
            for (Property prop : creature.getPropertyMap().values()) {
                // skip the hp field
                if (prop.getName() == config.getCreaturePropertyMaxHpName()) {
                    continue;
                }

                Property savedProperty = propertyService.copy(prop);
                propertyService.attachToMobile(savedProperty, savedMobile);
                savedMobile.getPropertyMap().put(savedProperty.getName(), savedProperty);
            }
        } catch (PropertyException ex) {
            log.debug("MobileServiceImpl::fromCreature failed to copy properties.");
            throw new MobileException("failed to copy properties", ex);
        }

        return savedMobile;
    }

    private String resolveMobName(String name) throws MobileException {
        if (mobileDao.existsByName(name)) {
            int count = mobileDao.countByNameBeginning(name);
            int tries = 0;
            String attempt;

            do {
                count++;
                tries++;

                if (tries >= config.getMobileNameMaxRetries()) {
                    log.debug("MobileServiceImpl::resolveMobName - failed to resolve {}, too many retries", name);
                    throw new MobileException("unable to resolve Name");
                }

                attempt = String.format("%s_%d", name, count);

            } while (mobileDao.existsByName(attempt));

            return attempt;
        } else {
            return name;
        }

    }
}
