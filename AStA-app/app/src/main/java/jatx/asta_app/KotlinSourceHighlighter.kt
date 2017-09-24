package jatx.asta_app

import android.util.Log

import java.util.ArrayList
import java.util.Arrays

/**
 * Created by jatx on 21.09.17.
 */

class KotlinSourceHighlighter {
    companion object {
        val JAVA_KEYWORDS = arrayOf(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double", "else",
                "enum", "extends", "final", "finally", "float", "for", "goto", "if",
                "implements", "import", "instanceof", "int", "interface", "long",
                "native", "new", "package", "private", "protected", "public", "return",
                "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null",
                "val", "var", "companion", "object", "fun", "in"
        )
        val JAVA_KEYWORDS_LIST = Arrays.asList(*JAVA_KEYWORDS)

        val SYNTAX_CHARACTERS = arrayOf(
                "{", "}", "(", ")", "[", "]", ".", ";", ",", "=", "<", ">",
                "&", "|", "!", "+", "-", "*", "%", "/", ",", ":", "?", ":"
        )
        val SYNTAX_CHARACTERS_LIST = Arrays.asList(*SYNTAX_CHARACTERS)

        val JAVA_LANG_CLASSES = arrayOf(
                "Appendable", "AutoCloseable", "CharSequense", "Cloneable", "Comparable", "Iterable",
                "Readable", "Runnable", "UncaughtExceptionHandler", "Boolean", "Byte", "Character",
                "Subset", "UnicodeBlock", "Class", "ClassLoader", "ClassValue", "Compiler", "Double",
                "Enum", "Float", "InheritableThreadLocal", "Integer", "Long", "Math", "Number",
                "Object", "Package", "Process", "ProcessBuilder", "Redirect", "Runtime",
                "RuntimePermission", "SecurityManager", "Short", "StackTraceElement",
                "StrictMath", "String", "StringBuffer", "StringBuilder", "System", "Thread",
                "ThreadGroup", "ThreadLocal", "Throwable", "Void", "UnicodeScript", "Type",
                "State", "ArithmeticException", "ArrayIndexOutOfBoundsException", "ArrayStoreException",
                "ClassCastException", "ClassNotFoundException", "CloneNotSupportedException",
                "EnumConstantNotPresentException", "Exception", "IllegalAccessException",
                "IllegalArgumentException", "IllegalMonitorStateException", "IllegalStateException",
                "IllegalThreadStateException", "IndexOutOfBoundsException", "InstantiationException",
                "InterruptedException", "NegativeArraySizeException", "NoSuchFieldException",
                "NoSushMethodException", "NullPointerException", "NumberFormatException",
                "ReflectiveOperationException", "RuntimeException", "SecurityException",
                "StringIndexOutOfBoundsException", "TypeNotPresentException", "UnsupportedOperationException",
                "AbstractMethodError", "AssertionError", "BootstrapMethodError", "ClassCircularityError",
                "ClassFormatError", "Error", "ExceptionInInitializerError", "IllegalAccessError",
                "IncompatibleClassChangeError", "InstantiationError", "InternalError", "LinkageError",
                "NoClassDefFoundError", "NoSuchFieldError", "NoSuchMethodError", "OutOfMemoryError",
                "StackOverflowError", "ThreadDeath", "UnknownError", "UnsatisfiedLinkError",
                "UnsupportedClassVersionError", "VerifyError", "VirtualMachineError"
        )
        val JAVA_LANG_CLASSES_LIST = Arrays.asList(*JAVA_LANG_CLASSES)

        private var insideMultilineComment: Boolean = false
        private var importedClassNames: MutableList<String>? = null

        fun highlightKotlin(src: String): String {
            insideMultilineComment = false
            analizeImports(src)

            val htmlBuilder = StringBuilder()

            val lines = src.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in lines.indices) {
                val line = lines[i]

                val wrappedLine = wrapLine(line, i == lines.size - 1)

                Log.i("line " + i, wrappedLine)
                htmlBuilder.append(wrappedLine)
            }

            return htmlBuilder.toString()
        }

        private fun analizeImports(src: String) {
            var src = src
            importedClassNames = ArrayList()

            src = src.replace("\t", " ")

            val lines = src.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (_line in lines) {
                if (_line.startsWith("import")) {
                    var line = _line
                    line = line.replace("import", "")
                    //line = line.replace(";", "")
                    line = line.replace(" ", "")

                    val words = line.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (word in words) {
                        if (word.length > 0 && word[0] >= 'A' && word[0] <= 'Z') {
                            importedClassNames!!.add(word)
                        }
                    }
                }
            }
        }

        private fun isStringLiteral(word: String): Boolean {
            return word.startsWith("\"") && word.endsWith("\"")
        }

        private fun isCharLiteral(word: String): Boolean {
            return word.startsWith("'") && word.endsWith("'")
        }

        private fun isNumberLiteral(word: String): Boolean {
            if (word.length == 0) return false

            var result = true

            var wasDot = false

            for (i in 0 until word.length) {
                val c = word[i]

                if (i == word.length - 1 && word.length > 1) {
                    result = result && (c >= '0' && c <= '9' || c == '.' || c == 'f' || c == 'l')
                } else if (word.length > 1) {
                    result = result && (c >= '0' && c <= '9' || c == '.')
                } else {
                    result = result && c >= '0' && c <= '9'
                }

                if (wasDot && c == '.') return false
                wasDot = wasDot || c == '.'
            }

            return result
        }

