package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasProperties;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.MobileService;
import net.dalamori.GMFriend.services.NoteService;
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class PropertySetCommandUnitTest {

    // a test class with the requisite interface.
    public class PropertyWidget implements HasProperties, Serializable {
        private Map<String, Property> map = new HashMap<>();
        public Map<String, Property> getPropertyMap() {
            return map;
        }
        public String toString(){
            return map.toString();
        }
        public boolean equals(PropertyWidget operand) {
            return map.equals(operand.getPropertyMap());
        }
    }

    @Mock private SimpleCrudeService<PropertyWidget> mockService;
    @Mock private PrettyPrinter<PropertyWidget> mockPrinter;
    @Mock private CreatureService mockCreatureService;
    @Mock private LocationService mockLocationService;
    @Mock private MobileService mockMobileService;
    @Mock private NoteService mockNoteService;

    private PropertySetCommand<PropertyWidget> command;
    private CommandContext context;

    private PropertyWidget victim;

    private static final List<String> INTEGER_COMMAND = Arrays.asList("widget set Victim test 20".split("\\s"));
    private static final List<String> DECIMAL_COMMAND = Arrays.asList("widget set Victim test 3.1415".split("\\s"));
    private static final List<String> CREATURE_COMMAND = Arrays.asList("widget set Victim test creature 42".split("\\s"));
    private static final List<String> LOCATION_COMMAND = Arrays.asList("widget set Victim test location 83".split("\\s"));
    private static final List<String> MOBILE_COMMAND = Arrays.asList("widget set Victim test mobile 24".split("\\s"));
    private static final List<String> NOTE_COMMAND = Arrays.asList("widget set Victim test note 35".split("\\s"));
    private static final List<String> STRING_COMMAND = Arrays.asList("widget set Victim test This is a test.".split("\\s"));
    private static final List<String> ADD_COMMAND = Arrays.asList("widget set Victim test add 25".split("\\s"));
    private static final List<String> ADD_DEFAULT_COMMAND = Arrays.asList("widget set Victim test add".split("\\s"));
    private static final List<String> SUBTRACT_COMMAND = Arrays.asList("widget set Victim test subtract 12".split("\\s"));
    private static final List<String> SUBTRACT_DEFAULT_COMMAND = Arrays.asList("widget set Victim test subtract".split("\\s"));
    private static final List<String> NO_KEY_COMMAND = Arrays.asList("widget set Victim".split("\\s"));
    private static final List<String> NO_VALUE_COMMAND = Arrays.asList("widget set Victim test".split("\\s"));

    private static final String PRINTER_RETVAL = "Test Printer Return Value!";

    @Before
    public void setup() throws DmFriendGeneralServiceException {
        MockitoAnnotations.initMocks(this);

        // given: a sample command object
        command = new PropertySetCommand<>();
        command.setService(mockService);
        command.setPrinter(mockPrinter);
        command.setCreatureService(mockCreatureService);
        command.setLocationService(mockLocationService);
        command.setMobileService(mockMobileService);
        command.setNoteService(mockNoteService);

        // and: a sample context
        context = new CommandContext();
        context.setIndex(2);

        // and: a victim we can look up in service
        victim = new PropertyWidget();
        Mockito.when(mockService.read("Victim")).thenReturn(victim);
        Mockito.when(mockService.update(victim)).thenReturn(victim);

        // and: a mock printer than can output set values for us.
        Mockito.when(mockPrinter.print(Mockito.eq(victim))).thenReturn(PRINTER_RETVAL);

    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenInteger() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(INTEGER_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an INTEGER", PropertyType.INTEGER, testProperty.getType());
        Assert.assertEquals("should set the value to 20", "20", victim.getPropertyMap().get("test").getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenDecimal() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(DECIMAL_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be a DECIMAL", PropertyType.DECIMAL, testProperty.getType());
        Assert.assertEquals("should set the value to 3.1415", "3.1415", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenCreature() throws DmFriendGeneralServiceException {
        // given: a valid creature set command
        context.setCommand(CREATURE_COMMAND);

        // and: a creature to look up
        Creature target = new Creature();
        target.setName("Beebelbroxx");
        target.setOwner("Zaphod");
        target.setPrivacy(PrivacyType.NORMAL);
        target.setId(42L);

        Mockito.when(mockCreatureService.exists("42")).thenReturn(true);
        Mockito.when(mockCreatureService.read("42")).thenReturn(target);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be a CREATURE", PropertyType.CREATURE, testProperty.getType());
        Assert.assertEquals("should set the value to Beebelbroxx", "Beebelbroxx", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenLocation() throws DmFriendGeneralServiceException {
        // given: a valid location set command
        context.setCommand(LOCATION_COMMAND);

        // and: a creature to look up
        Location target = new Location();
        target.setName("Nowhere");
        target.setOwner("Me");
        target.setPrivacy(PrivacyType.NORMAL);
        target.setId(83L);

        Mockito.when(mockLocationService.exists("83")).thenReturn(true);
        Mockito.when(mockLocationService.read("83")).thenReturn(target);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be a LOCATION", PropertyType.LOCATION, testProperty.getType());
        Assert.assertEquals("should set the value to Nowhere", "Nowhere", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenMobile() throws DmFriendGeneralServiceException {
        // given: a valid location set command
        context.setCommand(MOBILE_COMMAND);

        // and: a creature to look up
        Mobile target = new Mobile();
        target.setName("Link");
        target.setOwner("Zelda");
        target.setPrivacy(PrivacyType.NORMAL);
        target.setId(24L);
        target.setMaxHp(27L);
        target.setHp(20L);
        target.setInitiative(21);
        target.setAlive(true);
        target.setCreatureId(83L);

        Mockito.when(mockMobileService.exists("24")).thenReturn(true);
        Mockito.when(mockMobileService.read("24")).thenReturn(target);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be a MOBILE", PropertyType.MOBILE, testProperty.getType());
        Assert.assertEquals("should set the value to Link", "Link", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenNote() throws DmFriendGeneralServiceException {
        // given: a valid location set command
        context.setCommand(NOTE_COMMAND);

        // and: a creature to look up
        Note target = new Note();
        target.setTitle("Mario");
        target.setOwner("Luigi");
        target.setPrivacy(PrivacyType.NORMAL);
        target.setId(35L);

        Mockito.when(mockNoteService.exists("35")).thenReturn(true);
        Mockito.when(mockNoteService.read("35")).thenReturn(target);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be a NOTE", PropertyType.NOTE, testProperty.getType());
        Assert.assertEquals("should set the value to Mario", "Mario", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenString() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(STRING_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property testProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an STRING", PropertyType.STRING, testProperty.getType());
        Assert.assertEquals("should set the value to 20", "This is a test.", testProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenIncrement() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(ADD_COMMAND);

        // and: that there is a property already there to increment;
        Property testProperty = new Property();
        testProperty.setId(44L);
        testProperty.setType(PropertyType.DECIMAL);
        testProperty.setName("test");
        testProperty.setOwner("Bob");
        testProperty.setPrivacy(PrivacyType.NORMAL);
        testProperty.setValue("123.45");
        victim.getPropertyMap().put("test", testProperty);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property resultProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an DECIMAL", PropertyType.DECIMAL, resultProperty.getType());
        Assert.assertEquals("should set the value to 148.45", "148.45", resultProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenIncrementDefault() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(ADD_DEFAULT_COMMAND);

        // and: that there is a property already there to increment;
        Property testProperty = new Property();
        testProperty.setId(44L);
        testProperty.setType(PropertyType.DECIMAL);
        testProperty.setName("test");
        testProperty.setOwner("Bob");
        testProperty.setPrivacy(PrivacyType.NORMAL);
        testProperty.setValue("123.45");
        victim.getPropertyMap().put("test", testProperty);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property resultProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an DECIMAL", PropertyType.DECIMAL, resultProperty.getType());
        Assert.assertEquals("should set the value to 124.45", "124.45", resultProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test(expected = InterpreterException.class)
    public void propertySetCommand_handle_shouldFailWhenIncrementNewProperty() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(ADD_COMMAND);

        // and: no "test" property exists

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail
        Assert.fail("should refuse to increment a property which doesn't exist");
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenDecrement() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(SUBTRACT_COMMAND);

        // and: that there is a property already there to increment;
        Property testProperty = new Property();
        testProperty.setId(44L);
        testProperty.setType(PropertyType.INTEGER);
        testProperty.setName("test");
        testProperty.setOwner("Bob");
        testProperty.setPrivacy(PrivacyType.NORMAL);
        testProperty.setValue("12345");
        victim.getPropertyMap().put("test", testProperty);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property resultProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an INTEGER", PropertyType.INTEGER, resultProperty.getType());
        Assert.assertEquals("should set the value to 12333", "12333", resultProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test
    public void propertySetCommand_handle_shouldHappyPathWhenWhenDecrementDefault() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(SUBTRACT_DEFAULT_COMMAND);

        // and: that there is a property already there to increment;
        Property testProperty = new Property();
        testProperty.setId(44L);
        testProperty.setType(PropertyType.INTEGER);
        testProperty.setName("test");
        testProperty.setOwner("Bob");
        testProperty.setPrivacy(PrivacyType.NORMAL);
        testProperty.setValue("12345");
        victim.getPropertyMap().put("test", testProperty);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to see the expected retval from the printer in response
        Assert.assertEquals("should get retval", PRINTER_RETVAL, context.getResponse());

        // and: I should see the proper property set
        Assert.assertTrue("should set the test property", victim.getPropertyMap().containsKey("test"));
        Property resultProperty = victim.getPropertyMap().get("test");
        Assert.assertEquals("should be an INTEGER", PropertyType.INTEGER, resultProperty.getType());
        Assert.assertEquals("should set the value to 12344", "12344", resultProperty.getValue());

        // and: I should see the proper calls to the service
        Mockito.verify(mockService).update(victim);
    }

    @Test(expected = InterpreterException.class)
    public void propertySetCommand_handle_shouldFailWhenDecrementNewProperty() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(SUBTRACT_COMMAND);

        // and: no "test" property exists

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail
        Assert.fail("should refuse to decrement a property which doesn't exist");
    }

    @Test(expected = InterpreterException.class)
    public void propertySetCommand_handle_shouldFailWhenNoKey() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(NO_KEY_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail
        Assert.fail("should throw an error failing to parse the command");
    }

    @Test(expected = InterpreterException.class)
    public void propertySetCommand_handle_shouldFailWhenNoValue() throws DmFriendGeneralServiceException {
        // given: a valid integer set command
        context.setCommand(NO_VALUE_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to fail
        Assert.fail("should throw an error failing to parse the command");
    }
}
