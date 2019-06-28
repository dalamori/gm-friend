package net.dalamori.GMFriend.exceptions;

public class PropertyException extends Exception {
    public PropertyException(String description, Throwable ex){
        super(description,ex);
    }
}
