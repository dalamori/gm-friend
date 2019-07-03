package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.repository.PropertyDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class PropertyServiceUnitTest {

    @Autowired
    DmFriendConfig config;

    @Mock private PropertyDao mockPropertyDao;
    @Mock private GroupService mockGroupService;

    @Before
    public void setup() {

    }

    @Test
    @Ignore
    public void stub() {

    }
}
