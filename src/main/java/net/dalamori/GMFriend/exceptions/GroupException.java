package net.dalamori.GMFriend.exceptions;

public class GroupException extends Exception {
    public GroupException(String description, Throwable ex){
        super(description,ex);
    }
    public GroupException(String description) {
        super(description);
    }
}
