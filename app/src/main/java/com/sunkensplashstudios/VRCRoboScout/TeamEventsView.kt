package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.helperviews.EventRow
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamEventsViewModel: ViewModel() {
    var events by mutableStateOf(listOf<Event>())
    var team by mutableStateOf(Team())
    var loading by mutableStateOf(true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun TeamEventsView(teamEventsViewModel: TeamEventsViewModel = viewModel(), navController: NavController, team: Team) {

    LaunchedEffect(Unit) {
        eventDataTransferManager.clearEvents()
        if (teamEventsViewModel.events.isNotEmpty()) {
            return@LaunchedEffect
        }
        teamEventsViewModel.loading = true
        CoroutineScope(Dispatchers.Default).launch {
            team.fetchInfo()
            team.fetchEvents()
            withContext(Dispatchers.Main) {
                teamEventsViewModel.loading = false
                teamEventsViewModel.events = team.events
                teamEventsViewModel.team = team
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${teamEventsViewModel.team.number} Events", fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (teamEventsViewModel.loading) {
                LoadingView()
            }
            else if (teamEventsViewModel.events.isEmpty()) {
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
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
                        ) {
                            teamEventsViewModel.events.reversed().forEach { event ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    EventRow(navController, event, teamEventsViewModel.team)
                                }
                                if (teamEventsViewModel.events.indexOf(event) != 0) {
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