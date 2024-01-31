package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun LookupView() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Search()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Search() {

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        var number by rememberSaveable { mutableStateOf("229V") }
        var team by rememberSaveable { mutableStateOf(Team()) }
        var events by rememberSaveable { mutableStateOf(listOf(Event())) }
        var fetched by rememberSaveable { mutableStateOf(false) }

        Spacer(Modifier.height(20.dp))
        TextField(
            value = number,
            onValueChange = { number = it },
            singleLine = true,
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                number = ""
                                fetched = false
                            }
                        }
                    }
                },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 34.sp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    coroutineScope.launch {
                        println("fetch")
                        team = Team(number)
                        fetched = true
                    }
                })
        )
        Spacer(Modifier.height(10.dp))
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Name")
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(if (fetched) team.team_name else "")
                }
            }
            item {
                Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
            }
            item {
                Row {
                    Text("Robot")
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(if (fetched) team.robot_name else "")
                }
            }
            item {
                Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
            }
            item {
                Row {
                    Text("Organization")
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(if (fetched) team.organization else "")
                }
            }
            item {
                Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
            }
            item {
                Row {
                    Text("Location")
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(if (fetched && (team.location.country ?: "").isNotEmpty()) "${team.location.city}, ${team.location.region}" else "")
                }
            }
        }
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