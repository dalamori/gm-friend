package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.impl.GroupServiceImpl;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class GroupServiceUnitTest {

    @Autowired
    public DmFriendConfig config;

    @Mock private GroupDao mockDao;

    @Captor private ArgumentCaptor<Group> groupCaptor;

    private GroupService service;
    private Group group;
    private Group savedGroup;

    public static final Long GROUP_ID = 42L;
    public static final String GROUP_NAME = "a List of Notes";
    public static final String OWNER = "Steve";
    public static final Set<Long> NOTE_IDS = new HashSet<>(Arrays.asList(1337L, 7331L, 7777L, 9999L));


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TestDataFactory.OWNER_NAME = config.getSystemGroupOwner();

        group = TestDataFactory.makeGroup();
        group.setName(GROUP_NAME);
        group.setOwner(OWNER);
        group.getContents().addAll(NOTE_IDS);
        group.setContentType(PropertyType.NOTE);
        group.setPrivacy(PrivacyType.NORMAL);

        savedGroup = TestDataFactory.makeGroup();
        savedGroup.setId(GROUP_ID);
        savedGroup.setName(GROUP_NAME);
        savedGroup.setOwner(OWNER);
        savedGroup.getContents().addAll(NOTE_IDS);
        savedGroup.setContentType(PropertyType.NOTE);
        savedGroup.setPrivacy(PrivacyType.NORMAL);

        GroupServiceImpl impl = new GroupServiceImpl();
        impl.setGroupDao(mockDao);
        impl.setConfig(config);

        service = impl;
    }

    @Test
    public void groupService_create_shouldHappyPath() throws GroupException {
        // given: a sample group to return from mock dao
        Mockito.when(mockDao.save(group)).thenReturn(savedGroup);

        // when: I create the group
        Group result = service.create(group);

        // then: I expect to see the correct return value
        Assert.assertEquals("got correct return value", savedGroup, result);

        // and: I expect to the right calls to the dao
        Mockito.verify(mockDao).save(group);

    }

    @Test(expected = GroupException.class)
    public void groupService_create_shouldFailWhenIdSet() throws GroupException {
        // given: a sample group to return from mock dao
        Mockito.when(mockDao.save(group)).thenReturn(savedGroup);

        // and: that my sample group already has an ID set
        group.setId(1L);

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

            Assert.fail("should refuse to create a group which fails validation");

        }

        // then: it should throw an error before the DAO is called
        catch(GroupException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void groupService_read_shouldHappyPathById() throws GroupException {
        // given: a sample group saved in the db
        Mockito.when(mockDao.findById(GROUP_ID)).thenReturn(Optional.of(savedGroup));

        // when: I try to lookup by ID
        Group result = service.read(GROUP_ID);

        // then: I should succeed;
        Assert.assertEquals("groups should match", result, savedGroup);

    }

    @Test(expected = GroupException.class)
    public void groupService_read_shouldFailWhenNotFoundById() throws GroupException {
        // given; nothing saved in the DB

        // when: I try to lookup by ID
        service.read(GROUP_ID);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void groupService_read_shouldHappyPathByName() throws GroupException {
        // given: a sample group saved in the db
        Mockito.when(mockDao.findByName(GROUP_NAME)).thenReturn(Optional.of(savedGroup));

        // when: I try to lookup by Name
        Group result = service.read(GROUP_NAME);

        // then: I should succeed;
        Assert.assertEquals("groups should match", result, savedGroup);
    }

    @Test(expected = GroupException.class)
    public void groupService_read_shouldFailWhenNotFoundByName() throws GroupException {
        // given: nothing saved in the DB

        // when: I try to lookup by ID
        service.read(GROUP_NAME);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void groupService_exists_shouldHappyPathById() {
        // given: a sample ID
        Long id = 42L;

        // and: a mock reply
        Mockito.when(mockDao.existsById(id)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(id);

        // then: I expect to see that passed thru to the dao, and the dao's retval returned
        Assert.assertTrue("should get returnvalue from the dao", result);
        Mockito.verify(mockDao).existsById(id);

    }

    @Test
    public void groupService_exists_shouldHandleNullIds() {
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
    public void groupService_exists_shouldHappyPathByName() {
        // given: a sample ID
        String name = "George";

        // and: a mock reply
        Mockito.when(mockDao.existsByName(name)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(name);

        // then: I expect to see that passed thru to the dao, and the dao's retval returned
        Assert.assertTrue("should get returnvalue from the dao", result);
        Mockito.verify(mockDao).existsByName(name);

    }

    @Test
    public void groupService_exists_shouldHandleNullStrings() {
        // given: a sample ID
        String name = null;

        // and: a mock reply
        Mockito.when(mockDao.existsByName(name)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(name);

        // then: I expect to get a short-circuit false
        Assert.assertFalse("should get default return", result);
        Mockito.verify(mockDao, Mockito.never()).existsByName(name);
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

    @Test
    public void groupService_delete_shouldHappyPath() throws GroupException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(true);

        // when: I try to delete the group
        group.setId(GROUP_ID);
        service.delete(group);

        // then: I expect to see that call pass thru to to dao
        Mockito.verify(mockDao).deleteById(GROUP_ID);
    }

    @Test(expected = GroupException.class)
    public void groupService_delete_shouldFailWhenIdNotSet() throws GroupException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(true);

        // when: I try to delete the group
        group.setId(null);
        service.delete(group);

        // then: I expect to get an error
        Assert.fail("should have thrown an error for no id by now");
    }

    @Test(expected = GroupException.class)
    public void groupService_delete_shouldFailWhenDoesntExist() throws GroupException {
        // given: dao is set to report that group doesn't exist
        Mockito.when(mockDao.existsById(GROUP_ID)).thenReturn(false);

        // when: I try to delete the group
        group.setId(GROUP_ID);
        service.delete(group);

        // then: I expect to see that call fail with a not found
        Assert.fail("should have thrown a not found error by now");
    }

    @Test
    public void noteServiceImpl_resolveNoteGroup_shouldHappyPath() throws GroupException {
        // given: a note group saved in the db
        String name = "Stuart";
        Group group = TestDataFactory.makeGroup(name);

        Mockito.when(mockDao.existsByName(name)).thenReturn(true);
        Mockito.when(mockDao.findByName(name)).thenReturn(Optional.of(group));

        // when: I try to pull the group
        Group result = service.resolveSystemGroup(name, PropertyType.NOTE);

        // then: i expect to that group to be returned
        Assert.assertEquals("should lookup the expected group", group, result);

    }

    @Test
    public void noteServiceImpl_resolveNoteGroup_shouldCreateIfNeeded() throws GroupException {
        // given: a note group saved in the db
        String name = "Glenn";
        Group group = TestDataFactory.makeGroup(name);

        Mockito.when(mockDao.existsByName(name)).thenReturn(false);
        Mockito.when(mockDao.save(Mockito.any())).thenReturn(group);

        // when: I try to pull the group
        Group result = service.resolveSystemGroup(name, PropertyType.NOTE);

        // then: i expect to that group to be returned
        Assert.assertEquals("should return the created group", group, result);
    }

    @Test
    public void noteServiceImpl_resolveNoteGroup_shouldResolveConflict() throws GroupException {
        // given: a non-note group
        String name = "Stanley";
        Long id = 54321L;
        Group group = TestDataFactory.makeGroup(id, name);
        group.setContentType(PropertyType.LINK);

        Mockito.when(mockDao.existsByName(name)).thenReturn(true);
        Mockito.when(mockDao.findByName(name)).thenReturn(Optional.of(group));

        // and: an existing group taking up the first notes' conflict spot
        String conflictName = config.getSystemGroupCollisionPrefix().concat(name);
        Group conflictGroup = TestDataFactory.makeGroup(id + 1, conflictName);

        Mockito.when(mockDao.existsByName(conflictName)).thenReturn(true);
        Mockito.when(mockDao.findByName(conflictName)).thenReturn(Optional.of(conflictGroup));

        // and: a third group to represent the new group to be created
        Group newGroup = TestDataFactory.makeGroup(id + 2, name);

        // and: a stub for the dao save method which will update conflictGroup and create newGroup
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(new Answer<Group>() {
            @Override
            public Group answer(InvocationOnMock invocation) throws Throwable {
                Group group = invocation.getArgument(0);

                if (group.getId() == null) {
                    // create
                    return newGroup;
                } else {
                    // update
                    return conflictGroup;
                }
            }
        });

        // when: I try to resolve the group
        Group result = service.resolveSystemGroup(name, PropertyType.NOTE);

        // then: I expect to see a new group created, saved, and returned
        Assert.assertEquals("should return new group", newGroup, result);

        Mockito.verify(mockDao, Mockito.times(2)).save(groupCaptor.capture());
        Assert.assertEquals("should save a new group with the proper name", name, groupCaptor.getAllValues().get(1).getName());

        // and: I expect to see the original group renamed and saved
        Assert.assertEquals("original should be renamed with conflict alert prefix", conflictName, group.getName());
        Assert.assertEquals("should save the updated original group", groupCaptor.getAllValues().get(0), group);

        // and: I expect the group previously occupying the conflict spot
        Mockito.verify(mockDao).deleteById(conflictGroup.getId());

    }

}
