package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.services.impl.CreatureServiceImpl;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@Category(UnitTest.class)
@SpringBootTest
public class CreatureServiceUnitTest {

    @Autowired
    private DmFriendConfig config;

    @Mock private CreatureDao mockDao;
    @Mock private PropertyService mockPropertyService;

    @Captor private ArgumentCaptor<Creature> creatureCaptor;

    private CreatureService service;

    private Creature steve;
    private Creature savedSteve;
    private Property propA;
    private Property propB;
    private Property propC;

    private static final Long STEVE_ID = 42L;
    private static final Long PROP_A_ID = 123L;
    private static final Long PROP_B_ID = 124L;
    private static final Long PROP_C_ID = 125L;

    private static final Answer<Property> MOCK_PROPERTY_SAVE = new Answer<Property>() {
        @Override
        public Property answer(InvocationOnMock invocation) throws Throwable {
            Property property = invocation.getArgument(0);
            Property clone = new Property();

            clone.setName(property.getName());
            clone.setOwner(property.getOwner());
            clone.setPrivacy(property.getPrivacy());
            clone.setType(property.getType());
            clone.setValue(property.getValue());
            clone.setId(property.getId());

            return clone;
        }
    };

    @Before
    public void setup() {
        // mocks
        MockitoAnnotations.initMocks(this);

        // fixtures
        steve = TestDataFactory.makeCreature("steve");
        savedSteve = TestDataFactory.makeCreature(STEVE_ID, "steve");

        propA = TestDataFactory.makeProperty(PROP_A_ID, "A");
        propB = TestDataFactory.makeProperty(PROP_B_ID, "B");
        propC = TestDataFactory.makeProperty("C");

        // service
        CreatureServiceImpl impl = new CreatureServiceImpl();
        impl.setCreatureDao(mockDao);
        impl.setPropertyService(mockPropertyService);
        impl.setConfig(config);

        service = impl;
    }

    @Test
    public void creatureService_create_shouldHappyPath() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to succeed.
        Assert.assertEquals("should get savedSteve", savedSteve, result);
        Mockito.verify(mockDao).save(steve);

        // and: I expect saved properties in savedSteve
        Assert.assertEquals("should have 3 properties", 3, result.getPropertyMap().size());

        Mockito.verify(mockPropertyService).create(propC);

