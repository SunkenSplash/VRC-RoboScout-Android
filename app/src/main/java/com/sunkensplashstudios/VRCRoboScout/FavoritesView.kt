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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.TeamEventsViewDestination

@OptIn(ExperimentalMaterial3Api::class)
@RootNavGraph(start = true)
@Destination
@Composable
fun FavoritesView(navController: NavController) {

    val localContext = LocalContext.current
    val favorites = remember { UserSettings(localContext).getData("favorites", "").replace("[", "").replace("]", "").split(", ") }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Favorites", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                Text(
                    if (favorites.any { it.isNotBlank() }) "FAVORITE TEAMS" else "ADD FAVORITE TEAMS IN THE TEAM LOOKUP",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Card(modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        if (favorites.any { it.isNotBlank() }) {
                            favorites
                                .sorted()
                                .sortedBy {
                                    (it.filter { char -> "0123456789".contains(char) }
                                        .toIntOrNull() ?: 0)
                                }.forEach { favorite ->
                                    if (favorite == "") return@forEach
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 10.dp).clickable {
                                            navController.navigate(
                                                TeamEventsViewDestination(
                                                    Team(favorite, false)
                                                )
                                            )
                                        }
                                    ) {
                                        Text(favorite, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            modifier = Modifier.size(18.dp),
                                            contentDescription = "Show Events"
                                        )
                                    }
                                    if (favorites.sorted()
                                            .sortedBy {
                                                (it.filter { char -> "0123456789".contains(char) }
                                                    .toIntOrNull() ?: 0)
                                            }.indexOf(favorite) < favorites.size - 1
                                    ) {
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                        }
                        else {
                            Text(
                                "Find a team",
                                modifier = Modifier.padding(10.dp).fillMaxWidth().clickable {
                                    // TODO: Change tab index to show team lookup
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}