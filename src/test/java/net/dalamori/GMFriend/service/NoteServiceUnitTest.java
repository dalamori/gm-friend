package net.dalamori.GMFriend.service;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.repository.NoteDao;
import net.dalamori.GMFriend.services.GroupService;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.services.impl.NoteServiceImpl;
import net.dalamori.GMFriend.testing.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(UnitTest.class)
public class NoteServiceUnitTest {

    @Autowired
    public DmFriendConfig config;

    @Mock private NoteDao mockDao;
    @Mock private GroupService mockGroupService;

    private NoteServiceImpl impl;
    private NoteService noteService;
    private Note note;
    private Note savedNote;

    public static final Long NOTE_ID = Long.valueOf(8675309);
    public static final String NOTE_BODY = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";
    public static final String NOTE_OWNER = "Some Test Guy";
    public static final String NOTE_TITLE = "A Test Title";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        note = new Note();
        note.setTitle(NOTE_TITLE);
        note.setBody(NOTE_BODY);
        note.setOwner(NOTE_OWNER);
        note.setPrivacy(PrivacyType.NORMAL);

        savedNote = new Note();
        savedNote.setId(NOTE_ID);
        savedNote.setTitle(NOTE_TITLE);
        savedNote.setBody(NOTE_BODY);
        savedNote.setOwner(NOTE_OWNER);
        savedNote.setPrivacy(PrivacyType.NORMAL);

        impl = new NoteServiceImpl();
        impl.setConfig(config);
        impl.setGroupService(mockGroupService);
        impl.setNoteDao(mockDao);

        noteService = impl;

    }

    @Test
    public void noteService_create_shouldHappyPath() throws NoteException {

    }

    @Test
    public void setNoteService_create_shouldFailIfIdSet() throws NoteException {

    }

    @Test
    public void setNoteService_create_shouldFailWhenInvalid() throws NoteException {

    }
}
