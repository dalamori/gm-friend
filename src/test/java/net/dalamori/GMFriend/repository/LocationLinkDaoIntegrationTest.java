package net.dalamori.GMFriend.repository;

import jdk.vm.ci.aarch64.AArch64;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
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
public class LocationLinkDaoIntegrationTest {

    @Autowired
    public LocationDao locationDao;

    @Autowired
    public LocationLinkDao linkDao;

    private Location origin;
    private Location destination;
    private LocationLink link;

    public static final String ORIGIN_NAME = "Here";
    public static final String DEST_NAME = "There";
    public static final String OWNER = "Me";
    public static final String LINK_DESC = "downstairs";

    @After
    public void teardown(){
        linkDao.deleteAll();
        locationDao.deleteAll();
    }

    @Before
    public void setup() {
        origin = new Location();
        origin.setName(ORIGIN_NAME);
        origin.setOwner(OWNER);
        origin.setPrivacy(PrivacyType.NORMAL);

        destination = new Location();
        destination.setName(DEST_NAME);
        destination.setOwner(OWNER);
        destination.setPrivacy(PrivacyType.NORMAL);

        link = new LocationLink();
        link.setShortDescription(LINK_DESC);
        link.setPrivacy(PrivacyType.NORMAL);

    }

    @Test
    public void setLocationDao_save_shouldHappyPath() {
        // given: two locations saved to the DB
        Location savedOrigin = locationDao.save(origin);
        Location savedDest = locationDao.save(destination);

        // and: a link between them
        link.setOrigin(savedOrigin);
        link.setDestination(savedDest);

        // when: I try to save the link
        LocationLink result = linkDao.save(link);

        // then: I expect to get a result with an id
        Assert.assertTrue("return value should be a link", result instanceof LocationLink);
        Assert.assertTrue("return value should have an Id set", result.getId() instanceof Long);

        // and: I should be able to look that item up
        LocationLink findLink = linkDao.findById(result.getId()).get();
        Assert.assertTrue("link can be retrieved", findLink instanceof LocationLink);
        Assert.assertEquals("link should have the correct shortDesc", LINK_DESC, findLink.getShortDescription());
        Assert.assertEquals("link should have the correct origin", savedOrigin, findLink.getOrigin());
        Assert.assertEquals("link should have the correct dest", savedDest, findLink.getDestination());
        Assert.assertEquals("link should have the correct privacy policy set", PrivacyType.NORMAL, findLink.getPrivacy());
    }
}
