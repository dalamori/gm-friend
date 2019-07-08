package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.PropertyService;
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
@Transactional(rollbackFor = CreatureException.class)
@Service("creatureService")
public class CreatureServiceImpl implements CreatureService {

    @Autowired
    private DmFriendConfig config;

    @Autowired
    private CreatureDao creatureDao;

    @Autowired
    private PropertyService propertyService;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public Creature create(Creature creature) throws CreatureException {

        if (creature.getId() != null) {
            log.debug("CreatureServiceImpl::create asked to create a creature which already has an id");
            throw new CreatureException("asked to create creature which already has id");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Creature>> violations = validator.validate(creature);
        if (violations.size() > 0) {
            for (ConstraintViolation<Creature> violation : violations) {
                log.debug("CreatureServiceImpl::create validation violation for creature {} : {}", creature, violation.getMessage());
            }

            throw new CreatureException("asked to create invalid creature");
        }

        if (creatureDao.existsByName(creature.getName())) {
            log.debug("CreatureServiceImpl::create name collision detected for {}", creature.getName());
            throw new CreatureException("a creature by that name already exists");
        }

        if (!propertyService.validatePropertyMapNames(creature)) {
            log.debug("CreatureServiceImpl::create propertyMap validation for creature {}", creature);
            throw new CreatureException("propertyMap validation failed");
        }

        Creature savedCreature = creatureDao.save(creature);

        // copy properties
        try {
            for (String key : creature.getPropertyMap().keySet()) {
                Property property = creature.getPropertyMap().get(key);
                Property savedProperty;
                if (property.getId() == null) {
                    savedProperty = propertyService.create(property);
                } else {
                    savedProperty = property;
                }
                propertyService.attachToCreature(savedProperty, savedCreature);
                savedCreature.getPropertyMap().put(key, savedProperty);
            }
        } catch (PropertyException ex) {
            log.debug("CreatureServiceImpl::create failed to create properties, ex");
            throw new CreatureException("failed to create properties", ex);
        }

        return savedCreature;
    }

    @Override
    public Creature read(Long id) throws CreatureException {

        Optional<Creature> result = creatureDao.findById(id);

        if (!result.isPresent()) {
            log.debug("CreatureServiceImpl::read - Id # {} not found", id);
            throw new CreatureException("not found");
        }

        Creature creature = result.get();
        try {
            List<Property> properties = propertyService.getCreatureProperties(creature);

            for (Property property : properties) {
                creature.getPropertyMap().put(property.getName(), property);
            }
        } catch (PropertyException ex) {
            log.debug("CreatureServiceImpl::read - couldn't read properties for creature id #", id, ex);
            throw new CreatureException("error reading properties", ex);
        }

        return creature;
    }

    @Override
    public Creature read(String name) throws CreatureException {
        Optional<Creature> result = creatureDao.findByName(name);

        if (!result.isPresent()) {
            log.debug("CreatureServiceImpl::read - Id # {} not found", name);
            throw new CreatureException("not found");
        }

        Creature creature = result.get();
        try {
            List<Property> properties = propertyService.getCreatureProperties(creature);

            for (Property property : properties) {
                creature.getPropertyMap().put(property.getName(), property);
            }
        } catch (PropertyException ex) {
            log.debug("CreatureServiceImpl::read - couldn't read properties for creature named ", name, ex);
            throw new CreatureException("error reading properties", ex);
        }

        return creature;
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return creatureDao.existsById(id);
        }

        return false;
    }

    @Override
    public boolean exists(String name) {
        if (name != null) {
            return creatureDao.existsByName(name);
        }

        return false;
    }

