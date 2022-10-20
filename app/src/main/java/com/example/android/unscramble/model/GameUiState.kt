package com.example.android.unscramble.model

data class GameUiState(
    val currentScrambleWord: String = "",
    val score: Int = 0,
    val stage: Int = 0,
    val errorText: String = "",
    val isGameOver: Boolean = false
)