package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.EventTeamMatchesViewDestination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//class EventDivisionRankingsViewModel(private val state: SavedStateHandle): ViewModel() {
class EventDivisionRankingsViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var division by mutableStateOf(Division())
    var rankings by mutableStateOf(listOf<TeamRanking>())
    var teamPerformanceRatings by mutableStateOf(mapOf<Int, TeamPerformanceRatings>())
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventDivisionRankingsView(event: Event, division: Division, eventDivisionRankingsViewModel: EventDivisionRankingsViewModel = viewModel(), navController: NavController) {

    var loading by remember { mutableStateOf(eventDivisionRankingsViewModel.rankings.isEmpty() || eventDivisionRankingsViewModel.teamPerformanceRatings.isEmpty()) }

    fun updateRankings() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                event.fetchRankings(division)
                eventDivisionRankingsViewModel.rankings = (event.rankings[division] ?: emptyList()).toMutableList()
                event.calculateTeamPerformanceRatings(division)
                eventDivisionRankingsViewModel.teamPerformanceRatings = event.teamPerformanceRatings[division] ?: emptyMap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        eventDivisionRankingsViewModel.event = event
        eventDivisionRankingsViewModel.division = division
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${division.name} Rankings", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(10.dp)
                            .clickable {
                                navController.navigateUp()
                            },
                        tint = MaterialTheme.colorScheme.onTopContainer
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            var update by remember { mutableStateOf(true) }

            if (update) {
                update = false
                updateRankings()
            }

            if (loading) {
                LoadingView()
            }
            else if (eventDivisionRankingsViewModel.rankings.isEmpty()) {
                NoDataView()
            }
            else {
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
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            eventDivisionRankingsViewModel.rankings.reversed().forEach { ranking ->
                                val performanceRatings = eventDivisionRankingsViewModel.teamPerformanceRatings[ranking.team.id]
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(
                                                EventTeamMatchesViewDestination(
                                                    event,
                                                    eventDivisionRankingsViewModel.event.getTeam(
                                                        ranking.team.id
                                                    ) ?: Team()
                                                )
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1.0f)
                                    ) {
                                        Row {
                                            Text(
                                                ranking.team.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                modifier = Modifier.width(80.dp)
                                            )
                                            Text(
                                                eventDivisionRankingsViewModel.event.getTeam(ranking.team.id)?.name ?: "Unknown",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 17.sp
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {

                                                Text(
                                                    "# ${ranking.rank}",
                                                )
                                                Text(
                                                    "${ranking.wins}-${ranking.losses}-${ranking.ties}"
                                                )
                                            }
                                            Column {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    Spacer(modifier = Modifier.weight(0.5f))
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy((-10).dp)
                                                    ) {
                                                        Text(
                                                            "WP: ${ranking.wp}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "OPR: ${performanceRatings?.opr.let { "%.1f".format(it) }}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "HIGH: ${ranking.highScore}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.weight(0.5f))
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy((-10).dp)
                                                    ) {
                                                        Text(
                                                            "AP: ${ranking.ap}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "DPR: ${performanceRatings?.dpr.let { "%.1f".format(it) }}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "AVG: ${ranking.averagePoints.let { "%.0f".format(it) }}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.weight(0.5f))
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy((-10).dp)
                                                    ) {
                                                        Text(
                                                            "SP: ${ranking.sp}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "CCWM: ${performanceRatings?.ccwm.let { "%.1f".format(it) }}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "TTL: ${ranking.totalPoints}",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        contentDescription = "Show ${eventDivisionRankingsViewModel.event.getTeam(ranking.team.id)?.number} Match List"
                                    )
                                }
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
    }
}