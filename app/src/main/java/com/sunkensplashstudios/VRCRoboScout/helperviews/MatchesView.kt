package com.sunkensplashstudios.VRCRoboScout.helperviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunkensplashstudios.VRCRoboScout.AllianceColor
import com.sunkensplashstudios.VRCRoboScout.Match
import com.sunkensplashstudios.VRCRoboScout.Team
import com.sunkensplashstudios.VRCRoboScout.ui.theme.allianceBlue
import com.sunkensplashstudios.VRCRoboScout.ui.theme.allianceRed

@Composable
fun MatchesView(matchList: List<Match>, team: Team? = null) {

    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

    fun determineColor(match: Match, team: Team?, defaultColor: Color): Color {
        if (match.completed()) {
            return if (team != null) {
                if (match.winningAlliance() == null) {
                    Color.Yellow
                } else if (match.winningAlliance() == AllianceColor.RED && match.redAlliance.members.find { member -> member.team.id == team.id } != null) {
                    Color.Green
                } else if (match.winningAlliance() == AllianceColor.BLUE && match.blueAlliance.members.find { member -> member.team.id == team.id } != null) {
                    Color.Green
                } else {
                    Color.Red
                }
            } else {
                defaultColor
            }
        }
        else {
            return defaultColor
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.padding(10.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                disabledContainerColor = Color.Unspecified.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = Color.Unspecified
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 0.dp)
            ) {
                (matchList).forEach { match ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.width(65.dp),
                            verticalArrangement = Arrangement.spacedBy((-3).dp)
                        ) {
                            Text(
                                text = match.shortName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = determineColor(match, team, MaterialTheme.colorScheme.onSurface)
                            )
                            Text(
                                text = match.startedDate?.let { timeFormat.format(it) }
                                    ?: match.scheduledDate?.let { timeFormat.format(it) }
                                    ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(60.dp),
                                verticalArrangement = Arrangement.spacedBy((-5).dp)
                            ) {
                                match.redAlliance.members.forEach{ member ->
                                    Text(
                                        text = member.team.name,
                                        fontSize = 15.sp,
                                        color = allianceRed,
                                        textDecoration = if (team != null && member.team.id == team.id) TextDecoration.Underline else TextDecoration.None
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (match.completed()) {
                                Text(
                                    match.redScore.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = allianceRed,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.width(50.dp),
                                    textDecoration = if (team != null && match.redAlliance.members.find { member -> member.team.id == team.id } != null ) TextDecoration.Underline else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    match.blueScore.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = allianceBlue,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(50.dp),
                                    textDecoration = if (team != null && match.blueAlliance.members.find { member -> member.team.id == team.id } != null ) TextDecoration.Underline else TextDecoration.None
                                )
                            }
                            else {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    match.field ?: "",
                                    fontSize = 14.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(100.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(60.dp),
                                verticalArrangement = Arrangement.spacedBy((-5).dp)
                            ) {
                                match.blueAlliance.members.forEach{ member ->
                                    Text(
                                        text = member.team.name,
                                        fontSize = 15.sp,
                                        color = allianceBlue,
                                        textDecoration = if (team != null && member.team.id == team.id) TextDecoration.Underline else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                    if (match != matchList.last()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                    }
                }
            }
        }
    }
}