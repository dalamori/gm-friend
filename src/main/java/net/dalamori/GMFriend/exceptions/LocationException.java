package net.dalamori.GMFriend.exceptions;

public class LocationException extends DmFriendGeneralServiceException {
    public LocationException(String description, Throwable ex){
        super(description,ex);
    }
    public LocationException(String description) {
        super(description);
    }
}
