package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.LocationException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.repository.LocationDao;
import net.dalamori.GMFriend.repository.LocationLinkDao;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.services.impl.LocationServiceImpl;
import net.dalamori.GMFriend.testing.TestDataFactory;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class LocationServiceUnitTest {

    @Autowired private DmFriendConfig config;

    @Mock private LocationDao mockDao;
    @Mock private LocationLinkDao mockLinkDao;
    @Mock private NoteService mockNoteService;

    private LocationService service;

    private Location here;
    private Location there;
    private Location farAway;

    private LocationLink hereThereLink;
    private LocationLink thereHereLink;

    private Note noteA;
    private Note noteB;

    public static final Long NEW_LOCATION_ID = 42L;
    public static final Long NEW_LOCATION_LINK_ID = 2442L;

    public static final Answer<Location> MOCK_LOCATION_SAVE = new Answer<Location>() {
        @Override
        public Location answer(InvocationOnMock invocation) throws Throwable {
            Location oldLocation = invocation.getArgument(0);
            Location savedLocation = TestDataFactory.makeLocation(NEW_LOCATION_ID, oldLocation.getName());
            savedLocation.setOwner(oldLocation.getOwner());
            savedLocation.setPrivacy(oldLocation.getPrivacy());
            return savedLocation;
        }
    };
    public static final Answer<LocationLink> MOCK_LINK_SAVE = new Answer<LocationLink>() {
        @Override
        public LocationLink answer(InvocationOnMock invocation) throws Throwable {
            LocationLink oldLink = invocation.getArgument(0);
            LocationLink savedLink = TestDataFactory.makeLink(oldLink.getOrigin(), oldLink.getDestination());
            savedLink.setId(NEW_LOCATION_LINK_ID);
            savedLink.setShortDescription(oldLink.getShortDescription());
            savedLink.setPrivacy(oldLink.getPrivacy());
            return savedLink;
        }
    };

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        here = TestDataFactory.makeLocation(123L, "Here");
        there = TestDataFactory.makeLocation(234L, "There");
        farAway = TestDataFactory.makeLocation(345L, "Somewhere Far Away");

        hereThereLink = TestDataFactory.makeLink(here, there);
        thereHereLink = TestDataFactory.makeLink(there, here);

        noteA = TestDataFactory.makeNote(42L, "NoteA");
        noteB = TestDataFactory.makeNote(72L, "NoteB");

        LocationServiceImpl impl = new LocationServiceImpl();
        impl.setLocationDao(mockDao);
        impl.setLinkDao(mockLinkDao);
        impl.setNoteService(mockNoteService);

        service = impl;

    }

    @Test
    public void locationService_create_shouldHappyPath() throws LocationException, NoteException {
        // given: an unsaved origin and a destination for links to point to.
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);

        // and: some notes
        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: it should return a new location with an id
        Assert.assertTrue("should return a location", result instanceof Location);
        Assert.assertEquals("should have an Id assigned", NEW_LOCATION_ID, result.getId());

        Mockito.verify(mockDao).save(here);

        // and: it should have a saved link to dest
        Assert.assertEquals("should have a single link", 1, result.getLinks().size());
        Assert.assertEquals("link should have id", NEW_LOCATION_LINK_ID, result.getLinks().get(0).getId());
        Assert.assertEquals("link should point to there", there, result.getLinks().get(0).getDestination());

        Mockito.verify(mockLinkDao).save(hereThereLink);

        // and: it should have 2 notes.
        Assert.assertEquals("should have 2 notes", 2, result.getNotes().size());
        Assert.assertTrue("should contain noteA", result.getNotes().contains(noteA));
        Assert.assertTrue("should contain noteB", result.getNotes().contains(noteB));

        Mockito.verify(mockNoteService).attachToLocation(noteA, here);
        Mockito.verify(mockNoteService).attachToLocation(noteB, here);

    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenIdSet() throws LocationException {
        // given: an origin and a destination for links to point to.
        here.setId(54321L);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);

        // and: some notes
        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create location when id set");
    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenInvalid() throws LocationException {
        // given: an invalid origin and a destination for links to point to.
        here.setOwner("");
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);

        // and: some notes
        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create invalid location");
    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenNoteInvalid() throws LocationException {
        // given: an unsaved origin and a destination for links to point to.
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);

        // and: some notes, onw of which is invalid...
        noteB.setBody("");

        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create location when an attached note is invalid");
    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenNoteIdNotSet() throws LocationException {
        // given: an unsaved origin and a destination for links to point to.
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);

        // and: some notes, one of which has no id.
        noteB.setId(null);

        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create location when attached note has no id");
    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenLinkInvalid() throws LocationException {
        // given: an unsaved origin and a destination for links to point to.
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);
        hereThereLink.setShortDescription("");

        // and: some notes
        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create location with invalid link");
    }

    @Test(expected = LocationException.class)
    public void locationService_create_shouldFailWhenLinkDoesntOriginateHere() throws LocationException {
        // given: an unsaved origin and a destination for links to point to.
        here.setId(null);
        Mockito.when(mockDao.existsById(there.getId())).thenReturn(true);

        // and: a link that points to said destination
        here.getLinks().add(hereThereLink);
        here.getLinks().add(thereHereLink);

        // and: some notes
        List<Note> notes = here.getNotes();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.exists(noteA.getId())).thenReturn(true);
        Mockito.when(mockNoteService.exists(noteB.getId())).thenReturn(true);

        // and: some stubs to mimic the mocks' create patterns
        Mockito.when(mockDao.save(Mockito.any(Location.class))).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any(LocationLink.class))).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to create the location
        Location result = service.create(here);

        // then: I should fail miserably
        Assert.fail("should refuse to create location when link doesn't originate here");
    }

    @Test
    public void locationService_read_shouldHappyPathById() {

    }

    @Test
    public void locationService_read_shouldFailWhenNotFoundById() {

    }

    @Test
    public void locationService_read_shouldHappyPathByName() {

    }

    @Test
    public void locationService_read_shouldFailWhenNotFoundByName() {

    }

    @Test
    public void locationService_exists_shouldHappyPathById() {

    }

    @Test
    public void locationService_exists_shouldHandleNullIds() {

    }

    @Test
    public void locationService_exists_shouldHappyPathByName() {

    }

    @Test
    public void locationService_exists_shouldHandleNullStrings() {

    }

    @Test
    public void locationService_update_shouldHappyPath() {

    }

    @Test
    public void locationService_update_shouldFailWhenIdNotSet() {

    }

    @Test
    public void locationService_update_shouldFailWhenInvalid() {

    }

    @Test
    public void locationService_update_shouldFailWhenInvalidLink() {

    }

    @Test
    public void locationService_update_shouldFailWhenInvalidNote() {

    }

    @Test
    public void locationService_update_shouldFailWhenNoteIdNotSet() {

    }

    @Test
    public void locationService_update_shouldFailWhenLinkDoesntOriginateHere() {

    }

    @Test
    public void locationService_delete_shouldHappyPath() {

    }

    @Test
    public void locationService_delete_shouldFailWhenNotFound() {

    }
}
