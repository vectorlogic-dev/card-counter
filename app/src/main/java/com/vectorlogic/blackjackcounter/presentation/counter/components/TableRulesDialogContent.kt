package com.vectorlogic.blackjackcounter.presentation.counter.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorlogic.blackjackcounter.R
import com.vectorlogic.blackjackcounter.domain.model.BlackjackPayoutRule
import com.vectorlogic.blackjackcounter.domain.model.DealerPeekRule
import com.vectorlogic.blackjackcounter.domain.model.DealerSoft17Rule
import com.vectorlogic.blackjackcounter.domain.model.DoubleDownRule
import com.vectorlogic.blackjackcounter.domain.model.SurrenderRule
import com.vectorlogic.blackjackcounter.domain.model.TableRules

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
            title = stringResource(R.string.rules_dealer_soft_17),
            selectedValue = rules.dealerSoft17Rule,
            options = listOf(
                DealerSoft17Rule.STAND to stringResource(R.string.rules_s17_option),
                DealerSoft17Rule.HIT to stringResource(R.string.rules_h17_option)
            ),
            onSelected = onDealerSoft17RuleChanged
        )

        RulesRadioCard(
            title = stringResource(R.string.rules_dealer_peek),
            selectedValue = rules.dealerPeekRule,
            options = listOf(
                DealerPeekRule.ACE_AND_TEN to stringResource(R.string.rules_peek_ace_and_ten),
                DealerPeekRule.ACE_ONLY to stringResource(R.string.rules_peek_ace_only),
                DealerPeekRule.NO_PEEK to stringResource(R.string.rules_no_peek_option)
            ),
            onSelected = onDealerPeekRuleChanged
        )

        RulesRadioCard(
            title = stringResource(R.string.rules_blackjack_payout),
            selectedValue = rules.blackjackPayoutRule,
            options = listOf(
                BlackjackPayoutRule.THREE_TO_TWO to "3:2",
                BlackjackPayoutRule.SIX_TO_FIVE to "6:5"
            ),
            onSelected = onBlackjackPayoutRuleChanged
        )

        RulesRadioCard(
            title = stringResource(R.string.rules_double_down),
            selectedValue = rules.doubleDownRule,
            options = listOf(
                DoubleDownRule.ANY_TWO_CARDS to stringResource(R.string.rules_double_any_two),
                DoubleDownRule.NINE_TO_ELEVEN to stringResource(R.string.rules_double_nine_to_eleven),
                DoubleDownRule.TEN_TO_ELEVEN to stringResource(R.string.rules_double_ten_to_eleven)
            ),
            onSelected = onDoubleDownRuleChanged
        )

        RulesRadioCard(
            title = stringResource(R.string.action_surrender),
            selectedValue = rules.surrenderRule,
            options = listOf(
                SurrenderRule.NONE to stringResource(R.string.rules_none),
                SurrenderRule.LATE to stringResource(R.string.rules_late_surrender),
                SurrenderRule.EARLY to stringResource(R.string.rules_early_surrender)
            ),
            onSelected = onSurrenderRuleChanged
        )

        RulesSwitchCard(
            title = stringResource(R.string.rules_double_after_split),
            checked = rules.doubleAfterSplit,
            onCheckedChange = onDoubleAfterSplitChanged
        )

        RulesSwitchCard(
            title = stringResource(R.string.rules_resplit_aces),
            checked = rules.resplitAces,
            onCheckedChange = onResplitAcesChanged
        )

        RulesSwitchCard(
            title = stringResource(R.string.rules_hit_split_aces),
            checked = rules.hitSplitAces,
            onCheckedChange = onHitSplitAcesChanged
        )

        RulesSwitchCard(
            title = stringResource(R.string.rules_six_card_charlie),
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
                text = stringResource(R.string.rules_max_split_hands),
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
