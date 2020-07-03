package net.dalamori.GMFriend.exceptions;

public class UserException extends DmFriendGeneralServiceException {
    public UserException(String description, Throwable ex){
        super(description,ex);
    }
    public UserException(String description) {
            super(description);
        }
}
