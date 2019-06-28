package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
public class GroupDaoIntegrationTest {

    @Autowired
    public GroupDao groupDao;

    private Group group;

    public static final String GROUP_NAME = "A dummy list";
    public static final String OWNER_NAME = "Some Dude named Kevin";
    public static final Long CONTENT_A = Long.valueOf(1234);
    public static final Long CONTENT_B = Long.valueOf(4321);
    public static final Long CONTENT_C = Long.valueOf(5555);


    @Before
    public void setup() {
        groupDao.deleteAll();

        group = new Group();
        group.setContents(new HashSet<>());
        Set<Long> contents = group.getContents();
        contents.add(CONTENT_A);
        contents.add(CONTENT_B);
        contents.add(CONTENT_C);
        group.setName(GROUP_NAME);
        group.setOwner(OWNER_NAME);
        group.setPrivacy(PrivacyType.NORMAL);
        group.setContentType(PropertyType.NOTE);

    }

    @Test
    public void groupDao_save_shouldHappyPath() {
        // when: I save a group object
        Group result = groupDao.save(group);

        // then; I expect to get a result with an id
        Assert.assertTrue("should return a group", result instanceof Group);
        Assert.assertTrue("should have an id", result.getId() instanceof Long);

        // and: i should be able to read that group back
        Group findResult = groupDao.findById(result.getId()).get();

        Assert.assertEquals("lookup should return a copy with the correct name",
                GROUP_NAME, findResult.getName());
        Assert.assertEquals("lookup should return a copy with the correct owner",
                OWNER_NAME, findResult.getOwner());
        Assert.assertEquals("lookup should return a copy with the correct privacy",
                PrivacyType.NORMAL, findResult.getPrivacy());
        Assert.assertEquals("lookup should return a copy with the correct content type",
                PropertyType.NOTE, findResult.getContentType());

        // and; the content ids should be correct
        Set<Long> contents = findResult.getContents();
        Assert.assertEquals("contents should have 3 members", 3, contents.size());
        Assert.assertTrue("contents should include Content A", contents.contains(CONTENT_A));
        Assert.assertTrue("contents should include Content B", contents.contains(CONTENT_B));
        Assert.assertTrue("contents should include Content C", contents.contains(CONTENT_C));
    }
}
