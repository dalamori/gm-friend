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
public class AttachCommandUnitTest {

    @Mock private SimpleCrudeService<String> mockStringService;
    @Mock private SimpleCrudeService<Long> mockLongService;
    @Mock private PrettyPrinter<String> mockPrinter;

    private AttachCommand<String, Long> command;
    private CommandContext context;

    private static final List<String> RAW_COMMAND = Arrays.asList("1 2 3 4 5".split("\\s"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        command = new AttachCommand<String, Long>() {
            @Override
            public String updateItem(CommandContext context, String parent, Long child) {
                context.getData().put("parent", parent);
                context.getData().put("child", child);

                return "RETVAL";
            }
        };
        command.setService(mockStringService);
        command.setChildService(mockLongService);
        command.setPrinter(mockPrinter);

        context = new CommandContext();
        context.setIndex(2);
        context.setCommand(RAW_COMMAND);
    }

    @Test
    public void attachCommand_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: mock responses for the services
        Mockito.when(mockStringService.read(Mockito.anyString())).thenReturn("STRING");
        Mockito.when(mockLongService.read(Mockito.anyString())).thenReturn(76543L);
        Mockito.when(mockPrinter.print(Mockito.anyString())).thenReturn("CHOCOLATE");

        Mockito.when(mockStringService.update(Mockito.anyString())).thenReturn("FOO");

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see response from my printer mock
        Assert.assertEquals("should be CHOCOLATE", "CHOCOLATE", context.getResponse());

        // and: I expect to see the proper calls to my mock.
        Mockito.verify(mockStringService).read("3");
        Mockito.verify(mockLongService).read("4");
        Mockito.verify(mockPrinter).print("FOO");

        // and: I expect to see the proper data passed into updateItem method
        Assert.assertEquals("parent should be STRING", "STRING", context.getData().get("parent"));
        Assert.assertEquals("child should be 76543", 76543L, context.getData().get("child"));

    }

}
