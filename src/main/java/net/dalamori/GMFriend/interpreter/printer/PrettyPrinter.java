package net.dalamori.GMFriend.interpreter.printer;


public abstract class PrettyPrinter<T> {


    public abstract String print(T object);


    public static String formatName(String input) {
        return input.replaceAll("_", " ");

    }

    public static String truncate(String input, int length) {
        if (input.length() <= length ) {
            return input;
        }

        return input.substring(0, length).concat("...");
    }

}
