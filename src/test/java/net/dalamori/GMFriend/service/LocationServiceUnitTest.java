package net.dalamori.GMFriend.service;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class LocationServiceUnitTest {

    @Mock private LocationDao mockDao;
    @Mock private LocationLinkDao mockLinkDao;
    @Mock private NoteService mockNoteService;

    @Captor private ArgumentCaptor<Iterable<LocationLink>> linksCaptor;

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
            if (oldLocation.getId() != null) {
                savedLocation.setId(oldLocation.getId());
            }
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
            if (oldLink.getId() == null) {
                savedLink.setId(NEW_LOCATION_LINK_ID);
            } else {
                savedLink.setId(oldLink.getId());
            }
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
    public void locationService_read_shouldHappyPathById() throws LocationException, NoteException {
        // given: a subject
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.of(here));

        // and: some notes
        List<Note> notes = new ArrayList<>();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(notes);

        // and: a link
        Set<LocationLink> links = new HashSet<>();
        links.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(links);

        // when: I try to read by id
        Location result = service.read(here.getId());

        // then: I should get a copy of here, with attached notes and link
        Assert.assertEquals("should return here", here, result);

        Assert.assertEquals("should have 2 notes", 2, result.getNotes().size());
        Assert.assertTrue("should contain noteA", result.getNotes().contains(noteA));
        Assert.assertTrue("should contain noteB", result.getNotes().contains(noteB));

        Assert.assertEquals("should have a link to there", 1, result.getLinks().size());
        Assert.assertEquals("link should go There", there, result.getLinks().get(0).getDestination());
    }

    @Test(expected = LocationException.class)
    public void locationService_read_shouldFailWhenNotFoundById() throws LocationException, NoteException {
        // given: a subject
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(null));

        // and: some notes
        List<Note> notes = new ArrayList<>();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(notes);

        // and: a link
        Set<LocationLink> links = new HashSet<>();
        links.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(links);

        // when: I try to read by id
        Location result = service.read(here.getId());

        // then: I expect to fail
        Assert.fail("should throw error looking up non-existent location");
    }

    @Test
    public void locationService_read_shouldHappyPathByName() throws LocationException, NoteException {
        // given: a subject
        Mockito.when(mockDao.findByName(here.getName())).thenReturn(Optional.of(here));

        // and: some notes
        List<Note> notes = new ArrayList<>();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(notes);

        // and: a link
        Set<LocationLink> links = new HashSet<>();
        links.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(links);

        // when: I try to read by id
        Location result = service.read(here.getName());

        // then: I should get a copy of here, with attached notes and link
        Assert.assertEquals("should return here", here, result);

        Assert.assertEquals("should have 2 notes", 2, result.getNotes().size());
        Assert.assertTrue("should contain noteA", result.getNotes().contains(noteA));
        Assert.assertTrue("should contain noteB", result.getNotes().contains(noteB));

        Assert.assertEquals("should have a link to there", 1, result.getLinks().size());
        Assert.assertEquals("link should go There", there, result.getLinks().get(0).getDestination());
    }

    @Test(expected = LocationException.class)
    public void locationService_read_shouldFailWhenNotFoundByName() throws LocationException, NoteException {
        // given: a subject
        Mockito.when(mockDao.findByName(here.getName())).thenReturn(Optional.ofNullable(null));

        // and: some notes
        List<Note> notes = new ArrayList<>();
        notes.add(noteA);
        notes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(notes);

        // and: a link
        Set<LocationLink> links = new HashSet<>();
        links.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(links);

        // when: I try to read by id
        Location result = service.read(here.getName());

        // then: I expect to fail
        Assert.fail("should throw error looking up non-existent location");
    }

    @Test
    public void locationService_exists_shouldHappyPathById() {
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
    public void locationService_exists_shouldHandleNullIds() {
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
    public void locationService_exists_shouldHappyPathByName() {
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
    public void locationService_exists_shouldHandleNullStrings() {
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
    public void locationService_update_shouldHappyPath() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I expect to get a proper retval
        Assert.assertEquals("should get a copy of updated location", newHere, result);

        // and: I expect to see the dao save call
        Mockito.verify(mockDao).save(Mockito.eq(newHere));

        // and: I expect to see the call to unlink noteA
        Mockito.verify(mockNoteService).detachFromLocation(noteA, newHere);

        // and: I expect to see the call to link noteC
        Mockito.verify(mockNoteService).attachToLocation(noteC, newHere);

        // and: I don't expect to see any fiddling with noteB
        Mockito.verify(mockNoteService, Mockito.never()).detachFromLocation(Mockito.eq(noteB), Mockito.any());
        Mockito.verify(mockNoteService, Mockito.never()).attachToLocation(Mockito.eq(noteB), Mockito.any());

        // and: I expect to see the call to delete hereThereLink
        Mockito.verify(mockLinkDao).deleteAll(linksCaptor.capture());
        Assert.assertEquals("should delete hereThereLink", hereThereLink, linksCaptor.getValue().iterator().next());

        // and: I expect to see the call to save hereFarLink
        Mockito.verify(mockLinkDao).save(hereFarLink);
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenIdNotSet() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(null, "Here"); // invalid id

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail with a LocationError
        Assert.fail("Should refuse to update location when id not set.");
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenInvalid() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");
        newHere.setOwner(""); // make location invalid

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail
        Assert.fail("should refuse to update an invalid location");
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenInvalidLink() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setShortDescription(""); // make link invalid
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail
        Assert.fail("should refuse to update locations with invalid links");
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenInvalidNote() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        noteC.setOwner(""); // make note invalid
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail
        Assert.fail("should refuse to update a link with an invalid note");
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenNoteIdNotSet() throws LocationException, NoteException {
        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(null, "NoteC"); // null id
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        LocationLink hereFarLink = TestDataFactory.makeLink(newHere, farAway);
        hereFarLink.setId(721L);
        newHere.getLinks().add(hereFarLink);

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail
        Assert.fail("should refuse to create a location with a unsaved note");
    }

    @Test(expected = LocationException.class)
    public void locationService_update_shouldFailWhenLinkDoesntOriginateHere() throws LocationException, NoteException {

        // given: "here" is saved in the dao
        Mockito.when(mockDao.findById(here.getId())).thenReturn(Optional.ofNullable(here));

        // and: "here" has notes A and B saved in the noteService
        List<Note> originalNotes = new ArrayList<>();
        originalNotes.add(noteA);
        originalNotes.add(noteB);
        Mockito.when(mockNoteService.getLocationNotes(Mockito.any())).thenReturn(originalNotes);
        Mockito.when(mockNoteService.exists(Mockito.anyLong())).thenReturn(true);

        // and: "here" has a link to "there"
        Set<LocationLink> originalLinks = new HashSet<>();
        originalLinks.add(hereThereLink);
        Mockito.when(mockLinkDao.findAllByOrigin(Mockito.any())).thenReturn(originalLinks);

        // and: an updated version of "here" to save
        Location newHere = TestDataFactory.makeLocation(123L, "Here");

        // and: an updated list of notes
        Note noteC = TestDataFactory.makeNote(99L, "NoteC");
        newHere.getNotes().add(noteB);
        newHere.getNotes().add(noteC);

        // and: a different link
        newHere.getLinks().add(thereHereLink); // doesn't originate here

        // and: mock save functions that return retvals
        Mockito.when(mockDao.save(Mockito.any())).thenAnswer(MOCK_LOCATION_SAVE);
        Mockito.when(mockLinkDao.save(Mockito.any())).thenAnswer(MOCK_LINK_SAVE);

        // when: I try to update here with newhere
        Location result = service.update(newHere);

        // then: I should fail
        Assert.fail("should refuse to update locations with links that dont originate at that location");
    }

    @Test
    public void locationService_delete_shouldHappyPath() {

    }

    @Test
    public void locationService_delete_shouldFailWhenNotFound() {

    }
}
