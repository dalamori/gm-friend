package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.repository.LocationDao;
import net.dalamori.GMFriend.repository.NoteDao;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.testing.IntegrationTest;
import net.dalamori.GMFriend.testing.TestDataFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class LocationCommandMenuIntegrationTest {

    @Autowired
    public DmFriendConfig config;

    @Autowired
    public GroupDao groupDao;

    @Autowired
    public LocationDao locationDao;

    @Autowired
    public NoteDao noteDao;

    @Autowired
    public PropertyDao propertyDao;

    @Autowired
    public LocationService locationService;

    @Autowired
    public PropertyService propertyService;

    @Autowired
    public AbstractCommand rootCommand;

    @Before
    public void setup() {
        Note noteA = TestDataFactory.makeNote("Note_A");
        Note noteB = TestDataFactory.makeNote("Note_B");

        noteDao.save(noteA);
        noteDao.save(noteB);
    }

    @After
    public void teardown() {
        groupDao.deleteAll();
        propertyDao.deleteAll();
        noteDao.deleteAll();
        locationDao.deleteAll(); // cascade deletes locationLinks with locations.
    }

    @Test
    public void locationMenu_shouldHappyPathCreateEditDelete() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a location;
        List<String> commands = Arrays.asList((
                ";; location new Green_Room\n" +
                ";; location new Blue_Room\n" +
                ";; location new Red_Room\n" +
                ";; location new Black_Room\n" +
                ";; location link Green_Room Blue_Room a cyan passage leads blue-ward\n" +
                ";; location link Green_Room Red_Room a yellow passage passage leads red-ward\n" +
                ";; location unlink Green_Room Red_Room\n" +
                ";; location note Green_Room Note_A\n" +
                ";; location note Green_Room Note_B\n" +
                ";; location unnote Green_Room Note_B\n" +
                ";; location show Black_Room\n" + // will throw an error if black room doesn't exist
                ";; location remove Black_Room\n" +
                ";; location move Green_Room\n"
        ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands ){
            CommandContext context = TestDataFactory.makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect to see the rooms I made, but not the one I deleted
        Assert.assertTrue("Red_Room should exist", locationDao.existsByName("Red_Room"));
        Assert.assertTrue("Blue_Room should exist", locationDao.existsByName("Blue_Room"));
        Assert.assertFalse("Black_Room should not exist", locationDao.existsByName("Black_Room"));

        Location result = locationService.read("Green_Room");
        Assert.assertEquals("should have exactly one link", 1, result.getLinks().size());
        Assert.assertEquals("Green_Room should have a link to Blue_Room, not Red_Room",
                "Blue_Room",
                result.getLinks().get(0).getDestination().getName());

        Assert.assertEquals("should have exactly 1 note", 1, result.getNotes().size());
        Assert.assertEquals("should have note A, not note B", "Note_A", result.getNotes().get(0).getTitle());

        // and: $HERE should be set, and point to Green Room
        Map<String, Property> globalProperties = propertyService.getGlobalProperties();
        Assert.assertTrue("should set $HERE", globalProperties.containsKey(config.getLocationHereGlobalName()));
        Assert.assertEquals("$HERE should point to Green room",
                result.getId().toString(),
                globalProperties.get(config.getLocationHereGlobalName()).getValue());

    }

}
