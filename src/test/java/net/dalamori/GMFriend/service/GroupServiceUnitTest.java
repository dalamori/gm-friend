package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.impl.GroupServiceImpl;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class GroupServiceUnitTest {

    @Mock private GroupDao mockDao;

    private GroupService service;
    private Group group;
    private Group oldGroup;

    public static final Long GROUP_ID = Long.valueOf(42);
    public static final String GROUP_NAME = "a List of Notes";
    public static final String OWNER = "Steve";
    public static final Set<Long> NOTE_IDS = new HashSet<>(Arrays.asList(
            Long.valueOf(1337),
            Long.valueOf(7331),
            Long.valueOf(7777),
            Long.valueOf(9999)));


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        group = new Group();
        group.setName(GROUP_NAME);
        group.setOwner(OWNER);
        group.getContents().addAll(NOTE_IDS);
        group.setContentType(PropertyType.NOTE);
        group.setPrivacy(PrivacyType.NORMAL);

        oldGroup = new Group();
        oldGroup.setId(GROUP_ID);
        oldGroup.setName(GROUP_NAME);
        oldGroup.setOwner(OWNER);
        oldGroup.getContents().addAll(NOTE_IDS);
        oldGroup.setContentType(PropertyType.NOTE);
        oldGroup.setPrivacy(PrivacyType.NORMAL);

        GroupServiceImpl impl = new GroupServiceImpl();
        impl.setGroupDao(mockDao);

        service = impl;
    }

    @Test
    public void groupService_create_shouldHappyPath() throws GroupException {
        // given: a sample group to return from mock dao
        Mockito.when(mockDao.save(group)).thenReturn(oldGroup);

        // when: I create the group
        Group result = service.create(group);

        // then: I expect to see the correct return value
        Assert.assertEquals("got correct return value", oldGroup, result);

        // and: I expect to the right calls to the dao
        Mockito.verify(mockDao).save(group);

    }

    @Test(expected = GroupException.class)
    public void groupService_create_shouldFailWhenIdSet() throws GroupException {
        // given: a sample group to return from mock dao
        Mockito.when(mockDao.save(group)).thenReturn(oldGroup);

        // and: that my sample group already has an ID set
        group.setId(Long.valueOf(1));

        // when: I create the group
        try {
            service.create(group);

            Assert.fail("should refuse to create a group with an id");

        // then: it should throw an error before the DAO is called
        } catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = GroupException.class)
    public void groupService_create_shouldFailWhenInvalidGroup() throws GroupException {
        // given: a sample group which doesn't conform to validation rules
        group.setOwner("");
        group.setContentType(null);

        // when: I create the group
        try {
            service.create(group);

            Assert.fail("should refuse to create a group with an id");

        }

        // then: it should throw an error before the DAO is called
        catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void groupService_find_shouldHappyPathById() throws GroupException {
        // given: a sample group saved in the db
        Mockito.when(mockDao.findById(GROUP_ID)).thenReturn(Optional.of(oldGroup));

        // when: I try to lookup by ID
        Group result = service.read(GROUP_ID);

        // then: I should succeed;
        Assert.assertEquals("groups should match", result, oldGroup);

    }

    @Test(expected = GroupException.class)
    public void groupService_find_shouldFailWhenNotFoundById() throws GroupException {
        // given; nothing saved in the DB

        // when: I try to lookup by ID
        service.read(GROUP_ID);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void groupService_find_shouldHappyPathByName() throws GroupException {
        // given: a sample group saved in the db
        Mockito.when(mockDao.findByName(GROUP_NAME)).thenReturn(Optional.of(oldGroup));

        // when: I try to lookup by Name
        Group result = service.read(GROUP_NAME);

        // then: I should succeed;
        Assert.assertEquals("groups should match", result, oldGroup);
    }

    @Test(expected = GroupException.class)
    public void groupService_find_shouldFailWhenNotFoundByName() throws GroupException {
        // given: nothing saved in the DB

        // when: I try to lookup by ID
        service.read(GROUP_NAME);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void groupService_update_shouldHappyPath() throws GroupException {
        // given: an example group with updated attributes to save
        group.setId(GROUP_ID);
        group.setPrivacy(PrivacyType.HIDDEN);

        // and: a saved copy of that group in the DB:
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(true);
        Mockito.when(mockDao.save(group)).thenReturn(group);

        // when: I update the group
        Group result = service.update(group);

        // then: I expect to see the updated result
        Assert.assertEquals("return value should come from dao save call", group, result);

        // and: I expect to see the proper calls to the Dao
        Mockito.verify(mockDao).existsById(GROUP_ID);
        Mockito.verify(mockDao).save(group);

    }

    @Test(expected = GroupException.class)
    public void groupService_update_shouldFailWhenInvalidGroup() throws GroupException {
        // given: an example group with invalid attributes to save
        group.setId(GROUP_ID);
        group.setPrivacy(null);
        group.setOwner("");

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(true);

        // when: I try to update the group
        try {
            service.update(group);

            Assert.fail("should refuse to create a group with bad attributes");

        }

        // then: it should throw an error before the DAO is called
        catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }

    }

    @Test(expected = GroupException.class)
    public void groupService_update_shouldFailWhenNotFound() throws GroupException {
        // given: an example group with updated attributes to save
        group.setId(GROUP_ID);
        group.setPrivacy(PrivacyType.PUBLIC);

        // and: no saved copies appear to be in the db
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(false);

        // when: I try to update the group
        try {
            service.update(group);

            Assert.fail("should refuse to update a group which doesnt exist");

        }

        // then: it should throw an error before the DAO is called
        catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = GroupException.class)
    public void groupService_update_shouldFailWhenNoIdSet() throws GroupException {
        // given: an example group with updated attributes to save, but no ID
        group.setId(null);
        group.setPrivacy(PrivacyType.PUBLIC);

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(true);

        // when: I try to update the group
        try {
            service.update(group);

            Assert.fail("should refuse to update a group with a null id");

        }

        // then: it should throw an error before the DAO is called
        catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }
}
