/*
 * Copyright (c)2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.unscramble.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.unscramble.R
import com.example.android.unscramble.ui.theme.UnscrambleTheme


@Composable
fun GameScreen(modifier: Modifier = Modifier, gameViewModel: GameViewModel = viewModel()) {
    val gameUiState = gameViewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        GameStatus(score = gameUiState.value.score, stage = gameUiState.value.stage + 1)
        GameLayout(
            scrambledWord = gameUiState.value.currentScrambleWord,
            errorText = gameUiState.value.errorText, inputValue = gameViewModel.inputValue,
            onDoneKeyboard = { gameViewModel.nextAndCheck() }) { gameViewModel.updateInputValue(it) }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            OutlinedButton(
                onClick = { gameViewModel.skipWord() },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.skip))
            }
            Button(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 8.dp),
                onClick = { gameViewModel.nextAndCheck() }
            ) {
                Text(stringResource(R.string.submit))
            }
        }
    }

    if (gameUiState.value.isGameOver)
        FinalScoreDialog(
            score = gameUiState.value.score,
            onDismissDialog = { gameViewModel.dismissDialog() }) {
            gameViewModel.resetGame()
        }
}

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentScope<Int>.numberTransitionSpec(): ContentTransform =
    if (targetState < initialState) {
        slideInVertically { height -> -height } + fadeIn() with slideOutVertically { height -> height } + fadeOut()
    } else {
        slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
    }.using(SizeTransform(clip = false))

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameStatus(modifier: Modifier = Modifier, stage: Int = 1, score: Int = 0) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = { numberTransitionSpec() }
            ) {
                Text(
                    text = it.toString(),
                    fontSize = 18.sp,
                )
            }
            Text(
                text = stringResource(R.string.word_count),
                fontSize = 18.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.score),
                fontSize = 18.sp,
            )
            AnimatedContent(
                targetState = score,
                transitionSpec = { numberTransitionSpec() }
            ) {
                Text(
                    text = it.toString(),
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
fun GameLayout(
    scrambledWord: String, inputValue: String, modifier: Modifier = Modifier,
    errorText: String = "", onDoneKeyboard: () -> Unit, onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),

        ) {
        Text(
            text = scrambledWord,
            fontSize = 45.sp,
            modifier = modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.instructions),
            fontSize = 17.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        OutlinedTextField(
            value = inputValue,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { onValueChange(it) },
            label = { Text(errorText.ifEmpty { stringResource(R.string.enter_your_word) }) },
            isError = errorText.isNotEmpty(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDoneKeyboard() }
            ),
        )
    }
}

/*
 * Creates and shows an AlertDialog with final score.
 */
@Composable
private fun FinalScoreDialog(
    score: Int,
    modifier: Modifier = Modifier,
    onDismissDialog: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val activity = (LocalContext.current as Activity)

    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onCloseRequest.

            // Fill it empty if you want make dialog un-cancellable
            // if not uncomment code below
//            onDismissDialog()
        },
        title = { Text(stringResource(R.string.congratulations)) },
        text = { Text(stringResource(R.string.you_scored, score)) },
        modifier = modifier,
        dismissButton = {
            TextButton(
                onClick = {
                    activity.finish()
                }
            ) {
                Text(text = stringResource(R.string.exit))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onPlayAgain
            ) {
                Text(text = stringResource(R.string.play_again))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UnscrambleTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GameScreen()
        }
    }
}