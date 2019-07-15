package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.services.SimpleCrudeService;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class DeleteCommandUnitTest {

    @Mock private SimpleCrudeService<String> mockService;

    private DeleteCommand<String> command;
    private CommandContext context;

    private static final List<String> RAW_COMMAND = Arrays.asList("42 43 44 45 46".split("\\s"));

    @Before
    public void setup() {
        command = new DeleteCommand<>();
        command.setService(mockService);

        context = new CommandContext();
        context.setCommand(RAW_COMMAND);
        context.setIndex(2);
    }

    @Test
    public void deleteCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: a stub so delete can look up
        Mockito.when(mockService.read(Mockito.anyString())).thenReturn("Wanda");

        // when: I invoke the command;
        command.handle(context);

        // then: I expect to see the the delete call to the service
        Mockito.verify(mockService).read("44");
        Mockito.verify(mockService).delete("Wanda");

        // and: some acknowledgement has been sent
        Assert.assertTrue("should give some reply", context.getResponse().length() > 0);
    }
}
