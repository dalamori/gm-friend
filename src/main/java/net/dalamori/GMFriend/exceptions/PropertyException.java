package net.dalamori.GMFriend.exceptions;

public class PropertyException extends DmFriendGeneralServiceException {
    public PropertyException(String description, Throwable ex){
        super(description,ex);
    }
    public PropertyException(String description) {
        super(description);
    }
}
