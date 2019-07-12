package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Creature;
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
public class CreatureDaoIntegrationTest {

    @Autowired
    public CreatureDao creatureDao;

    private Creature creature;

    public static final String CREATURE_NAME = "Bob";
    public static final String OWNER = "Me";

    @Before
    public void setup() {
        creature = new Creature();
        creature.setName(CREATURE_NAME);
        creature.setOwner(OWNER);
        creature.setPrivacy(PrivacyType.NORMAL);
    }

    @After
    public void teardown() {
        creatureDao.deleteAll();
    }

    @Test
    public void creatureDao_save_shouldHappyPath() {
        // when: I try to save the creature
        Creature savedCreature = creatureDao.save(creature);

        // then: I expect the return value to have an Id
        Assert.assertTrue("should be a creature", savedCreature instanceof Creature);
        Assert.assertTrue("should have an id", savedCreature.getId() instanceof Long);

        // and: I expect the creature to lookup
        Creature findResult = creatureDao.findById(savedCreature.getId()).get();

        Assert.assertEquals("names should match", CREATURE_NAME, findResult.getName());
        Assert.assertEquals("owners should match", OWNER, findResult.getOwner());
        Assert.assertEquals("privacy should match", PrivacyType.NORMAL, findResult.getPrivacy());
    }
}
