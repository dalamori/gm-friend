package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.CreatureService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class CreatureCommandMenuIntegrationTest {

    @Autowired
    public CreatureDao creatureDao;

    @Autowired
    public CreatureService creatureService;

    @Autowired
    public GroupDao groupDao;

    @Autowired
    public InterpreterConfig interpreterConfig;

    @Autowired
    public PropertyDao propertyDao;

    private AbstractCommand rootCommand;

    @Before
    public void setup() {
        // get the rootCommand
        rootCommand = interpreterConfig.getRootCommand();
    }

    @After
    public void teardown() {
        groupDao.deleteAll();
        propertyDao.deleteAll();
        creatureDao.deleteAll();
    }

    @Test
    public void creatureMenu_shouldHappyPathCreateEdit() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a creature.
        List<String> commands = Arrays.asList((
                ";; creature new Bill_Nye\n" +
                ";; creature set Bill_Nye maxHp 12\n" +
                ";; creature set Bill_Nye shortDesc A tall, thin man in a lab coat talking about Global Warming.\n" +
                ";; creature set Bill_Nye enunciation Ex ability. 3/day Can convince open-minded creature of the truth.\n" +
                ";; creature set Bill_Nye maxHp ++\n" +
                ";; creature unset Bill_Nye enunciation\n"
                ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands ){
            CommandContext context = makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect to see Bill_Nye in the DB
        Assert.assertTrue(creatureDao.existsByName("Bill_Nye"));

        // and: I expect him to have the right properties...
        Map<String, Property> nyeProperties = creatureService.read("Bill_Nye").getPropertyMap();
        Assert.assertEquals("should have 13 maxHp", "13", nyeProperties.get("maxHp").getValue());
        Assert.assertEquals("should have short desc", "A tall, thin man in a lab coat talking about Global Warming.", nyeProperties.get("shortDesc").getValue());
        Assert.assertFalse("should not contain a enunciation property", nyeProperties.containsKey("enunciation"));
    }

    @Test
    public void creatureMenu_shouldHappyPathDelete() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a creature.
        List<String> commands = Arrays.asList((
                ";; creature show Neil_DeGrasse_Tyson\n" +  // this show command is a control, it will error if Dr Tyson doesn't exist.
                ";; creature delete Neil_DeGrasse_Tyson\n"
        ).split("\n"));

        // and: a sample Dr. Tyson to delete (if it helps, please think of this as an experiment, sir...)
        Creature drTyson = new Creature();
        drTyson.setName("Neil_DeGrasse_Tyson");
        drTyson.setPrivacy(PrivacyType.NORMAL);
        drTyson.setOwner("Neil_DeGrasse_Tyson");

        Creature savedDrTyson = creatureDao.save(drTyson);

        // when: I run the commands:
        for (String commandLine : commands ){
            CommandContext context = makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect to see Dr Tyson has left the DB.
        Assert.assertFalse("Dr Tyson should be gone", creatureDao.existsByName("Neil_DeGrasse_Tyson"));
    }


    private CommandContext makeContextFromCommandLine(String commandLine) {
        CommandContext context = new CommandContext();
        context.setOwner("Anyone, really...");
        context.setIndex(0);
        context.setCommand(Arrays.asList(commandLine.split("\\s")));

        return context;
    }
}
