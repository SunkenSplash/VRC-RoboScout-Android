package com.sunkensplashstudios.VRCRoboScout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.cascade.CascadeDropdownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun WorldSkillsView(navController: NavController) {

    var viewTitle by rememberSaveable { mutableStateOf("World Skills") }
    val context = LocalContext.current

    // filters
    var isFilteredByFavorites by remember { mutableStateOf(false) }
    var hasFilteredLetter by remember { mutableStateOf(' ') }
    var hasFilteredRegion by remember { mutableStateOf(0) }

    // get favorite teams
    val favoriteTeams = remember {
        UserSettings(context).getData("favoriteTeams", "").replace("[", "").replace("]", "")
            .split(", ")
    }

    // clear filters
    fun clearFilters() {
        isFilteredByFavorites = false
        hasFilteredLetter = ' '
        hasFilteredRegion = 0
        viewTitle = "World Skills"
    }

    // filter by favorites
    fun filterByFavorites() {
        isFilteredByFavorites = true
        hasFilteredRegion = 0
        hasFilteredLetter = ' '
        viewTitle = "Favorites Skills"
    }

    // filter by letter
    fun filterByLetter(letter: Char) {
        hasFilteredLetter = letter
        hasFilteredRegion = 0
        isFilteredByFavorites = false
        viewTitle = "$letter Skills"
    }

    // filter by region
    fun filterByRegion(region: Int, regionName: String) {
        hasFilteredRegion = region
        isFilteredByFavorites = false
        hasFilteredLetter = ' '
        viewTitle = "$regionName Skills"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(viewTitle, fontWeight = FontWeight.Bold)
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (importing) {
                ImportingDataView()
            } else if (API.wsCache.isEmpty()) {
                NoDataView()
            } else {
                // start filter menu retracted
                var filterDropdownExpanded by remember { mutableStateOf(false) }

                Card(
                    shape = RectangleShape,
                    modifier = Modifier
                        .padding(0.dp, 1.dp, 0.dp, 15.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .indication(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            )
                            .clickable {
                                // toggle the filter dropdown
                                filterDropdownExpanded = !filterDropdownExpanded
                            }

                    ) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Text(
                                text = "Filter",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                // not sure why the iOS app has more padding at the bottom, but I guess I'll replicate it
                                modifier = Modifier
                                    .padding(bottom = 18.dp, top = 10.dp)
                            )

                            CascadeDropdownMenu(
                                expanded = filterDropdownExpanded,
                                onDismissRequest = { filterDropdownExpanded = false },
                                // padding top -10dp
                                offset = DpOffset(x = (-72).dp, y = (-12).dp),

                                modifier = Modifier
                                    .heightIn(max = 265.dp)

                                // set max height to 200dp

                            ) {
                                DropdownMenuItem(
                                    text = { Text("Favorites") },
                                    onClick = {
                                        filterDropdownExpanded = false
                                        filterByFavorites()
                                    }
                                )

                                HorizontalDivider(
                                    color = Color.Gray,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                )

                                DropdownMenuItem(
                                    text = { Text("Region") },
                                    children = {
                                        API.regionsMap.toSortedMap().forEach { (name, id) ->
                                            HorizontalDivider(
                                                color = Color.Gray,
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(horizontal = 5.dp)
                                            )

                                            DropdownMenuItem(
                                                text = { Text(name) },
                                                onClick = {
                                                    filterByRegion(id, name)
                                                    filterDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                )

                                HorizontalDivider(
                                    color = Color.Gray,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                )

                                DropdownMenuItem(
                                    text = { Text("Letter") },
                                    children = {
                                        // make a list of all the letters
                                        for (letter in 'A'..'Z') {
                                            HorizontalDivider(
                                                color = Color.Gray,
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
                                    color = Color.Gray,
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
                    }
                }

                Card(modifier = Modifier.padding(10.dp)) {
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
                            }

                            else itemsIndexed(API.wsCache.filter { it.team.number in favoriteTeams }) { index, wsEntry ->

                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column (
                                        modifier = Modifier.padding(vertical = 7.dp)
                                    ) {
                                        Text(
                                            "#" + (index + 1),
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                        Text(
                                            "(#" + wsEntry.rank.toString() + ")",
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(wsEntry.team.number, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            wsEntry.scores.score.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
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

                                if (index != API.wsCache.filter { it.team.number in favoriteTeams }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        else if (hasFilteredLetter != ' ') {
                            itemsIndexed(API.wsCache.filter { it.team.number.last() == hasFilteredLetter }) { index, wsEntry ->

                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column (
                                        modifier = Modifier.padding(vertical = 7.dp)
                                    ) {
                                        Text(
                                            "#" + (index + 1),
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                        Text(
                                            "(#" + wsEntry.rank.toString() + ")",
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(wsEntry.team.number, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            wsEntry.scores.score.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
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

                                if (index != API.wsCache.filter { it.team.number.last() == hasFilteredLetter }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        else if (hasFilteredRegion != 0) {

                            itemsIndexed(API.wsCache.filter { it.team.eventRegionId == hasFilteredRegion }) { index, wsEntry ->

                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column (
                                        modifier = Modifier.padding(vertical = 7.dp)
                                    ) {
                                        Text(
                                            "#" + (index + 1),
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                        Text(
                                            "(#" + wsEntry.rank.toString() + ")",
                                            fontSize = 18.sp,
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(wsEntry.team.number, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            wsEntry.scores.score.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
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

                                if (index != API.wsCache.filter { it.team.eventRegionId == hasFilteredRegion }.size - 1) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // no filtering!
                        else {
                            items(API.wsCache) { wsEntry ->

                                var expanded by remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 0.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "#" + wsEntry.rank.toString(),
                                        fontSize = 18.sp,
                                        modifier = Modifier.width(130.dp)
                                    )
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Text(wsEntry.team.number, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1.0f))
                                    Row(
                                        modifier = Modifier.width(130.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Text(
                                            wsEntry.scores.score.toString(),
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
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
}