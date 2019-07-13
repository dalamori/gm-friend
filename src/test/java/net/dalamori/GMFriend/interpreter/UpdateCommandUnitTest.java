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
public class UpdateCommandUnitTest {

    @Mock private SimpleCrudeService<String> mockService;
    @Mock private PrettyPrinter<String> mockPrinter;

    private UpdateCommand<String> command;
    private CommandContext context;

    private static final List<String> RAW_COMMAND = Arrays.asList("10 9 8 7 6".split("\\s"));

    @Before
    public void setup() {

        // set command up so it's update item saves args to context
        command = new UpdateCommand<String>() {
            @Override
            public String updateItem(CommandContext context, String item) {
                context.getData().put("item", item);
                return item;
            }
        };
        command.setService(mockService);
        command.setPrinter(mockPrinter);

        context = new CommandContext();
        context.setCommand(RAW_COMMAND);
        context.setIndex(1);
    }

    @Test
    public void updateCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: some mock responses
        Mockito.when(mockService.read(Mockito.anyLong())).thenReturn("Thorin");
        Mockito.when(mockService.update(Mockito.any())).thenReturn("is my dog's name");
        Mockito.when(mockPrinter.print(Mockito.any())).thenReturn("He's a good boy");

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the correct response
        Assert.assertEquals("should return 85%-truthful statement about my dog", "He's a good boy", context.getResponse());

        // and: I expect to see the proper mocks.
        Mockito.verify(mockService).update("Thorin");
        Mockito.verify(mockPrinter).print("is my dog's name");
    }
}
