package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.GroupException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.NoteDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.services.impl.NoteServiceImpl;
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
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class NoteServiceUnitTest {

    @Autowired
    public DmFriendConfig config;

    @Mock private NoteDao mockDao;
    @Mock private GroupService mockGroupService;
    @Captor private ArgumentCaptor<Iterable<Long>> findIdsCaptor;

    private NoteService service;
    private Note note;
    private Note savedNote;

    public static final Long NOTE_ID = 8675309L;
    public static final String NOTE_OWNER = "Some Test Guy";
    public static final String NOTE_TITLE = "A Test Title";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        note = TestDataFactory.makeNote(null, NOTE_TITLE);
        note.setOwner(NOTE_OWNER);
        note.setPrivacy(PrivacyType.NORMAL);

        savedNote = TestDataFactory.makeNote(NOTE_ID, NOTE_TITLE);
        savedNote.setOwner(NOTE_OWNER);
        savedNote.setPrivacy(PrivacyType.NORMAL);

        NoteServiceImpl impl;
        impl = new NoteServiceImpl();
        impl.setConfig(config);
        impl.setGroupService(mockGroupService);
        impl.setNoteDao(mockDao);

        service = impl;


    }

    @Test
    public void noteService_create_shouldHappyPath() throws NoteException {
        // given: a sample note to return from mock dao
        Mockito.when(mockDao.save(note)).thenReturn(savedNote);

        // when: I create the note
        Note result = service.create(note);

        // then: I expect to see the correct return value
        Assert.assertEquals("got correct return value", savedNote, result);

        // and: I expect to the right calls to the dao
        Mockito.verify(mockDao).save(note);
    }

    @Test(expected = NoteException.class)
    public void noteService_create_shouldFailIfIdSet() throws NoteException {
        // given: a sample note to return from mock dao
        Mockito.when(mockDao.save(note)).thenReturn(savedNote);

        // and: that my sample note already has an ID set
        note.setId(Long.valueOf(1));

        // when: I create the note
        try {
            service.create(note);

            Assert.fail("should refuse to create a note with an id");

            // then: it should throw an error before the DAO is called
        } catch(NoteException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = NoteException.class)
    public void noteService_create_shouldFailWhenInvalid() throws NoteException {
        // given: a sample note which doesn't conform to validation rules
        note.setOwner("");
        note.setPrivacy(null);

        // when: I create the note
        try {
            service.create(note);

            Assert.fail("should refuse to create a note which fails validation");

        }

        // then: it should throw an error before the DAO is called
        catch(NoteException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void noteService_read_shouldHappyPathById() throws NoteException {
        // given: a sample note saved in the db
        Mockito.when(mockDao.findById(NOTE_ID)).thenReturn(Optional.of(savedNote));

        // when: I try to lookup by ID
        Note result = service.read(NOTE_ID);

        // then: I should succeed;
        Assert.assertEquals("notes should match", result, savedNote);
    }

    @Test
    public void noteService_read_shouldHappyPathByIdString() throws NoteException {
        // given: a sample note saved in the db
        Mockito.when(mockDao.findById(NOTE_ID)).thenReturn(Optional.of(savedNote));

        // when: I try to lookup by ID
        Note result = service.read(NOTE_ID.toString());

        // then: I should succeed;
        Assert.assertEquals("notes should match", result, savedNote);

        // and: it should look up by Id
        Mockito.verify(mockDao).findById(NOTE_ID);
    }

    @Test(expected = NoteException.class)
    public void noteService_read_shouldFailWhenNotFoundById() throws NoteException {
        // given; nothing saved in the DB

        // when: I try to lookup by ID
        service.read(NOTE_ID);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void noteService_read_shouldHappyPathByTitle() throws NoteException {
        // given: a sample note saved in the db
        Mockito.when(mockDao.findByTitle(NOTE_TITLE)).thenReturn(Optional.of(savedNote));

        // when: I try to lookup by Name
        Note result = service.read(NOTE_TITLE);

        // then: I should succeed;
        Assert.assertEquals("notes should match", result, savedNote);
    }

    @Test(expected = NoteException.class)
    public void noteService_read_shouldFailWhenNotFoundByName() throws NoteException {
        // given: nothing saved in the DB

        // when: I try to lookup by ID
        service.read(NOTE_TITLE);

        // then: I should fail
        Assert.fail("Should have thrown an error by now");
    }

    @Test
    public void noteService_exists_shouldHappyPathById() {
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
    public void noteService_exists_shouldHappyPathByIdString() {
        // given: a mock reply
        Mockito.when(mockDao.existsById(62L)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists("62");

        // then: I expect to get my result
        Assert.assertTrue("should get true", result);
        Mockito.verify(mockDao).existsById(62L);
    }

    @Test
    public void noteService_exists_shouldHandleNullIds() {
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
    public void noteService_exists_shouldHappyPathByTitle() {
        // given: a sample ID
        String title = "War and Peace";

        // and: a mock reply
        Mockito.when(mockDao.existsByTitle(title)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(title);

        // then: I expect to see that passed thru to the dao, and the dao's retval returned
        Assert.assertTrue("should get returnvalue from the dao", result);
        Mockito.verify(mockDao).existsByTitle(title);
    }

    @Test
    public void noteService_exists_shouldHandleNullStrings() {
        // given: a sample ID
        String title = null;

        // and: a mock reply
        Mockito.when(mockDao.existsByTitle(title)).thenReturn(true);

        // when: I poll the service
        boolean result = service.exists(title);

        // then: I expect to get a short-circuit false
        Assert.assertFalse("should get default return", result);
        Mockito.verify(mockDao, Mockito.never()).existsByTitle(title);
    }

    @Test
    public void noteService_update_shouldHappyPath() throws NoteException {
        // given: an example note with updated attributes to save
        note.setId(NOTE_ID);
        note.setPrivacy(PrivacyType.HIDDEN);

        // and: a saved copy of that note in the DB:
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(true);
        Mockito.when(mockDao.save(note)).thenReturn(note);

        // when: I update the note
        Note result = service.update(note);

        // then: I expect to see the updated result
        Assert.assertEquals("return value should come from dao save call", note, result);

        // and: I expect to see the proper calls to the Dao
        Mockito.verify(mockDao).existsById(NOTE_ID);
        Mockito.verify(mockDao).save(note);
    }

    @Test(expected = NoteException.class)
    public void noteService_update_shouldFailWhenIdNotSet() throws NoteException {
        // given: an example note with updated attributes to save, but no ID
        note.setId(null);
        note.setPrivacy(PrivacyType.PUBLIC);

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(true);

        // when: I try to update the note
        try {
            service.update(note);

            Assert.fail("should refuse to update a note with a null id");

        }

        // then: it should throw an error before the DAO is called
        catch(NoteException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = NoteException.class)
    public void noteService_update_shouldFailWhenNotFound() throws NoteException {
        // given: an example note with updated attributes to save
        note.setId(NOTE_ID);
        note.setPrivacy(PrivacyType.PUBLIC);

        // and: no saved copies appear to be in the db
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(false);

        // when: I try to update the note
        try {
            service.update(note);

            Assert.fail("should refuse to update a note which doesnt exist");

        }

        // then: it should throw an error before the DAO is called
        catch(NoteException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test(expected = NoteException.class)
    public void noteService_update_shouldFailWhenInvalidNote() throws NoteException {
        // given: an example note with invalid attributes to save
        note.setId(NOTE_ID);
        note.setPrivacy(null);
        note.setOwner("");

        // and: proper saved copies appear to be in the db
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(true);

        // when: I try to update the note
        try {
            service.update(note);

            Assert.fail("should refuse to create a note with bad attributes");

        }

        // then: it should throw an error before the DAO is called
        catch(NoteException ex) {
            Mockito.verify(mockDao, Mockito.never()).save(Mockito.any());

            throw ex;
        }
    }

    @Test
    public void noteService_delete_shouldHappyPath() throws NoteException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(true);

        // when: I try to delete the note
        note.setId(NOTE_ID);
        service.delete(note);

        // then: I expect to see that call pass thru to to dao
        Mockito.verify(mockDao).deleteById(NOTE_ID);
    }

    @Test(expected = NoteException.class)
    public void noteService_delete_shouldFailWhenIdNotSet() throws NoteException {
        // given: a proper saved copy appears to be in the DB:
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(true);

        // when: I try to delete the note
        note.setId(null);
        service.delete(note);

        // then: I expect to get an error
        Assert.fail("should have thrown an error for no id by now");
    }

    @Test(expected = NoteException.class)
    public void noteService_delete_shouldFailWhenNotFound() throws NoteException {
        // given: dao is set to report that note doesn't exist
        Mockito.when(mockDao.existsById(NOTE_ID)).thenReturn(false);

        // when: I try to delete the note
        note.setId(NOTE_ID);
        service.delete(note);

        // then: I expect to see that call fail with a not found
        Assert.fail("should have thrown a not found error by now");
    }


    @Test
    public void noteService_attachToGlobalContext_shouldHappyPath() throws NoteException, GroupException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE))).thenReturn(noteGroup);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);

        // when: I try to attach the note
        service.attachToGlobalContext(note);

        // then: i expect to see the note id added to the group
        Assert.assertTrue("note id added to group", noteGroup.getContents().contains(id));

        // and: I expect to see the group saved
        Mockito.verify(mockGroupService).update(noteGroup);

    }

    @Test(expected = NoteException.class)
    public void noteService_attachToGlobalContext_shouldFailWhenNoteIdNotSet() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: a saved note
        Long id = null;
        note.setId(id);

        // when: I try to attach the note
        service.attachToGlobalContext(note);

        // then: I expect it to fail
        Assert.fail("should have thrown an error by now");
    }

    @Test
    public void noteService_attachToLocation_shouldHappyPath() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: a saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(1555L);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);

        // when: I try to attach the note
        service.attachToLocation(note, location);

        // then: the group should have the new content
        Assert.assertTrue("group should contain noteId", noteGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(noteGroup);
    }

    @Test(expected = NoteException.class)
    public void noteService_attachToLocation_shouldFailWhenNoteIdNotSet() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: a saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(1555L);

        // and: a saved note
        Long id = null;
        note.setId(id);

        // when: I try to attach the note
        service.attachToGlobalContext(note);

        // then: I should get a NoteException
        Assert.fail("Should refuse to attach the note");
    }

    @Test(expected = NoteException.class)
    public void noteService_attachToLocation_shouldFailWhenLocationIdNotSet() throws NoteException, GroupException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: an un-saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(null);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);

        // when: I try to attach the note
        service.attachToLocation(note, location);

        // then: I should get a NoteException
        Assert.fail("Should refuse to attach the note");
    }

    @Test
    public void noteService_detachFromGlobalContext_shouldHappyPath() throws NoteException, GroupException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE))).thenReturn(noteGroup);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);
        noteGroup.getContents().add(id);

        // when: I try to attach the note
        service.detachFromGlobalContext(note);

        // then: I expect to see the note id added to the group
        Assert.assertFalse("note id removed to group", noteGroup.getContents().contains(id));

        // and: I expect to see the group saved
        Mockito.verify(mockGroupService).update(noteGroup);

    }

    @Test(expected = NoteException.class)
    public void noteService_detachFromGlobalContext_shouldFailWhenNoteIdNotSet() throws NoteException, GroupException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE))).thenReturn(noteGroup);

        // and: a saved note
        Long id = null;
        note.setId(id);
        noteGroup.getContents().add(id);

        // when: I try to attach the note
        service.detachFromGlobalContext(note);

        // then: I expect to fail
        Assert.fail("should throw a NoteException");
    }

    @Test
    public void noteService_detachFromLocation_shouldHappyPath() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: a saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(1555L);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);
        noteGroup.getContents().add(id);

        // when: I try to detach the note
        service.detachFromLocation(note, location);

        // then: the group should have the new content
        Assert.assertFalse("group should contain noteId", noteGroup.getContents().contains(id));

        // and: that group should be updated
        Mockito.verify(mockGroupService).update(noteGroup);
    }

    @Test(expected = NoteException.class)
    public void noteService_detachFromLocation_shouldFailWhenNoteIdNotSet() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: a saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(1555L);

        // and: a saved note
        Long id = null;
        note.setId(id);
        noteGroup.getContents().add(id);

        // when: I try to detach the note
        service.detachFromLocation(note, location);

        // then: I should fail
        Assert.fail("should refuse to detach unsaved note");
    }

    @Test(expected = NoteException.class)
    public void noteService_detachFromLocation_shouldFailWhenLocationIdNotSet() throws GroupException, NoteException {
        // given: a group
        Group noteGroup = TestDataFactory.makeGroup();

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.anyString(), Mockito.eq(PropertyType.NOTE)))
                .thenReturn(noteGroup);

        // and: an un-saved location
        Location location = TestDataFactory.makeLocation();
        location.setId(null);

        // and: a saved note
        Long id = 4321L;
        note.setId(id);
        noteGroup.getContents().add(id);

        // when: I try to detach the note
        service.detachFromLocation(note, location);

        // then: I should fail
        Assert.fail("should refuse to detach from an unsaved location");
    }

    @Test
    public void noteService_getGlobalNotes_shouldHappyPath() throws GroupException, NoteException {
        // given: a mock response list
        Long id = 1000L;
        List<Note> noteList = new ArrayList<>();
        noteList.add(TestDataFactory.makeNote(id, "Note A"));
        noteList.add(TestDataFactory.makeNote(id + 1, "Note B"));
        noteList.add(TestDataFactory.makeNote(id + 2, "Note C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(noteList);

        // and: a group containing the IDs
        Group globalNoteGroup = TestDataFactory.makeGroup();
        Set<Long> globalNoteContents = globalNoteGroup.getContents();
        globalNoteContents.add(id);
        globalNoteContents.add(id + 1);
        globalNoteContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(globalNoteGroup);

        // when: I get global notes
        List<Note> result = service.getGlobalNotes();

        // then: i expect to see the the mock noteList passed back as retval
        Assert.assertEquals("should return noteList", noteList, result);

        // and: I expect to see the group contents passed to the noteDao
        Mockito.verify(mockDao).findAllById(findIdsCaptor.capture());
        Set<Long> capturedIds = (Set<Long>) findIdsCaptor.getValue();
        Assert.assertEquals("should pass mock group contents into dao", globalNoteContents, capturedIds);

    }

    @Test
    public void noteService_getLocationNotes_shouldHappyPath() throws GroupException, NoteException {
        // given: a mock response list
        Long id = 1400L;
        List<Note> noteList = new ArrayList<>();
        noteList.add(TestDataFactory.makeNote(id, "Note A"));
        noteList.add(TestDataFactory.makeNote(id + 1, "Note B"));
        noteList.add(TestDataFactory.makeNote(id + 2, "Note C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(noteList);

        // and: a group containing the IDs
        Group locationNoteGroup = TestDataFactory.makeGroup();
        Set<Long> locationNoteContents = locationNoteGroup.getContents();
        locationNoteContents.add(id);
        locationNoteContents.add(id + 1);
        locationNoteContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(locationNoteGroup);

        // and: a location;
        Location location = TestDataFactory.makeLocation(5400L, "Test Location");

        // when: I get global notes
        List<Note> result = service.getLocationNotes(location);

        // then: i expect to see the the mock noteList passed back as retval
        Assert.assertEquals("should return noteList", noteList, result);

        // and: I expect to see the group contents passed to the noteDao
        Mockito.verify(mockDao).findAllById(findIdsCaptor.capture());
        Set<Long> capturedIds = (Set<Long>) findIdsCaptor.getValue();
        Assert.assertEquals("should pass mock group contents into dao", locationNoteContents, capturedIds);
    }

    @Test(expected = NoteException.class)
    public void noteService_getLocationNotes_shouldFailWhenLocationIdNotSet() throws GroupException, NoteException {
        // given: a mock response list
        Long id = 1400L;
        List<Note> noteList = new ArrayList<>();
        noteList.add(TestDataFactory.makeNote(id, "Note A"));
        noteList.add(TestDataFactory.makeNote(id + 1, "Note B"));
        noteList.add(TestDataFactory.makeNote(id + 2, "Note C"));

        Mockito.when(mockDao.findAllById(Mockito.any())).thenReturn(noteList);

        // and: a group containing the IDs
        Group locationNoteGroup = TestDataFactory.makeGroup();
        Set<Long> locationNoteContents = locationNoteGroup.getContents();
        locationNoteContents.add(id);
        locationNoteContents.add(id + 1);
        locationNoteContents.add(id + 2);

        Mockito.when(mockGroupService.resolveSystemGroup(Mockito.any(), Mockito.any())).thenReturn(locationNoteGroup);

        // and: a location;
        Location location = TestDataFactory.makeLocation(null, "Test Location");

        // when: I get global notes
        try {
            List<Note> result = service.getLocationNotes(location);

            // then: I expect to fail
            Assert.fail("should reject unsaved locations");
        } catch (NoteException ex) {
            throw ex;
        } finally {

            // and: I shouldn't see calls to the dao or groupServiced
            Mockito.verify(mockDao, Mockito.never()).findAllById(Mockito.any());
            Mockito.verify(mockGroupService, Mockito.never()).resolveSystemGroup(Mockito.anyString(), Mockito.any());
        }
    }
}
