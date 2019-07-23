package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.repository.CreatureDao;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class CreatureMenuIntegrationTest {

    @Autowired
    public CreatureDao creatureDao;


}
