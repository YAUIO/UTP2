package Utils;

import Utils.Terminal.OutputTerminal;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;

import java.awt.*;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class Print {
    public static final Color info = new Color(154, 106, 221);
    public static final Color debug = new Color(0, 34, 244);
    public static final Color log = new Color(121, 121, 121);
    public static final Color ioLog = new Color(65, 228, 107);
    public static final Color plainBg = new Color(127, 127, 127);
    public static final Color serverInfo = new Color(116, 213, 234);
    public static final Color error = new Color(225, 85, 166);

    public boolean isActive;
    private String prefix;
    private Color prefixColor;
    private boolean prefixAddition;
    public static OutputTerminal out;

    public Print(boolean active) {
        isActive = active;
        prefixAddition = true;
    }

    public void setPrefix(String pre) {
        prefix = pre;
    }

    public void setPrefix(String pre, Color c) {
        prefix = pre;
        prefixColor = c;
    }

    public void setPrefixAddition(boolean b) {
        prefixAddition = b;
    }

    public static void format(String s) {
        if (out == null) {
            if (s.startsWith("<log>")) {
                System.out.println(Ansi.colorize(s, getBackAttribute(log)));
            } else if (s.startsWith("<sent>") || s.startsWith("<received>")) {
                String msg = "";
                if (s.contains("<sent>")) {
                    msg += Ansi.colorize("<sent> ", getBackAttribute(ioLog));
                } else {
                    msg += Ansi.colorize("<received> ", getBackAttribute(ioLog));
                }
                msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), getBackAttribute(plainBg));
                System.out.println(msg);
            } else if (s.startsWith("<info>")) {
                System.out.println(Ansi.colorize(s, Attribute.BOLD(), getBackAttribute(info)));
            } else if (s.startsWith("<debug>")) {
                String msg = Ansi.colorize("<debug> ", getBackAttribute(debug), Attribute.WHITE_TEXT());
                msg += Ansi.colorize(s.substring(s.indexOf(' ')), Attribute.ITALIC(), getBackAttribute(plainBg));
                System.out.println(msg);
            } else if (s.contains("<serverInfo>")) {
                System.out.println(Ansi.colorize(s, getBackAttribute(serverInfo), Attribute.BLACK_TEXT()));
            } else {
                System.out.println(s);
            }
        } else {
            printOut(s);
        }
    }

    public void formatR(String s) {
        if (isActive) {
            if (out == null) {
                synchronized (System.out) {
                    if (prefix != null && prefixAddition) {
                        System.out.print(prefix);
                    }

                    format(s);
                }
            } else {
                if (prefix != null && prefixAddition && prefixColor != null) {
                    out.append(prefix, prefixColor);
                }

                printOut(s);
            }
        }
    }

    public static void error(String s) {
        if (out == null) {
            System.out.println(Ansi.colorize("<error> " + s, Attribute.BLACK_TEXT(), getBackAttribute(error)));
        } else {
            out.appendln("<error> " + s, error);
        }
    }

    public void errorR(String s) {
        if (out == null) {
            synchronized (System.out) {
                if (isActive) {
                    if (prefix != null && prefixAddition) {
                        System.out.print(prefix);
                    }
                    error(s);
                }
            }
        } else {
            if (prefix != null && prefixAddition && prefixColor != null) {
                out.append(prefix, prefixColor);
            }

            out.appendln("<error> " + s, error);
        }
    }

    private static void printOut(String s) {
        if (s.startsWith("<log>")) {
            out.appendln(s, log);
        } else if (s.startsWith("<sent>") || s.startsWith("<received>")) {
            if (s.contains("<sent>")) {
                out.append("<sent> ", ioLog);
            } else {
                out.append("<received> ", ioLog);
            }
            out.appendln(s.substring(s.indexOf(' ')), plainBg);
        } else if (s.startsWith("<info>")) {
            out.appendln(s, info);
        } else if (s.startsWith("<debug>")) {
            out.append("<debug> ", debug);
            out.appendln(s.substring(s.indexOf(' ')), plainBg);
        } else if (s.contains("<serverInfo>")) {
            out.appendln(s, serverInfo);
        } else {
            out.appendln(s, out.getBackground());
        }
    }

    public static <T> String toStr(Collection<T> col) {
        StringBuilder r = new StringBuilder();

        for (T element : col) {
            r.append(element).append(", ");
        }

        if (r.length() >= 2) {
            r = new StringBuilder(r.substring(0, r.length() - 2));
        }

        return r.toString();
    }

    public static <T> String toStr(Collection<T> col, Function<T, String> proj) {
        StringBuilder r = new StringBuilder();

        for (T element : col) {
            r.append(proj.apply(element)).append(", ");
        }

        if (r.length() >= 2) {
            r = new StringBuilder(r.substring(0, r.length() - 2));
        }

        return r.toString();
    }

    public static <T> String toStr(Collection<T> col, boolean count) {
        StringBuilder r = new StringBuilder();

        if (count) {
            int c = 0;


            for (T element : col) {
                r.append("(").append(c).append(") ").append(element).append(", ");
                c++;
            }

            if (r.length() >= 2) {
                r = new StringBuilder(r.substring(0, r.length() - 2));
            }
        } else {
            return toStr(col);
        }

        return r.toString();
    }

    public static String normalize(String s, int len) {
        StringBuilder r = new StringBuilder();

        if (s.length() < len) {
            for (int i = 0; i < (len - s.length()) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }

            r.append(Ansi.colorize(s, Attribute.BLUE_BACK()));

            for (int i = 0; i < (len - s.length()) / 2 + (len - s.length()) % 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }
        } else {
            for (int i = 0; i < (s.length() - len) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }

            r.append(Ansi.colorize(s.substring((s.length() - len) / 2, s.length() - (s.length() - len)), Attribute.BLUE_BACK()));

            for (int i = 0; i < (s.length() - len) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }
        }

        r.append(Ansi.colorize("|", Attribute.BLUE_BACK()));

        return r.toString();
    }


    public static String normalize(String s, int len, boolean isBool) {
        StringBuilder r = new StringBuilder();

        if (s.length() < len) {
            for (int i = 0; i < (len - s.length()) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }

            if (isBool) {
                if (s.equals("true")) {
                    r.append(Ansi.colorize(s, Attribute.GREEN_BACK()));
                } else {
                    r.append(Ansi.colorize(s, Attribute.RED_BACK()));
                }
            } else {
                r.append(Ansi.colorize(s, Attribute.BLUE_BACK()));
            }

            for (int i = 0; i < (len - s.length()) / 2 + (len - s.length()) % 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }
        } else {
            for (int i = 0; i < (s.length() - len) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }

            r.append(Ansi.colorize(s.substring((s.length() - len) / 2, s.length() - (s.length() - len)), Attribute.BLUE_BACK()));

            for (int i = 0; i < (s.length() - len) / 2; i++) {
                r.append(Ansi.colorize(" ", Attribute.BLUE_BACK()));
            }
        }

        r.append(Ansi.colorize("|", Attribute.BLUE_BACK()));

        return r.toString();
    }

    public static <T> String toStr(Collection<T> col, Function<T, String> proj, boolean count, String separator) {
        StringBuilder r = new StringBuilder();

        if (count) {
            int c = 0;

            for (T element : col) {
                r.append("(").append(c).append(") ").append(proj.apply(element)).append(separator);
                c++;
            }

            if (r.length() >= separator.length()) {
                r = new StringBuilder(r.substring(0, r.length() - separator.length()));
            }
        } else {
            return toStr(col, proj);
        }

        return r.toString();
    }

    public static Attribute getBackAttribute(Color color) {
        return Attribute.BACK_COLOR(color.getRed(), color.getGreen(), color.getBlue());
    }
}
