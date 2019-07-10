package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@Category(UnitTest.class)
@SpringBootTest
public class AbstractCommandUnitTests {

    private CommandContext context;
    private AbstractCommand command;

    private static final String COMMAND_INPUT = "lorem ipsum dolor sit amet";
    private static final String OWNER = "Tony";

    @Before
    public void setup() {

        context = new CommandContext();
        context.setCommand(Arrays.asList(COMMAND_INPUT.split("\\s")));
        context.setOwner(OWNER);
        context.setIndex(0);

        command = new AbstractCommand() {
            @Override
            public void handle(CommandContext context) throws InterpreterException {
                return;
            }
        };
    }

    @Test
    public void abstractCommand_getCurrentCommandPart_shouldHappyPath() {
        // given: index is 2
        context.setIndex(2);

        // when: I look up the current command part
        String result = command.getCurrentCommandPart(context);

        // then: I should get the current command part
        Assert.assertEquals("should get the correct part", "dolor", result);
    }

    @Test
    public void abstractCommand_getCurrentCommandPart_shouldReturnBlankStringWhenOutOfBounds() {
        // given: index is 200
        context.setIndex(200);

        // when: I look up the current command part
        String result = command.getCurrentCommandPart(context);

        // then: I should get the current command part
        Assert.assertEquals("should get an empty string", "", result);

    }

    @Test
    public void abstractCommand_getRemainingCommand_shouldHappyPath() {
        // given: index is 2
        context.setIndex(2);

        // when: I get the remaining command;
        String result = command.getRemainingCommand(context);

        // then: I should get the remaining command as a string
        Assert.assertEquals("should return rest of command", "sit amet", result);
    }

    @Test
    public void abstractCommand_getRemainingCommand_shouldReturnBlankStringWhenOutOfBounds() {
        // given: index is 250
        context.setIndex(250);

        // when: I get the remaining command;
        String result = command.getRemainingCommand(context);

        // then: I should get the remaining command as a string
        Assert.assertEquals("should return rest of command", "", result);
    }

}
