package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.models.enums.UserRole;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class MapCommandUnitTest {

    private InfoCommand target1;
    private InfoCommand target2;
    private InfoCommand defaultTarget;
    private MapCommand command;
    private CommandContext context;

    private static final List<String> COMMAND = Arrays.asList("lorem ipsum dolor sit amet".split("\\s"));
    private static final List<String> TOO_SHORT = COMMAND.subList(0,2);
    private static final List<String> NOT_FOUND = Arrays.asList("The quick brown Fox jumped over the lazy dog".split("\\s"));

    @Before
    public void setup() {
        target1 = new InfoCommand();
        target1.setInfo("target1");

        target2 = new InfoCommand();
        target2.setInfo("target2");

        defaultTarget = new InfoCommand();
        defaultTarget.setInfo("defaultTarget");

        command = new MapCommand();
        command.getMap().put("dolor", new MapSubcommand(UserRole.ROLE_STRANGER, "", target1));
        command.getMap().put("ipsum", new MapSubcommand(UserRole.ROLE_STRANGER, "", target2));
        command.setDefaultAction(defaultTarget);

        context = new CommandContext();
    }

    @Test
    public void mapCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: a command which will map to target 1;
        context.setCommand(COMMAND);
        context.setIndex(2);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to get the result from target1;
        Assert.assertEquals("should return target1", "target1", context.getResponse());
    }

    @Test
    public void mapCommand_handle_shouldDefaultWhenNotFound() throws DmFriendGeneralServiceException {
        // given: a command which will not map to any target
        context.setCommand(NOT_FOUND);
        context.setIndex(2);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to get the result from the default target
        Assert.assertEquals("should return default", "defaultTarget", context.getResponse());
    }

    @Test
    public void mapCommand_handle_shouldDefaultWhenTooShort() throws DmFriendGeneralServiceException {
        // given: a command which will not map to any target
        context.setCommand(TOO_SHORT);
        context.setIndex(2);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to get the result from the default target
        Assert.assertEquals("should return default", "defaultTarget", context.getResponse());
    }
}