    @Override
    public Creature update(Creature creature) throws CreatureException {
        if (creature.getId() == null) {
            log.debug("CreatureServiceImpl::update asked to create a creature which has null id");
            throw new CreatureException("asked to update creature which has no id");
        }

        if (!creatureDao.existsById(creature.getId())) {
            log.debug("CreatureServiceImpl::update creature with id # {} not found", creature.getId());
            throw new CreatureException("asked to update creature which is not found");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Creature>> violations = validator.validate(creature);
        if (violations.size() > 0) {
            for (ConstraintViolation<Creature> violation : violations) {
                log.debug("CreatureServiceImpl::update validation violation for creature {} : {}", creature, violation.getMessage());
            }

            throw new CreatureException("asked to update invalid creature");
        }

        if (!propertyService.validatePropertyMapNames(creature)) {
            log.debug("CreatureServiceImpl::update propertyMap validation for creature {}", creature);
            throw new CreatureException("propertyMap validation failed");
        }

        Creature savedCreature = creatureDao.save(creature);

        // sync properties
        try {
            List<Property> originalProperties = propertyService.getCreatureProperties(creature);
            Map<Long, Property> propertyIdMap = new HashMap<>();
            Set<Property> propertiesToRemove = new HashSet<>();

            propertiesToRemove.addAll(originalProperties);

            // populate id map
            for (Property property : originalProperties) {
                propertyIdMap.put(property.getId(), property);
            }

            // check against map for properties to delete
            for (String key : creature.getPropertyMap().keySet()) {
                Property property = creature.getPropertyMap().get(key);

                // props without an id need created.
                if (property.getId() == null) {
                    property = propertyService.create(property);
                    propertyService.attachToCreature(property, savedCreature);
                }

                if (propertyIdMap.containsKey(property.getId())) {
                    propertiesToRemove.remove(propertyIdMap.get(property.getId()));
                }

                savedCreature.getPropertyMap().put(property.getName(), property);
            }

            // remove
            for (Property property : propertiesToRemove) {
                propertyService.detachFromCreature(property, savedCreature);
            }

        } catch(PropertyException ex) {
            log.debug("CreatureServiceImpl::update failed to sync properties");
            throw new CreatureException("failed to sync properties", ex);
        }

        return savedCreature;
    }

    @Override
    public void delete(Creature creature) throws CreatureException {
        if (creature.getId() == null) {
            log.debug("CreatureServiceImpl::delete asked to delete creature without an id");
            throw new CreatureException("asked to save creature without id");
        }

        if (!creatureDao.existsById(creature.getId())) {
            log.debug("CreatureServiceImpl::delete creature not found");
            throw new CreatureException("creature not found");
        }

        try {
            List<Property> properties = propertyService.getCreatureProperties(creature);
            for (Property property : properties) {
                propertyService.detachFromCreature(property, creature);
            }
        } catch (PropertyException ex) {
            log.debug("CreatureServiceImpl::delete unable to unlink creature properties");
            throw new CreatureException("failed to unlink creature properties", ex);
        }

        creatureDao.deleteById(creature.getId());
    }

    @Override
    public Creature fromMobile(Mobile mobile) throws CreatureException {

        if (mobile.getId() == null) {
            log.debug("CreatureServiceImpl::fromMobile mobile id not set");
            throw new CreatureException("can't convert unsaved mobile");
        }

        if (creatureDao.existsByName(mobile.getName())) {
            log.debug("CreatureServiceImpl::fromMobile creature with name {} already exists", mobile.getName());
            throw new CreatureException("cant convert mobile due to creature name collision");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();
        Set<ConstraintViolation<Mobile>> violations = validator.validate(mobile);
        if (violations.size() > 0) {
            for (ConstraintViolation<Mobile> violation : violations) {
                log.debug("CreatureServiceImpl::create validation violation for creature {} : {}", mobile, violation.getMessage());
            }
            throw new CreatureException("can't convert invalid mobile");
        }

        if (!propertyService.validatePropertyMapNames(mobile)) {
            log.debug("CreatureServiceImpl::fromMobile propertyMap names didn't validate");
            throw new CreatureException("propertyMap names inconsistent");
        }

        Creature creature = new Creature();
        creature.setName(mobile.getName());
        creature.setOwner(mobile.getOwner());
        creature.setPrivacy(mobile.getPrivacy());

        Creature savedCreature = creatureDao.save(creature);

        // copy properties
        try {
            for (Property prop : mobile.getPropertyMap().values()) {
                Property savedProperty = propertyService.copy(prop);
                propertyService.attachToCreature(savedProperty, savedCreature);
                savedCreature.getPropertyMap().put(savedProperty.getName(), savedProperty);
            }
        } catch (PropertyException ex) {
            log.debug("CreatureServiceImpl::fromMobile failed to copy properties.");
            throw new CreatureException("failed to copy properties", ex);
        }

        // make prop to store HP
        if (!savedCreature.getPropertyMap().containsKey(config.getCreaturePropertyMaxHpName())) {
            Property hp = new Property();
            hp.setOwner(savedCreature.getOwner());
            hp.setType(PropertyType.INTEGER);
            hp.setPrivacy(PrivacyType.NORMAL);
            hp.setName(config.getCreaturePropertyMaxHpName());
            hp.setValue(Long.toString(mobile.getMaxHp()));

            try {
                hp = propertyService.create(hp);
                propertyService.attachToCreature(hp, savedCreature);
                savedCreature.getPropertyMap().put(hp.getName(), hp);
            } catch (PropertyException ex) {
                log.debug("CreatureServiceImpl::fromMobile failed to create hp property");
                throw new CreatureException("failed to create hp property");
            }
        }

        return savedCreature;
    }

}
