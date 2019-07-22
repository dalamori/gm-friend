package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.MobileException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.MobileDao;
import net.dalamori.GMFriend.services.MobileService;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.services.impl.MobileServiceImpl;
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
@SpringBootTest
@Category(UnitTest.class)
public class MobileServiceUnitTest {

    @Autowired
    private DmFriendConfig config;

    @Mock
    private MobileDao mockDao;
    @Mock private PropertyService mockPropertyService;

    @Captor
    private ArgumentCaptor<Mobile> mobileCaptor;

    private MobileService service;

    private Mobile bucky;
    private Mobile savedBucky;
    private Property propA;
    private Property propB;
    private Property propC;

    private static final Long BUCKY_ID = 42L;
    private static final Long PROP_A_ID = 123L;
    private static final Long PROP_B_ID = 124L;

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
        bucky = TestDataFactory.makeMobile("bucky");
        savedBucky = TestDataFactory.makeMobile(BUCKY_ID, "bucky");

        propA = TestDataFactory.makeProperty(PROP_A_ID, "A");
        propB = TestDataFactory.makeProperty(PROP_B_ID, "B");
        propC = TestDataFactory.makeProperty("C");

        // service
        MobileServiceImpl impl = new MobileServiceImpl();
        impl.setMobileDao(mockDao);
        impl.setPropertyService(mockPropertyService);
        impl.setConfig(config);

