package net.dalamori.GMFriend.interpreter.printer;


public abstract class PrettyPrinter<T> {


    public abstract String print(T object);


    public static String formatName(String input) {
        return input.replaceAll("_", " ");

    }

    public static String truncate(String input, int length) {
        int cutAt = length;
        int firstNewline = input.indexOf("\n");

        if (firstNewline > 0 && firstNewline < length) {
            cutAt = firstNewline;
        }

        if (input.length() <= cutAt ) {
            return input;
        }

        return input.substring(0, cutAt).concat("...");
    }

}
