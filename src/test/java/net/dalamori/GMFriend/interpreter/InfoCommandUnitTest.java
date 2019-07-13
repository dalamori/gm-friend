package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class InfoCommandUnitTest {

    private static final String INFO = "It's a small world after all";

    private InfoCommand command;
    private CommandContext context;

    @Before
    public void setup() {
        command = new InfoCommand();
        command.setInfo(INFO);

        context = new CommandContext();
    }

    @Test
    public void infoCommand_shouldHappyPath() throws DmFriendGeneralServiceException {
        // when: I invoke the command
        command.handle(context);

        // then: I should get INFO as my response;
        Assert.assertEquals("should return INFO", INFO, context.getResponse());
    }
}
