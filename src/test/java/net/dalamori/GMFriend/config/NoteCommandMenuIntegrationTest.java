package net.dalamori.GMFriend.config;

import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.repository.GroupDao;
import net.dalamori.GMFriend.repository.NoteDao;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.testing.IntegrationTest;
import net.dalamori.GMFriend.testing.TestDataFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.AssertTrue;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class NoteCommandMenuIntegrationTest {

    @Autowired
    public NoteDao noteDao;

    @Autowired
    public GroupDao groupDao;

    @Autowired
    public NoteService noteService;

    @Autowired
    public AbstractCommand rootCommand;

    private static final String PARAGRAPH_1 = "Lorem ipsum dolor sit amet\n";
    private static final String PARAGRAPH_2 = "The quick brown fox jumped over the lazy dog\n";
    private static final String EXPECTED = PARAGRAPH_1.concat(PARAGRAPH_2);


    @After
    public void teardown() {
        groupDao.deleteAll();
        noteDao.deleteAll();
    }

    @Test
    public void noteMenu_shouldHappyPathCreateEditDelete() throws DmFriendGeneralServiceException {
        // given: a set of commands which create, and then edit a location;
        List<String> commands = Arrays.asList((
                String.format(  ";; note new Note_A %s", PARAGRAPH_2) +
                String.format(  ";; note set Note_A %s", PARAGRAPH_1) +
                String.format(  ";; note append Note_A %s", PARAGRAPH_2) +
                String.format(  ";; note new Note_B %s", PARAGRAPH_2) +
                                ";; note new Note_C foo\n" +
                                ";; note show Note_C\n" +
                                ";; note delete Note_C\n"
                ).split("\n"));

        // when: I run the commands:
        for (String commandLine : commands) {
            CommandContext context = TestDataFactory.makeContextFromCommandLine(commandLine);
            rootCommand.handle(context);
        }

        // then: I expect Note_A to exist
        Assert.assertTrue("Note_A should exist", noteDao.existsByTitle("Note_A"));
        Note noteA = noteService.read("Note_A");
        Assert.assertEquals("should have the expected contents", EXPECTED, noteA.getBody());

        // and: I expect Note_B to exist
        Assert.assertTrue("Note_B should exist", noteDao.existsByTitle("Note_B"));
        Note noteB = noteService.read("Note_B");
        Assert.assertEquals("should have the right contents", PARAGRAPH_2, noteB.getBody());

        // and: I expect Note_C to not exist
        Assert.assertFalse("Note_C should no longer exist", noteDao.existsByTitle("Note_C"));

        // and: I expect the global_notes to contain Notes A and B
        List<Note> noteList = noteService.getGlobalNotes();
        List<String> expectedNoteTitles = Arrays.asList("Note_A Note_B".split("\\s"));
        Assert.assertEquals("global notes should be 2 long", 2, noteList.size());
        Assert.assertTrue("should contain noteA", expectedNoteTitles.contains(noteList.get(0).getTitle()));
        Assert.assertTrue("should contain noteB", expectedNoteTitles.contains(noteList.get(1).getTitle()));
    }
}
