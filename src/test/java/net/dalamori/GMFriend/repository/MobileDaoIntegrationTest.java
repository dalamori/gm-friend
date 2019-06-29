package net.dalamori.GMFriend.repository;

import ch.qos.logback.core.net.AbstractSSLSocketAppender;
import net.dalamori.GMFriend.models.Mobile;
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
public class MobileDaoIntegrationTest {

    @Autowired
    public MobileDao mobileDao;

    private Mobile mobile;

    public static final String MOB_NAME = "Bob";
    public static final String OWNER = "Me";
    public static final String POSITION = "Crouching behind the pillar";
    public static final Long CREATURE_ID = Long.valueOf(1234);
    public static final long MAX_HP = 120;
    public static final long HP = 64;
    public static final int INITIATIVE = 12;

    @Before
    public void setup() {
        mobile = new Mobile();

        mobile.setName(MOB_NAME);
        mobile.setOwner(OWNER);
        mobile.setPrivacy(PrivacyType.NORMAL);
        mobile.setPosition(POSITION);
        mobile.setMaxHp(MAX_HP);
        mobile.setHp(HP);
        mobile.setCreatureId(CREATURE_ID);
        mobile.setInitiative(INITIATIVE);
        mobile.setAlive(true);
    }

    @After
    public void teardown() {
        mobileDao.deleteAll();
    }

    @Test
    public void mobileDao_save_shouldHappyPath() {
        // when: I try to save the mobile
        Mobile savedMob = mobileDao.save(mobile);

        // then: I expect to get a return value with an ID
        Assert.assertTrue("return value is mobile", savedMob instanceof Mobile);
        Assert.assertTrue("return value has Id", savedMob.getId() instanceof Long);

        // and: I should be able to look that mobile up
        Mobile findResult = mobileDao.findById(savedMob.getId()).get();

        Assert.assertEquals("mob name should match", MOB_NAME, findResult.getName());
        Assert.assertEquals("mob owner should match", OWNER, findResult.getOwner());
        Assert.assertEquals("mob privacy should match", PrivacyType.NORMAL, findResult.getPrivacy());
        Assert.assertEquals("mob position should match", POSITION, findResult.getPosition());
        Assert.assertEquals("mob max hp should match", MAX_HP, findResult.getMaxHp());
        Assert.assertEquals("mob hp should match", HP, findResult.getHp());
        Assert.assertEquals("mob creature type should match", CREATURE_ID, findResult.getCreatureId());
        Assert.assertEquals("mob initiative should match", INITIATIVE, findResult.getInitiative());
        Assert.assertTrue("mob should be alive", findResult.isAlive());

    }
}
