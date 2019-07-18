package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.services.impl.PropertyServiceImpl;
import net.dalamori.GMFriend.testing.TestDataFactory;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class PropertyServiceUnitTest {

    @Autowired
    DmFriendConfig config;

    @Mock private PropertyDao mockDao;
    @Mock private GroupService mockGroupService;
    @Captor private ArgumentCaptor<List<Long>> findIdsCaptor;

    private PropertyService service;
    private Property property;
    private Property savedProperty;

    public static final Long PROP_ID = 777L;
    public static final String PROP_NAME = "KTD Ratio";
    public static final String PROP_VALUE = "3.1415";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        property = TestDataFactory.makeProperty(PROP_NAME);
        property.setValue(PROP_VALUE);

        savedProperty = TestDataFactory.makeProperty(PROP_ID, PROP_NAME);
        property.setValue(PROP_VALUE);

        PropertyServiceImpl impl = new PropertyServiceImpl();

        impl.setConfig(config);
        impl.setGroupService(mockGroupService);
        impl.setPropertyDao(mockDao);

        service = impl;
    }

    @Test
    public void propertyService_copy_shouldHappyPath() throws PropertyException {
        // given: a sample property to return from mock dao
        property.setId(444L);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedProperty);

        // when: I create the property
        Property result = service.copy(property);

        // then: I expect to see the correct return value
        Assert.assertEquals("got correct return value", savedProperty, result);

        // and: I expect to the right calls to the dao, we should save a copy of property without id, not property itself
        Mockito.verify(mockDao, Mockito.never()).save(property);

        property.setId(null);
        Mockito.verify(mockDao).save(Mockito.eq(property));
    }

    @Test
    public void propertyService_create_shouldHappyPath() throws PropertyException {
        // given: a sample property to return from mock dao
        Mockito.when(mockDao.save(property)).thenReturn(savedProperty);

        // when: I create the property
        Property result = service.create(property);

        // then: I expect to see the correct return value
        Assert.assertEquals("got correct return value", savedProperty, result);

        // and: I expect to the right calls to the dao
        Mockito.verify(mockDao).save(property);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_create_shouldFailWhenPropertyIdSet() throws PropertyException {
        // given: a sample property to return from mock dao
        Mockito.when(mockDao.save(property)).thenReturn(savedProperty);

        // and: that my sample property already has an ID set
        property.setId(Long.valueOf(1));

        // when: I create the property
        try {
            service.create(property);

            Assert.fail("should refuse to create a property with an id");

            // then: it should throw an error before the DAO is called
        } catch(PropertyException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = PropertyException.class)
    public void propertyService_create_shouldFailWhenInvalid() throws PropertyException {
        // given: a sample property which doesn't conform to validation rules
        property.setOwner("");
        property.setPrivacy(null);

        // when: I create the property
        try {
            service.create(property);

            Assert.fail("should refuse to create a property which fails validation");

        }

        // then: it should throw an error before the DAO is called
        catch(PropertyException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void propertyService_read_shouldHappyPath() throws PropertyException {
        // given: a sample property saved in the db
        Mockito.when(mockDao.findById(PROP_ID)).thenReturn(Optional.of(savedProperty));

        // when: I try to lookup by ID
        Property result = service.read(PROP_ID);

        // then: I should succeed;
        Assert.assertEquals("properties should match", result, savedProperty);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_read_shouldFailWhenNotFound() throws PropertyException {
        // given; nothing saved in the DB

        // when: I try to lookup by ID
        service.read(PROP_ID);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void propertyService_exists_shouldHappyPath() {
        // given: a sample ID
        Long id = null;

        // and: a mock reply
        Mockito.when(mockDao.existsById(id)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(id);

        // then: I expect to get a short-circuit false
        Assert.assertFalse("should get default return", result);
        Mockito.verify(mockDao, Mockito.never()).existsById(id);
    }

    @Test
    public void propertyService_exists_shouldHandleNulls() {
        // given: a sample ID
        Long id = null;

        // and: a mock reply
        Mockito.when(mockDao.existsById(id)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(id);

        // then: I expect to get a short-circuit false
        Assert.assertFalse("should get default return", result);
        Mockito.verify(mockDao, Mockito.never()).existsById(id);
    }

    @Test
    public void propertyService_update_shouldHappyPath() throws PropertyException {
        // given: an example property with updated attributes to save
        property.setId(PROP_ID);
        property.setPrivacy(PrivacyType.HIDDEN);

        // and: a saved copy of that property in the DB:
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(true);
        Mockito.when(mockDao.save(property)).thenReturn(property);

        // when: I update the property
        Property result = service.update(property);

        // then: I expect to see the updated result
        Assert.assertEquals("return value should come from dao save call", property, result);

        // and: I expect to see the proper calls to the Dao
        Mockito.verify(mockDao).existsById(PROP_ID);
        Mockito.verify(mockDao).save(property);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_update_shouldFailWhenIdNotSet() throws PropertyException {
        // given: an example property with updated attributes to save, but no ID
        property.setId(null);
        property.setPrivacy(PrivacyType.PUBLIC);

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(true);

        // when: I try to update the property
        try {
            service.update(property);

            Assert.fail("should refuse to update a property with a null id");

        }

        // then: it should throw an error before the DAO is called
        catch(PropertyException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = PropertyException.class)
    public void propertyService_update_shouldFailWhenInvalid() throws PropertyException {
        // given: an example property with invalid attributes to save
        property.setId(PROP_ID);
        property.setPrivacy(null);
        property.setOwner("");

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(true);

        // when: I try to update the property
        try {
            service.update(property);

            Assert.fail("should refuse to create a property with bad attributes");

        }

        // then: it should throw an error before the DAO is called
        catch(PropertyException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = PropertyException.class)
    public void propertyService_update_shouldFailWhenNotFound() throws PropertyException {
        // given: an example property with updated attributes to save
        property.setId(PROP_ID);
        property.setPrivacy(PrivacyType.PUBLIC);

        // and: no saved copies appear to be in the db
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(false);

        // when: I try to update the property
        try {
            service.update(property);

            Assert.fail("should refuse to update a property which doesnt exist");

        }

        // then: it should throw an error before the DAO is called
        catch(PropertyException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void propertyService_delete_shouldHappyPath() throws PropertyException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(true);

        // when: I try to delete the property
        property.setId(PROP_ID);
        service.delete(property);

        // then: I expect to see that call pass thru to to dao
        Mockito.verify(mockDao).deleteById(PROP_ID);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_delete_shouldFailWhenIdNotSet() throws PropertyException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(true);

        // when: I try to delete the property
        property.setId(null);
        service.delete(property);

        // then: I expect to get an error
        Assert.fail("should have thrown an error for no id by now");
    }

    @Test(expected = PropertyException.class)
    public void propertyService_delete_shouldFailWhenNotFound() throws PropertyException {
        // given: dao is set to report that property doesn't exist
        Mockito.when(mockDao.existsById(PROP_ID)).thenReturn(false);

        // when: I try to delete the property
        property.setId(PROP_ID);
        service.delete(property);

        // then: I expect to see that call fail with a not found
        Assert.fail("should have thrown a not found error by now");
    }

    @Test
    public void propertyService_attachToCreature_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved creature
        Creature creature = TestDataFactory.makeCreature(1776L, "Scott");

        // and: a saved property
        Long id = 4321L;
        property.setId(id);

        // when: I try to attach the property
        service.attachToCreature(property, creature);

        // then: the group should have the new content
        Assert.assertTrue("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_attachToCreature_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: an un-saved creature
        Creature creature = TestDataFactory.makeCreature(1776L, "Charles");

        // and: an un-saved property
        property.setId(null);

        // when: I try to attach the property
        service.attachToCreature(property, creature);

        // then: I should get a PropertyException
        Assert.fail("Should refuse to attach the property");
    }

    @Test(expected = PropertyException.class)
    public void propertyService_attachToCreature_shouldFailWhenCreatureIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved creature
        Creature creature = TestDataFactory.makeCreature(null, "Scott");

        // and: a saved property
        Long id = 4321L;
        property.setId(id);

        // when: I try to attach the property
        service.attachToCreature(property, creature);

        // then: I should get a PropertyException
        Assert.fail("Should refuse to attach the property");
    }

    @Test
    public void propertyService_attachToMobile_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile(1776L, "Scott");

        // and: a saved property
        Long id = 4321L;
        property.setId(id);

        // when: I try to attach the property
        service.attachToMobile(property, mobile);

        // then: the group should have the new content
        Assert.assertTrue("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_attachToMobile_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile(1776L, "Scott");

        // and: a saved property
        property.setId(null);

        // when: I try to attach the property
        service.attachToMobile(property, mobile);

        // then: I should fail
        Assert.fail("should refuse to attach property to unsaved mobile");
    }

    @Test(expected = PropertyException.class)
    public void propertyService_attachToMobile_shouldFailWhenMobileIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile(null, "Scott");

        // and: a saved property
        property.setId(654321L);

        // when: I try to attach the property
        service.attachToMobile(property, mobile);

        // then: I should fail
        Assert.fail("should refuse to attach property to unsaved mobile");
    }

    @Test
    public void propertyService_attachToGlobalContext_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved property
        Long id = 4321L;
        property.setId(id);

        // when: I try to attach the property
        service.attachToGlobalContext(property);

        // then: the group should have the new content
        Assert.assertTrue("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_attachToGlobalContext_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved property
        property.setId(null);

        // when: I try to attach the property
        service.attachToGlobalContext(property);

        // then: I should Fail
        Assert.fail("should refuse to attach the property");
    }

    @Test
    public void propertyService_detachFromCreature_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved creature
        Creature creature = TestDataFactory.makeCreature();
        creature.setId(1555L);

        // and: a saved property
        Long id = 4321L;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromCreature(property, creature);

        // then: the group should have the new content
        Assert.assertFalse("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_detachFromCreature_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved creature
        Creature creature = TestDataFactory.makeCreature(1555L, "Swamp Thing");

        // and: a saved property
        Long id = null;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromCreature(property, creature);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved Property");
    }

    @Test(expected = PropertyException.class)
    public void propertyService_detachFromCreature_shouldFailWhenCreatureIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved creature
        Creature creature = TestDataFactory.makeCreature(null, "Aquaman");

        // and: a saved property
        Long id = 8080L;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromCreature(property, creature);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved Property");
    }

    @Test
    public void propertyService_detachFromMobile_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile();
        mobile.setId(1555L);

        // and: a saved property
        Long id = 4321L;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromMobile(property, mobile);

        // then: the group should have the new content
        Assert.assertFalse("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_detachFromMobile_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile(1555L, "Swamp Thing");

        // and: a saved property
        Long id = null;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromMobile(property, mobile);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved Property");
    }

    @Test(expected = PropertyException.class)
    public void propertyService_detachFromMobile_shouldFailWhenMobileIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved mobile
        Mobile mobile = TestDataFactory.makeMobile(null, "Aquaman");

        // and: a saved property
        Long id = 8080L;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromMobile(property, mobile);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved Property");
    }

    @Test
    public void propertyService_detachFromGlobalContext_shouldHappyPath() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved property
        Long id = 4321L;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromGlobalContext(property);

        // then: the group should have the new content
        Assert.assertFalse("group should contain propertyId", propertyGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(propertyGroup);
    }

    @Test(expected = PropertyException.class)
    public void propertyService_detachFromGlobalContext_shouldFailWhenPropertyIdNotSet() throws GroupException, PropertyException {
        // given: a group
        Group propertyGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.PROPERTY)))
                .thenReturn(propertyGroup);

        // and: a saved property
        Long id = null;
        property.setId(id);
        propertyGroup.getContents().add(id);

        // when: I try to detach the property
        service.detachFromGlobalContext(property);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved Property");
    }

    @Test
    public void propertyService_getCreatureProperties_shouldHappyPath() throws GroupException, PropertyException {
        // given: a mock response list
        Long id = 1400L;
        List<Property> propertyList = new ArrayList<>();
        propertyList.add(TestDataFactory.makeProperty(id, "Property A"));
        propertyList.add(TestDataFactory.makeProperty(id + 1, "Property B"));
        propertyList.add(TestDataFactory.makeProperty(id + 2, "Property C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(propertyList);

        // and: a group containing the IDs
        Group creaturePropertyGroup = TestDataFactory.makeGroup();
        Set<Long> creaturePropertyContents = creaturePropertyGroup.getContents();
        creaturePropertyContents.add(id);
        creaturePropertyContents.add(id + 1);
        creaturePropertyContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(creaturePropertyGroup);

        // and: a creature;
        Creature creature = TestDataFactory.makeCreature(5400L, "Test Creature");

        // when: I get global properties
        List<Property> result = service.getCreatureProperties(creature);

        // then: i expect to see the the mock propertyList passed back as retval
        Assert.assertEquals("should return propertyList", propertyList, result);

        // and: I expect to see the group contents passed to the propertyDao
        Mockito.verify(mockDao).findAllById(findIdsCaptor.capture());
        Set<Long> capturedIds = (Set<Long>) findIdsCaptor.getValue();
        Assert.assertEquals("should pass mock group contents into dao", creaturePropertyContents, capturedIds);
    }

    @Test
    public void propertyService_getMobileProperties_shouldHappyPath() throws GroupException, PropertyException {
        // given: a mock response list
        Long id = 1400L;
        List<Property> propertyList = new ArrayList<>();
        propertyList.add(TestDataFactory.makeProperty(id, "Property A"));
        propertyList.add(TestDataFactory.makeProperty(id + 1, "Property B"));
        propertyList.add(TestDataFactory.makeProperty(id + 2, "Property C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(propertyList);

        // and: a group containing the IDs
        Group mobilePropertyGroup = TestDataFactory.makeGroup();
        Set<Long> mobilePropertyContents = mobilePropertyGroup.getContents();
        mobilePropertyContents.add(id);
        mobilePropertyContents.add(id + 1);
        mobilePropertyContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(mobilePropertyGroup);

        // and: a mobile;
        Mobile mobile = TestDataFactory.makeMobile(5400L, "Test Mobile");

        // when: I get global properties
        List<Property> result = service.getMobileProperties(mobile);

        // then: i expect to see the the mock propertyList passed back as retval
        Assert.assertEquals("should return propertyList", propertyList, result);

        // and: I expect to see the group contents passed to the propertyDao
        Mockito.verify(mockDao).findAllById(findIdsCaptor.capture());
        Set<Long> capturedIds = (Set<Long>) findIdsCaptor.getValue();
        Assert.assertEquals("should pass mock group contents into dao", mobilePropertyContents, capturedIds);
    }

    @Test
    public void propertyService_getGlobalProperties_shouldHappyPath() throws GroupException, PropertyException {
        // given: a mock response list
        Long id = 1400L;
        List<Property> propertyList = new ArrayList<>();
        propertyList.add(TestDataFactory.makeProperty(id, "Property A"));
        propertyList.add(TestDataFactory.makeProperty(id + 1, "Property B"));
        propertyList.add(TestDataFactory.makeProperty(id + 2, "Property C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(propertyList);

        // and: a group containing the IDs
        Group globalPropertyGroup = TestDataFactory.makeGroup();
        Set<Long> globalPropertyContents = globalPropertyGroup.getContents();
        globalPropertyContents.add(id);
        globalPropertyContents.add(id + 1);
        globalPropertyContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(globalPropertyGroup);

        // when: I get global properties
        Map<String, Property> result = service.getGlobalProperties();

        // then: i expect to see the the mock propertyList passed back as retval
        Assert.assertEquals("should return a map with 3 properties", 3, result.size());
        Assert.assertEquals("should contain Property A", propertyList.get(0), result.get("Property A"));
        Assert.assertEquals("should contain Property B", propertyList.get(1), result.get("Property B"));
        Assert.assertEquals("should contain Property C", propertyList.get(2), result.get("Property C"));

        // and: I expect to see the group contents passed to the propertyDao
        Mockito.verify(mockDao).findAllById(findIdsCaptor.capture());
        Set<Long> capturedIds = (Set<Long>) findIdsCaptor.getValue();
        Assert.assertEquals("should pass mock group contents into dao", globalPropertyContents, capturedIds);
    }
}
