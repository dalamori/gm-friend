package net.dalamori.GMFriend.repository;

import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.testing.IntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class NoteDaoIntegrationTest {

    @Autowired
    public NoteDao noteDao;

    private Note note;

    public static final String NOTE_BODY = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";
    public static final String NOTE_OWNER = "Some Test Guy";
    public static final String NOTE_TITLE = "A Test Title";

    @Before
    public void setup() {
        noteDao.deleteAll();

        note = new Note();
        note.setTitle(NOTE_TITLE);
        note.setBody(NOTE_BODY);
        note.setOwner(NOTE_OWNER);
        note.setPrivacy(PrivacyType.NORMAL);
    }

    @Test
    public void noteDao_save_shouldHappyPath() {
        // when: I try to save a note
        Note result = noteDao.save(note);

        // then: I should get a result with an id
        Assert.assertTrue("should return a result", result instanceof Note);
        Assert.assertTrue("should have an id", result.getId() instanceof Long);

        // and: I should be able to read that note back
        Note findResult = noteDao.findById(result.getId()).get();

        Assert.assertEquals("lookup returns copy with correct title", NOTE_TITLE, findResult.getTitle());
        Assert.assertEquals("lookup returns copy with correct body", NOTE_BODY, findResult.getBody());
        Assert.assertEquals("lookup returns copy with correct owner", NOTE_OWNER, findResult.getOwner());
        Assert.assertEquals("lookup returns copy with correct privacy", PrivacyType.NORMAL, findResult.getPrivacy());
    }
}