        private fun isKeyword(word: String): Boolean {
            return JAVA_KEYWORDS_LIST.contains(word)
        }

        private fun isSyntaxCharacter(word: String): Boolean {
            return SYNTAX_CHARACTERS_LIST.contains(word)
        }

        private fun isClassName(word: String): Boolean {
            return JAVA_LANG_CLASSES_LIST.contains(word) || importedClassNames!!.contains(word)
        }

        private fun isNotImportedClassName(word: String): Boolean {
            if (word.isEmpty()) return false

            var result = true

            result = result && word[0] >= 'A' && word[0] <= 'Z'

            for (i in 1 until word.length) {
                val c = word[i]
                result = result && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
            }

            return result
        }

        private fun isConstName(word: String): Boolean {
            if (word.isEmpty()) return false

            var result = true

            for (i in 0 until word.length) {
                val c = word[i]
                result = result && (c >= 'A' && c <= 'Z' || c == '_')
            }

            return result
        }

        private fun isAnnotation(word: String): Boolean {
            return word.startsWith("@")
        }

        private fun isComment(word: String): Boolean {
            return word.startsWith("//") || word.startsWith("/*") || word.endsWith("*/")
        }

        private fun wrapWord(word: String): String {
            if (word.isEmpty()) return word

            if (word == " ") {
                return "&nbsp;"
            }

            if (isComment(word)) {
                return wrapWordWithColor(word, "#999999")
            }

            if (isStringLiteral(word)) {
                return wrapWordWithColor(word, "#00FF00")
            }

            if (isCharLiteral(word)) {
                return wrapWordWithColor(word, "#CCFF77")
            }

            if (isNumberLiteral(word)) {
                return wrapWordWithColor(word, "#CCCC77")
            }

            if (isKeyword(word)) {
                return wrapWordWithColor(word, "#0000FF")
            }

            if (isSyntaxCharacter(word)) {
                return wrapWordWithColor(word, "#7777FF")
            }


            if (isClassName(word)) {
                return wrapWordWithColor(word, "#77FFFF")
            }

            if (isConstName(word)) {
                return wrapWordWithColor(word, "#FF77FF")
            }

            if (isNotImportedClassName(word)) {
                return wrapWordWithColor(word, "#77FFFF")
            }

            return if (isAnnotation(word)) {
                wrapWordWithColor(word, "#FF7700")
            } else wrapWordWithColor(word, "#FFFFFF")

        }

        private fun wrapWordWithColor(word: String, color: String): String {
            var word = word
            word = word.replace("<", "&lt;")
            word = word.replace(">", "&gt;")
            return "<font color=\"$color\">$word</font>"
        }

        private fun wrapLine(line: String, isLast: Boolean): String {
            var line = line
            line = line.replace("\t", "    ")

            val lineBuilder = StringBuilder()

            var insideStringConst = false
            var isCommentStarted = false

            var wholeLineIsComment = insideMultilineComment

            val words = ArrayList<String>()
            var wordBuilder = StringBuilder()
            var k = 0
            while (k < line.length) {
                val c = line[k]
                if (isCommentStarted) {
                    wordBuilder.append(c)
                } else if (c == '/' && k < line.length - 1 && line[k + 1] == '/' && !insideMultilineComment && !insideStringConst) {
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                    wordBuilder.append(c)
                    isCommentStarted = true
                } else if (c == '/' && k < line.length - 1 && line[k + 1] == '*' && !insideMultilineComment && !insideStringConst) {
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                    wordBuilder.append("/*")
                    k++
                    insideMultilineComment = true
                } else if (c == '*' && k < line.length - 1 && line[k + 1] == '/') {
                    insideMultilineComment = false
                    wholeLineIsComment = false
                    k++
                    wordBuilder.append("*/")
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                } else if (c == ' ' && !insideStringConst && !insideMultilineComment) {
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                    words.add(" ")
                } else if (c == '"' && (k == 0 || line[k - 1] != '\\') && !insideMultilineComment) {
                    if (!insideStringConst) {
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                        wordBuilder.append(c)
                    } else {
                        wordBuilder.append(c)
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                    }

                    insideStringConst = !insideStringConst
                } else if (!insideStringConst && SYNTAX_CHARACTERS_LIST.contains(c.toString()) && !insideMultilineComment) {
                    if (!wordBuilder.toString().isEmpty()) {
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                    }
                    words.add(c.toString())
                } else {
                    wordBuilder.append(c)
                    //Log.e("char", Integer.toString((int)c));
                }
                k++
            }
            words.add(wordBuilder.toString())

            if (wholeLineIsComment) {
                return wrapWordWithColor(line.replace(" ", "&nbsp;"), "#999999") + "<br>"
            }

            for (j in words.indices) {
                val word = words[j]

                //Log.e("word", word);

                lineBuilder.append(wrapWord(word))
            }

            if (!isLast) lineBuilder.append("<br>")

            return lineBuilder.toString()
        }
    }
}
