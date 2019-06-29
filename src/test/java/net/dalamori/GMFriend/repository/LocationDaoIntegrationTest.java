package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class LocationDaoIntegrationTest {

    @Autowired
    public LocationDao locationDao;

    @Autowired
    public NoteDao noteDao;

    @Autowired
    public LocationLinkDao linkDao;

    private Location origin;
    private Location dest;
    private Note noteA;
    private Note noteB;
    private LocationLink link;


    public static final String ORIGIN_NAME = "Here";
    public static final String DEST_NAME = "There";
    public static final String NOTE_TITLE = "Notica Importica";
    public static final String LIPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";
    public static final String OWNER = "Me";
    public static final String LINK_DESC = "downstairs";

    @Before
    public void setup() {
        noteDao.deleteAll();
        linkDao.deleteAll();
        locationDao.deleteAll();

        noteA = new Note();
        noteA.setTitle(NOTE_TITLE + " A_TITLE");
        noteA.setBody(LIPSUM);
        noteA.setPrivacy(PrivacyType.NORMAL);
        noteA.setOwner(OWNER);

        noteB = new Note();
        noteB.setTitle(NOTE_TITLE + " B_TITLE");
        noteB.setBody(LIPSUM);
        noteB.setPrivacy(PrivacyType.NORMAL);
        noteB.setOwner(OWNER);

        origin = new Location();
        origin.setName(ORIGIN_NAME);
        origin.setPrivacy(PrivacyType.NORMAL);
        origin.setOwner(OWNER);

        dest = new Location();
        dest.setName(DEST_NAME);
        dest.setPrivacy(PrivacyType.NORMAL);
        dest.setOwner(OWNER);

        link = new LocationLink();
        link.setShortDescription(LINK_DESC);
        link.setPrivacy(PrivacyType.NORMAL);

    }

    @Test
    public void LocationDao_save_shouldHappyPath() {
        // given: a set of two notes, saved to DB
        noteA = noteDao.save(noteA);
        noteB = noteDao.save(noteB);

        // and: a set of two locations, saved to DB
        Location savedOrigin = locationDao.save(origin);

        locationDao.save(savedOrigin);

        Location savedDest = locationDao.save(dest);

        // and: a link between the two locations, saved to DB
        link.setOrigin(savedOrigin);
        link.setDestination(savedDest);
        link = linkDao.save(link);

        // when: I try to save a location with notes and links
        origin.getNotes().add(noteA);
        origin.getNotes().add(noteB);

        origin.getLinks().add(link);

        Location result = locationDao.save(origin);

        // then: i should get a return value with an id
        Assert.assertTrue("return should be a location",result instanceof Location);
        Assert.assertTrue("return should have an ID",result.getId() instanceof Long);

        // and: I should be able to look that item up
        Location findResult = locationDao.findById(result.getId()).get();

        Assert.assertTrue("result should be a location", findResult instanceof Location);
        Assert.assertEquals("lookup should return copy with correct name", ORIGIN_NAME, findResult.getName());
        Assert.assertEquals("lookup should return copy with correct owner", OWNER, findResult.getOwner());

        // and: it should have the right notes
        Assert.assertEquals("should have 2 notes", 2, findResult.getNotes().size());
        Assert.assertTrue("should contain NoteA", findResult.getNotes().contains(noteA));
        Assert.assertTrue("should contain NoteB", findResult.getNotes().contains(noteB));

        // and: it should have the right links
        Assert.assertEquals("should contain the link", 1, findResult.getLinks().size());
        Assert.assertTrue("should contain link", findResult.getLinks().contains(link));
    }

}
