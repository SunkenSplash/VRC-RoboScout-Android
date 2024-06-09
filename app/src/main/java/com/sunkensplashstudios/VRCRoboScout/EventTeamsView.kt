package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

class EventTeamsViewModel: ViewModel() {
    var division by mutableStateOf(Division())
    var teams by mutableStateOf(listOf<Team>())
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventTeamsView(eventId: Int, eventTeamsViewModel: EventTeamsViewModel = viewModel(), navController: NavController, division: Division? = null) {

    LaunchedEffect(Unit) {
        eventTeamsViewModel.division = division ?: Division()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${division?.name ?: "Event"} Teams", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier.padding(10.dp).clickable {
                            navController.navigateUp()
                        },
                        tint = MaterialTheme.colorScheme.onTopContainer
                    )
                }
            )
        }
    ) { padding ->

        var loading by remember { mutableStateOf(eventTeamsViewModel.teams.isEmpty()) }
        var fetching by remember { mutableStateOf(false) }

        fun fetchTeamsList() {
            if (fetching) return
            fetching = true
            CoroutineScope(Dispatchers.Default).launch {
                if (eventTeamsViewModel.teams.isNotEmpty()) {
                    return@launch
                }
                val event = eventDataTransferManager.getEvent(eventId) ?: return@launch
                if (division != null) {
                    event.fetchTeams()
                    event.fetchRankings(division)
                    withContext(Dispatchers.Main) {
                        eventTeamsViewModel.teams =
                            Event.sortTeamsByNumber(event.rankings[division]!!.map {
                                event.getTeam(it.team.id) ?: Team()
                            }, if (event.program.id == 4) "College" else "Not College").toMutableList()
                        loading = false
                        fetching = false
                    }
                }
                else {
                    if (event.teams.isEmpty()) {
                        event.fetchTeams()
                    }
                    withContext(Dispatchers.Main) {
                        eventTeamsViewModel.teams = event.teams
                        loading = false
                        fetching = false
                    }
                }
            }
        }

        if (loading) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                LoadingView()
            }
            fetchTeamsList()
        }
        else if (eventTeamsViewModel.teams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NoDataView()
            }
        }
        else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                            eventTeamsViewModel.teams.forEach { team ->
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(
                                                EventTeamMatchesViewDestination(Event(eventId, false), team)
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        team.number,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    Column {
                                        Text(
                                            team.name,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        Text(
                                            team.location.toString(),
                                            modifier = Modifier.padding(start = 10.dp),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        contentDescription = "Show Match List"
                                    )
                                }
                                if (eventTeamsViewModel.teams.indexOf(team) != eventTeamsViewModel.teams.size - 1) {
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
}