package com.sunkensplashstudios.VRCRoboScout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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

class MainActivity : ComponentActivity() {
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
        var team by remember { mutableStateOf(Team("")) }

        Spacer(Modifier.height(20.dp))
        Text("Search")
        Spacer(Modifier.height(20.dp))
        TextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Team Number") }
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            coroutineScope.launch {
                team = Team(number)
            }
        }) {
            Text("Search")
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = team.team_name
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    VRCRoboScoutTheme {
        Search()
    }
}