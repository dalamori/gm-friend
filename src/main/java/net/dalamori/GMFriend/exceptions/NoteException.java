package net.dalamori.GMFriend.exceptions;

public class NoteException extends DmFriendGeneralServiceException {
    public NoteException(String description, Throwable ex){
        super(description,ex);
    }
    public NoteException(String description) {
        super(description);
    }
}
