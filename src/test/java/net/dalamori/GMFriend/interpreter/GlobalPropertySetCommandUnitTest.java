package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.MobileService;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.services.PropertyService;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class GlobalPropertySetCommandUnitTest {

    @Mock private PrettyPrinter<Property> mockPrinter;
    @Mock private CreatureService mockCreatureService;
    @Mock private LocationService mockLocationService;
    @Mock private MobileService mockMobileService;
    @Mock private NoteService mockNoteService;
    @Mock private PropertyService mockPropertyService;
    @Captor private ArgumentCaptor<Property> propertyCaptor;

    private GlobalPropertySetCommand command;
    private CommandContext context;
    private Property property;
    private Property savedProperty;
    private Map<String, Property> propertyMap;

    private static final String OWNER = "Bubba";
    private static final String PRINTER_OUTPUT = "THIS IS A TEST (no, really)";

    private static final List<String> INTEGER_COMMAND = Arrays.asList("blah set test 20".split("\\s"));
    private static final List<String> DECIMAL_COMMAND = Arrays.asList("blah set test 3.1415".split("\\s"));
    private static final List<String> CREATURE_COMMAND = Arrays.asList("blah set test creature bob".split("\\s"));
    private static final List<String> LOCATION_COMMAND = Arrays.asList("blah set test location 83".split("\\s"));
    private static final List<String> MOBILE_COMMAND = Arrays.asList("blah set test mobile 24".split("\\s"));
    private static final List<String> NOTE_COMMAND = Arrays.asList("blah set test note 35".split("\\s"));
    private static final List<String> STRING_COMMAND = Arrays.asList("blah set test This is a test.".split("\\s"));
    private static final List<String> ADD_COMMAND = Arrays.asList("blah set test add 25".split("\\s"));
    private static final List<String> ADD_DEFAULT_COMMAND = Arrays.asList("blah set test add".split("\\s"));
    private static final List<String> SUBTRACT_COMMAND = Arrays.asList("blah set test subtract 12".split("\\s"));
    private static final List<String> SUBTRACT_DEFAULT_COMMAND = Arrays.asList("blah set test subtract".split("\\s"));
    private static final List<String> NO_KEY_COMMAND = Arrays.asList("blah set".split("\\s"));
    private static final List<String> NO_VALUE_COMMAND = Arrays.asList("blah set test".split("\\s"));


    @Before
    public void setup() throws DmFriendGeneralServiceException {
        MockitoAnnotations.initMocks(this);

        // given: a sample context
        context = new CommandContext();
        context.setOwner(OWNER);
        context.setIndex(1);

        // and: a sample "test" property to manipulate
        property = new Property();
        property.setName("test");
        property.setOwner(OWNER);
        property.setType(PropertyType.STRING);
        property.setPrivacy(PrivacyType.NORMAL);

        // and: a copy of it with an id set
        savedProperty = new Property();
        savedProperty.setName("test");
        savedProperty.setOwner(OWNER);
        savedProperty.setType(PropertyType.STRING);
        savedProperty.setPrivacy(PrivacyType.NORMAL);
        savedProperty.setId(13L);

        // and: there's a map containing said property, keyed by name.
        propertyMap = new HashMap<>();
        propertyMap.put("test", property);

        // and: a sample command
        command = new GlobalPropertySetCommand();
        command.setCreatureService(mockCreatureService);
        command.setLocationService(mockLocationService);
        command.setMobileService(mockMobileService);
        command.setPropertyService(mockPropertyService);
        command.setNoteService(mockNoteService);
        command.setPropertyPrinter(mockPrinter);

        // and: a mock printer which produces test output
        Mockito.when(mockPrinter.print(Mockito.any())).thenReturn(PRINTER_OUTPUT);

        // and: a mock propertyService which will return savedProperty and propertyMap.
        Mockito.when(mockPropertyService.create(Mockito.any())).thenReturn(savedProperty);
        Mockito.when(mockPropertyService.update(Mockito.any())).thenReturn(savedProperty);
        Mockito.when(mockPropertyService.getGlobalProperties()).thenReturn(propertyMap);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithInteger() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(INTEGER_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to 20", "20", result.getValue());
        Assert.assertEquals("should set property to INTEGER", PropertyType.INTEGER, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithDecimal() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(DECIMAL_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to 3.1415", "3.1415", result.getValue());
        Assert.assertEquals("should set property to DECIMAL", PropertyType.DECIMAL, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithCreature() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(CREATURE_COMMAND);

        // and: a sample creature to look up
        Creature bob = new Creature();
        bob.setOwner(OWNER);
        bob.setPrivacy(PrivacyType.NORMAL);
        bob.setName("Robert Von Testguy");
        bob.setId(22L);

        Mockito.when(mockCreatureService.exists("bob")).thenReturn(true);
        Mockito.when(mockCreatureService.read("bob")).thenReturn(bob);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to bob's name", bob.getName(), result.getValue());
        Assert.assertEquals("should set property to CREATURE", PropertyType.CREATURE, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithLocation() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(LOCATION_COMMAND);

        // and: a sample location to look up
        Location farAway = new Location();
        farAway.setOwner(OWNER);
        farAway.setPrivacy(PrivacyType.NORMAL);
        farAway.setName("Way_over_there");
        farAway.setId(83L);

        Mockito.when(mockLocationService.exists("83")).thenReturn(true);
        Mockito.when(mockLocationService.read("83")).thenReturn(farAway);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to the targets name", farAway.getName(), result.getValue());
        Assert.assertEquals("should set property to LOCATION", PropertyType.LOCATION, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithMobile() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(MOBILE_COMMAND);

        // and: a sample mobile to look up
        Mobile fred = new Mobile();
        fred.setOwner(OWNER);
        fred.setPrivacy(PrivacyType.NORMAL);
        fred.setName("Fred");
        fred.setId(83L);

        Mockito.when(mockMobileService.exists("24")).thenReturn(true);
        Mockito.when(mockMobileService.read("24")).thenReturn(fred);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to the targets name", fred.getName(), result.getValue());
        Assert.assertEquals("should set property to MOBILE", PropertyType.MOBILE, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithNote() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(NOTE_COMMAND);

        // and: a sample note to look up
        Note note = new Note();
        note.setOwner(OWNER);
        note.setPrivacy(PrivacyType.NORMAL);
        note.setTitle("Memo_to_Self");
        note.setId(83L);

        Mockito.when(mockNoteService.exists("35")).thenReturn(true);
        Mockito.when(mockNoteService.read("35")).thenReturn(note);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to the targets title", note.getTitle(), result.getValue());
        Assert.assertEquals("should set property to NOTE", PropertyType.NOTE, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWithString() throws DmFriendGeneralServiceException {
        // given: a valid integer command
        context.setCommand(STRING_COMMAND);

        // when: I invoke the command
        command.handle(context);

        // then: I expect to succeed, with the retval from my printer in response
        Assert.assertEquals("should reply with printer response", PRINTER_OUTPUT, context.getResponse());

        // and: I expect to see property saved with the correct value

        Mockito.verify(mockPropertyService).create(propertyCaptor.capture());
        Property result = propertyCaptor.getValue();
        Assert.assertEquals("should set property to my test message", "This is a test.", result.getValue());
        Assert.assertEquals("should set property to STRING", PropertyType.STRING, result.getType());
        Mockito.verify(mockPropertyService).attachToGlobalContext(savedProperty);
    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWhenAdding() {

    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWhenAddingDefault() {

    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWhenSubtracting() {

    }

    @Test
    public void globalPropertySetCommand_handle_shouldHappyPathWhenSubtractingDefault() {

    }

    @Test
    public void globalPropertySetCommand_handle_shouldFailWhenNoKey() {

    }

    @Test
    public void globalPropertySetCommand_handle_shouldFailWhenNoValue() {
        Assert.fail("Stub!");
    }

}
