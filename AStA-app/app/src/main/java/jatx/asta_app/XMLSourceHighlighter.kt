package jatx.asta_app

import android.util.Log

import java.util.ArrayList

/**
 * Created by jatx on 21.09.17.
 */

fun highlightXML(src: String): String {
    XMLSourceHighlighter.insideComment = false

    val htmlBuilder = StringBuilder()

    val lines = src.split("\\r?\\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

    for (i in lines.indices) {
        val line = lines[i]

        val wrappedLine = XMLSourceHighlighter.wrapLine(line, i == lines.size - 1)

        Log.i("line " + i, wrappedLine)
        htmlBuilder.append(wrappedLine)
    }

    return htmlBuilder.toString()
}

object XMLSourceHighlighter {
    val SYNTAX_CHARACTERS_LIST = listOf(
            "{", "}", "(", ")", "[", "]", ".", ";", ",", "=", "<", ">",
            "&", "|", "!", "+", "-", "*", "%", "/", ",", ":", "?", ":"
    )

    var insideComment: Boolean = false
    var importedClassNames: MutableList<String>? = null

    private fun analizeImports(src: String) {
        var src = src
        importedClassNames = ArrayList()

        src = src.replace("\t", " ")

        val lines = src.split("\\r?\\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        for (_line in lines) {
            if (_line.startsWith("import") && _line.endsWith(";")) {
                var line = _line
                line = line.replace("import", "")
                line = line.replace(";", "")
                line = line.replace(" ", "")

                val words = line.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                for (word in words) {
                    if (word.length > 0 && word.get(0) >= 'A' && word.get(0) <= 'Z') {
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

    private fun isSyntaxCharacter(word: String): Boolean {
        return SYNTAX_CHARACTERS_LIST.contains(word)
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

    private fun isComment(word: String): Boolean {
        return word.startsWith("<!--") || word.endsWith("-->")
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

        if (isNumberLiteral(word)) {
            return wrapWordWithColor(word, "#CCCC77")
        }

        if (isSyntaxCharacter(word)) {
            return wrapWordWithColor(word, "#7777FF")
        }

        return wrapWordWithColor(word, "#FFFFFF")
    }

    private fun wrapWordWithColor(word: String, color: String): String {
        var word = word
        word = word.replace("<", "&lt;")
        word = word.replace(">", "&gt;")
        return "<font color=\"$color\">$word</font>"
    }

    fun wrapLine(line: String, isLast: Boolean): String {
        var line = line
        line = line.replace("\t", "    ")

        val lineBuilder = StringBuilder()

        var insideStringConst = false

        var wholeLineIsComment = insideComment

        val words = ArrayList<String>()
        var wordBuilder = StringBuilder()
        var k = 0
        while (k < line.length) {
            val c = line[k]
            if (c == '<' && k < line.length - 3 && line[k + 1] == '!' && line[k+2] == '-' && line[k+3] == '-'
                    && !insideComment && !insideStringConst) {
                words.add(wordBuilder.toString())
                wordBuilder = StringBuilder()
                wordBuilder.append("<!--")
                k+=3
                insideComment = true
            } else if (c == '-' && k < line.length - 2 && line[k + 1] == '-' && line[k+2] == '>') {
                insideComment = false
                wholeLineIsComment = false
                k+=2
                wordBuilder.append("-->")
                words.add(wordBuilder.toString())
                wordBuilder = StringBuilder()
            } else if (c == ' ' && !insideStringConst && !insideComment) {
                words.add(wordBuilder.toString())
                wordBuilder = StringBuilder()
                words.add(" ")
            } else if (c == '"' && (k == 0 || line[k - 1] != '\\') && !insideComment) {
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
            } else if (!insideStringConst && SYNTAX_CHARACTERS_LIST.contains(c.toString()) && !insideComment) {
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
