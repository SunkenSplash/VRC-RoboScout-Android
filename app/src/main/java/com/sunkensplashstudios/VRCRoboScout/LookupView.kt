package com.sunkensplashstudios.VRCRoboScout

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.sunkensplashstudios.VRCRoboScout.helperviews.EventRow
import com.sunkensplashstudios.VRCRoboScout.helperviews.SegmentText
import com.sunkensplashstudios.VRCRoboScout.helperviews.SegmentedControl
import com.sunkensplashstudios.VRCRoboScout.ui.theme.button
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromJsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class LookupViewModel : ViewModel() {
    var lookupType = mutableStateOf("Teams")
    var applicationContext: Context? = null

    // TeamLookup
    var teamTextColor = mutableStateOf(Color.Gray)
    var number = mutableStateOf("229V\u200B")
    var team = mutableStateOf(Team())
    var wsEntry = mutableStateOf(WSEntry())
    var vdaEntry = mutableStateOf(VDAEntry())
    var avgRanking = mutableDoubleStateOf(0.0)
    var awardCounts = LinkedHashMap<String, Int>()
    var fetchedTeams = mutableStateOf(false)
    var loadingTeams = mutableStateOf(false)

    // EventLookup
    var eventTextColor = mutableStateOf(Color.Gray)
    var eventName = mutableStateOf("Event Name\u200B")
    var events = mutableStateOf(listOf<Event>())
    var page = mutableIntStateOf(1)
    var fetchedEvents = mutableStateOf(false)
    var loadingEvents = mutableStateOf(false)

    fun fetchTeam() {
        loadingTeams.value = true
        teamTextColor.value = Color.Unspecified
        CoroutineScope(Dispatchers.Default).launch {
            val fetchedTeam = Team(number.value)
            fetchedTeam.fetchAwards()
            val fetchedAwardCounts = LinkedHashMap<String, Int>()
            for (award in fetchedTeam.awards) {
                fetchedAwardCounts[award.title] = (fetchedAwardCounts[award.title] ?: 0) + 1
            }
            withContext(Dispatchers.Main) {
                team.value = fetchedTeam
                wsEntry.value = API.worldSkillsFor(fetchedTeam)
                vdaEntry.value = API.vdaFor(fetchedTeam, true)
                avgRanking.doubleValue = fetchedTeam.averageQualifiersRanking()
                awardCounts = fetchedAwardCounts
                fetchedTeams.value = fetchedTeam.id != 0
                loadingTeams.value = false
            }
        }
    }

    fun fetchEvents(name: String? = null, season: Int? = null, level: Int? = null, grade: Int? = null, region: Int? = null, noLeagues: Boolean = false, page: Int = 1) {

        loadingEvents.value = true
        eventTextColor.value = Color.Unspecified

        val scraperParams = mutableMapOf<String, Any>()

        if (name != null && name != "") {
            scraperParams["name"] = name
        }
        if (season != null) {
            scraperParams["seasonId"] = season
        }
        if (noLeagues || name == null || name == "") {
            scraperParams["eventType"] = 1
        }
        if (level != null) {
            scraperParams["level_class_id"] = level
        }
        if (grade != null) {
            scraperParams["grade_level_id"] = grade
        }
        if (region != null) {
            scraperParams["event_region"] = region
        }

        scraperParams["page"] = page

        scraperParams["seasonId"] = UserSettings(applicationContext!!).getSelectedSeasonId()

        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)

        scraperParams["from_date"] = if (name.isNullOrEmpty()) formatter.format(Date()) else "01-Jan-1970"

        CoroutineScope(Dispatchers.Default).launch {
            val skuArray = RoboScoutAPI.roboteventsCompetitionScraper(params = scraperParams)
            if (skuArray.isEmpty()) {
                withContext(Dispatchers.Main) {
                    events.value = listOf()
                    fetchedEvents.value = false
                    loadingEvents.value = false
                }
                return@launch
            }
            val data = RoboScoutAPI.roboteventsRequest(
                requestUrl = "/seasons/${season ?: UserSettings(applicationContext!!).getSelectedSeasonId()}/events",
                params = mapOf("sku" to skuArray)
            )
            withContext(Dispatchers.Main) {
                val fetchedEventsList = mutableListOf<Event>()
                for (eventData in data) {
                    val fetchedEvent: Event = jsonWorker.decodeFromJsonElement(eventData)
                    fetchedEventsList.add(fetchedEvent)
                }
                events.value = fetchedEventsList
                fetchedEvents.value = true
                loadingEvents.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun LookupView(lookupViewModel: LookupViewModel = viewModels["lookup_view"] as LookupViewModel, navController: NavController) {

    val userSettings = UserSettings(LocalContext.current)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        lookupViewModel.applicationContext = LocalContext.current.applicationContext
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
            },
            bottomBar = {
                if (lookupViewModel.lookupType.value != "Teams") {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            enabled = lookupViewModel.page.intValue != 1,
                            onClick = {
                                lookupViewModel.page.intValue -= 1
                                lookupViewModel.fetchEvents(
                                    name = lookupViewModel.eventName.value,
                                    page = lookupViewModel.page.intValue,
                                    grade = if (userSettings.getGradeLevel() == "Middle School") 2 else if (userSettings.getGradeLevel() == "High School") 3 else null
                                )
                            }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Previous Page",
                                modifier = Modifier.width(30.dp),
                                tint = if (lookupViewModel.page.intValue != 1) MaterialTheme.colorScheme.button else Color.Gray
                            )
                        }
                        Text(
                            "${lookupViewModel.page.intValue}",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            enabled = lookupViewModel.events.value.size == 20,
                            onClick = {
                                lookupViewModel.page.intValue += 1
                                lookupViewModel.fetchEvents(
                                    name = lookupViewModel.eventName.value,
                                    page = lookupViewModel.page.intValue,
                                    grade = if (userSettings.getGradeLevel() == "Middle School") 2 else if (userSettings.getGradeLevel() == "High School") 3 else null
                                )
                            }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Next Page",
                                modifier = Modifier.width(30.dp),
                                tint = if (lookupViewModel.events.value.size == 20) MaterialTheme.colorScheme.button else Color.Gray
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                SegmentedControl(
                    listOf("Teams", "Events"),
                    lookupViewModel.lookupType.value,
                    onSegmentSelected = { lookupViewModel.lookupType.value = it },
                    modifier = Modifier.padding(10.dp)
                ) {
                    SegmentText(
                        text = it
                    )
                }
                if (lookupViewModel.lookupType.value == "Teams") {
                    TeamLookup(lookupViewModel = lookupViewModel, navController = navController)
                }
                else {
                    EventLookup(lookupViewModel = lookupViewModel, navController = navController)
                }
            }
        }
    }
}

