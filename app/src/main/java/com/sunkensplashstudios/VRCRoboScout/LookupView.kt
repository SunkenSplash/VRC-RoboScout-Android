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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Destination
@Composable
fun LookupView(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Lookup()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Lookup() {

    val keyboardController = LocalSoftwareKeyboardController.current

    val localContext = LocalContext.current
    val userSettings = remember { UserSettings(localContext) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // eventually rememberSaveable
        var textColor by remember { mutableStateOf(Color.Gray) }
        var number by remember { mutableStateOf("229V\u200B") }
        var team by remember { mutableStateOf(Team()) }
        var fetched by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }
        var favorites by remember { mutableStateOf(userSettings.getData("favorites", "").replace("[", "").replace("]", "").split(", ")) }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
            Icon(Icons.Filled.Star, modifier = Modifier.size(32.dp).alpha(0F), contentDescription = "Unfavorite")
            Spacer(modifier = Modifier.weight(1.0F))
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
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 34.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedTextColor = textColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        loading = true
                        CoroutineScope(Dispatchers.Default).launch {
                            val fetchedTeam = Team(number)
                            withContext(Dispatchers.Main) {
                                team = fetchedTeam
                                fetched = true
                                loading = false
                                textColor = Color.Unspecified
                            }
                        }
                    })
            )
            Spacer(modifier = Modifier.weight(1.0F))
            IconButton(onClick = {
                if (number.isEmpty() || number == "229V\u200B") {
                    return@IconButton
                }
                else if (favorites.contains(number.uppercase()) && textColor != Color.Unspecified) {
                    userSettings.removeFavoriteTeam(number.uppercase())
                    favorites = userSettings.getData("favorites", "").replace("[", "").replace("]", "").split(", ")
                }
                else {
                    userSettings.addFavoriteTeam(number.uppercase())
                    favorites = userSettings.getData("favorites", "").replace("[", "").replace("]", "").split(", ")
                }
            }) {
                if (favorites.contains(number.uppercase())) {
                    Icon(Icons.Filled.Star, modifier = Modifier.size(32.dp), contentDescription = "Favorite")
                }
                else {
                    Icon(Icons.Outlined.StarOutline, modifier = Modifier.size(32.dp),  contentDescription = "Unfavorite")
                }
            }
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.width(30.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        else {
            Spacer(Modifier.height(40.dp))
        }
        Card(modifier = Modifier.padding(10.dp)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Name")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(if (fetched) team.name else "")
                    }
                }
                item {
                    Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                }
                item {
                    Row {
                        Text("Robot")
                        Spacer(modifier = Modifier.weight(1.0f))
                        Text(if (fetched) team.robotName else "")
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
                        Text(
                            if (fetched && (team.location.country ?: "").isNotEmpty()
                            ) "${team.location.city}, ${team.location.region}" else ""
                        )
                    }
                }
            }
        }
    }
}