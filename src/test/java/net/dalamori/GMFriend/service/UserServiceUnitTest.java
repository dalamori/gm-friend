package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.repository.UserDao;
import net.dalamori.GMFriend.services.impl.UserServiceImpl;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
@Ignore
public class UserServiceUnitTest {

    @Mock
    private UserDao mockDao;

    private UserServiceImpl impl;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this.getClass());
        impl = new UserServiceImpl();
        impl.setUserDao(mockDao);
    }

    // create

    // read

    // update

    // delete

    // exists

    // deleteByOwner

    // deleteByGame

    // forGame

}
