package com.sunkensplashstudios.VRCRoboScout

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Path
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
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import com.sunkensplashstudios.VRCRoboScout.destinations.*
import com.sunkensplashstudios.VRCRoboScout.ui.theme.VRCRoboScoutTheme
import kotlinx.coroutines.launch

class UserSettings(context: Context) {
    private val userSettings: SharedPreferences =
        context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)

    fun saveData(key: String, value: String) {
        println("Saving $key with value $value")
        val editor = userSettings.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getData(key: String, defaultValue: String): String {
        return userSettings.getString(key, defaultValue) ?: defaultValue
    }

    fun addFavoriteTeam(number: String) {
        println("Adding $number")
        println("Data: " + this.getData("favorites", ""))
        val newFavorites = this.getData("favorites", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        if (!newFavorites.contains(number)) newFavorites.add(number)
        println("New Data: $newFavorites")
        this.saveData("favorites", newFavorites.toString())
    }

    fun removeFavoriteTeam(number: String) {
        println("Removing $number")
        println("Data: " + this.getData("favorites", ""))
        val newFavorites = this.getData("favorites", "").replace("[", "").replace("]", "").split(", ").toMutableList()
        newFavorites.removeAll(listOf(""))
        newFavorites.remove(number)
        println("New Data: $newFavorites")
        this.saveData("favorites", newFavorites.toString())
    }
}

data class TabBarItem(
    val title: String,
    val direction: Direction,
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
            val favoritesTab = TabBarItem(title = "Favorites", direction = FavoritesViewDestination(), selectedIcon = Icons.Filled.Star, unselectedIcon = Icons.Outlined.Star)
            val worldSkillsTab = TabBarItem(title = "World Skills", direction = WorldSkillsViewDestination(), selectedIcon = Icons.Filled.Language, unselectedIcon = Icons.Outlined.Language)
            val trueskillTab = TabBarItem(title = "TrueSkill", direction = TrueSkillViewDestination(), selectedIcon = Icons.Filled.TrendingUp, unselectedIcon = Icons.Outlined.TrendingUp)
            val lookupTab = TabBarItem(title = "Lookup", direction = LookupViewDestination(), selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search)
            val settingsTab = TabBarItem(title = "Settings", direction = SettingsViewDestination(), selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)

            // creating a list of all the tabs
            val tabBarItems = listOf(favoritesTab, worldSkillsTab, trueskillTab, lookupTab, settingsTab)

            // creating our navController
            val navController = rememberNavController()

            val tabState by navController.currentBackStackEntryAsState()

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
                        topBar = {
                            if (routeNameMap[tabState?.destination?.route] != null) {
                                CenterAlignedTopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    title = {
                                        Text(
                                            routeNameMap[tabState?.destination?.route]
                                                ?: "VRC RoboScout", fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        },
                        bottomBar = {
                            if (routeNameMap[tabState?.destination?.route] != null) {
                                TabView(tabBarItems, navController)
                            }
                        }
                    ) { padding ->
                        DestinationsNavHost(
                            navController = navController,
                            navGraph = NavGraphs.root,
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize(),
                            startRoute = FavoritesViewDestination
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.direction)
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