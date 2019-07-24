package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasProperties;
import net.dalamori.GMFriend.services.SimpleCrudeService;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class PropertyDeleteCommandUnitTest {

    // a test class with the requisite interface.
    public class PropertyWidget implements HasProperties, Serializable {
        private Map<String, Property> map = new HashMap<>();
        public Map<String, Property> getPropertyMap() {
            return map;
        }
        public String toString(){
            return map.toString();
        }
        public boolean equals(PropertySetCommandUnitTest.PropertyWidget operand) {
            return map.equals(operand.getPropertyMap());
        }
    }

    @Mock private SimpleCrudeService<PropertyWidget> mockService;
    @Mock private PrettyPrinter<PropertyWidget> mockPrinter;

    private PropertyDeleteCommand<PropertyWidget> command;
    private CommandContext context;
    private PropertyWidget victim;

    private static final List<String> DELETE_COMMAND = Arrays.asList("widget unset Victim test".split("\\s"));
    private static final List<String> NO_KEY_COMMAND = Arrays.asList("widget unset Victim".split("\\s"));

    @Before
    public void setup() throws DmFriendGeneralServiceException {
        MockitoAnnotations.initMocks(this);

        // given: a context
        context = new CommandContext();
        context.setIndex(2);

        // and: a command
        command = new PropertyDeleteCommand<>();
        command.setService(mockService);
        command.setPrinter(mockPrinter);

        // and: a victim in the mock service
        victim = new PropertyWidget();
        Mockito.when(mockService.exists("Victim")).thenReturn(true);
        Mockito.when(mockService.read("Victim")).thenReturn(victim);
        Mockito.when(mockService.update(victim)).thenReturn(victim);

        // and: a printer
        Mockito.when(mockPrinter.print(victim)).thenReturn("Hello");
    }

    @Test
    public void propertyDeleteCommand_handle_shouldHappyPath() throws DmFriendGeneralServiceException {
        // given: a valid delete command
        context.setCommand(DELETE_COMMAND);

        // and: that the "test" property is set
        Property target = new Property();
        target.setOwner("Ed");
        target.setPrivacy(PrivacyType.NORMAL);
        target.setName("test");
        target.setType(PropertyType.INTEGER);
        target.setId(33L);
        target.setValue("321");

        victim.getPropertyMap().put("test", target);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the anticipated response
        Assert.assertEquals("should respond Hello", "Hello", context.getResponse());


        // and: I expect to see the property removed.
        Assert.assertFalse("should not have test property", victim.getPropertyMap().containsKey("test"));

        // and: I expect to see the calls to my mocks
        Mockito.verify(mockService).update(victim);
        Mockito.verify(mockPrinter).print(victim);
    }

    @Test(expected = InterpreterException.class)
    public void propertyDeleteCommand_handle_shouldFailWhenNotFound() throws DmFriendGeneralServiceException {
        // given: a valid delete command
        context.setCommand(DELETE_COMMAND);

        // and: that the "test" property is not set

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail with not found
        Assert.fail("should refuse to delete non-existent property");
    }

    @Test(expected = InterpreterException.class)
    public void propertyDeleteCommand_handle_shouldFailWhenNoKey() throws DmFriendGeneralServiceException {
        // given: a valid delete command
        context.setCommand(DELETE_COMMAND);

        // and: that the "test" property is not set

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail with not found
        Assert.fail("should throw an exception if it can't determine key");
    }
}
