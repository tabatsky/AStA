package jatx.asta_app

import android.util.Log
import java.util.ArrayList

/**
 * Created by jatx on 23.09.17.
 */
class GradleSourceHighlighter {
    companion object {
        val SYNTAX_CHARACTERS_SET = setOf(
                "{", "}", "(", ")", "[", "]", ".", ";", ",", "=", "<", ">",
                "&", "|", "!", "+", "-", "*", "%", "/", ",", ":", "?", ":"
        )

        private var insideMultilineComment = false

        fun highlghtGradle(src: String): String {
            insideMultilineComment = false;

            val htmlBuilder = StringBuilder()

            val lines = src.split("\\r?\\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

            for (i in lines.indices) {
                val line = lines[i]

                val wrappedLine = wrapLine(line, i == lines.size - 1)

                Log.i("line $i", wrappedLine)
                htmlBuilder.append(wrappedLine)
            }

            return htmlBuilder.toString()
        }

        private fun wrapLine(line: String, isLast: Boolean): String {
            var line = line
            line = line.replace("\t", "    ")

            val lineBuilder = StringBuilder()

            var insideStringConst = false
            var insideStringConst2 = false
            var isCommentStarted = false

            var wholeLineIsComment = insideMultilineComment

            val words = ArrayList<String>()
            var wordBuilder = StringBuilder()
            var k = 0
            while (k < line.length) {
                val c = line[k]
                if (isCommentStarted) {
                    wordBuilder.append(c)
                } else if (c == '/' && k < line.length - 1 && line[k + 1] == '/'
                        && !insideMultilineComment && !insideStringConst && !insideStringConst2) {
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                    wordBuilder.append(c)
                    isCommentStarted = true
                } else if (c == '/' && k < line.length - 1 && line[k + 1] == '*'
                        && !insideMultilineComment && !insideStringConst && !insideStringConst2) {
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
                } else if (c == ' ' && !insideStringConst && !insideStringConst2 && !insideMultilineComment) {
                    words.add(wordBuilder.toString())
                    wordBuilder = StringBuilder()
                    words.add(" ")
                } else if (c == '"' && (k == 0 || line[k - 1] != '\\') && !insideMultilineComment && !insideStringConst2) {
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
                } else if (c == '\'' && (k==0 || line[k - 1] != '\\') && !insideMultilineComment && !insideStringConst) {
                    if (!insideStringConst2) {
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                        wordBuilder.append(c)
                    } else {
                        wordBuilder.append(c)
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                    }

                    insideStringConst2 = !insideStringConst2
                } else if (!insideStringConst && !insideStringConst2
                        && SYNTAX_CHARACTERS_SET.contains(c.toString()) && !insideMultilineComment) {
                    if (!wordBuilder.toString().isEmpty()) {
                        words.add(wordBuilder.toString())
                        wordBuilder = StringBuilder()
                    }
                    words.add(c.toString())
                } else {
                    wordBuilder.append(c)
                }
                k++
            }
            words.add(wordBuilder.toString())

            if (wholeLineIsComment) {
                return wrapWordWithColor(line.replace(" ", "&nbsp;"), "#999999") + "<br>"
            }

            for (j in words.indices) {
                val word = words[j]

                Log.e("word", word);

                lineBuilder.append(wrapWord(word))
            }

            if (!isLast) lineBuilder.append("<br>")

            return lineBuilder.toString()
        }

        private fun wrapWordWithColor(word: String, color: String): String {
            var word = word
            word = word.replace("<", "&lt;")
            word = word.replace(">", "&gt;")
            return "<font color=\"$color\">$word</font>"
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

        private fun isSyntaxCharacter(word: String): Boolean {
            return SYNTAX_CHARACTERS_SET.contains(word)
        }

        private fun isComment(word: String): Boolean {
            return word.startsWith("//") || word.startsWith("/*") || word.endsWith("*/")
        }

        private fun isStringLiteral(word: String): Boolean {
            return (
                    word.startsWith("\"") && word.endsWith("\"")
                            || word.startsWith("'") && word.endsWith("'")
                    )
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
    }
}