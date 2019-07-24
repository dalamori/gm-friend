package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.repository.MobileDao;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.MobileService;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class MobileCommandMenuIntegrationTest {

    @Autowired
    public CreatureDao creatureDao;

    @Autowired
    public GroupDao groupDao;

    @Autowired
    public MobileDao mobileDao;

    @Autowired
    public PropertyDao propertyDao;

    @Autowired
    public CreatureService creatureService;

    @Autowired
    public MobileService mobileService;

    @Autowired
    public AbstractCommand rootCommand;

    @Before
    public void setup() throws DmFriendGeneralServiceException {
        // given: a creature to copy
        Creature template = creatureDao.save(TestDataFactory.makeCreature("orc"));

        Property maxHp = TestDataFactory.makeProperty("maxHp");
        maxHp.setValue("14");
        maxHp.setType(PropertyType.INTEGER);
        template.getPropertyMap().put("maxHp", maxHp);

        Property desc =  TestDataFactory.makeProperty("desc");
        desc.setType(PropertyType.STRING);
        desc.setValue("An ugly green humanoid with protruding lower canine teeth.");
        template.getPropertyMap().put("desc", desc);

        creatureService.update(template);
    }

    @After
    public void teardown() {
        groupDao.deleteAll();
        propertyDao.deleteAll();
        mobileDao.deleteAll();
        creatureDao.deleteAll();
    }


    @Test
    public void mobileMenu_shouldHappyPathCreateEditDelete() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a location;
        List<String> commands = Arrays.asList((
                ";; mobile new orc\n" + // orc
                ";; mobile position orc Up front\n" +
                ";; mobile init orc 5\n" +
                ";; mobile kill orc\n" +
                ";; mobile new orc\n" + // orc_2
                ";; mobile kill orc_2\n" + // orc_2 dead
                ";; mobile restore orc_2\n" + // orc_2 alive =)
                ";; mobile damage orc_2 10\n" + // orc_2 hp = 4
                ";; mobile position orc_2 In the back\n" +
                ";; mobile new orc\n" + // orc_3
                ";; mobile show orc_3\n" + // proves he exists by not throwing an error
                ";; mobile remove orc_3\n" +
                ";; mobile blank gary\n" + // gary
                ";; mobile set gary punch 1d2+1\n" +
                ";; mobile maxhp gary 24\n" +
                ";; mobile init gary 13\n" +
                ";; mobile heal gary 6\n" // gary hp = (7/24)
        ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands) {
            CommandContext context = TestDataFactory.makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: orc should exist with position "Up front" and init 5, and be dead
        Assert.assertTrue("orc should exist", creatureDao.existsByName("orc"));
        Mobile orc = mobileService.read("orc");
        Assert.assertEquals("orc should be up front", "Up front", orc.getPosition());
        Assert.assertEquals("orc should have init 5", 5, orc.getInitiative());
        Assert.assertEquals("orc should have 14 maxHp", 14L, orc.getMaxHp());
        Assert.assertTrue("orc should have a copy of desc", orc.getPropertyMap().containsKey("desc"));
        Assert.assertFalse("orc should be dead", orc.isAlive());

        // and: orc_2 should exist with 10 damage, and position "In the back"
        Assert.assertTrue("orc_2 should exist", mobileDao.existsByName("orc_2"));
        Mobile orc2 = mobileService.read("orc_2");
        Assert.assertEquals("orc_2 should be damaged", 4L, orc2.getHp());
        Assert.assertEquals("orc_2 should be in the rear", "In the back", orc2.getPosition());

        // and: orc_3 shouldn't exist
        Assert.assertFalse("orc_3 should not exist", mobileDao.existsByName("orc_3"));

        // and: gary should exist, with punch property, hp 7/24, init 13
        Assert.assertTrue("gary should exist", mobileDao.existsByName("gary"));
        Mobile gary = mobileService.read("gary");
        Assert.assertEquals("gary should have 24 max hp", 24L, gary.getMaxHp());
        Assert.assertEquals("gary should have 7 hp left", 7L, gary.getHp());
        Assert.assertEquals("gary should have initiative of 13", 13, gary.getInitiative());
        Assert.assertEquals("gary should have a punch property",
                "1d2+1",
                gary.getPropertyMap().get("punch").getValue());

    }
}
