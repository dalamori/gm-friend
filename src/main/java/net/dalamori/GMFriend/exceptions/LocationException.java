package net.dalamori.GMFriend.exceptions;

public class LocationException extends Exception {
    public LocationException(String description, Throwable ex){
        super(description,ex);
    }
    public LocationException(String description) {
        super(description);
    }
}
