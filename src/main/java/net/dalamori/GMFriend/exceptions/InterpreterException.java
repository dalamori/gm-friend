package net.dalamori.GMFriend.exceptions;

public class InterpreterException extends DmFriendGeneralServiceException {
    public InterpreterException(String description, Throwable ex){
        super(description,ex);
    }
    public InterpreterException(String description) {
        super(description);
    }
}