        // and the properties should each get an attach call
        Mockito.verify(mockPropertyService).attachToCreature(propA, savedSteve);
        Mockito.verify(mockPropertyService).attachToCreature(propB, savedSteve);
        Mockito.verify(mockPropertyService).attachToCreature(propC, savedSteve);

    }

    @Test(expected = CreatureException.class)
    public void creatureService_create_shouldFailWhenIdSet() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Steve has an ID set
        steve.setId(STEVE_ID);

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a creature whose Id is set");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_create_shouldFailWhenNameWouldCollide() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Steve is already taken
        Mockito.when(mockDao.existsByName("steve")).thenReturn(true);

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a creature whose name would collide");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_create_shouldFailWhenInvalid() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Steve is somehow invalid
        steve.setOwner("");

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a creature which fails java bean validation");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_create_shouldFailWhenPropertyInvalid() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that a property on steve is invalid in some way
        propB.setOwner("");

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a creature which has an invalid property");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_create_shouldFailWhenPropertyMappingInvalid() throws CreatureException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(steve)).thenReturn(savedSteve);

        // and: a few properties to save with him
        steve.getPropertyMap().put("A", propA);
        steve.getPropertyMap().put("B", propB);
        steve.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: that Steve's property mappings are wrong
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I save steve
        Creature result = service.create(steve);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a creature whose property mappings are wrong");
    }

    @Test
    public void creatureService_read_shouldHappyPathById() throws CreatureException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findById(STEVE_ID)).thenReturn(Optional.of(savedSteve));

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(savedSteve)).thenReturn(properties);

        // when: I look the creature up by Id
        Creature result = service.read(STEVE_ID);

        // then: I expect to get a copy of savedSteve
        Assert.assertEquals("should be savedSteve", savedSteve, result);

        // and: he should have the two properties I mocked the list for.
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propB", propB, result.getPropertyMap().get("B"));

    }

    @Test(expected = CreatureException.class)
    public void creatureService_read_shouldFailWhenNotFoundById() throws CreatureException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findById(STEVE_ID)).thenReturn(Optional.empty());

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(savedSteve)).thenReturn(properties);

        // when: I look the creature up by Id
        Creature result = service.read("steve");

        // then: I expect to fail
        Assert.fail("Should be unable to look up null");
    }

    @Test
    public void creatureService_read_shouldHappyPathByName() throws CreatureException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findByName("steve")).thenReturn(Optional.of(savedSteve));

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(savedSteve)).thenReturn(properties);

        // when: I look the creature up by Id
        Creature result = service.read("steve");

        // then: I expect to get a copy of savedSteve
        Assert.assertEquals("should be savedSteve", savedSteve, result);

        // and: he should have the two properties I mocked the list for.
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propB", propB, result.getPropertyMap().get("B"));
    }

    @Test(expected = CreatureException.class)
    public void creatureService_read_shouldHandleNullStrings() throws CreatureException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findByName("steve")).thenReturn(Optional.empty());

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(savedSteve)).thenReturn(properties);

        // when: I look the creature up by Id
        Creature result = service.read("steve");

        // then: I expect to fail
        Assert.fail("Should be unable to look up null");
    }

    @Test
    public void creatureService_update_shouldHappyPath() throws CreatureException, PropertyException {
        // given: an updated copy of steve
        steve.setId(STEVE_ID);
        steve.setPrivacy(PrivacyType.PUBLIC);
        steve.getPropertyMap().put("B", propB );
        steve.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: a list of steve's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(steve)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // when: I update steve
        Creature result = service.update(steve);

        // then: I expect to get a copy of savedSteve back
        Assert.assertEquals("should return savedSteve", savedSteve, result);
        Mockito.verify(mockDao).save(steve);

        // and he should have the right properties, creating there needed
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should have prop B", propB, result.getPropertyMap().get("B"));
        Assert.assertEquals("should have prop C", propC, result.getPropertyMap().get("C"));

        Mockito.verify(mockPropertyService).detachFromCreature(propA, savedSteve);
        Mockito.verify(mockPropertyService).attachToCreature(propC, savedSteve);

        Mockito.verify(mockPropertyService, Mockito.never()).detachFromCreature(propB, savedSteve);
        Mockito.verify(mockPropertyService, Mockito.never()).attachToCreature(propB, savedSteve);

        Mockito.verify(mockPropertyService).create(propC);

    }

    @Test(expected = CreatureException.class)
    public void creatureService_update_shouldFailWhenIdNotSet() throws CreatureException, PropertyException {
        // given: an updated copy of steve
        steve.setPrivacy(PrivacyType.PUBLIC);
        steve.getPropertyMap().put("B", propB );
        steve.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: a list of steve's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(steve)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: creature ID is not set
        steve.setId(null);

        // when: I update steve
        Creature result = service.update(steve);

        // then: I expect to fail
        Assert.fail("Should refuse to update a creature whose id is not set");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_update_shouldFailWhenInvalid() throws CreatureException, PropertyException {
        // given: an updated copy of steve
        steve.setPrivacy(PrivacyType.PUBLIC);
        steve.getPropertyMap().put("B", propB );
        steve.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: a list of steve's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(steve)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: creature is somehow invalid
        steve.setOwner(null);

        // when: I update steve
        Creature result = service.update(steve);

        // then: I expect to fail
        Assert.fail("Should refuse to update a creature whose id is not set");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_update_shouldFailWhenPropertyInvalid() throws CreatureException, PropertyException {
        // given: an updated copy of steve
        steve.setPrivacy(PrivacyType.PUBLIC);
        steve.getPropertyMap().put("B", propB );
        steve.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: a list of steve's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(steve)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: a property fails java bean validation
        propB.setValue(null);

        // when: I update steve
        Creature result = service.update(steve);

        // then: I expect to fail
        Assert.fail("Should refuse to update a creature with an invalid property");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_update_shouldFailWhenPropertyMappingInvalid() throws CreatureException, PropertyException {
        // given: an updated copy of steve
        steve.setPrivacy(PrivacyType.PUBLIC);
        steve.getPropertyMap().put("B", propB );
        steve.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: a list of steve's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(steve)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: the mapping validation is set to fail
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I update steve
        Creature result = service.update(steve);

        // then: I expect to fail
        Assert.fail("Should refuse to update a creature whose id is not set");
    }

    @Test
    public void creatureService_delete_shouldHappyPath() throws CreatureException, PropertyException {
        // given: Steve appears to be saved in the database
        steve.setId(STEVE_ID);
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: steve appears to have properties saved
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getCreatureProperties(Mockito.any())).thenReturn(properties);

        // when: I try to delete
        service.delete(steve);

        // then: I should succeed;
        Mockito.verify(mockDao).deleteById(STEVE_ID);

        // and: we should see the properties unlinked.
        Mockito.verify(mockPropertyService).detachFromCreature(propA, steve);
        Mockito.verify(mockPropertyService).detachFromCreature(propB, steve);
    }

    @Test(expected = CreatureException.class)
    public void creatureService_delete_shouldFailWhenIdNotSet() throws CreatureException {
        // given: Steve appears to be saved in the database
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(true);

        // and: id is not set
        steve.setId(null);

        // when: I try to delete
        service.delete(steve);

        // then: I should fail
        Assert.fail("should refuse to delete a creature with unset id");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_delete_shouldFailWhenNotFound() throws CreatureException {
        // given: Steve has an id set
        steve.setId(STEVE_ID);

        // and: the dao reports it doesn't exist
        Mockito.when(mockDao.existsById(STEVE_ID)).thenReturn(false);

        // when: I try to delete
        service.delete(steve);

        // then: I should fail
        Assert.fail("should refuse to delete a creature which doesn't exist");
    }

    @Test
    public void creatureService_fromMobile_shouldHappyPath() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I expect to get savedSteve as my retval
        Assert.assertEquals("should return savedSteve", savedSteve, result);

        // and: he should have three properties, 2 from original, and 1 to hold maxHP
        Assert.assertEquals("should have 3 properties", 3, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propA", propB, result.getPropertyMap().get("B"));

        String maxHpPropName = config.getCreaturePropertyMaxHpName();
        Assert.assertEquals("should have maxHp property", "64", result.getPropertyMap().get(maxHpPropName).getValue());

        // and: the values passed to dao should match the mobile
        Mockito.verify(mockDao).save(creatureCaptor.capture());

        Creature argument = creatureCaptor.getValue();
        Assert.assertEquals("should save creature with correct name", pattern.getName(), argument.getName());
        Assert.assertEquals("should save creature with correct owner", pattern.getOwner(), argument.getOwner());
        Assert.assertEquals("should save creature with correct privacy", pattern.getPrivacy(), argument.getPrivacy());

    }

    @Test(expected = CreatureException.class)
    public void creatureService_fromMobile_shouldFailWhenMobileIdNotSet() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: mobile Id is not set
        pattern.setId(null);

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I should fail
        Assert.fail("should refuse to convert a mobile which has no id set");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_fromMobile_shouldFailIfNameWouldCollide() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: the name appears to already exist in the dao
        Mockito.when(mockDao.existsByName("fred")).thenReturn(true);

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I should fail
        Assert.fail("should refuse to convert a mobile whose name would collide");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_fromMobile_shouldFailIfMobileInvalid() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: mobile is invalid somehow
        pattern.setPosition("");

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I should fail
        Assert.fail("should refuse to convert a mobile which fails java bean validation");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_fromMobile_shouldFailWhenInvalidProperty() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: one of the mobile's properties is invalid somehow
        propA.setValue("");

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I should fail
        Assert.fail("should refuse to convert a mobile with a property which fails java bean validation");
    }

    @Test(expected = CreatureException.class)
    public void creatureService_fromMobile_shouldFailWhenInvalidPropertyMapping() throws CreatureException, PropertyException {
        // given: a pattern Mobile
        Mobile pattern = TestDataFactory.makeMobile(1337L, "fred");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);
        pattern.setMaxHp(64);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedSteve);

        // and: the mapping validation is set to fail
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I convert a mobile
        Creature result = service.fromMobile(pattern);

        // then: I should fail
        Assert.fail("should refuse to convert a mobile with a property which fails property mapping validation");
    }
}
