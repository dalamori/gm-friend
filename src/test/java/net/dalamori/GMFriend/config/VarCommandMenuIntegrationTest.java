package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.testing.IntegrationTest;
import net.dalamori.GMFriend.testing.TestDataFactory;
import org.junit.After;
import org.junit.Assert;
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
public class VarCommandMenuIntegrationTest {

    @Autowired
    public PropertyDao propertyDao;

    @Autowired
    public PropertyService propertyService;

    @Autowired
    public AbstractCommand rootCommand;

    @After
    public void teardown() {
        propertyDao.deleteAll();
    }

    @Test
    public void varMenu_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a location;
        List<String> commands = Arrays.asList((
                ";; var set party_gp 33.34\n" +
                ";; var set party_gp ++\n" +
                ";; var set party_gop false\n" +
                ";; var delete party_gop\n"
        ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands) {
            CommandContext context = TestDataFactory.makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect to see party_gp set
        Map<String, Property> propertyMap = propertyService.getGlobalProperties();
        Assert.assertTrue("should have party_gp set", propertyMap.containsKey("party_gp"));
        Assert.assertEquals("should hold correct value", "34.34", propertyMap.get("party_gp").getValue());

        // and: I don't expect to see party_gop, which was deleted.
        Assert.assertFalse("party_gop should not be set", propertyMap.containsKey("party_gop"));
    }
}
