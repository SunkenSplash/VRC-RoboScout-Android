package com.sunkensplashstudios.VRCRoboScout

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import com.sunkensplashstudios.VRCRoboScout.destinations.FavoritesViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.LookupViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.SettingsViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.TrueSkillViewDestination
import com.sunkensplashstudios.VRCRoboScout.destinations.WorldSkillsViewDestination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.VRCRoboScoutTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserSettings(context: Context) {
    private val userSettings: SharedPreferences =
        context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)

    fun saveData(key: String, value: String) {
        val editor = userSettings.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getData(key: String, defaultValue: String): String {
        return userSettings.getString(key, defaultValue) ?: defaultValue
    }

    fun addFavoriteTeam(number: String) {
        val newFavorites = this.getData("favoriteTeams", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        if (!newFavorites.contains(number)) newFavorites.add(number)
        this.saveData("favoriteTeams", newFavorites.toString())
    }

    fun removeFavoriteTeam(number: String) {
        val newFavorites = this.getData("favoriteTeams", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        newFavorites.remove(number)
        this.saveData("favoriteTeams", newFavorites.toString())
    }

    fun addFavoriteEvent(sku: String) {
        val newFavorites = this.getData("favoriteEvents", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        if (!newFavorites.contains(sku)) newFavorites.add(sku)
        this.saveData("favoriteEvents", newFavorites.toString())
    }

    fun removeFavoriteEvent(sku: String) {
        val newFavorites = this.getData("favoriteEvents", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        newFavorites.remove(sku)
        this.saveData("favoriteEvents", newFavorites.toString())
    }
}

data class TabBarItem(
    val title: String,
    val direction: Direction,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .width(50.dp)
                .padding(10.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun NoDataView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "No Data",
            tint = Color.Gray,
            modifier = Modifier.size(40.dp).padding(5.dp)
        )
        Text("No Data", color = Color.Gray)
    }
}

@Composable
fun ImportingDataView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp).padding(5.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text("Importing Data", color = Color.Gray)
    }
}

val viewModels = mapOf(
    "favorites_view" to FavoritesViewModel(),
    "lookup_view" to LookupViewModel()
)

class RootActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(Unit) {
                if (!API.importedWS) {
                    CoroutineScope(Dispatchers.Default).launch {
                        API.updateWorldSkillsCache()
                    }
                }
                if (!API.importedVDA) {
                    CoroutineScope(Dispatchers.Default).launch {
                        API.updateVDACache()
                    }
                }
            }

            // setting up the individual tabs
            val favoritesTab = TabBarItem(title = "Favorites", direction = FavoritesViewDestination(), selectedIcon = Icons.Filled.Star, unselectedIcon = Icons.Outlined.StarOutline)
            val worldSkillsTab = TabBarItem(title = "World Skills", direction = WorldSkillsViewDestination(), selectedIcon = Icons.Filled.Language, unselectedIcon = Icons.Outlined.Language)
            val trueskillTab = TabBarItem(title = "TrueSkill", direction = TrueSkillViewDestination(), selectedIcon = Icons.AutoMirrored.Filled.TrendingUp, unselectedIcon = Icons.AutoMirrored.Outlined.TrendingUp)
            val lookupTab = TabBarItem(title = "Lookup", direction = LookupViewDestination(), selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search)
            val settingsTab = TabBarItem(title = "Settings", direction = SettingsViewDestination(), selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)

            // creating a list of all the tabs
            val tabBarItems = listOf(favoritesTab, worldSkillsTab, trueskillTab, lookupTab, settingsTab)

            // creating our navController
            val navController = rememberNavController()

            val navHostEngine = rememberAnimatedNavHostEngine(
                navHostContentAlignment = Alignment.TopCenter,
                rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING
            )

            val tabState by navController.currentBackStackEntryAsState()

            var selectedTabIndex by rememberSaveable {
                mutableIntStateOf(0)
            }

            val routeNameMap = mapOf(
                "favorites_view" to "Favorites",
                "world_skills_view" to "World Skills",
                "true_skill_view" to "World TrueSkill",
                "lookup_view" to "Lookup",
                "settings_view" to "Settings"
            )

            VRCRoboScoutTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            if (routeNameMap[tabState?.destination?.route] != null) {
                                TabView(tabBarItems, navController, selectedTabIndex, onSelectedTabIndexChange = { index ->
                                    selectedTabIndex = index
                                })
                            }
                        }
                    ) { padding ->
                        DestinationsNavHost(
                            navController = navController,
                            navGraph = NavGraphs.root,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            startRoute = FavoritesViewDestination,
                            engine = navHostEngine
                        ) {

                            composable(FavoritesViewDestination) {
                                FavoritesView(
                                    favoritesViewModel = viewModels["favorites_view"] as FavoritesViewModel,
                                    onSelectedTabIndexChange = { index ->
                                        selectedTabIndex = index
                                    },
                                    navController = navController
                                )
                            }
                            composable(WorldSkillsViewDestination) {
                                WorldSkillsView(
                                    navController = navController
                                )
                            }
                            composable(TrueSkillViewDestination) {
                                TrueSkillView(
                                    navController = navController
                                )
                            }
                            composable(LookupViewDestination) {
                                LookupView(
                                    lookupViewModel = viewModels["lookup_view"] as LookupViewModel,
                                    navController = navController
                                )
                            }
                            composable(SettingsViewDestination) {
                                SettingsView(
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController, selectedTabIndex: Int, onSelectedTabIndexChange: (Int) -> Unit) {

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    onSelectedTabIndexChange(index)
                    navController.clearBackStack(navController.graph.startDestinationId)
                    navController.navigate(tabBarItem.direction)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String
) {
    Box {
        Icon(
            imageVector = if (isSelected) {
                selectedIcon
            } else {
                unselectedIcon
            },
            contentDescription = title,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(28.dp)
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