package com.sunkensplashstudios.VRCRoboScout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sunkensplashstudios.VRCRoboScout.ui.theme.VRCRoboScoutTheme
import kotlinx.coroutines.launch

class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VRCRoboScoutTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Search()
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(10.dp, 20.dp)
        ) {
            items(events) { event ->
                Text(event.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    VRCRoboScoutTheme {
        Search()
    }
}