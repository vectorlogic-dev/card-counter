package com.vectorlogic.blackjackcounter.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.presentation.asString
import com.vectorlogic.blackjackcounter.presentation.counter.StrategyPlay
import com.vectorlogic.blackjackcounter.presentation.counter.formatSignedCount

private val QuestionListSaver: Saver<List<QuizQuestion>, Any> = listSaver(
    save = { questions -> questions.flatMap { listOf(it.itemIndex.toDouble(), it.trueCount) } },
    restore = { flat -> flat.chunked(2) { (index, count) -> QuizQuestion(index.toInt(), count) } }
)

private val AnswerListSaver: Saver<List<StrategyPlay>, Any> = listSaver(
    save = { answers -> answers.map { it.ordinal } },
    restore = { ordinals -> ordinals.map { StrategyPlay.entries[it] } }
)

@Composable
fun DeviationQuizDialog(
    quiz: DeviationQuiz,
    onRoundFinished: (QuizRoundScore) -> Unit,
    onDismiss: () -> Unit
) {
    // Saveable so a rotation keeps the round, the answers and the feedback on screen.
    var questions by rememberSaveable(stateSaver = QuestionListSaver) { mutableStateOf(quiz.newRound()) }
    var answers by rememberSaveable(stateSaver = AnswerListSaver) { mutableStateOf(emptyList()) }
    // The question on screen; it equals answers.size until answered, then shows feedback.
    var position by rememberSaveable { mutableIntStateOf(0) }

    val finished = questions.isNotEmpty() && position >= questions.size
    val answered = !finished && answers.size > position
    val roundInProgress = !finished && answers.isNotEmpty()

    AlertDialog(
        // A stray tap outside shouldn't throw away a round in progress; Close still ends it.
        onDismissRequest = { if (!roundInProgress) onDismiss() },
        title = { QuizTitle(quiz) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    questions.isEmpty() -> Text(
                        stringResource(R.string.quiz_no_hands),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    finished -> QuizResults(quiz = quiz, questions = questions, answers = answers)
                    else -> QuestionContent(
                        quiz = quiz,
                        question = questions[position],
                        number = position + 1,
                        total = questions.size,
                        correctSoFar = quiz.score(questions, answers).correctCount,
                        answer = answers.getOrNull(position),
                        onAnswer = { play -> if (!answered) answers = answers + play }
                    )
                }
            }
        },
        confirmButton = {
            when {
                finished -> Button(onClick = {
                    questions = quiz.newRound()
                    answers = emptyList()
                    position = 0
                }) { Text(stringResource(R.string.drill_run_again)) }
                answered -> Button(onClick = {
                    // Recorded here, once, rather than when the results show, which a rotation repeats.
                    if (position == questions.lastIndex) onRoundFinished(quiz.score(questions, answers))
                    position += 1
                }) {
                    Text(
                        stringResource(
                            if (position == questions.lastIndex) R.string.quiz_see_results else R.string.quiz_next
                        )
                    )
                }
                else -> {}
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
private fun QuizTitle(quiz: DeviationQuiz) {
    Column {
        Text(stringResource(R.string.quiz_title))
        Text(
            text = if (quiz.usesSelectedSystem) {
                quiz.systemName
            } else {
                stringResource(R.string.quiz_running_count_system, quiz.systemName)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuestionContent(
    quiz: DeviationQuiz,
    question: QuizQuestion,
    number: Int,
    total: Int,
    correctSoFar: Int,
    answer: StrategyPlay?,
    onAnswer: (StrategyPlay) -> Unit
) {
    val item = quiz.item(question)
    val correctPlay = quiz.correctPlay(question)
    val trueCount = formatSignedCount(question.trueCount)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            stringResource(R.string.quiz_progress, number, total),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            pluralStringResource(R.plurals.quiz_score_so_far, correctSoFar, correctSoFar),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HandFact(label = stringResource(R.string.quiz_your_hand), value = item.handLabel, modifier = Modifier.weight(1f))
            HandFact(label = stringResource(R.string.quiz_dealer_shows), value = item.dealerLabel, modifier = Modifier.weight(1f))
            HandFact(label = stringResource(R.string.quiz_true_count), value = trueCount, modifier = Modifier.weight(1f))
        }
    }

    Text(
        stringResource(R.string.quiz_question),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        quiz.choices(question).forEach { play ->
            PlayButton(
                play = play,
                answer = answer,
                correctPlay = correctPlay,
                onClick = { onAnswer(play) }
            )
        }
    }

    if (answer != null) {
        val isCorrect = answer == correctPlay
        Text(
            text = if (isCorrect) {
                stringResource(R.string.quiz_correct)
            } else {
                stringResource(R.string.quiz_wrong, stringResource(correctPlay.labelRes))
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { heading() }
        )
        val index = stringResource(
            R.string.quiz_index_rule,
            item.row.playLabel.asString(),
            item.row.indexLabel.asString()
        )
        Text(
            text = stringResource(
                if (quiz.indexApplies(question)) R.string.quiz_index_applies else R.string.quiz_index_does_not_apply,
                trueCount,
                index
            ),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HandFact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/** Before answering every play is open; afterwards the right one is filled and a wrong pick is red. */
@Composable
private fun PlayButton(
    play: StrategyPlay,
    answer: StrategyPlay?,
    correctPlay: StrategyPlay,
    onClick: () -> Unit
) {
    val label = stringResource(play.labelRes)
    when {
        answer == null -> OutlinedButton(onClick = onClick) { Text(label) }
        play == correctPlay -> Button(onClick = {}) { Text(label) }
        play == answer -> Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) { Text(label) }
        else -> OutlinedButton(onClick = {}, enabled = false) { Text(label) }
    }
}

@Composable
private fun QuizResults(
    quiz: DeviationQuiz,
    questions: List<QuizQuestion>,
    answers: List<StrategyPlay>
) {
    val misses = questions.zip(answers).filter { (question, answer) -> quiz.correctPlay(question) != answer }
    Text(
        pluralStringResource(
            R.plurals.quiz_result_score,
            questions.size - misses.size,
            questions.size - misses.size,
            questions.size
        ),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = if (misses.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
    if (misses.isEmpty()) {
        Text(stringResource(R.string.quiz_result_perfect), style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text(
        stringResource(R.string.quiz_result_review),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
    misses.forEach { (question, answer) ->
        val item = quiz.item(question)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(
                    R.string.quiz_result_miss,
                    item.row.playLabel.asString(),
                    formatSignedCount(question.trueCount),
                    stringResource(quiz.correctPlay(question).labelRes),
                    stringResource(answer.labelRes)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(
                    R.string.quiz_index_rule,
                    item.row.playLabel.asString(),
                    item.row.indexLabel.asString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
