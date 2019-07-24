package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.repository.MobileDao;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.MobileService;
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
public class TurnCommandMenuIntegrationTest {

    @Autowired
    public GroupDao groupDao;

    @Autowired
    public MobileDao mobileDao;

    @Autowired
    public PropertyDao propertyDao;

    @Autowired
    public MobileService mobileService;

    @Autowired
    public PropertyService propertyService;

    @Autowired
    public DmFriendConfig config;

    @Autowired
    public AbstractCommand rootCommand;

    @Before
    public void setup() throws DmFriendGeneralServiceException {

        // given: 3 stooges
        Mobile larry = TestDataFactory.makeMobile("Larry");
        larry.setInitiative(7);
        mobileService.create(larry);

        Mobile curly = TestDataFactory.makeMobile("Curly");
        curly.setInitiative(14);
        mobileService.create(curly);

        Mobile moe = TestDataFactory.makeMobile("Moe");
        moe.setInitiative(18);
        mobileService.create(moe);

        // and: a dead guy
        Mobile charlie = TestDataFactory.makeMobile("Charlie");
        charlie.setInitiative(10);
        charlie.setAlive(false);
    }

    @After
    public void teardown() {
        groupDao.deleteAll();
        propertyDao.deleteAll();
        mobileDao.deleteAll();
    }

    @Test
    public void turnMenu_shouldHappyPathSkippingDead() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a location;
        List<String> commands = Arrays.asList((
                ";; turn next\n" + // larry
                ";; turn done\n" + // EoL
                ";; turn next\n" + // larry
                ";; turn next\n" // curly
        ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands) {
            CommandContext context = TestDataFactory.makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect to wind up on curly
        Map<String,Property> globalProperties = propertyService.getGlobalProperties();
        Assert.assertTrue("$ACTIVE is set", globalProperties.containsKey(config.getMobileActiveGlobalName()));
        Property active = globalProperties.get(config.getMobileActiveGlobalName());
        Assert.assertEquals("$ACTIVE points to curly", "14|Curly", active.getValue());
    }

}
