package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.User;
import net.dalamori.GMFriend.models.enums.UserRole;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class UserDaoIntegrationTest {

    @Autowired
    public UserDao userDao;

    private User user;

    public static final String GAME_A = "This is Game A";
    public static final String GAME_B = "Game B over here";
    public static final String OWNER_A = "a man";
    public static final String OWNER_B = "a woman";

    private String GLOBAL_GAME = User.GLOBAL_GAME_ID;

    @Before
    public void setup() {
        user = new User();
        user.setGame(GAME_A);
        user.setOwner(OWNER_A);
        user.setRole(UserRole.ROLE_OWNER);
    }

    @After
    public void teardown() {
        userDao.deleteAll();
    }

    @Test
    public void userDao_save_shouldHappyPath() {
        // when: I save the user
        User result = userDao.save(user);

        // then: i expect the return value to have an ID
        Assert.assertTrue("should be a User", result instanceof User);
        Assert.assertTrue("should have an ID", result.getId() instanceof Long);

        // and when: I look the user up
        Optional<User> findResult = userDao.findByOwnerAndGame(OWNER_A, GAME_A);

        // then: I should find him, with the correctly set properties.
        Assert.assertTrue("should exist", findResult.isPresent());
        Assert.assertEquals("should store correct role", UserRole.ROLE_OWNER, findResult.get().getRole());
    }

    @Test
    public void userDao_findHighestAuth_queryShouldHappyPath() {
        // given: a selection of saved accounts
        User globalUserA = new User();
        globalUserA.setRole(UserRole.ROLE_OBSERVER);
        globalUserA.setGame(GLOBAL_GAME);
        globalUserA.setOwner(OWNER_A);
        globalUserA = userDao.save(globalUserA);

        User globalUserB = new User();
        globalUserB.setOwner(OWNER_B);
        globalUserB.setGame(GLOBAL_GAME);
        globalUserB.setRole(UserRole.ROLE_SUPER_USER);
        globalUserB = userDao.save(globalUserB);

        User userA1 = userDao.save(user);

        User userA2 = new User();
        userA2.setOwner(OWNER_A);
        userA2.setGame(GAME_B);
        userA2.setRole(UserRole.ROLE_AUTHOR);
        userA2 = userDao.save(userA2);

        // when: I look up owner A and game A
        List<String> games = new ArrayList<>();
        games.add(GLOBAL_GAME);
        games.add(GAME_A);
        Optional<User> findResultA = userDao.findFirstByOwnerAndGameInOrderByRoleDesc(OWNER_A, games);

        // then: I expect to see the Owner role from userA1
        Assert.assertTrue("should find a result", findResultA.isPresent());
        Assert.assertEquals("should be ROLE_OWNER", UserRole.ROLE_OWNER, findResultA.get().getRole());
        Assert.assertEquals("should be userA1", userA1.getId(), findResultA.get().getId());

        // and when: I look up owner B and game A
        Optional<User> findResultB = userDao.findFirstByOwnerAndGameInOrderByRoleDesc(OWNER_B, games);

        // then: I expect to see the super user role from globalUserB
        Assert.assertTrue("should find a result", findResultB.isPresent());
        Assert.assertEquals("should be ROLE_SUPER_USER", UserRole.ROLE_SUPER_USER, findResultB.get().getRole());
        Assert.assertEquals("should be globalUserB", globalUserB.getId(), findResultB.get().getId());
    }
}
