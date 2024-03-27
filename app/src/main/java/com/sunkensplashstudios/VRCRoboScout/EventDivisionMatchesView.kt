package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

class EventDivisionMatchesViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var division by mutableStateOf(Division())
}

@Composable
fun MatchesView(matchList: List<Match>) {
    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
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
                                color = MaterialTheme.colorScheme.onSurface
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
                                        color = allianceRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                match.redScore.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = allianceRed,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.width(50.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                match.blueScore.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = allianceBlue,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(50.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(60.dp),
                                verticalArrangement = Arrangement.spacedBy((-5).dp)
                            ) {
                                match.blueAlliance.members.forEach{ member ->
                                    Text(
                                        text = member.team.name,
                                        fontSize = 15.sp,
                                        color = allianceBlue
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

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventDivisionMatchesView(event: Event, division: Division, eventDivisionMatchesViewModel: EventDivisionMatchesViewModel = viewModel(), navController: NavController) {

    var loading by remember { mutableStateOf(event.matches[division] == null) }

    fun updateMatches() {
        if (event.matches[division] == null) {
            loading = true
        }
        CoroutineScope(Dispatchers.Default).launch {
            event.fetchMatches(division)
            withContext(Dispatchers.Main) {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        eventDivisionMatchesViewModel.event = event
        eventDivisionMatchesViewModel.division = division
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text("${division.name} Match List", fontWeight = FontWeight.Bold)
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

            if (loading) {
                LoadingView()
            }
            else if ((event.matches[division] ?: emptyList()).isEmpty()) {
                NoDataView()
            }
            else {
                MatchesView(event.matches[division] ?: emptyList())
            }
        }
    }
}