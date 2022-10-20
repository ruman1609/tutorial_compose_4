package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.allWords
import com.example.android.unscramble.model.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    var inputValue by mutableStateOf("")
        private set

    fun updateInputValue(inputValue: String) {
        this.inputValue = inputValue
    }

    private var currentWord = ""
    private val usedWords = mutableListOf<String>()

    private fun randomizeWord(word: String): String {
        val tempWords = word.toCharArray()
        while (String(tempWords) == word) tempWords.shuffle()
        return String(tempWords)
    }

    private fun pickRandomWord(): String {
        currentWord = allWords.random()
        return if (currentWord in usedWords) pickRandomWord()
        else {
            usedWords.add(currentWord)
            randomizeWord(currentWord)
        }
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(pickRandomWord())
    }

    fun nextAndCheck() {
        _uiState.update {
            it.copy(score = if (inputValue == currentWord && it.score < MAX_NO_OF_WORDS * SCORE_INCREASE) it.score + SCORE_INCREASE else it.score)
        }
        if (inputValue.isNotEmpty()) {
            skipWord()
        } else if (inputValue.isEmpty()) _uiState.update { it.copy(errorText = "Must not empty!") }
    }

    fun skipWord() {
        inputValue = ""
        if (uiState.value.stage + 1 < MAX_NO_OF_WORDS) {
            _uiState.update {
                it.copy(
                    currentScrambleWord = pickRandomWord(), stage = it.stage + 1, errorText = ""
                )
            }
        } else _uiState.update { it.copy(isGameOver = true) }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(isGameOver = false)
        }
    }

    init {
        resetGame()
    }
}