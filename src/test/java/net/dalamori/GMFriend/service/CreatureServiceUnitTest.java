package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Property;
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

            return TestDataFactory.makeProperty(property.getId(), property.getName());
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
    public void creatureService_update_shouldHappyPath() {

    }

    @Test
    public void creatureService_update_shouldFailWhenIdNotSet() {

    }

    @Test
    public void creatureService_update_shouldFailWhenInvalid() {

    }

    @Test
    public void creatureService_update_shouldFailWhenPropertyInvalid() {

    }

    @Test
    public void creatureService_update_shouldFailWhenPropertyMappingInvalid() {

    }

    @Test
    public void creatureService_delete_shouldHappyPath() {

    }

    @Test
    public void creatureService_delete_shouldFailWhenNotFound() {

    }

    @Test
    public void creatureService_fromMobile_shouldHappyPath() {

    }

    @Test
    public void creatureService_fromMobile_shouldFailWhenMobileIdNotSet() {

    }

    @Test
    public void creatureService_fromMobile_shouldFailIfNameWouldCollide() {

    }

    @Test
    public void creatureService_fromMobile_shouldFailIfMobileInvalid() {

    }

    @Test
    public void creatureService_fromMobile_shouldFailWhenInvalidProperty() {

    }

    @Test
    public void creatureService_fromMobile_shouldFailWhenInvalidPropertyMapping() {

    }
}
