package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@EventDivisionNavGraph(start = true)
@Destination
@Composable
fun EventTeamsView(navController: NavController, event: Event? = null, division: Division? = null) {

    var teams by remember { mutableStateOf(if (division == null) event!!.teams.toList() else event!!.rankings[division]?.map { event.getTeam(it.team.id) ?: Team() }
        ?.let { Event.sortTeamsByNumber(it) } ?: emptyList()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        var loading by remember { mutableStateOf(division != null && event!!.rankings[division] == null) }

        fun fetchDivisionalTeamsList() {
            CoroutineScope(Dispatchers.Default).launch {
                event!!.fetchRankings(division!!)
                withContext(Dispatchers.Main) {
                    teams = Event.sortTeamsByNumber(event.rankings[division]!!.map { event.getTeam(it.team.id) ?: Team() }).toMutableList()
                    loading = false
                }
            }
        }

        if (loading) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                LoadingView()
            }
            fetchDivisionalTeamsList()
        }
        else if (teams.isEmpty()) {
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
                    Card(modifier = Modifier.padding(10.dp)) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            teams.forEach { team ->
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp)
                                        .fillMaxWidth(),
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
                                }
                                if (teams.indexOf(team) != teams.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary
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