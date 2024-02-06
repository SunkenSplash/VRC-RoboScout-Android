package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun EventView(navController: NavController, event: Event) {

    var event by remember { mutableStateOf(event) }
    val navState by navController.currentBackStackEntryAsState()
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        CoroutineScope(Dispatchers.Default).launch {
            // event.fetchTeams()
            withContext(Dispatchers.Main) {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(event.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(50.dp).padding(10.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            else {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Event", modifier = Modifier.padding(horizontal = 10.dp), color = Color.Gray)
                Card(modifier = Modifier.padding(10.dp)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        item {
                            Text("Information")
                        }
                        item {
                            Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                        }
                        item {
                            Text("Teams")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Skills", modifier = Modifier.padding(horizontal = 10.dp), color = Color.Gray)
                Card(modifier = Modifier.padding(10.dp)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        item {
                            Row {
                                Text("Skills Rankings")
                                Spacer(modifier = Modifier.weight(1.0f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Divisions", modifier = Modifier.padding(horizontal = 10.dp), color = Color.Gray)
                Card(modifier = Modifier.padding(10.dp)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        item {
                            Row {
                                Text("Default")
                                Spacer(modifier = Modifier.weight(1.0f))
                            }
                        }
                    }
                }
            }
        }
    }
}