        service = impl;
    }

    @Test
    public void mobileService_create_shouldHappyPath() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to succeed.
        Assert.assertEquals("should get savedBucky", savedBucky, result);
        Mockito.verify(mockDao).save(bucky);

        // and: I expect saved properties in savedBucky
        Assert.assertEquals("should have 3 properties", 3, result.getPropertyMap().size());

        Mockito.verify(mockPropertyService).create(propC);

        // and the properties should each get an attach call
        Mockito.verify(mockPropertyService).attachToMobile(propA, savedBucky);
        Mockito.verify(mockPropertyService).attachToMobile(propB, savedBucky);
        Mockito.verify(mockPropertyService).attachToMobile(propC, savedBucky);

    }

    @Test(expected = MobileException.class)
    public void mobileService_create_shouldFailWhenIdSet() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Bucky has an ID set
        bucky.setId(BUCKY_ID);

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a mobile whose Id is set");
    }

    @Test(expected = MobileException.class)
    public void mobileService_create_shouldFailWhenNameWouldCollide() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Bucky is already taken
        Mockito.when(mockDao.existsByName("bucky")).thenReturn(true);

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a mobile whose name would collide");
    }

    @Test(expected = MobileException.class)
    public void mobileService_create_shouldFailWhenInvalid() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that Bucky is somehow invalid
        bucky.setOwner("");

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a mobile which fails java bean validation");
    }

    @Test(expected = MobileException.class)
    public void mobileService_create_shouldFailWhenPropertyInvalid() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: that a property on bucky is invalid in some way
        propB.setOwner("");

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a mobile which has an invalid property");
    }

    @Test(expected = MobileException.class)
    public void mobileService_create_shouldFailWhenPropertyMappingInvalid() throws MobileException, PropertyException {
        // given: a test subject to save
        Mockito.when(mockDao.save(bucky)).thenReturn(savedBucky);

        // and: a few properties to save with him
        bucky.getPropertyMap().put("A", propA);
        bucky.getPropertyMap().put("B", propB);
        bucky.getPropertyMap().put("C", propC);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: that Bucky's property mappings are wrong
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I save bucky
        Mobile result = service.create(bucky);

        // then: I expect it to fail
        Assert.fail("Should refuse to create a mobile whose property mappings are wrong");
    }

    @Test
    public void mobileService_read_shouldHappyPathById() throws MobileException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findById(BUCKY_ID)).thenReturn(Optional.of(savedBucky));

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(savedBucky)).thenReturn(properties);

        // when: I look the mobile up by Id
        Mobile result = service.read(BUCKY_ID);

        // then: I expect to get a copy of savedBucky
        Assert.assertEquals("should be savedBucky", savedBucky, result);

        // and: he should have the two properties I mocked the list for.
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propB", propB, result.getPropertyMap().get("B"));

    }

    @Test(expected = MobileException.class)
    public void mobileService_read_shouldFailWhenNotFoundById() throws MobileException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findById(BUCKY_ID)).thenReturn(Optional.empty());

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(savedBucky)).thenReturn(properties);

        // when: I look the mobile up by Id
        Mobile result = service.read("bucky");

        // then: I expect to fail
        Assert.fail("Should be unable to look up null");
    }

    @Test
    public void mobileService_read_shouldHappyPathByName() throws MobileException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findByName("bucky")).thenReturn(Optional.of(savedBucky));

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(savedBucky)).thenReturn(properties);

        // when: I look the mobile up by Id
        Mobile result = service.read("bucky");

        // then: I expect to get a copy of savedBucky
        Assert.assertEquals("should be savedBucky", savedBucky, result);

        // and: he should have the two properties I mocked the list for.
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propB", propB, result.getPropertyMap().get("B"));
    }

    @Test
    public void mobileService_read_shouldHappyPathByIdString() throws MobileException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findById(64L)).thenReturn(Optional.of(savedBucky));

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(savedBucky)).thenReturn(properties);

        // when: I look the mobile up by Id
        Mobile result = service.read("64");

        // then: I expect to get a copy of savedBucky
        Assert.assertEquals("should be savedBucky", savedBucky, result);

        // and: he should have the two properties I mocked the list for.
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should contain propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should contain propB", propB, result.getPropertyMap().get("B"));
    }

    @Test(expected = MobileException.class)
    public void mobileService_read_shouldHandleNullStrings() throws MobileException, PropertyException {
        // given: a test subject to look up
        Mockito.when(mockDao.findByName("bucky")).thenReturn(Optional.empty());

        // and: a list of properties to look up
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(savedBucky)).thenReturn(properties);

        // when: I look the mobile up by Id
        Mobile result = service.read("bucky");

        // then: I expect to fail
        Assert.fail("Should be unable to look up null");
    }

    @Test
    public void mobileService_exists_shouldHappyPathById() {
        // given: a mock response
        Mockito.when(mockDao.existsById(49L)).thenReturn(true);

        // when: I test existence
        boolean result = service.exists(49L);

        // then: I should get true
        Assert.assertTrue("should return true", result);
        Mockito.verify(mockDao).existsById(49L);
    }

    @Test
    public void mobileService_exists_shouldReturnFalseWhenNullId() {
        // when: I test existence
        Long id = null;
        boolean result = service.exists(id);

        // then: I should get false
        Assert.assertFalse("should return false", result);

        // and: I shouldn't see any calls to the dao
        Mockito.verifyZeroInteractions(mockDao);
    }

    @Test
    public void mobileService_exists_shouldHappyPathByName() {
        // given: a mock response
        Mockito.when(mockDao.existsByName("bucky")).thenReturn(true);

        // when: I test existence
        boolean result = service.exists("bucky");

        // then: I should get true
        Assert.assertTrue("should return true", result);
        Mockito.verify(mockDao).existsByName("bucky");
    }

    @Test
    public void mobileService_exists_shouldHappyPathByIdString() {
        // given: a mock response
        Mockito.when(mockDao.existsById(49L)).thenReturn(true);

        // when: I test existence
        boolean result = service.exists("49");

        // then: I should get true
        Assert.assertTrue("should return true", result);
        Mockito.verify(mockDao).existsById(49L);
    }

    @Test
    public void mobileService_exists_shouldReturnFalseWhenNullString() {
        // when: I test existence
        String name = null;
        boolean result = service.exists(name);

        // then: I should get false
        Assert.assertFalse("should return false", result);

        // and: I shouldn't see any calls to the dao
        Mockito.verifyZeroInteractions(mockDao);
    }

    @Test
    public void mobileService_update_shouldHappyPath() throws MobileException, PropertyException {
        // given: an updated copy of bucky
        bucky.setId(BUCKY_ID);
        bucky.setPrivacy(PrivacyType.PUBLIC);
        bucky.getPropertyMap().put("B", propB );
        bucky.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: a list of bucky's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(bucky)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // when: I update bucky
        Mobile result = service.update(bucky);

        // then: I expect to get a copy of savedBucky back
        Assert.assertEquals("should return savedBucky", savedBucky, result);
        Mockito.verify(mockDao).save(bucky);

        // and he should have the right properties, creating there needed
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should have prop B", propB, result.getPropertyMap().get("B"));
        Assert.assertEquals("should have prop C", propC, result.getPropertyMap().get("C"));

        Mockito.verify(mockPropertyService).detachFromMobile(propA, savedBucky);
        Mockito.verify(mockPropertyService).attachToMobile(propC, savedBucky);

        Mockito.verify(mockPropertyService, Mockito.never()).detachFromMobile(propB, savedBucky);
        Mockito.verify(mockPropertyService, Mockito.never()).attachToMobile(propB, savedBucky);

        Mockito.verify(mockPropertyService).create(propC);

    }

    @Test(expected = MobileException.class)
    public void mobileService_update_shouldFailWhenIdNotSet() throws MobileException, PropertyException {
        // given: an updated copy of bucky
        bucky.setPrivacy(PrivacyType.PUBLIC);
        bucky.getPropertyMap().put("B", propB );
        bucky.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: a list of bucky's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(bucky)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: mobile ID is not set
        bucky.setId(null);

        // when: I update bucky
        Mobile result = service.update(bucky);

        // then: I expect to fail
        Assert.fail("Should refuse to update a mobile whose id is not set");
    }

    @Test(expected = MobileException.class)
    public void mobileService_update_shouldFailWhenInvalid() throws MobileException, PropertyException {
        // given: an updated copy of bucky
        bucky.setPrivacy(PrivacyType.PUBLIC);
        bucky.getPropertyMap().put("B", propB );
        bucky.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: a list of bucky's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(bucky)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: mobile is somehow invalid
        bucky.setOwner(null);

        // when: I update bucky
        Mobile result = service.update(bucky);

        // then: I expect to fail
        Assert.fail("Should refuse to update a mobile whose id is not set");
    }

    @Test(expected = MobileException.class)
    public void mobileService_update_shouldFailWhenPropertyInvalid() throws MobileException, PropertyException {
        // given: an updated copy of bucky
        bucky.setPrivacy(PrivacyType.PUBLIC);
        bucky.getPropertyMap().put("B", propB );
        bucky.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: a list of bucky's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(bucky)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: a property fails java bean validation
        propB.setValue(null);

        // when: I update bucky
        Mobile result = service.update(bucky);

        // then: I expect to fail
        Assert.fail("Should refuse to update a mobile with an invalid property");
    }

    @Test(expected = MobileException.class)
    public void mobileService_update_shouldFailWhenPropertyMappingInvalid() throws MobileException, PropertyException {
        // given: an updated copy of bucky
        bucky.setPrivacy(PrivacyType.PUBLIC);
        bucky.getPropertyMap().put("B", propB );
        bucky.getPropertyMap().put("C", propC );
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: a list of bucky's original properties
        List<Property> originalProperties = new ArrayList<>();
        originalProperties.add(propA);
        originalProperties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(bucky)).thenReturn(originalProperties);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);

        // and: the mapping validation is set to fail
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I update bucky
        Mobile result = service.update(bucky);

        // then: I expect to fail
        Assert.fail("Should refuse to update a mobile whose id is not set");
    }

    @Test
    public void mobileService_delete_shouldHappyPath() throws MobileException, PropertyException {
        // given: Bucky appears to be saved in the database
        bucky.setId(BUCKY_ID);
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: bucky appears to have properties saved
        List<Property> properties = new ArrayList<>();
        properties.add(propA);
        properties.add(propB);
        Mockito.when(mockPropertyService.getMobileProperties(Mockito.any())).thenReturn(properties);

        // when: I try to delete
        service.delete(bucky);

        // then: I should succeed;
        Mockito.verify(mockDao).deleteById(BUCKY_ID);

        // and: we should see the properties unlinked.
        Mockito.verify(mockPropertyService).detachFromMobile(propA, bucky);
        Mockito.verify(mockPropertyService).detachFromMobile(propB, bucky);
    }

    @Test(expected = MobileException.class)
    public void mobileService_delete_shouldFailWhenIdNotSet() throws MobileException {
        // given: Bucky appears to be saved in the database
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(true);

        // and: id is not set
        bucky.setId(null);

        // when: I try to delete
        service.delete(bucky);

        // then: I should fail
        Assert.fail("should refuse to delete a mobile with unset id");
    }

    @Test(expected = MobileException.class)
    public void mobileService_delete_shouldFailWhenNotFound() throws MobileException {
        // given: Bucky has an id set
        bucky.setId(BUCKY_ID);

        // and: the dao reports it doesn't exist
        Mockito.when(mockDao.existsById(BUCKY_ID)).thenReturn(false);

        // when: I try to delete
        service.delete(bucky);

        // then: I should fail
        Assert.fail("should refuse to delete a mobile which doesn't exist");
    }

    @Test
    public void mobileService_fromCreature_shouldHappyPath() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: some conflicting names to force the service to search for an appropriate name
        Mockito.when(mockDao.existsByName("Randall")).thenReturn(true);
        Mockito.when(mockDao.countByNameBeginning("Randall")).thenReturn(12);
        Mockito.when(mockDao.existsByName("Randall_13")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_14")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_15")).thenReturn(false);

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to get savedBucky back, because thats what the mock does, is return him.
        Assert.assertEquals("should return savedBucky", savedBucky, result);

        // and: bucky should have 2 properties, down from the original 3
        Assert.assertEquals("should have 2 properties", 2, result.getPropertyMap().size());
        Assert.assertEquals("should have propA", propA, result.getPropertyMap().get("A"));
        Assert.assertEquals("should have propB", propB, result.getPropertyMap().get("B"));

        // and: hp should have been parsed from its property correctly
        Mockito.verify(mockDao).save(mobileCaptor.capture());
        Mobile savedMobile = mobileCaptor.getValue();
        Assert.assertEquals("should have 71 hp", 71, savedMobile.getMaxHp());

        // and: name should have been resolved correctly
        Assert.assertEquals("should resolve name", "Randall_15", savedMobile.getName());

        // and: should have a reference to the creature which spawned it
        Assert.assertEquals("should have creature id", (Long) 123L, savedMobile.getCreatureId());
    }

    @Test(expected = MobileException.class)
    public void mobileService_fromCreature_shouldFailWhenIdNotSet() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: some conflicting names to force the service to search for an appropriate name
        Mockito.when(mockDao.existsByName("Randall")).thenReturn(true);
        Mockito.when(mockDao.countByNameBeginning("Randall")).thenReturn(12);
        Mockito.when(mockDao.existsByName("Randall_13")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_14")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_15")).thenReturn(false);

        // and: the Creature Id is not set
        pattern.setId(null);

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to fail
        Assert.fail("Should refuse to create a mobile from an unsaved creature");

    }

    @Test(expected = MobileException.class)
    public void mobileService_fromCreature_shouldFailWhenInvalid() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: some conflicting names to force the service to search for an appropriate name
        Mockito.when(mockDao.existsByName("Randall")).thenReturn(true);
        Mockito.when(mockDao.countByNameBeginning("Randall")).thenReturn(12);
        Mockito.when(mockDao.existsByName("Randall_13")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_14")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_15")).thenReturn(false);

        // and: pattern is invalid somehow
        pattern.setOwner("");

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to fail
        Assert.fail("Should refuse to create a mobile from a creature which fails java bean validation");
    }

    @Test(expected = MobileException.class)
    public void mobileService_fromCreature_shouldFailWhenInvalidProperty() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: some conflicting names to force the service to search for an appropriate name
        Mockito.when(mockDao.existsByName("Randall")).thenReturn(true);
        Mockito.when(mockDao.countByNameBeginning("Randall")).thenReturn(12);
        Mockito.when(mockDao.existsByName("Randall_13")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_14")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_15")).thenReturn(false);

        // and: one of the properties is invalid
        propA.setValue("");

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to fail
        Assert.fail("Should refuse to create a mobile from a creature with an invalid property");
    }

    @Test(expected = MobileException.class)
    public void mobileService_fromCreature_shouldFailWhenInvalidPropertyMapping() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);

        // and: some conflicting names to force the service to search for an appropriate name
        Mockito.when(mockDao.existsByName("Randall")).thenReturn(true);
        Mockito.when(mockDao.countByNameBeginning("Randall")).thenReturn(12);
        Mockito.when(mockDao.existsByName("Randall_13")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_14")).thenReturn(true);
        Mockito.when(mockDao.existsByName("Randall_15")).thenReturn(false);

        // and: the Creature Id is not set
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(false);

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to fail
        Assert.fail("Should refuse to create a mobile from a creature with invalid property mappings");
    }

    @Test(expected = MobileException.class)
    public void mobileService_fromCreature_shouldFailWhenCantResolveName() throws MobileException, PropertyException {
        // given: a creature to use as a pattern
        Creature pattern = TestDataFactory.makeCreature(123L, "Randall");

        pattern.getPropertyMap().put("A", propA);
        pattern.getPropertyMap().put("B", propB);

        Property maxHpProp = TestDataFactory.makeProperty(555L, config.getCreaturePropertyMaxHpName());
        maxHpProp.setType(PropertyType.INTEGER);
        maxHpProp.setValue("71");

        pattern.getPropertyMap().put(maxHpProp.getName(), maxHpProp);

        // and: some mocks to handle return values
        Mockito.when(mockPropertyService.copy(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockPropertyService.create(Mockito.any())).thenAnswer(MOCK_PROPERTY_SAVE);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(savedBucky);
        Mockito.when(mockPropertyService.validatePropertyMapNames(Mockito.any())).thenReturn(true);

        // and: every conceivable name is already taken
        Mockito.when(mockDao.existsByName(Mockito.anyString())).thenReturn(true);

        // when: I try to convert
        Mobile result = service.fromCreature(pattern);

        // then: I expect to fail
        Assert.fail("Should refuse to create a mobile if it can't find a suitable name");
    }

}
