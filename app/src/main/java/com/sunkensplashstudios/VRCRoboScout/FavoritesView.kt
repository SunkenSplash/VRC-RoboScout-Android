package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.*

@RootNavGraph(start = true)
@Destination
@Composable
fun FavoritesView(navController: NavController) {

    val localContext = LocalContext.current
    val favorites = remember { UserSettings(localContext).getData("favorites", "").replace("[", "").replace("]", "").split(", ") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Card(modifier = Modifier.padding(10.dp)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                items(favorites) { favorite ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(10.dp).clickable {
                            navController.navigate(TeamEventsViewDestination(Team(favorite, false)))
                        }
                    ) {
                        Text(favorite, fontSize = 18.sp)
                        Spacer(modifier = Modifier.weight(1.0f))
                    }
                    if (favorites.indexOf(favorite) < favorites.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                    }
                }
            }
        }
    }
}