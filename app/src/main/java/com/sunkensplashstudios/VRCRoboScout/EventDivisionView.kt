package com.sunkensplashstudios.VRCRoboScout

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventDivisionAwardsViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventDivisionMatchesViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventDivisionRankingsViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.EventTeamsViewDestination

class EventDivisionViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var division by mutableStateOf(Division())
}

@Composable
fun DivisionTabView(tabBarItems: List<TabBarItem>, navController: NavController, selectedTabIndex: Int, onSelectedTabIndexChange: (Int) -> Unit) {
    val localContext = LocalContext.current
    val userSettings = UserSettings(localContext)
    NavigationBar(
        containerColor = if (userSettings.getMinimalisticMode()) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            if (userSettings.getMinimalisticMode()) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        onSelectedTabIndexChange(index)
                        navController.clearBackStack(navController.graph.startDestinationId)
                    },
                    icon = {
                        TabBarIconView(
                            isSelected = selectedTabIndex == index,
                            selectedIcon = tabBarItem.selectedIcon,
                            unselectedIcon = tabBarItem.unselectedIcon,
                            title = tabBarItem.title
                        )
                    }
                )
            }
            else {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        onSelectedTabIndexChange(index)
                        navController.clearBackStack(navController.graph.startDestinationId)
                    },
                    icon = {
                        TabBarIconView(
                            isSelected = selectedTabIndex == index,
                            selectedIcon = tabBarItem.selectedIcon,
                            unselectedIcon = tabBarItem.unselectedIcon,
                            title = tabBarItem.title
                        )
                    },
                    label = {
                        Text(
                            text = tabBarItem.title,
                            fontSize = 9.sp,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Destination
@Composable
fun EventDivisionView(eventId: Int, division: Division, eventDivisionViewModel: EventDivisionViewModel = viewModel(), navController: NavController) {

    LaunchedEffect(Unit) {
        eventDivisionViewModel.event = eventDataTransferManager.getEvent(eventId) ?: Event(eventId, false)
        eventDivisionViewModel.division = division
    }

    // setting up the individual tabs
    val teamsTab = TabBarItem(title = "Teams", direction = EventTeamsViewDestination(eventDivisionViewModel.event.id, eventDivisionViewModel.division), selectedIcon = Icons.Filled.People, unselectedIcon = Icons.Outlined.PeopleOutline)
    val matchListTab = TabBarItem(title = "Match List", direction = EventDivisionMatchesViewDestination(eventDivisionViewModel.event, eventDivisionViewModel.division), selectedIcon = Icons.Filled.AccessTimeFilled, unselectedIcon = Icons.Outlined.AccessTime)
    val rankingsTab = TabBarItem(title = "Rankings", direction = EventDivisionRankingsViewDestination(eventDivisionViewModel.event, eventDivisionViewModel.division), selectedIcon = Icons.Filled.FormatListNumbered, unselectedIcon = Icons.Filled.FormatListNumbered)
    val awardsTab = TabBarItem(title = "Awards", direction = EventDivisionAwardsViewDestination(eventDivisionViewModel.event, eventDivisionViewModel.division), selectedIcon = Icons.Filled.EmojiEvents, unselectedIcon = Icons.Outlined.EmojiEvents)

    // creating a list of all the tabs
    val tabBarItems = listOf(teamsTab, matchListTab, rankingsTab, awardsTab)

    val tabState by navController.currentBackStackEntryAsState()

    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    val routeNameMap = mapOf(
        "event_teams_view" to "Teams",
        "event_division_matches_view" to "Match List",
        "event_division_rankings_view" to "Rankings",
        "event_division_awards_view" to "Awards"
    )

    Scaffold(
        bottomBar = {
            if (routeNameMap[tabState?.destination?.route] != null || true) {
                DivisionTabView(
                    tabBarItems,
                    navController,
                    selectedTabIndex,
                    onSelectedTabIndexChange = { index ->
                        selectedTabIndex = index
                    }
                )
                val localContext = LocalContext.current
                val userSettings = UserSettings(localContext)
                val view = LocalView.current
                val surfaceContainerLow = if (userSettings.getMinimalisticMode()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainerLow
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        window.navigationBarColor = surfaceContainerLow.toArgb()
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Replace DestinationsNavHost below with if statement to check if the tab is selected
            when (selectedTabIndex) {
                0 -> {
                    EventTeamsView(
                        navController = navController,
                        eventId = eventId,
                        division = division
                    )
                }
                1 -> {
                    EventDivisionMatchesView(
                        navController = navController,
                        event = eventDivisionViewModel.event,
                        division = division,
                        //eventDivisionMatchesViewModel = divisionViewModels["event_division_matches_view"] as EventDivisionMatchesViewModel
                    )
                }
                2 -> {
                    EventDivisionRankingsView(
                        event = eventDivisionViewModel.event,
                        division = division,
                        navController = navController,
                        //eventDivisionRankingsViewModel = divisionViewModels["event_division_rankings_view"] as EventDivisionRankingsViewModel
                    )
                }
                3 -> {
                    EventDivisionAwardsView(
                        event = eventDivisionViewModel.event,
                        division = division,
                        navController = navController,
                        //eventDivisionAwardsViewModel = divisionViewModels["event_division_awards_view"] as EventDivisionAwardsViewModel
                    )
                }
            }
        }
    }
}