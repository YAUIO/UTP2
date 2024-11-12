package Utils;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

public class Print {
    public static void format(String s) {
        if (s.startsWith("<log>")) {
            System.out.println(Ansi.colorize(s,Attribute.BRIGHT_BLACK_BACK()));
        } else if (s.startsWith("<sent>") || s.startsWith("<received>") ){
            System.out.println(Ansi.colorize(s,Attribute.GREEN_TEXT()));
        } else if (s.startsWith("<info>") ){
            System.out.println(Ansi.colorize(s,Attribute.BOLD(), Attribute.BLUE_BACK()));
        } else {
            System.out.println(s);
        }
    }

    public static void error(String s) {
        System.out.println(Ansi.colorize("<error> " + s, Attribute.BLACK_TEXT(), Attribute.RED_BACK()));
    }
}
