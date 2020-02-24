package com.denizenscript.denizencore.objects;

import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.debugging.Debug;

import java.util.*;

public class ArgumentHelper {

    // <--[language]
    // @name Number and Decimal
    // @group Common Terminology
    // @description
    // Many arguments in Denizen require the use of a 'number', or 'decimal'. Sometimes shorthanded to '#' or '#.#',
    // this kind of input can generally be filled with any reasonable positive or negative number.
    // 'decimal' inputs allow (but don't require) a decimal point in the number.
    // 'number' inputs will be rounded, so avoided a decimal point is better. For example, '3.1' will be interpreted as just '3'.
    // Numbers can be verified with the 'if' commands' 'matches' functionality.
    // For example: "- if <number> matches number" ... will return true if <number> is a valid number.
    // -->

    // <--[language]
    // @name Percentage
    // @group Common Terminology
    // @description
    // Promotes the usage of a 'percentage' format to be used in applicable arguments. The 'percentage' in Denizen is
    // much like the 'number', except arguments which utilize percentages instead of numbers can also include a %.
    // Percentage arguments can generally be filled with any reasonable positive or negative number with or without a
    // decimal point and/or percentage sign. Arguments and other usages will typically refer to a percentage as
    // #.#% or <percentage>. Percentages can be verified with the 'if' commands' 'matches' functionality.
    // For example: - if <percentage> matches percentage ... will return true if <percentage> is a valid percentage.
    //
    // Generally it's best to not include the '%' symbol, and some percentage inputs will actually not accept a '%'.
    //
    // While most things explicitly labeled as being a percentage scale from zero to one hundred (0 - 100)
    // others may go from zero to one (0.0 - 1.0).
    // To translate between the two formats, you only need to multiply or divide by one hundred (100).
    //
    // -->

    /**
     * Turns a list of string arguments (separated by buildArgs) into Argument
     * Objects for easy matching and ObjectTag creation throughout Denizen.
     *
     * @param args a list of string arguments
     * @return a list of Arguments
     */
    public static List<Argument> interpret(List<String> args) {
        List<Argument> arg_list = new ArrayList<>(args.size());
        for (String string : args) {
            arg_list.add(new Argument(string));
        }
        return arg_list;
    }

    /**
     * Builds an arguments array, recognizing items in quotes as a single item, but
     * otherwise splitting on a space.
     *
     * @param stringArgs the line of arguments that need split
     * @return an array of arguments
     */
    public static String[] buildArgs(String stringArgs) {
        if (stringArgs == null) {
            return null;
        }
        stringArgs = stringArgs.trim();
        stringArgs = stringArgs.replace('\r', ' ').replace('\n', ' ');
        ArrayList<String> matchList = new ArrayList<>(stringArgs.length() / 7);
        int start = 0;
        int len = stringArgs.length();
        char currentQuote = 0;
        for (int i = 0; i < len; i++) {
            char c = stringArgs.charAt(i);
            if (c == ' ' && currentQuote == 0) {
                if (i > start) {
                    matchList.add(stringArgs.substring(start, i));
                }
                start = i + 1;
            }
            else if (c == '"' || c == '\'') {
                if (currentQuote == 0) {
                    if (i - 1 < 0 || stringArgs.charAt(i - 1) == ' ') {
                        currentQuote = c;
                        start = i + 1;
                    }
                }
                else if (currentQuote == c) {
                    if (i + 1 >= len || stringArgs.charAt(i + 1) == ' ') {
                        currentQuote = 0;
                        if (i >= start) {
                            matchList.add(stringArgs.substring(start, i));
                        }
                        i++;
                        start = i + 1;
                    }
                }
            }
        }
        if (start < len) {
            matchList.add(stringArgs.substring(start));
        }

        if (Debug.showScriptBuilder) {
            Debug.log("Constructed args: " + Arrays.toString(matchList.toArray()));
        }

        return matchList.toArray(new String[0]);
    }

    public static String debugObj(String prefix, Object value) {
        return "<G>" + prefix + "='<Y>" + (value != null ? (value instanceof ObjectTag ? ((ObjectTag) value).debuggable() : value.toString()) : "null") + "<G>'  ";
    }

    public static <T extends ObjectTag> String debugList(String prefix, Collection<T> objects) {
        if (objects == null) {
            return debugObj(prefix, null);
        }
        StringBuilder sb = new StringBuilder();
        for (ObjectTag obj : objects) {
            sb.append(obj.debuggable()).append("<G>, ");
        }
        if (sb.length() == 0) {
            return debugObj(prefix, sb);
        }
        else {
            return debugObj(prefix, "[" + sb.substring(0, sb.length() - "<G>, ".length()) + "<Y>]");
        }
    }

    public static String debugUniqueObj(String prefix, String id, Object value) {
        return "<G>" + prefix + "='<A>" + id + "<Y>(" + (value != null ? value.toString() : "null") + ")<G>'  ";
    }

    private static String DIGITS = "0123456789", PREFIXES = "+-", DOUBLE_CHARS = ".eE";
    private static AsciiMatcher INTEGER_MATCHER = new AsciiMatcher(DIGITS + PREFIXES);
    private static AsciiMatcher DOUBLE_AFTERFIRST_MATCHER = new AsciiMatcher(DIGITS + DOUBLE_CHARS);

    public static boolean matchesDouble(String arg) {
        if (arg.length() == 0) {
            return false;
        }
        if (!INTEGER_MATCHER.isMatch(arg.charAt(0))) {
            return false;
        }
        for (int i = 1; i < arg.length(); i++) {
            if (!DOUBLE_AFTERFIRST_MATCHER.isMatch(arg.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesInteger(String arg) {
        return matchesDouble(arg) && INTEGER_MATCHER.isOnlyMatches(arg);
    }
}
