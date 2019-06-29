package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.After;
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

    private Location location;
    private Note noteA;
    private Note noteB;


    public static final String LOCATION_NAME = "Here";
    public static final String NOTE_TITLE = "Notica Importica";
    public static final String LIPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";
    public static final String OWNER = "Me";

    @After
    public void teardown() {
        locationDao.deleteAll();
    }

    @Before
    public void setup() {

        location = new Location();
        location.setName(LOCATION_NAME);
        location.setPrivacy(PrivacyType.NORMAL);
        location.setOwner(OWNER);

    }

    @Test
    public void LocationDao_save_shouldHappyPath() {
        // when: I try to save a location
        Location result = locationDao.save(location);

        // then: i should get a return value with an id
        Assert.assertTrue("return should be a location",result instanceof Location);
        Assert.assertTrue("return should have an ID",result.getId() instanceof Long);

        // and: I should be able to look that item up
        Location findResult = locationDao.findById(result.getId()).get();

        Assert.assertTrue("result should be a location", findResult instanceof Location);
        Assert.assertEquals("lookup should return copy with correct name", LOCATION_NAME, findResult.getName());
        Assert.assertEquals("lookup should return copy with correct owner", OWNER, findResult.getOwner());
    }

}
