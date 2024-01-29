package com.sunkensplashstudios.VRCRoboScout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sunkensplashstudios.VRCRoboScout.ui.theme.VRCRoboScoutTheme
import kotlinx.coroutines.launch

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

class RootActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // setting up the individual tabs
            val favoritesTab = TabBarItem(title = "Favorites", selectedIcon = Icons.Filled.Star, unselectedIcon = Icons.Outlined.Star)
            val worldSkillsTab = TabBarItem(title = "World Skills", selectedIcon = Icons.Filled.Language, unselectedIcon = Icons.Outlined.Language)
            val trueskillTab = TabBarItem(title = "TrueSkill", selectedIcon = Icons.Filled.TrendingUp, unselectedIcon = Icons.Outlined.TrendingUp)
            val lookupTab = TabBarItem(title = "Lookup", selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search)
            val settingsTab = TabBarItem(title = "Settings", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)

            // creating a list of all the tabs
            val tabBarItems = listOf(favoritesTab, worldSkillsTab, trueskillTab, lookupTab, settingsTab)

            // creating our navController
            val navController = rememberNavController()

            val tabState by navController.currentBackStackEntryAsState()

            VRCRoboScoutTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = {
                                    Text(tabState?.destination?.route ?: "VRC RoboScout", fontWeight = FontWeight.Bold)
                                }
                            )
                        },
                        bottomBar = { TabView(tabBarItems, navController) }
                    ) { padding ->
                        NavHost(navController = navController, startDestination = favoritesTab.title, modifier = Modifier
                            .padding(padding)) {
                            composable(favoritesTab.title) {
                                FavoritesView()
                            }
                            composable(worldSkillsTab.title) {
                                WorldSkillsView()
                            }
                            composable(trueskillTab.title) {
                                TrueSkillView()
                            }
                            composable(lookupTab.title) {
                                LookupView()
                            }
                            composable(settingsTab.title) {
                                SettingsView()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Search() {

    val coroutineScope = rememberCoroutineScope()
    //var text by remember { mutableStateOf("Hello") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        var number by remember { mutableStateOf("") }
        var sku by remember { mutableStateOf("") }
        var team by remember { mutableStateOf(Team()) }
        var event by remember { mutableStateOf(Event()) }
        var events by remember { mutableStateOf(listOf(Event()))}

        Spacer(Modifier.height(20.dp))
        Text("Search")
        Spacer(Modifier.height(20.dp))
        TextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Team Number") }
        )
        Spacer(Modifier.height(10.dp))
        TextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("Event SKU") }
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            coroutineScope.launch {
                team = Team(number)
                event = Event(sku)
            }
        }) {
            Text("Search")
        }
        if (team.number.isNotEmpty()) {
            Button(onClick = {
                coroutineScope.launch {
                    team.fetchEvents()
                    events = team.events
                }
            }) {
                Text("Fetch Events")
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = team.team_name
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = event.name
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(10.dp, 20.dp)
        ) {
            items(events) { event ->
                Text(event.name)
            }
        }
    }
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title)
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(text = tabBarItem.title, fontSize = 11.sp)})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RootActivityPreview() {
    RootActivity()
}