package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventSkillsRankingsView(navController: NavController, event: Event) {

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        CoroutineScope(Dispatchers.Default).launch {
            event.fetchSkillsRankings()
            withContext(Dispatchers.Main) {
                loading = false
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
                    Text(event.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        if (loading) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                LoadingView()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (event.skillsRankings.isEmpty()) {
                        NoDataView()
                    } else {
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
                                event.skillsRankings.forEach { skillsRanking ->
                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 10.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                skillsRanking.team.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                modifier = Modifier.width(80.dp)
                                            )
                                            Text(
                                                "# ${skillsRanking.rank}",
                                            )
                                            Text(
                                                skillsRanking.combinedScore.toString()
                                            )
                                        }
                                        Column {
                                            Text(
                                                event.getTeam(skillsRanking.team.id)?.name
                                                    ?: "Unknown",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 18.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                Column {
                                                    Text(
                                                        "Prog: ${skillsRanking.programmingAttempts}",
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        skillsRanking.programmingScore.toString(),
                                                        color = Color.Gray
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        "Driver: ${skillsRanking.driverAttempts}",
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        skillsRanking.driverScore.toString(),
                                                        color = Color.Gray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(20.dp))
                                            }
                                        }
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
}