package net.dalamori.GMFriend.exceptions;

public class DmFriendGeneralServiceException extends Exception {
    public DmFriendGeneralServiceException(String description, Throwable ex){
        super(description,ex);
    }
    public DmFriendGeneralServiceException(String description) {
        super(description);
    }
}
