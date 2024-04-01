package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.TeamEventsViewDestination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.*

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class LookupViewModel : ViewModel() {
    var lookupType = mutableStateOf("team")

    // TeamLookup
    var textColor = mutableStateOf(Color.Gray)
    var number = mutableStateOf("229V\u200B")
    var team = mutableStateOf(Team())
    var wsEntry = mutableStateOf(WSEntry())
    var vdaEntry = mutableStateOf(VDAEntry())
    var avgRanking = mutableDoubleStateOf(0.0)
    var fetched = mutableStateOf(false)
    var loading = mutableStateOf(false)

    // EventLookup
    var fetchedEvent = Event()

    fun fetchTeam() {
        loading.value = true
        textColor.value = Color.Unspecified
        CoroutineScope(Dispatchers.Default).launch {
            val fetchedTeam = Team(number.value)
            withContext(Dispatchers.Main) {
                team.value = fetchedTeam
                wsEntry.value = API.worldSkillsFor(fetchedTeam)
                vdaEntry.value = API.vdaFor(fetchedTeam)
                avgRanking.doubleValue = fetchedTeam.averageQualifiersRanking()
                fetched.value = fetchedTeam.id != 0
                loading.value = false
            }
        }
    }
}

@Destination
@Composable
fun LookupView(lookupViewModel: LookupViewModel = viewModels["lookup_view"] as LookupViewModel, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TeamLookup(lookupViewModel = lookupViewModel, navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamLookup(lookupViewModel: LookupViewModel, navController: NavController) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val localContext = LocalContext.current
    val userSettings = remember { UserSettings(localContext) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
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
            var favoriteTeams by remember {
                mutableStateOf(
                    userSettings.getData("favoriteTeams", "").replace("[", "").replace("]", "")
                        .split(", ")
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.width(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        modifier = Modifier.size(30.dp).alpha(0F),
                        contentDescription = "Unfavorite",
                    )
                }
                Spacer(modifier = Modifier.weight(1.0F))
                Box(
                    modifier = Modifier.width(200.dp)
                ) {
                    TextField(
                        value = lookupViewModel.number.value,
                        onValueChange = { lookupViewModel.number.value = it },
                        singleLine = true,
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            lookupViewModel.number.value = ""
                                            lookupViewModel.fetched.value = false
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
                            unfocusedTextColor = lookupViewModel.textColor.value
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                lookupViewModel.fetchTeam()
                            })
                    )
                }
                Spacer(modifier = Modifier.weight(1.0F))
                Box(
                    modifier = Modifier.width(40.dp)
                ) {
                    IconButton(onClick = {
                        favoriteTeams = if (lookupViewModel.number.value.isEmpty() || lookupViewModel.number.value == "229V\u200B") {
                            return@IconButton
                        } else if (favoriteTeams.contains(lookupViewModel.number.value.uppercase()) && lookupViewModel.textColor.value != Color.Unspecified) {
                            userSettings.removeFavoriteTeam(lookupViewModel.number.value.uppercase())
                            userSettings.getData("favoriteTeams", "").replace("[", "")
                                .replace("]", "")
                                .split(", ")
                        } else {
                            userSettings.addFavoriteTeam(lookupViewModel.number.value.uppercase())
                            userSettings.getData("favoriteTeams", "").replace("[", "")
                                .replace("]", "")
                                .split(", ")
                        }
                    }) {
                        if (favoriteTeams.contains(lookupViewModel.number.value.uppercase()) && lookupViewModel.number.value.isNotBlank()) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.button
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = "Unfavorite",
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.button
                            )
                        }
                    }
                }
            }
            if (lookupViewModel.loading.value) {
                Column(
                    modifier = Modifier.height(60.dp),
                ) {
                    LoadingView()
                }
            } else {
                Spacer(Modifier.height(60.dp))
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Name")
                                Spacer(modifier = Modifier.weight(1.0f))
                                Text(if (lookupViewModel.fetched.value) lookupViewModel.team.value.name else "")
                            }
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Row {
                            Text("Robot")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(if (lookupViewModel.fetched.value) (lookupViewModel.team.value.robotName ?: "") else "")
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Row {
                            Text("Organization")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(if (lookupViewModel.fetched.value) lookupViewModel.team.value.organization else "")
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Row {
                            Text("Location")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value && (lookupViewModel.team.value.location.country ?: "").isNotEmpty()
                                ) lookupViewModel.team.value.location.toString() else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )

                        var tsExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("TrueSkill Ranking", modifier = Modifier.clickable {
                                tsExpanded = !tsExpanded
                            }, color = MaterialTheme.colorScheme.button)
                            DropdownMenu(
                                expanded = tsExpanded,
                                onDismissRequest = { tsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (lookupViewModel.fetched.value) "${lookupViewModel.vdaEntry.value.trueskill} TrueSkill" else "No TrueSkill data") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${if ((lookupViewModel.vdaEntry.value.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                abs(
                                                    lookupViewModel.vdaEntry.value.rankingChange ?: 0.0
                                                ).toInt()
                                            } places since last update"
                                        )
                                    },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value) "# ${lookupViewModel.vdaEntry.value.tsRanking} of ${API.vdaCache.size}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Row {
                            Text("World Skills Ranking")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value) "# ${lookupViewModel.wsEntry.value.rank} of ${API.wsCache.size}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )

                        var wsExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("World Skills Score", modifier = Modifier.clickable {
                                wsExpanded = !wsExpanded
                            }, color = MaterialTheme.colorScheme.button)
                            DropdownMenu(
                                expanded = wsExpanded,
                                onDismissRequest = { wsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("${lookupViewModel.wsEntry.value.scores.programming} Programming") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${lookupViewModel.wsEntry.value.scores.driver} Driver") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${lookupViewModel.wsEntry.value.scores.maxProgramming} Highest Programming") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("${lookupViewModel.wsEntry.value.scores.maxDriver} Highest Driver") },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value) lookupViewModel.wsEntry.value.scores.score.toString() else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )

                        var msExpanded by remember { mutableStateOf(false) }

                        Row {
                            Text("Match Statistics", modifier = Modifier.clickable {
                                msExpanded = !msExpanded
                            }, color = MaterialTheme.colorScheme.button)
                            DropdownMenu(
                                expanded = msExpanded,
                                onDismissRequest = { msExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            String.format(
                                                "Average Qualifiers Ranking: %.1f",
                                                lookupViewModel.avgRanking.doubleValue
                                            )
                                        )
                                    },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("CCWM: ${lookupViewModel.vdaEntry.value.ccwm}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Winrate: ${lookupViewModel.vdaEntry.value.totalWinningPercent}%") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Matches: ${lookupViewModel.vdaEntry.value.totalMatches.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Wins: ${lookupViewModel.vdaEntry.value.totalWins.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Losses: ${lookupViewModel.vdaEntry.value.totalLosses.toInt()}") },
                                    onClick = { }
                                )
                                DropdownMenuItem(
                                    text = { Text("Total Ties: ${lookupViewModel.vdaEntry.value.totalTies.toInt()}") },
                                    onClick = { }
                                )
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value) "${lookupViewModel.vdaEntry.value.totalWins.toInt()}-${lookupViewModel.vdaEntry.value.totalLosses.toInt()}-${lookupViewModel.vdaEntry.value.totalTies.toInt()}" else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Row {
                            Text("Qualifications")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(
                                if (lookupViewModel.fetched.value) {
                                    listOf(
                                        if (lookupViewModel.vdaEntry.value.qualifiedForWorlds == 1) "Worlds" else "",
                                        if (lookupViewModel.vdaEntry.value.qualifiedForRegionals == 1) "Regionals" else ""
                                    )
                                        .filter { it.isNotEmpty() }
                                        .joinToString(", ")
                                } else ""
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        )
                        Column(modifier = Modifier.clickable {
                            if (lookupViewModel.fetched.value) {
                                navController.navigate(
                                    TeamEventsViewDestination(
                                        lookupViewModel.team.value
                                    )
                                )
                            }
                        }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Events",
                                    color = MaterialTheme.colorScheme.button
                                )
                                Spacer(modifier = Modifier.weight(1.0f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    modifier = Modifier.size(15.dp).alpha(if (lookupViewModel.fetched.value) 1F else 0F),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    contentDescription = "Show Events"
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}