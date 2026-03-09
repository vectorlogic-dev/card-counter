package com.yourname.blackjackcounter.presentation.counter.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourname.blackjackcounter.domain.model.BlackjackPayoutRule
import com.yourname.blackjackcounter.domain.model.DealerPeekRule
import com.yourname.blackjackcounter.domain.model.DealerSoft17Rule
import com.yourname.blackjackcounter.domain.model.DoubleDownRule
import com.yourname.blackjackcounter.domain.model.SurrenderRule
import com.yourname.blackjackcounter.domain.model.TableRules

@Composable
fun TableRulesDialogContent(
    rules: TableRules,
    onDealerSoft17RuleChanged: (DealerSoft17Rule) -> Unit,
    onDealerPeekRuleChanged: (DealerPeekRule) -> Unit,
    onBlackjackPayoutRuleChanged: (BlackjackPayoutRule) -> Unit,
    onDoubleDownRuleChanged: (DoubleDownRule) -> Unit,
    onDoubleAfterSplitChanged: (Boolean) -> Unit,
    onSurrenderRuleChanged: (SurrenderRule) -> Unit,
    onResplitAcesChanged: (Boolean) -> Unit,
    onHitSplitAcesChanged: (Boolean) -> Unit,
    onSixCardCharlieAutoWinChanged: (Boolean) -> Unit,
    onMaxSplitHandsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RulesRadioCard(
            title = "Dealer Soft 17",
            selectedValue = rules.dealerSoft17Rule,
            options = listOf(
                DealerSoft17Rule.STAND to "S17 (Stand on soft 17)",
                DealerSoft17Rule.HIT to "H17 (Hit soft 17)"
            ),
            onSelected = onDealerSoft17RuleChanged
        )

        RulesRadioCard(
            title = "Dealer Peek Rule",
            selectedValue = rules.dealerPeekRule,
            options = listOf(
                DealerPeekRule.ACE_AND_TEN to "Peek on Ace and 10",
                DealerPeekRule.ACE_ONLY to "Peek on Ace only (no peek on 10)",
                DealerPeekRule.NO_PEEK to "No peek (European style)"
            ),
            onSelected = onDealerPeekRuleChanged
        )

        RulesRadioCard(
            title = "Blackjack Payout",
            selectedValue = rules.blackjackPayoutRule,
            options = listOf(
                BlackjackPayoutRule.THREE_TO_TWO to "3:2",
                BlackjackPayoutRule.SIX_TO_FIVE to "6:5"
            ),
            onSelected = onBlackjackPayoutRuleChanged
        )

        RulesRadioCard(
            title = "Double Down Rule",
            selectedValue = rules.doubleDownRule,
            options = listOf(
                DoubleDownRule.ANY_TWO_CARDS to "Any 2 cards",
                DoubleDownRule.NINE_TO_ELEVEN to "9-11 only",
                DoubleDownRule.TEN_TO_ELEVEN to "10-11 only"
            ),
            onSelected = onDoubleDownRuleChanged
        )

        RulesRadioCard(
            title = "Surrender",
            selectedValue = rules.surrenderRule,
            options = listOf(
                SurrenderRule.NONE to "None",
                SurrenderRule.LATE to "Late surrender",
                SurrenderRule.EARLY to "Early surrender"
            ),
            onSelected = onSurrenderRuleChanged
        )

        RulesSwitchCard(
            title = "Double After Split (DAS)",
            checked = rules.doubleAfterSplit,
            onCheckedChange = onDoubleAfterSplitChanged
        )

        RulesSwitchCard(
            title = "Resplit Aces",
            checked = rules.resplitAces,
            onCheckedChange = onResplitAcesChanged
        )

        RulesSwitchCard(
            title = "Hit Split Aces",
            checked = rules.hitSplitAces,
            onCheckedChange = onHitSplitAcesChanged
        )

        RulesSwitchCard(
            title = "6-Card Charlie Auto Win",
            checked = rules.sixCardCharlieAutoWin,
            onCheckedChange = onSixCardCharlieAutoWinChanged
        )

        MaxSplitHandsCard(
            selectedValue = rules.maxSplitHands,
            onSelected = onMaxSplitHandsChanged
        )
    }
}

@Composable
private fun <T> RulesRadioCard(
    title: String,
    selectedValue: T,
    options: List<Pair<T, String>>,
    onSelected: (T) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            options.forEach { (value, label) ->
                val selected = value == selectedValue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onSelected(value) }
                        )
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RulesSwitchCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun MaxSplitHandsCard(
    selectedValue: Int,
    onSelected: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Max Split Hands",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (2..6).forEach { value ->
                    val selected = value == selectedValue
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onSelected(value) }
                            )
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
