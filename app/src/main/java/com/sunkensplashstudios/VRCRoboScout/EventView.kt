package com.sunkensplashstudios.VRCRoboScout

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.EventDivisionViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventInformationViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventSkillsRankingsViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventTeamMatchesViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventTeamsViewDestination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var loading by mutableStateOf(true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventView(eventViewModel: EventViewModel = viewModel(), navController: NavController, event: Event, team: Team? = null) {

    fun getEventViewModel() {
        if (eventViewModelStore.getEventViewModel(event) != null) {
            eventViewModel.event = eventViewModelStore.getEventViewModel(event)!!.event
            eventViewModel.loading = false
            return
        }
        eventViewModel.loading = true
        CoroutineScope(Dispatchers.Default).launch {
            event.fetchTeams()
            withContext(Dispatchers.Main) {
                eventViewModel.event = event
                eventViewModel.loading = false
                eventViewModelStore.updateEventViewModel(eventViewModel)
            }
        }
    }

    LaunchedEffect(Unit) {
        getEventViewModel()
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
                },
                actions = {
                    val localContext = LocalContext.current
                    val userSettings = remember { UserSettings(localContext) }
                    var favoriteEvents by remember {
                        mutableStateOf(
                            userSettings.getData("favoriteEvents", "").replace("[", "").replace("]", "")
                                .split(", ")
                        )
                    }
                    IconButton(
                        onClick = {
                            favoriteEvents = if (favoriteEvents.contains(event.sku)) {
                                userSettings.removeFavoriteEvent(event.sku)
                                userSettings.getData("favoriteEvents", "").replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                            } else {
                                userSettings.addFavoriteEvent(event.sku)
                                userSettings.getData("favoriteEvents", "").replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                            }
                        }
                    ) {
                        if (favoriteEvents.contains(event.sku)) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.onTopContainer
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = "Unfavorite",
                                tint = MaterialTheme.colorScheme.onTopContainer
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val view = LocalView.current
        val background = MaterialTheme.colorScheme.background
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.navigationBarColor = background.toArgb()
            }
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (eventViewModel.loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingView()
                }
            }
            else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "EVENT",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
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
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate(
                                        EventInformationViewDestination(eventViewModel.event)
                                    )
                                }
                            ) {
                                Text("Information")
                                Spacer(modifier = Modifier.weight(1.0f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    contentDescription = "Show Event Information"
                                )
                            }
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            )
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    eventDataTransferManager.putEvent(eventViewModel.event)
                                    navController.navigate(
                                        EventTeamsViewDestination(eventViewModel.event.id)
                                    )
                                }
                            ) {
                                Text("Teams")
                                Spacer(modifier = Modifier.weight(1.0f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    contentDescription = "Show Event Teams"
                                )
                            }
                            if (team != null) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        println("Team ID: ${team.id}, Number: ${team.number}")
                                        navController.navigate(
                                            EventTeamMatchesViewDestination(eventViewModel.event, team)
                                        )
                                    }
                                ) {
                                    Text("${team.number} Match List")
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        contentDescription = "Show ${team.number} Match List"
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "SKILLS",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color.Gray,
                        fontSize = 13.sp,
                    )
                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Unspecified.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = Color.Unspecified
                        )) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate(
                                        EventSkillsRankingsViewDestination(eventViewModel.event)
                                    )
                                }
                            ) {
                                Text("Skills Rankings")
                                Spacer(modifier = Modifier.weight(1.0f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    contentDescription = "Show Skills Rankings"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "DIVISIONS",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Color.Gray,
                        fontSize = 13.sp,
                    )
                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        colors = CardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Unspecified.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = Color.Unspecified
                        )) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(10.dp)
                        ) {
                            eventViewModel.event.divisions.forEach { division ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        eventDataTransferManager.putEvent(eventViewModel.event)
                                        navController.navigate(
                                            EventDivisionViewDestination(eventViewModel.event.id, division)
                                        )
                                    }
                                ) {
                                    Text(division.name)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        contentDescription = "Show Division"
                                    )
                                }
                                if (eventViewModel.event.divisions.indexOf(division) != eventViewModel.event.divisions.size - 1) {
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