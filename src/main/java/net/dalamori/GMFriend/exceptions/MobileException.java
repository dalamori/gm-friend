package net.dalamori.GMFriend.exceptions;

public class MobileException extends DmFriendGeneralServiceException {
    public MobileException(String description, Throwable ex){
        super(description,ex);
    }
    public MobileException(String description) {
        super(description);
    }
}
