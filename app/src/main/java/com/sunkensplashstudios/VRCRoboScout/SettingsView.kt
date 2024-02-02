package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun SettingsView(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Settings")
    }
}