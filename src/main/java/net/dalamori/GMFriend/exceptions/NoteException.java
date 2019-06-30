package net.dalamori.GMFriend.exceptions;

public class NoteException extends Exception {
    public NoteException(String description, Throwable ex){
        super(description,ex);
    }
}
