package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.TeamEventsViewDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Destination
@Composable
fun LookupView(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Lookup(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Lookup(navController: NavController) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val localContext = LocalContext.current
    val userSettings = remember { UserSettings(localContext) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Lookup", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            // eventually rememberSaveable
            var textColor by remember { mutableStateOf(Color.Gray) }
            var number by remember { mutableStateOf("229V\u200B") }
            var team by remember { mutableStateOf(Team()) }
            var wsEntry by remember { mutableStateOf(WSEntry()) }
            var vdaEntry by remember { mutableStateOf(VDAEntry()) }
            var avgRanking by remember { mutableStateOf(0.0) }
            var fetched by remember { mutableStateOf(false) }
            var loading by remember { mutableStateOf(false) }
            var favorites by remember {
                mutableStateOf(
                    userSettings.getData("favorites", "").replace("[", "").replace("]", "")
                        .split(", ")
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Icon(
                    Icons.Filled.Star,
                    modifier = Modifier.size(32.dp).alpha(0F),
                    contentDescription = "Unfavorite"
                )
                Spacer(modifier = Modifier.weight(1.0F))
                TextField(
                    value = number,
                    onValueChange = { number = it },
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        number = ""
                                        fetched = false
                                    }
                                }
                            }
                        },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 34.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedTextColor = textColor
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            loading = true
                            CoroutineScope(Dispatchers.Default).launch {
                                val fetchedTeam = Team(number)
                                withContext(Dispatchers.Main) {
                                    team = fetchedTeam
                                    wsEntry = API.worldSkillsFor(team)
                                    vdaEntry = API.vdaFor(team)
                                    avgRanking = team.averageQualifiersRanking()
                                    fetched = true
                                    loading = false
                                    textColor = Color.Unspecified
                                }
                            }
                        })
                )
                Spacer(modifier = Modifier.weight(1.0F))
                IconButton(onClick = {
                    if (number.isEmpty() || number == "229V\u200B") {
                        return@IconButton
                    } else if (favorites.contains(number.uppercase()) && textColor != Color.Unspecified) {
                        userSettings.removeFavoriteTeam(number.uppercase())
                        favorites =
                            userSettings.getData("favorites", "").replace("[", "").replace("]", "")
                                .split(", ")
                    } else {
                        userSettings.addFavoriteTeam(number.uppercase())
                        favorites =
                            userSettings.getData("favorites", "").replace("[", "").replace("]", "")
                                .split(", ")
                    }
                }) {
                    if (favorites.contains(number.uppercase()) && number.isNotBlank()) {
                        Icon(
                            Icons.Filled.Star,
                            modifier = Modifier.size(32.dp),
                            contentDescription = "Favorite"
                        )
                    } else {
                        Icon(
                            Icons.Outlined.StarOutline,
                            modifier = Modifier.size(32.dp),
                            contentDescription = "Unfavorite"
                        )
                    }
                }
            }
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(30.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            } else {
                Spacer(Modifier.height(40.dp))
            }
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Card(modifier = Modifier.padding(10.dp)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Name")
                                Spacer(modifier = Modifier.weight(1.0f))
                                Text(if (fetched) team.name else "")
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text("Robot")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(if (fetched) team.robotName else "")
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text("Organization")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(if (fetched) team.organization else "")
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text("Location")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched && (team.location.country ?: "").isNotEmpty()
                                ) "${team.location.city}, ${team.location.region}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        var tsExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("TrueSkill Ranking", modifier = Modifier.clickable {
                                tsExpanded = !tsExpanded
                            }, color = MaterialTheme.colorScheme.primary)
                            DropdownMenu(
                                expanded = tsExpanded,
                                onDismissRequest = { tsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (fetched) "${vdaEntry.trueskill} TrueSkill" else "No TrueSkill data") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${if ((vdaEntry.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                abs(
                                                    vdaEntry.rankingChange ?: 0.0
                                                ).toInt()
                                            } places since last update"
                                        )
                                    },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched) "# ${vdaEntry.tsRanking} of ${API.vdaCache.size}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text("World Skills Ranking")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched) "# ${wsEntry.rank} of ${API.wsCache.size}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        var wsExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("World Skills Score", modifier = Modifier.clickable {
                                wsExpanded = !wsExpanded
                            }, color = MaterialTheme.colorScheme.primary)
                            DropdownMenu(
                                expanded = wsExpanded,
                                onDismissRequest = { wsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("${wsEntry.scores.programming} Programming") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${wsEntry.scores.driver} Driver") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${wsEntry.scores.maxProgramming} Highest Programming") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${wsEntry.scores.maxDriver} Highest Driver") },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched) wsEntry.scores.score.toString() else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        var msExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("Match Statistics", modifier = Modifier.clickable {
                                msExpanded = !msExpanded
                            }, color = MaterialTheme.colorScheme.primary)
                            DropdownMenu(
                                expanded = msExpanded,
                                onDismissRequest = { msExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            String.format(
                                                "Average Qualifiers Ranking: %.1f",
                                                avgRanking
                                            )
                                        )
                                    },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("CCWM: ${vdaEntry.ccwm}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Winrate: ${vdaEntry.totalWinningPercent}%") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Matches: ${vdaEntry.totalMatches.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Wins: ${vdaEntry.totalWins.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Losses: ${vdaEntry.totalLosses.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Ties: ${vdaEntry.totalTies.toInt()}") },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched) "${vdaEntry.totalWins.toInt()}-${vdaEntry.totalLosses.toInt()}-${vdaEntry.totalTies.toInt()}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            Text("Qualifications")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (fetched) {
                                    listOf(
                                        if (vdaEntry.qualifiedForWorlds == 1) "Worlds" else "",
                                        if (vdaEntry.qualifiedForRegionals == 1) "Regionals" else ""
                                    )
                                        .filter { it.isNotEmpty() }
                                        .joinToString(", ")
                                } else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        // Button to go to TeamEventsView labeled "Events"
                        Column(modifier = Modifier.clickable {
                            if (fetched) {
                                navController.navigate(
                                    TeamEventsViewDestination(
                                        team
                                    )
                                )
                            }
                        }) {
                            Text("Events", color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}