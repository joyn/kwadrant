package de.joyn.kwadrant.util

import org.gradle.internal.logging.text.StyledTextOutput

class Console(private val output: StyledTextOutput) {

    fun putNewLine() {
        output.println()
    }

    fun putHeader(text: String) {
        putStrLn(text, StyledTextOutput.Style.Header)
    }

    fun putInfo(text: String) {
        putStrLn(text, StyledTextOutput.Style.Info)
    }

    fun putNormal(text: String) {
        putStrLn(text.prependTab(), StyledTextOutput.Style.Normal)
    }

    fun putNormal(textList: List<String>) {
        textList.forEach {
            putNormal(it.prependTab())
        }
    }

    private fun putStrLn(text: String, style: StyledTextOutput.Style) = with (output) {
        withStyle(style).text(text)
        println()
    }

    private fun String.prependTab() = this // "\t$this"

}