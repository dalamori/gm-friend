package net.dalamori.GMFriend.services.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasProperties;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@Slf4j
@Service("propertyService")
@Transactional(rollbackFor = {PropertyException.class, GroupException.class})
public class PropertyServiceImpl implements PropertyService {

    @Autowired
    private PropertyDao propertyDao;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DmFriendConfig config;

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public Property copy(Property property) throws PropertyException {
        // create will do all our validation for us...
        Property clone = new Property();

        clone.setName(property.getName());
        clone.setOwner(property.getOwner());
        clone.setPrivacy(property.getPrivacy());
        clone.setType(property.getType());
        clone.setValue(property.getValue());

        return create(clone);
    }

    @Override
    public Property create(Property property) throws PropertyException {
        if (property.getId() instanceof Long) {
            log.debug("PropertyServiceImpl::create - already has Id");
            throw new PropertyException("property to create already has an ID set");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();

        Set<ConstraintViolation<Property>> violations = validator.validate(property);
        if (violations.size() > 0) {
            for (ConstraintViolation<Property> violation : violations) {
                log.debug("PropertyServiceImpl::create validation violation : {}", violation.getMessage());
            }

            throw new PropertyException("property to create failed validation");
        }

        try {
            return propertyDao.save(property);
        } catch (Throwable ex) {
            log.info("PropertyServiceImpl::create Record insert failed: {}", property, ex);
            throw new PropertyException("SQL failed to insert", ex);
        }
    }

    @Override
    public Property read(Long id) throws PropertyException {
        Optional<Property> result = propertyDao.findById(id);

        if (!result.isPresent()) {
            log.debug("PropertyServiceImpl::read - ID {} not found", id);
            throw new PropertyException("Not Found");
        }

        return result.get();
    }

    @Override
    public boolean exists(Long id) {
        if (id != null) {
            return propertyDao.existsById(id);
        }

        return false;
    }

    @Override
    public Property update(Property property) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::update - cannot update property with null Id");
            throw new PropertyException("property id cannot be null");
        }

        if (!propertyDao.existsById(property.getId())) {
            log.debug("PropertyServiceImpl::update - property with ID {} not found", property.getId());
            throw new PropertyException("Property not found");
        }

        Validator validator = VALIDATOR_FACTORY.getValidator();

        Set<ConstraintViolation<Property>> violations = validator.validate(property);
        if (violations.size() > 0) {
            for (ConstraintViolation<Property> violation : violations) {
                log.debug("PropertyServiceImpl::update validation violation : {}", violation.getMessage());
            }

            throw new PropertyException("property to create failed validation");
        }

