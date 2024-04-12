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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventDivisionAwardsViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var division by mutableStateOf(Division())
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventDivisionAwardsView(event: Event, division: Division, eventDivisionAwardsViewModel: EventDivisionAwardsViewModel = viewModel(), navController: NavController) {

    var loading by remember { mutableStateOf(event.awards[division] == null) }

    fun updateAwards() {
        if (event.matches[division] == null) {
            loading = true
        }
        CoroutineScope(Dispatchers.Default).launch {
            event.fetchAwards(division)
            withContext(Dispatchers.Main) {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        eventDivisionAwardsViewModel.event = event
        eventDivisionAwardsViewModel.division = division
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${division.name} Awards", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            var update by remember { mutableStateOf(true) }

            if (update) {
                update = false
                updateAwards()
            }

            if (loading) {
                LoadingView()
            }
            else if ((event.awards[division] ?: emptyList()).isEmpty()) {
                NoDataView()
            }
            else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                                alpha = 0.5f
                            ),
                            disabledContainerColor = Color.Unspecified.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = Color.Unspecified
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            event.awards[division]?.forEach { award ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                                ) {
                                    Column {
                                        Text(award.title)
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(-5.dp),
                                        ) {
                                            award.teamWinners.forEach { teamWinner ->
                                                val team =
                                                    event.getTeam(teamWinner.team.id) ?: Team()
                                                Row {
                                                    Text(
                                                        team.number,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Gray
                                                    )
                                                    Spacer(modifier = Modifier.size(5.dp))
                                                    Text(
                                                        team.name,
                                                        fontSize = 14.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                if (event.awards[division]?.last() != award) {
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