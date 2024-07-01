package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.sunkensplashstudios.VRCRoboScout.ui.theme.button
import com.sunkensplashstudios.VRCRoboScout.ui.theme.onTopContainer
import com.sunkensplashstudios.VRCRoboScout.ui.theme.topContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.cascade.CascadeDropdownMenu
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun TrueSkillView(navController: NavController) {

    var viewTitle by rememberSaveable { mutableStateOf("World TrueSkill") }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var isFilteredByFavorites by remember { mutableStateOf(false) }
    var filteredLetter by remember { mutableStateOf(' ') }
    var filteredRegion by remember { mutableStateOf("") }

    // fetch favorite teams
    val favoriteTeams = remember {
        UserSettings(context).getData("favoriteTeams", "").replace("[", "").replace("]", "")
            .split(", ")
    }

    // functions to filter
    fun clearFilters() {
        isFilteredByFavorites = false
        filteredLetter = ' '
        filteredRegion = ""
        viewTitle = "World TrueSkill"
    }

    fun filterByFavorites() {
        isFilteredByFavorites = true
        filteredRegion = ""
        filteredLetter = ' '
        viewTitle = "Favorites TrueSkill"
    }

    fun filterByLetter(letter: Char) {
        isFilteredByFavorites = false
        filteredRegion = ""
        filteredLetter = letter
        viewTitle = "$letter TrueSkill"
    }

    fun filterByRegion(regionName: String) {
        isFilteredByFavorites = false
        filteredRegion = regionName
        filteredLetter = ' '
        viewTitle = "$regionName TrueSkill"
    }

    var filterDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.topContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTopContainer,
                ),
                title = {
                    Text(viewTitle, fontWeight = FontWeight.Bold)
                },
                actions = {
                    // link to trueskill page
                    IconButton(
                        onClick = {
                            uriHandler.openUri("https://vrc-data-analysis.com/");
                        },
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Open Trueskill Rankings in Browser",
                            tint = MaterialTheme.colorScheme.onTopContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            filterDropdownExpanded = true
                        },
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onTopContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        var importing by rememberSaveable { mutableStateOf(!API.importedVDA) }

        LaunchedEffect(Unit) {
            CoroutineScope(Dispatchers.Default).launch {
                while (!API.importedVDA) {
                    continue
                }
                withContext(Dispatchers.Main) {
                    importing = false
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            CascadeDropdownMenu(
                expanded = filterDropdownExpanded,
                onDismissRequest = { filterDropdownExpanded = false },

                modifier = Modifier
                    .heightIn(max = 265.dp),
                    offset = DpOffset(10.dp, (-10).dp),
            ) {
                DropdownMenuItem(
                    text = { Text("Favorites") },
                    onClick = {
                        filterDropdownExpanded = false
                        filterByFavorites()
                    }
                )

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.1f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )

                DropdownMenuItem(
                    text = { Text("Region") },
                    children = {
                        API.regionsMap.toSortedMap().forEach { (name) ->
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.1f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )

                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    filterByRegion(name)
                                    filterDropdownExpanded = false
                                }
                            )
                        }
                    }
                )

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.1f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )

                DropdownMenuItem(
                    text = { Text("Letter") },
                    children = {
                        // make a list of all the letters
                        for (letter in 'A'..'Z') {
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.1f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )

                            DropdownMenuItem(
                                text = { Text(letter.toString()) },
                                onClick = {
                                    filterByLetter(letter)
                                    filterDropdownExpanded = false
                                }
                            )
                        }
                    }
                )

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.1f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )

                DropdownMenuItem(
                    text = { Text("Clear Filters") },
                    onClick = {
                        clearFilters()
                        filterDropdownExpanded = false
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (importing) {
                ImportingDataView()
            }
            else if (API.vdaCache.isEmpty()) {
                NoDataView()
            }
            else {
                Card(
                    modifier = Modifier.padding(10.dp),
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                        disabledContainerColor = Color.Unspecified.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContentColor = Color.Unspecified
                    )
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        if (isFilteredByFavorites) {
                            if (favoriteTeams.isEmpty() || favoriteTeams[0] == "") {
                                item {
                                    Text(
                                        "You have no favorite teams!",
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxSize()
                                            .wrapContentSize(align = Alignment.Center)
                                    )
                                }
                            } else itemsIndexed(API.vdaCache.filter { it.teamNumber in favoriteTeams }) { index, vdaEntry ->
                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column (
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        ) {
                                            Text(
                                                "#" + (index + 1),
                                                fontSize = 18.sp,
                                            )

                                            Text(
                                                "(#" + vdaEntry.tsRanking.toString() + ")",
                                                fontSize = 18.sp,
                                            )
                                        }

                                        if ((vdaEntry.rankingChange ?: 0.0) != 0.0) {
                                            Icon(
                                                imageVector = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                contentDescription = "${if ((vdaEntry.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                    abs(
                                                        vdaEntry.rankingChange ?: 0.0
                                                    ).toInt()
                                                } places since last update",
                                                tint = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                abs(
                                                    vdaEntry.rankingChange?.toInt() ?: 0
                                                ).toString(),
                                                fontSize = 16.sp,
                                                color = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(vdaEntry.teamNumber, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            vdaEntry.trueskill.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.button,
                                            modifier = Modifier.clickable {
                                                expanded = !expanded
                                            })
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("CCWM: ${vdaEntry.ccwm}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Wins: ${vdaEntry.totalWins.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Losses: ${vdaEntry.totalLosses.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Ties: ${vdaEntry.totalTies.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                        }
                                    }
                                }

                                if (index != API.vdaCache.filter { it.teamNumber in favoriteTeams }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    )
                                }
                            }
                        }

                        else if (filteredLetter != ' ') {
                            itemsIndexed(API.vdaCache.filter { it.teamNumber.last() == filteredLetter }) { index, vdaEntry ->
                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column (
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        ) {
                                            Text(
                                                "#" + (index + 1),
                                                fontSize = 18.sp,
                                            )

                                            Text(
                                                "(#" + vdaEntry.tsRanking.toString() + ")",
                                                fontSize = 18.sp,
                                            )
                                        }
                                        if ((vdaEntry.rankingChange ?: 0.0) != 0.0) {
                                            Icon(
                                                imageVector = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                contentDescription = "${if ((vdaEntry.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                    abs(
                                                        vdaEntry.rankingChange ?: 0.0
                                                    ).toInt()
                                                } places since last update",
                                                tint = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                abs(
                                                    vdaEntry.rankingChange?.toInt() ?: 0
                                                ).toString(),
                                                fontSize = 16.sp,
                                                color = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(vdaEntry.teamNumber, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            vdaEntry.trueskill.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.button,
                                            modifier = Modifier.clickable {
                                                expanded = !expanded
                                            })
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("CCWM: ${vdaEntry.ccwm}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Wins: ${vdaEntry.totalWins.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Losses: ${vdaEntry.totalLosses.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Ties: ${vdaEntry.totalTies.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                        }
                                    }
                                }

                                if (index != API.vdaCache.filter { it.teamNumber.last() == filteredLetter }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    )
                                }
                            }
                        }

                        else if (filteredRegion != "") {
                            itemsIndexed(API.vdaCache.filter { it.eventRegion == filteredRegion }) { index, vdaEntry ->
                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column (
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        ) {
                                            Text(
                                                "#" + (index + 1),
                                                fontSize = 18.sp,
                                            )

                                            Text(
                                                "(#" + vdaEntry.tsRanking.toString() + ")",
                                                fontSize = 18.sp,
                                            )
                                        }
                                        if ((vdaEntry.rankingChange ?: 0.0) != 0.0) {
                                            Icon(
                                                imageVector = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                contentDescription = "${if ((vdaEntry.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                    abs(
                                                        vdaEntry.rankingChange ?: 0.0
                                                    ).toInt()
                                                } places since last update",
                                                tint = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                abs(
                                                    vdaEntry.rankingChange?.toInt() ?: 0
                                                ).toString(),
                                                fontSize = 16.sp,
                                                color = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(vdaEntry.teamNumber, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            vdaEntry.trueskill.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.button,
                                            modifier = Modifier.clickable {
                                                expanded = !expanded
                                            })
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("CCWM: ${vdaEntry.ccwm}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Wins: ${vdaEntry.totalWins.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Losses: ${vdaEntry.totalLosses.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Ties: ${vdaEntry.totalTies.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                        }
                                    }
                                }

                                if (index != API.vdaCache.filter { it.eventRegion == filteredRegion }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    )
                                }
                            }
                        }

                        else {
                            items(API.vdaCache) { vdaEntry ->

                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#" + vdaEntry.tsRanking.toString(), fontSize = 18.sp)
                                        if ((vdaEntry.rankingChange ?: 0.0) != 0.0) {
                                            Icon(
                                                imageVector = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                                contentDescription = "${if ((vdaEntry.rankingChange ?: 0.0) >= 0.0) "Up" else "Down"} ${
                                                    abs(
                                                        vdaEntry.rankingChange ?: 0.0
                                                    ).toInt()
                                                } places since last update",
                                                tint = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                abs(
                                                    vdaEntry.rankingChange?.toInt() ?: 0
                                                ).toString(),
                                                fontSize = 16.sp,
                                                color = if ((vdaEntry.rankingChange
                                                        ?: 0.0) > 1.0
                                                ) Color(0xFF028A0F) else Color.Red,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(vdaEntry.teamNumber, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            vdaEntry.trueskill.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.button,
                                            modifier = Modifier.clickable {
                                                expanded = !expanded
                                            })
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("CCWM: ${vdaEntry.ccwm}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Wins: ${vdaEntry.totalWins.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Losses: ${vdaEntry.totalLosses.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Total Ties: ${vdaEntry.totalTies.toInt()}") },
                                                onClick = { },
                                                enabled = false,
                                                colors = disabledMenuItemColors(MaterialTheme)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}