package Utils;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class Print {
    public boolean isActive;

    public Print(boolean active) {
        isActive = active;
    }

    public static void format(String s) {
        if (s.startsWith("<log>")) {
            System.out.println(Ansi.colorize(s, Attribute.BRIGHT_BLACK_BACK()));
        } else if (s.startsWith("<sent>") || s.startsWith("<received>")) {
            String msg = "";
            if (s.contains("<sent>")) {
                msg += Ansi.colorize("<sent> ", Attribute.GREEN_BACK());
            } else {
                msg += Ansi.colorize("<received> ", Attribute.GREEN_BACK());
            }
            msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), Attribute.WHITE_BACK());
            System.out.println(msg);
        } else if (s.startsWith("<info>")) {
            System.out.println(Ansi.colorize(s, Attribute.BOLD(), Attribute.BLUE_BACK()));
        } else if (s.startsWith("<debug>")) {
            String msg = Ansi.colorize("<debug> ", Attribute.BACK_COLOR(0, 34, 244), Attribute.WHITE_TEXT());
            msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), Attribute.WHITE_BACK());
            System.out.println(msg);
        } else if (s.contains("<serverInfo>")) {
            System.out.println(Ansi.colorize(s, Attribute.CYAN_BACK(), Attribute.BLACK_TEXT()));
        } else {
            System.out.println(s);
        }
    }

    public void formatR(String s) {
        if (isActive) {
            if (s.startsWith("<log>")) {
                System.out.println(Ansi.colorize(s, Attribute.BRIGHT_BLACK_BACK()));
            } else if (s.startsWith("<sent>") || s.startsWith("<received>")) {
                String msg = "";
                if (s.contains("<sent>")) {
                    msg += Ansi.colorize("<sent> ", Attribute.GREEN_BACK());
                } else {
                    msg += Ansi.colorize("<received> ", Attribute.GREEN_BACK());
                }
                msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), Attribute.WHITE_BACK());
                System.out.println(msg);
            } else if (s.startsWith("<info>")) {
                System.out.println(Ansi.colorize(s, Attribute.BOLD(), Attribute.BLUE_BACK()));
            } else if (s.startsWith("<debug>")) {
                String msg = Ansi.colorize("<debug> ", Attribute.BACK_COLOR(0, 34, 244), Attribute.WHITE_TEXT());
                msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), Attribute.WHITE_BACK());
                System.out.println(msg);
            } else if (s.contains("<serverInfo>")) {
                System.out.println(Ansi.colorize(s, Attribute.CYAN_BACK(), Attribute.BLACK_TEXT()));
            } else {
                System.out.println(s);
            }
        }
    }

    public static void error(String s) {
        System.out.println(Ansi.colorize("<error> " + s, Attribute.BLACK_TEXT(), Attribute.RED_BACK()));
    }

    public void errorR(String s) {
        if (isActive) {
            System.out.println(Ansi.colorize("<error> " + s, Attribute.BLACK_TEXT(), Attribute.RED_BACK()));
        }
    }

    public static <T> String toStr(Collection<T> col) {
        String r = "";

        for (T element : col) {
            r += element + ", ";
        }

        if (r.length() >= 2) {
            r = r.substring(0, r.length() - 2);
        }

        return r;
    }

    public static <T> String toStr(Collection<T> col, Function<T, String> proj) {
        String r = "";

        for (T element : col) {
            r += proj.apply(element) + ", ";
        }

        if (r.length() >= 2) {
            r = r.substring(0, r.length() - 2);
        }

        return r;
    }

    public static <T> String toStr(Collection<T> col, boolean count) {
        String r = "";

        if (count) {
            int c = 0;


            for (T element : col) {
                r += "(" + c + ") " + element + ", ";
                c++;
            }

            if (r.length() >= 2) {
                r = r.substring(0, r.length() - 2);
            }
        } else {
            return toStr(col);
        }

        return r;
    }

    public static <T> String toStr(Collection<T> col, Function<T, String> proj, boolean count, String separator) {
        String r = "";

        if (count) {
            int c = 0;

            for (T element : col) {
                r += "(" + c + ") " + proj.apply(element) + separator;
                c++;
            }

            if (r.length() >= separator.length()) {
                r = r.substring(0, r.length() - separator.length());
            }
        } else {
            return toStr(col, proj);
        }

        return r;
    }
}
