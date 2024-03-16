package com.sunkensplashstudios.VRCRoboScout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.*

class EventDivisionViewModel: ViewModel() {
    var event by mutableStateOf(Event())
    var division by mutableStateOf(Division())
}

@RootNavGraph
@NavGraph
annotation class EventDivisionNavGraph(
    val start: Boolean = false
)

@Composable
fun DivisionTabView(tabBarItems: List<TabBarItem>, navController: NavController, selectedTabIndex: Int, onSelectedTabIndexChange: (Int) -> Unit) {

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
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
                    Text(text = tabBarItem.title, fontSize = 9.sp, color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Destination
@Composable
fun EventDivisionView(eventDivisionViewModel: EventDivisionViewModel = viewModel(), navController: NavController, event: Event, division: Division) {

    val divisionViewModels = mapOf(
        "event_division_matches_view" to EventDivisionMatchesViewModel(),
        "event_division_rankings_view" to EventDivisionRankingsViewModel(),
        "event_division_awards_view" to EventDivisionAwardsViewModel()
    )

    // setting up the individual tabs
    val teamsTab = TabBarItem(title = "Teams", direction = EventTeamsViewDestination(eventDivisionViewModel.event), selectedIcon = Icons.Filled.People, unselectedIcon = Icons.Outlined.PeopleOutline)
    val matchListTab = TabBarItem(title = "Match List", direction = EventDivisionMatchesViewDestination(), selectedIcon = Icons.Filled.AccessTimeFilled, unselectedIcon = Icons.Outlined.AccessTime)
    val rankingsTab = TabBarItem(title = "Rankings", direction = EventDivisionRankingsViewDestination(eventDivisionViewModel.event, eventDivisionViewModel.division), selectedIcon = Icons.Filled.FormatListNumbered, unselectedIcon = Icons.Filled.FormatListNumbered)
    val awardsTab = TabBarItem(title = "Awards", direction = EventDivisionAwardsViewDestination(), selectedIcon = Icons.Filled.EmojiEvents, unselectedIcon = Icons.Outlined.EmojiEvents)

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
                    })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Replace DestinationsNavHost below with if statement to check if the tab is selected
            if (selectedTabIndex == 0) {
                EventTeamsView(
                    navController = navController,
                    event = event,
                    division = division
                )
            }
            else if (selectedTabIndex == 1) {
                EventDivisionMatchesView(
                    navController = navController,
                    eventDivisionMatchesViewModel = divisionViewModels["event_division_matches_view"] as EventDivisionMatchesViewModel
                )
            }
            else if (selectedTabIndex == 2) {
                EventDivisionRankingsView(
                    event = event,
                    division = division,
                    navController = navController,
                    eventDivisionRankingsViewModel = divisionViewModels["event_division_rankings_view"] as EventDivisionRankingsViewModel
                )
            }
            else if (selectedTabIndex == 3) {
                EventDivisionAwardsView(
                    navController = navController,
                    eventDivisionAwardsViewModel = divisionViewModels["event_division_awards_view"] as EventDivisionAwardsViewModel
                )
            }
        }
    }
}