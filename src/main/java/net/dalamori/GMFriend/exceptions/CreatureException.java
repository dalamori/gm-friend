package net.dalamori.GMFriend.exceptions;

public class CreatureException extends DmFriendGeneralServiceException {
    public CreatureException(String description, Throwable ex){
        super(description,ex);
    }
    public CreatureException(String description) {
        super(description);
    }
}
