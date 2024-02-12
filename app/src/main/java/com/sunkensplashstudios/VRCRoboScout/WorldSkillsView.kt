package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.sunkensplashstudios.VRCRoboScout.destinations.TeamEventsViewDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun WorldSkillsView(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("World Skills", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->

        var importing by rememberSaveable { mutableStateOf(!API.importedWS) }

        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.Default).launch {
                while (!API.importedWS) {
                    continue
                }
                withContext(Dispatchers.Main) {
                    importing = false
                }
            }
        }

        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {

            if (importing) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(50.dp).padding(10.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Text("Importing Data")
                }
            }
            else {
                Card(modifier = Modifier.padding(10.dp)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        items(API.wsCache) { wsEntry ->

                            var expanded by remember { mutableStateOf(false) }

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 0.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#" + wsEntry.rank.toString(), fontSize = 18.sp, modifier = Modifier.width(130.dp))
                                Spacer(modifier = Modifier.weight(1.0f))
                                Text(wsEntry.team.number, fontSize = 18.sp)
                                Spacer(modifier = Modifier.weight(1.0f))
                                Row(
                                    modifier = Modifier.width(130.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(wsEntry.scores.score.toString(), fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable{
                                        expanded = !expanded
                                    })
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("${wsEntry.scores.score} Combined") },
                                            onClick = { }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${wsEntry.scores.programming} Programming") },
                                            onClick = { }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${wsEntry.scores.driver} Driver") },
                                            onClick = { }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${wsEntry.scores.maxProgramming} Highest Programming") },
                                            onClick = { }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("${wsEntry.scores.maxDriver} Highest Driver") },
                                            onClick = { }
                                        )
                                    }
                                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                                    Column {
                                        Text(
                                            wsEntry.scores.programming.toString(),
                                            fontSize = 12.sp
                                        )
                                        Text(wsEntry.scores.driver.toString(), fontSize = 12.sp)
                                    }
                                }
                            }
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}