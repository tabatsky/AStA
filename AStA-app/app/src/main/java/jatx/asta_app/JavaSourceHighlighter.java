package jatx.asta_app;

import android.content.Intent;
import android.util.Log;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jatx on 21.09.17.
 */

public class JavaSourceHighlighter {
    public static final String[] JAVA_KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
            "true", "false", "null"
    };
    public static final List<String> JAVA_KEYWORDS_LIST = Arrays.asList(JAVA_KEYWORDS);

    public static final String[] SYNTAX_CHARACTERS = new String[] {
            "{", "}", "(", ")", "[", "]", ".", ";", ",", "=", "<", ">", "&", "|", "!", "+", "-", "*", "/"
    };
    public static final List<String> SYNTAX_CHARACTERS_LIST = Arrays.asList(SYNTAX_CHARACTERS);

    public static String highlightJava(String src) {
        StringBuilder htmlBuilder = new StringBuilder();

        String[] lines = src.split("\\r?\\n");

        for (int i=0; i<lines.length; i++) {
            String line = lines[i];

            String wrappedLine = wrapLine(line, (i == lines.length - 1));

            Log.i("line " + i, wrappedLine);
            htmlBuilder.append(wrappedLine);
        }

        return htmlBuilder.toString();
    }

    private static boolean isStringLiteral(String word) {
        return word.startsWith("\"") && word.endsWith("\"");
    }

    private static boolean isCharLiteral(String word) {
        return word.startsWith("'") && word.endsWith("'");
    }

    private static boolean isNumberLiteral(String word) {
        if (word.length() == 0) return false;

        boolean result = true;

        boolean wasDot = false;

        for (int i=0; i<word.length(); i++) {
            char c = word.charAt(i);

            if (i==word.length()-1 && word.length()>1) {
                result = result && ((c >= '0' && c <='9') || c=='.' || c=='f' || c=='l');
            } else if (word.length() > 1){
                result = result && ((c >= '0' && c <='9') || c=='.');
            } else {
                result = result && (c >= '0' && c <= '9');
            }

            if (wasDot && c=='.') return false;
            wasDot = wasDot || (c == '.');
        }

        return result;
    }

    private static boolean isKeyword(String word) {
        return JAVA_KEYWORDS_LIST.contains(word);
    }

    private static boolean isSyntaxCharacter(String word) {
        return SYNTAX_CHARACTERS_LIST.contains(word);
    }

    private static boolean isClassName(String word) {
        if (word.isEmpty()) return false;

        boolean result = true;

        result = result && (word.charAt(0) >= 'A') && (word.charAt(0) <= 'Z');

        for (int i=1; i<word.length(); i++) {
            char c = word.charAt(i);
            result = result &&
                    ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'));
        }

        return result;
    }

    private static boolean isConstName(String word) {
        if (word.isEmpty()) return false;

        boolean result = true;

        for (int i=0; i<word.length(); i++) {
            char c = word.charAt(i);
            result = result &&
                    ((c >= 'A' && c <= 'Z') || (c == '_'));
        }

        return result;
    }

    private static boolean isAnnotation(String word) {
        return word.startsWith("@");
    }

    private static boolean isComment(String word) {
        return word.startsWith("//") || word.startsWith("/*") || word.endsWith("*/");
    }

    private static String wrapWord(String word) {
        if (word.isEmpty()) return word;

        if (word.equals(" ")) {
            return "&nbsp;";
        }

        if (isComment(word)) {
            return wrapWordWithColor(word, "#999999");
        }

        if (isStringLiteral(word)) {
            return wrapWordWithColor(word, "#00FF00");
        }

        if (isCharLiteral(word)) {
            return wrapWordWithColor(word, "#CCFF77");
        }

        if (isNumberLiteral(word)) {
            return wrapWordWithColor(word, "#CCCC77");
        }

        if (isKeyword(word)) {
            return wrapWordWithColor(word, "#0000FF");
        }

        if (isSyntaxCharacter(word)) {
            return wrapWordWithColor(word, "#7777FF");
        }

        if (isClassName(word)) {
            return wrapWordWithColor(word, "#77FFFF");
        }

        if (isConstName(word)) {
            return wrapWordWithColor(word, "#FF77FF");
        }

        if (isAnnotation(word)) {
            return wrapWordWithColor(word, "#FF7700");
        }

        return wrapWordWithColor(word, "#FFFFFF");
    }

    private static String wrapWordWithColor(String word, String color) {
        word = word.replace("<", "&lt;");
        word = word.replace(">", "&gt;");
        return "<font color=\"" + color + "\">" + word + "</font>";
    }

    private static boolean insideMultilineComment = false;
    private static String wrapLine(String line, boolean isLast) {
        line = line.replace("\t", "    ");

        StringBuilder lineBuilder = new StringBuilder();

        boolean insideStringConst = false;
        boolean isCommentStarted = false;

        boolean wholeLineIsComment = insideMultilineComment;

        List<String> words = new ArrayList<>();
        StringBuilder wordBuilder = new StringBuilder();
        for (int k=0; k<line.length(); k++) {
            char c = line.charAt(k);
            if (isCommentStarted) {
                wordBuilder.append(c);
            } else if (c=='/' && k < line.length() - 1 && line.charAt(k+1) == '/' && !insideMultilineComment && !insideStringConst) {
                words.add(wordBuilder.toString());
                wordBuilder = new StringBuilder();
                wordBuilder.append(c);
                isCommentStarted = true;
            } else if (c=='/' && k < line.length() - 1 && line.charAt(k+1) == '*' && !insideMultilineComment) {
                words.add(wordBuilder.toString());
                wordBuilder = new StringBuilder();
                wordBuilder.append("/*");
                k++;
                insideMultilineComment = true;
            } else if (c=='*' && k < line.length() - 1 && line.charAt(k+1) == '/') {
                insideMultilineComment = false;
                wholeLineIsComment = false;
                k++;
                wordBuilder.append("*/");
                words.add(wordBuilder.toString());
                wordBuilder = new StringBuilder();
            } else if (c==' ' && !insideStringConst && !insideMultilineComment) {
                words.add(wordBuilder.toString());
                wordBuilder = new StringBuilder();
                words.add(" ");
            } else if (c=='"' && (k==0 || line.charAt(k-1)!='\\') && !insideMultilineComment) {
                if (!insideStringConst) {
                    words.add(wordBuilder.toString());
                    wordBuilder = new StringBuilder();
                    wordBuilder.append(c);
                } else {
                    wordBuilder.append(c);
                    words.add(wordBuilder.toString());
                    wordBuilder = new StringBuilder();
                }

                insideStringConst = !insideStringConst;
            } else if (!insideStringConst && SYNTAX_CHARACTERS_LIST.contains(String.valueOf(c)) && !insideMultilineComment) {
                if (!wordBuilder.toString().isEmpty()) {
                    words.add(wordBuilder.toString());
                    wordBuilder = new StringBuilder();
                }
                words.add(String.valueOf(c));
            } else {
                wordBuilder.append(c);
                //Log.e("char", Integer.toString((int)c));
            }
        }
        words.add(wordBuilder.toString());

        if (wholeLineIsComment) {
            return wrapWordWithColor(line.replace(" ", "&nbsp;"), "#999999") + "<br>";
        }

        for (int j=0; j<words.size(); j++) {
            String word = words.get(j);

            //Log.e("word", word);

            lineBuilder.append(wrapWord(word));
        }

        if (!isLast) lineBuilder.append("<br>");

        return lineBuilder.toString();
    }
}