@Composable
fun TeamLookup(lookupViewModel: LookupViewModel, navController: NavController) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val localContext = LocalContext.current
    val userSettings = remember { UserSettings(localContext) }
    val isFocused = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        var favoriteTeams by remember {
            mutableStateOf(
                userSettings.getData("favoriteTeams", "").replace("[", "").replace("]", "")
                    .split(", ")
            )
        }
        Spacer(Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            Spacer(Modifier.width(14.dp)) // Thank you Android for making spacing weird
            Icon(
                Icons.Filled.Star,
                modifier = Modifier
                    .size(30.dp)
                    .alpha(0F),
                contentDescription = "Spacer",
            )
            Spacer(modifier = Modifier.weight(1.0f))
            TextField(
                modifier = Modifier.sizeIn(maxWidth = 200.dp),
                value = lookupViewModel.number.value,
                onValueChange = { lookupViewModel.number.value = it.trim() },
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                when (it) {
                                    is FocusInteraction.Focus -> isFocused.value = true
                                    is FocusInteraction.Unfocus -> isFocused.value = false
                                    is PressInteraction.Release -> {
                                        lookupViewModel.number.value = ""
                                        lookupViewModel.fetchedTeams.value = false
                                    }
                                }
                            }
                        }
                    },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp,
                    color = if (lookupViewModel.number.value.isEmpty() || lookupViewModel.number.value == "229V\u200B") Color.Gray else MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedTextColor = lookupViewModel.teamTextColor.value,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        lookupViewModel.fetchTeam()
                    }),
                placeholder = {
                    if (!isFocused.value && lookupViewModel.number.value.isEmpty()) {
                        Text(
                            "229V\u200B",
                            modifier = Modifier.fillMaxWidth(),
                            style = LocalTextStyle.current.copy(
                                color = Color.Gray,
                                fontSize = 34.sp,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Box {
                IconButton(
                    enabled = lookupViewModel.number.value != "229V\u200B" && lookupViewModel.number.value.isNotBlank(),
                    modifier = Modifier.alpha(if (lookupViewModel.number.value != "229V\u200B" && lookupViewModel.number.value.isNotBlank()) 1F else 0F),
                    onClick = {
                    favoriteTeams =
                        if (lookupViewModel.number.value.isEmpty() || lookupViewModel.number.value == "229V\u200B") {
                            return@IconButton
                        } else if (favoriteTeams.contains(lookupViewModel.number.value.uppercase()) && !lookupViewModel.loadingTeams.value) {
                            userSettings.removeFavoriteTeam(lookupViewModel.number.value.uppercase())
                            userSettings.getData("favoriteTeams", "").replace("[", "")
                                .replace("]", "")
                                .split(", ")
                        } else {
                            // allow adding to favorites only after fetching team data
                            if (!lookupViewModel.fetchedTeams.value) {
                                keyboardController?.hide()
                                lookupViewModel.fetchTeam()
                                return@IconButton
                            }

                            else {
                                userSettings.addFavoriteTeam(lookupViewModel.number.value.uppercase())
                                userSettings.getData("favoriteTeams", "").replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                            }
                        }
                }) {
                    if (favoriteTeams.contains(lookupViewModel.number.value.uppercase()) && lookupViewModel.number.value.isNotBlank()) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Unfavorite",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.button
                        )
                    } else {
                        Icon(
                            Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.button
                        )
                    }
                }
            }
        }
        if (lookupViewModel.loadingTeams.value) {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Name")
                            Spacer(modifier = Modifier.weight(1.0f))
                            Text(if (lookupViewModel.fetchedTeams.value) lookupViewModel.team.value.name else "")
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )
                    Row {
                        Text("Robot")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(if (lookupViewModel.fetchedTeams.value) (lookupViewModel.team.value.robotName ?: "") else "")
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )
                    Row {
                        Text("Organization")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(if (lookupViewModel.fetchedTeams.value) lookupViewModel.team.value.organization else "")
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )
                    Row {
                        Text("Location")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(
                            if (lookupViewModel.fetchedTeams.value && (lookupViewModel.team.value.location.country ?: "").isNotEmpty()
                            ) lookupViewModel.team.value.location.toString() else ""
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )

                    var tsExpanded by remember { mutableStateOf(false) }

                    Row {
                        Text(
                            "TrueSkill Ranking",
                            modifier =  if (lookupViewModel.fetchedTeams.value) {
                                Modifier.clickable {
                                    tsExpanded = !tsExpanded
                                }
                            } else {
                                Modifier
                            },
                            color = MaterialTheme.colorScheme.button,
                        )
                        DropdownMenu(
                            expanded = tsExpanded,
                            onDismissRequest = { tsExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (lookupViewModel.fetchedTeams.value) "${lookupViewModel.vdaEntry.value.trueskill} TrueSkill" else "No TrueSkill data") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
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
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(
                            if (lookupViewModel.fetchedTeams.value) "# ${lookupViewModel.vdaEntry.value.tsRanking} of ${API.vdaCache.size}" else ""
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
                            if (lookupViewModel.fetchedTeams.value) "# ${lookupViewModel.wsEntry.value.rank} of ${API.wsCache.size}" else ""
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )

                    var wsExpanded by remember { mutableStateOf(false) }

                    Row {
                        Text(
                            "World Skills Score",
                            modifier =  if (lookupViewModel.fetchedTeams.value) {
                                Modifier.clickable {
                                    wsExpanded = !wsExpanded
                                }
                            } else {
                                Modifier
                            },
                            color = MaterialTheme.colorScheme.button
                        )
                        DropdownMenu(
                            expanded = wsExpanded,
                            onDismissRequest = { wsExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("${lookupViewModel.wsEntry.value.scores.programming} Programming") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("${lookupViewModel.wsEntry.value.scores.driver} Driver") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("${lookupViewModel.wsEntry.value.scores.maxProgramming} Highest Programming") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("${lookupViewModel.wsEntry.value.scores.maxDriver} Highest Driver") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(
                            if (lookupViewModel.fetchedTeams.value) lookupViewModel.wsEntry.value.scores.score.toString() else ""
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )

                    var msExpanded by remember { mutableStateOf(false) }

                    Row {
                        Text(
                            "Match Statistics",
                            modifier =  if (lookupViewModel.fetchedTeams.value) {
                                Modifier.clickable {
                                    msExpanded = !msExpanded
                                }
                            } else {
                                Modifier
                            },
                            color = MaterialTheme.colorScheme.button
                        )
                        DropdownMenu(
                            expanded = msExpanded,
                            onDismissRequest = { msExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Average Qualifiers Ranking: ${lookupViewModel.avgRanking.doubleValue.round(1)}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("CCWM: ${lookupViewModel.vdaEntry.value.ccwm}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("Winrate: ${lookupViewModel.vdaEntry.value.totalWinningPercent.round(1)}%") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("Total Matches: ${lookupViewModel.vdaEntry.value.totalMatches.toInt()}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("Total Wins: ${lookupViewModel.vdaEntry.value.totalWins.toInt()}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("Total Losses: ${lookupViewModel.vdaEntry.value.totalLosses.toInt()}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                            DropdownMenuItem(
                                text = { Text("Total Ties: ${lookupViewModel.vdaEntry.value.totalTies.toInt()}") },
                                onClick = { },
                                enabled = false,
                                colors = disabledMenuItemColors(MaterialTheme)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(
                            if (lookupViewModel.fetchedTeams.value) "${lookupViewModel.vdaEntry.value.totalWins.toInt()}-${lookupViewModel.vdaEntry.value.totalLosses.toInt()}-${lookupViewModel.vdaEntry.value.totalTies.toInt()}" else ""
                        )
                    }
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    )

                    var awardsExpanded by remember { mutableStateOf(false) }

                    Row {
                        Text(
                            "Awards",
                            modifier =  if (lookupViewModel.fetchedTeams.value) {
                                Modifier.clickable {
                                    awardsExpanded = !awardsExpanded
                                }
                            } else {
                                Modifier
                            },
                            color = MaterialTheme.colorScheme.button
                        )
                        DropdownMenu(
                            expanded = awardsExpanded,
                            onDismissRequest = { awardsExpanded = false }
                        ) {
                            lookupViewModel.awardCounts.forEach { award ->
                                DropdownMenuItem(
                                    text = { Text("${award.value}x ${award.key}") },
                                    onClick = { },
                                    enabled = false,
                                    colors = disabledMenuItemColors(MaterialTheme)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(
                            if (lookupViewModel.fetchedTeams.value) "${lookupViewModel.team.value.awards.size}" else ""
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
                            if (lookupViewModel.fetchedTeams.value) {
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
                    Column(
                        modifier = if (lookupViewModel.fetchedTeams.value) {
                            Modifier.clickable {
                                if (lookupViewModel.fetchedTeams.value) {
                                    navController.navigate(
                                        TeamEventsViewDestination(
                                            lookupViewModel.team.value
                                        )
                                    )
                                }
                            }
                        } else {
                            Modifier
                        }
                    ) {
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
                                modifier = Modifier
                                    .size(15.dp)
                                    .alpha(if (lookupViewModel.fetchedTeams.value) 1F else 0F),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                contentDescription = "Show Events"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventLookup(lookupViewModel: LookupViewModel, navController: NavController) {
    val userSettings = UserSettings(LocalContext.current)

    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (lookupViewModel.events.value.isEmpty()) {
            lookupViewModel.fetchEvents(
                grade = if (userSettings.getGradeLevel() == "Middle School") 2 else if (userSettings.getGradeLevel() == "High School") 3 else null
            )
            lookupViewModel.page.intValue = 1
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = lookupViewModel.eventName.value,
                onValueChange = { lookupViewModel.eventName.value = it },
                singleLine = true,
                interactionSource = remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                when (it) {
                                    is FocusInteraction.Focus -> isFocused.value = true
                                    is FocusInteraction.Unfocus -> isFocused.value = false
                                    is PressInteraction.Release -> {
                                        lookupViewModel.eventName.value = ""
                                        lookupViewModel.fetchedEvents.value = false
                                    }
                                }
                            }
                        }
                    },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp,
                    color = if (lookupViewModel.eventName.value.isEmpty() || lookupViewModel.eventName.value == "Event Name\u200B") Color.Gray else MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedTextColor = lookupViewModel.eventTextColor.value
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        lookupViewModel.fetchEvents(name = lookupViewModel.eventName.value, page = 1, grade = if (userSettings.getGradeLevel() == "Middle School") 2 else if (userSettings.getGradeLevel() == "High School") 3 else null)
                        lookupViewModel.page.intValue = 1
                    }),
                placeholder = {
                    if (!isFocused.value && lookupViewModel.eventName.value.isEmpty()) {
                        Text(
                            "Event Name\u200B",
                            modifier = Modifier.fillMaxWidth(),
                            style = LocalTextStyle.current.copy(
                                color = Color.Gray,
                                fontSize = 34.sp,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }
            )
        }
        if (lookupViewModel.loadingEvents.value) {
            Column(
                modifier = Modifier.height(60.dp),
            ) {
                LoadingView()
            }
        } else {
            Spacer(Modifier.height(60.dp))
        }
        if (lookupViewModel.events.value.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f, false)
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
                        lookupViewModel.events.value.forEach { event ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EventRow(navController, event)
                            }
                            if (lookupViewModel.events.value.indexOf(event) != lookupViewModel.events.value.size - 1) {
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