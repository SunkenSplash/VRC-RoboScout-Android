package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.helperviews.MatchesView
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventTeamMatchesViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var team by mutableStateOf(Team())
    var loading by mutableStateOf(true)
    var matches by mutableStateOf(listOf<Match>())
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventTeamMatchesView(event: Event, team: Team, eventTeamMatchesViewModel: EventTeamMatchesViewModel = viewModel(), navController: NavController) {

    fun updateMatches() {
        if (eventTeamMatchesViewModel.matches.isEmpty()) {
            eventTeamMatchesViewModel.loading = true
        }
        CoroutineScope(Dispatchers.Default).launch {
            eventTeamMatchesViewModel.matches = team.matchesAt(event)
            withContext(Dispatchers.Main) {
                eventTeamMatchesViewModel.loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        eventTeamMatchesViewModel.event = event
        eventTeamMatchesViewModel.team = team
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${eventTeamMatchesViewModel.team.number} Match List", fontWeight = FontWeight.Bold)
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
            var update by remember { mutableStateOf(true) }

            if (update) {
                update = false
                updateMatches()
            }

            if (eventTeamMatchesViewModel.loading) {
                LoadingView()
            }
            else if ((eventTeamMatchesViewModel.matches).isEmpty()) {
                NoDataView()
            }
            else {
                MatchesView(eventTeamMatchesViewModel.matches, eventTeamMatchesViewModel.team)
            }
        }
    }
}