        try {
            return propertyDao.save(property);
        } catch (Throwable ex) {
            log.info("PropertyServiceImpl::update Record update failed: {}", property, ex);
            throw new PropertyException("SQL failed to update", ex);
        }
    }

    @Override
    public void delete(Property property) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::delete - cannot update property with null Id");
            throw new PropertyException("property id cannot be null");
        }

        if (!propertyDao.existsById(property.getId())) {
            log.debug("PropertyServiceImpl::delete - Property Id {} not found", property.getId());
            throw new PropertyException("Property not found");
        }

        try {
            propertyDao.deleteById(property.getId());
        } catch (Throwable ex) {
            log.info("PropertyServiceImpl::delete failed to delete {}", property, ex);
            throw new PropertyException("SQL failed to delete");
        }
    }

    @Override
    public void attachToMobile(Property property, Mobile mobile) throws PropertyException {

        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::attachToMobile - asked to attach unsaved property");
            throw new PropertyException("can't attach unsaved property");
        }

        if (mobile.getId() == null) {
            log.debug("PropertyServiceImpl::attachToMobile - asked to attach to unsaved mobile");
            throw new PropertyException("can't attach to unsaved mobile");
        }

        try {
            Group propertys = resolveMobilePropertiesGroup(mobile);

            propertys.getContents().add(property.getId());

            groupService.update(propertys);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::attachToMobile failed to attach property {} to mobile {}", property, mobile, ex);
            throw new PropertyException("unable to attach property to mobile", ex);
        }
    }

    @Override
    public void attachToCreature(Property property, Creature creature) throws PropertyException {

        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::attachToCreature - asked to attach unsaved property");
            throw new PropertyException("can't attach unsaved property");
        }

        if (creature.getId() == null) {
            log.debug("PropertyServiceImpl::attachToCreature - asked to attach to unsaved creature");
            throw new PropertyException("can't attach to unsaved creature");
        }

        try {
            Group propertys = resolveCreaturePropertiesGroup(creature);

            propertys.getContents().add(property.getId());

            groupService.update(propertys);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::attachToCreature failed to attach property {} to creature {}", property, creature, ex);
            throw new PropertyException("unable to attach property to creature", ex);
        }
    }

    @Override
    public void attachToGlobalContext(Property property) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::attachToCreature - asked to attach unsaved property");
            throw new PropertyException("can't attach unsaved property");
        }

        try {
            Group properties = resolveGlobalPropertiesGroup();

            properties.getContents().add(property.getId());

            groupService.update(properties);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::attachToCreature failed to attach property {} to global context", property, ex);
            throw new PropertyException("unable to attach property to creature", ex);
        }
    }

    @Override
    public void detachFromCreature(Property property, Creature creature) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::detachFromCreature - asked to detach unsaved property");
            throw new PropertyException("can't detach unsaved property");
        }

        if (creature.getId() == null) {
            log.debug("PropertyServiceImpl::detachFromCreature - asked to detach from unsaved creature");
            throw new PropertyException("can't detach from unsaved creature");
        }

        try {
            Group properties = resolveCreaturePropertiesGroup(creature);

            properties.getContents().remove(property.getId());

            groupService.update(properties);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::detachToCreature failed to detach property {} from creature {}", property, creature, ex);
            throw new PropertyException("unable to detach property from creature", ex);
        }
    }

    @Override
    public void detachFromMobile(Property property, Mobile mobile) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::detachFromMobile - asked to detach unsaved property");
            throw new PropertyException("can't detach unsaved property");
        }

        if (mobile.getId() == null) {
            log.debug("PropertyServiceImpl::detachFromMobile - asked to detach from unsaved mobile");
            throw new PropertyException("can't detach from unsaved mobile");
        }

        try {
            Group propertys = resolveMobilePropertiesGroup(mobile);

            propertys.getContents().remove(property.getId());

            groupService.update(propertys);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::detachToMobile failed to detach property {} from mobile {}", property, mobile, ex);
            throw new PropertyException("unable to detach property from mobile", ex);
        }
    }

    @Override
    public void detachFromGlobalContext(Property property) throws PropertyException {
        if (property.getId() == null) {
            log.debug("PropertyServiceImpl::detachFromMobile - asked to detach unsaved property");
            throw new PropertyException("can't detach unsaved property");
        }

        try {
            Group propertys = resolveGlobalPropertiesGroup();

            propertys.getContents().remove(property.getId());

            groupService.update(propertys);
        } catch (GroupException ex) {
            log.warn("PropertyServiceImpl::detachToMobile failed to detach property {}", property, ex);
            throw new PropertyException("unable to detach property", ex);
        }
    }

    @Override
    public List<Property> getGlobalProperties() throws PropertyException {
        List<Property> list = new ArrayList<>();

        try {
            Group notes = resolveGlobalPropertiesGroup();

            propertyDao.findAllById(notes.getContents()).iterator().forEachRemaining(list::add);

            return list;

        } catch (GroupException ex) {
            throw new PropertyException("Unable to retrieve creature properties", ex);
        }
    }

    @Override
    public List<Property> getCreatureProperties(Creature creature) throws PropertyException {
        List<Property> list = new ArrayList<>();

        try {
            Group notes = resolveCreaturePropertiesGroup(creature);

            propertyDao.findAllById(notes.getContents()).iterator().forEachRemaining(list::add);

            return list;

        } catch (GroupException ex) {
            throw new PropertyException("Unable to retrieve creature properties", ex);
        }
    }

    @Override
    public List<Property> getMobileProperties(Mobile mobile) throws PropertyException {
        List<Property> list = new ArrayList<>();

        try {
            Group notes = resolveMobilePropertiesGroup(mobile);

            propertyDao.findAllById(notes.getContents()).iterator().forEachRemaining(list::add);

            return list;

        } catch (GroupException ex) {
            throw new PropertyException("Unable to retrieve creature properties", ex);
        }
    }

    @Override
    public boolean validatePropertyMapNames(HasProperties subject) {
        Map<String, Property> propertyMap = subject.getPropertyMap();
        Iterator<String> keys = propertyMap.keySet().iterator();

        while(keys.hasNext()) {
            String key = keys.next();

            if (!propertyMap.get(key).getName().equals(key)) {
                return false;
            }
        }

        return true;
    }

    private Group resolveGlobalPropertiesGroup() throws GroupException {
        String name = config.getSystemGroupPrefix().concat(config.getSystemGroupGlobalVarsAction());

        return groupService.resolveSystemGroup(name, PropertyType.PROPERTY);
    }

    private Group resolveCreaturePropertiesGroup(Creature creature) throws GroupException, PropertyException {

        if (creature.getId() == null) {
            log.debug("PropertyServiceImpl::resolveCreaturePropertyGroup asked to resolve properties for unsaved location");
            throw new PropertyException(" cant lookup for creature with null id");
        }

        String name = String.format("%s%s%d",
                config.getSystemGroupPrefix(),
                config.getSystemGroupCreaturePropertyAction(),
                creature.getId());

        return groupService.resolveSystemGroup(name, PropertyType.PROPERTY);
    }

    private Group resolveMobilePropertiesGroup(Mobile mobile) throws GroupException, PropertyException {

        if (mobile.getId() == null) {
            log.debug("PropertyServiceImpl::resolveMobilePropertyGroup asked to resolve properties for unsaved location");
            throw new PropertyException(" cant lookup for mobile with null id");
        }

        String name = String.format("%s%s%d",
                config.getSystemGroupPrefix(),
                config.getSystemGroupMobilePropertyAction(),
                mobile.getId());

        return groupService.resolveSystemGroup(name, PropertyType.PROPERTY);
    }
}

