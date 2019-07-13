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
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class CreateCommandUnitTest {

    @Mock private SimpleCrudeService<String> mockService;
    @Mock private PrettyPrinter<String> mockPrinter;

    private CreateCommand<String> command;
    private CommandContext context;

    private static final List<String> RAW_COMMAND = Arrays.asList("lorem ipsum dolor sit amet".split("\\s"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        command = new CreateCommand<String>() {
            @Override
            public String buildItem(CommandContext context) {
                context.getData().put("test", "Yes");
                return "TEST";
            }
        };
        command.setService(mockService);
        command.setPrinter(mockPrinter);

        context = new CommandContext();
        context.setCommand(RAW_COMMAND);
        context.setIndex(3);
    }

    @Test
    public void createCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: some mock responses
        Mockito.when(mockPrinter.print(Mockito.anyString())).thenReturn("HAPPY");
        Mockito.when(mockService.create(Mockito.any())).thenReturn("PATH");

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the correct response value;
        Assert.assertEquals("should return HAPPY", "HAPPY", context.getResponse());
        Mockito.verify(mockPrinter).print("PATH");

    }
}
