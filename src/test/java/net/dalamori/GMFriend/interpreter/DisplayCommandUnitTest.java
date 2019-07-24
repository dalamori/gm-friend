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
public class DisplayCommandUnitTest {

    @Mock private PrettyPrinter<String> mockPrinter;
    @Mock private SimpleCrudeService<String> mockService;

    private DisplayCommand<String> command;
    private CommandContext context;

    private static final List<String> RAW_STRING = Arrays.asList("lorem ipsum dolor sit amet".split("\\s"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        command = new DisplayCommand<>();
        command.setService(mockService);
        command.setPrinter(mockPrinter);

        context = new CommandContext();
    }

    @Test
    public void displayCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: a service which will return Strings for IDs
        Mockito.when(mockService.read(Mockito.anyString())).thenReturn("OH-SNAP");
        Mockito.when(mockPrinter.print(Mockito.anyString())).thenReturn("BINGO!");

        // and: a numeric command;
        context.setCommand(RAW_STRING);
        context.setIndex(2);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the result from my mock
        Assert.assertEquals("should return BINGO", "BINGO!", context.getResponse());

        // and: I should see the proper calls to my mocks
        Mockito.verify(mockPrinter).print("OH-SNAP");
        Mockito.verify(mockService).read("dolor");
    }
}
