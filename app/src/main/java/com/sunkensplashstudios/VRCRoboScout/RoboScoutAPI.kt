package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.runtime.MutableState
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val API = RoboScoutAPI()
val jsonWorker = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        jsonWorker
    }
}

@Serializable
class WSTeam {
    var id: Int = 0
    var program: String = ""
    var teamRegId: Int = 0
    @SerialName("team") var number: String = ""
    @SerialName("teamName") var name: String = ""
    var organization: String = ""
    var city: String = ""
    var region: String? = ""
    var country: String = ""
    var gradeLevel: String = ""
    var link: String = ""
    var eventRegion: String = ""
    var eventRegionId: Int = 0
}

@Serializable
class WSEvent {
    var sku: String = ""
    var startDate: String = ""
    var seasonName: String = ""
}

@Serializable
class WSScores {
    var score: Int = 0
    var programming: Int = 0
    var driver: Int = 0
    var combinedStopTime: Int = 0
    var maxProgramming: Int = 0
    var maxDriver: Int = 0
    var progStopTime: Int = 0
    var driverStopTime: Int = 0
    var progScoredAt: String = ""
    var driverScoredAt: String = ""
}

@Serializable
class WSEntry : MutableState<WSEntry> {
    var rank: Int = 0
    var team: WSTeam = WSTeam()
    var event: WSEvent = WSEvent()
    var scores: WSScores = WSScores()
    override var value: WSEntry
        get() = this
        set(value) {
            this.rank = value.rank
            this.team = value.team
            this.event = value.event
            this.scores = value.scores
        }

    override fun component1(): WSEntry {
        return this
    }

    override fun component2(): (WSEntry) -> Unit {
        return { this.value = it }
    }
}

@Serializable
class VDAEntry : MutableState<VDAEntry> {
    @SerialName("ts_ranking") var tsRanking: Int = 0
    @SerialName("ranking_change") var rankingChange: Double? = 0.0
    @SerialName("ts_ranking_region") var tsRankingRegion: Int = 0
    @SerialName("team_link") var teamLink: String = ""
    @SerialName("team_number") var teamNumber: String = ""
    @SerialName("team_name") var teamName: String = ""
    var id: Double = 0.0
    var grade: String? = ""
    @SerialName("event_region") var eventRegion: String = ""
    @SerialName("loc_region") var locRegion: String = ""
    @SerialName("loc_country") var locCountry: String = ""
    var trueskill: Double = 0.0
    var ccwm: Double = 0.0
    @SerialName("total_wins") var totalWins: Double = 0.0
    @SerialName("total_losses") var totalLosses: Double = 0.0
    @SerialName("total_ties") var totalTies: Double = 0.0
    @SerialName("total_matches") var totalMatches: Double = 0.0
    @SerialName("total_winning_percent") var totalWinningPercent: Double = 0.0
    @SerialName("elimination_wins") var eliminationWins: Double = 0.0
    @SerialName("elimination_losses") var eliminationLosses: Double = 0.0
    @SerialName("elimination_ties") var eliminationTies: Double = 0.0
    @SerialName("elimination_winning_percent") var eliminationWinningPercent: Double? = 0.0
    @SerialName("qual_wins") var qualWins: Double = 0.0
    @SerialName("qual_losses") var qualLosses: Double = 0.0
    @SerialName("qual_ties") var qualTies: Double = 0.0
    @SerialName("qual_winning_percent") var qualWinningPercent: Double? = 0.0
    @SerialName("ap_per_match") var apPerMatch: Double = 0.0
    @SerialName("awp_per_match") var awpPerMatch: Double = 0.0
    @SerialName("wp_per_match") var wpPerMatch: Double = 0.0
    var mu: Double = 0.0
    @SerialName("ts_sigma") var tsSigma: Double = 0.0
    var opr: Double = 0.0
    var dpr: Double = 0.0
    @SerialName("qualified_for_regionals") var qualifiedForRegionals: Int = 0
    @SerialName("qualified_for_worlds") var qualifiedForWorlds: Int = 0
    override var value: VDAEntry
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun component1(): VDAEntry {
        TODO("Not yet implemented")
    }

    override fun component2(): (VDAEntry) -> Unit {
        TODO("Not yet implemented")
    }
}

class RoboScoutAPI {

    var wsCache: MutableList<WSEntry> = mutableListOf<WSEntry>()
    var vdaCache: MutableList<VDAEntry> = mutableListOf<VDAEntry>()
    var regionsMap: MutableMap<String, Int> = mutableMapOf<String, Int>()
    var importedWS: Boolean = false
    var importedVDA: Boolean = false

    companion object {

        fun roboteventsUrl(): String {
            return "https://www.robotevents.com/api/v2"
        }

        fun roboteventsAccessKey(): String {
            return BuildConfig.ROBOTEVENTS_API_KEY
        }

        fun roboteventsDate(date: String, localize: Boolean): Date? {
            val formatter = SimpleDateFormat()

            try {
                // Example date: "2023-04-26T11:54:40-04:00"
                return if (localize) {
                    formatter.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                    formatter.parse(date)
                } else {
                    formatter.applyPattern("yyyy-MM-dd'T'HH:mm:ss")
                    val split = date.split("-").toMutableList()
                    if (split.isNotEmpty()) {
                        split.removeAt(split.size - 1)
                    }
                    formatter.parse(split.joinToString("-"))
                }
            }
            catch (e: java.text.ParseException) {
                return null
            }
        }

        fun formatDate(date: Date?): String {
            if (date == null) return ""
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
            return outputFormat.format(date)
        }

        suspend fun roboteventsRequest(requestUrl: String, params: Map<String, Any> = emptyMap<String, Any>()): List<JsonObject> {
            var data = mutableListOf<JsonObject>()
            val requestUrl = this.roboteventsUrl() + requestUrl
            var page = 1
            var cont = true
            var params = params.toMutableMap()

            while (cont) {
                try {
                    val response = client.get(requestUrl) {
                        header("Authorization", "Bearer ${RoboScoutAPI.roboteventsAccessKey()}")
                        url {
                            params.forEach { param ->
                                if (param.value is List<*>) {
                                    for (value in param.value as List<*>) {
                                        parameters.append("${param.key}[]", value.toString())
                                    }
                                }
                                else {
                                    parameters.append(param.key, param.value.toString())
                                }
                            }
                            parameters.append("page", page.toString())
                            if (params["per_page"] == null) {
                                parameters.append("per_page", "250")
                            }
                        }
                        println("RobotEvents API request (page ${page}): $url")
                    }

                    val json = Json.parseToJsonElement(response.bodyAsText())

                    page += 1

                    json.jsonObject["data"]!!.jsonArray.forEach { element ->
                        data.add(element.jsonObject)
                    }

                    if (json.jsonObject["meta"]!!.jsonObject["last_page"] == json.jsonObject["meta"]!!.jsonObject["current_page"]) {
                        cont = false
                    }
                }
                catch (e: Exception) {
                    cont = false
                    println("Error: $e")
                    e.printStackTrace()
                }
            }
            return data
        }
    }

     suspend fun updateWorldSkillsCache(season: Int? = null) {

        this.importedWS = false
        this.wsCache.clear()

        try {
            val response = client.get("https://www.robotevents.com/api/seasons/${season ?: 181}/skills") {
                url {
                    parameters.append("grade_level", "High School")
                }
            }

            val json = Json.parseToJsonElement(response.bodyAsText())

            json.jsonArray.forEach { element ->
                val wsEntry: WSEntry = jsonWorker.decodeFromJsonElement(element)
                this.wsCache.add(wsEntry)
            }

            for (item in this.wsCache) {
                regionsMap[item.team.eventRegion.replace("Chinese Taipei", "Taiwan")] = item.team.eventRegionId
            }

            this.importedWS = true
            println("Updated world skills cache")
        }
        catch (e: Exception) {
            println("Failed to update world skills cache, error: $e")
            e.printStackTrace()
        }
    }

     suspend fun updateVDACache(season: Int? = null) {

        this.importedVDA = false
        this.vdaCache.clear()

        try {
            val response = client.get("https://vrc-data-analysis.com/v1/allteams")

            val json = Json.parseToJsonElement(response.bodyAsText())

            json.jsonArray.forEach { element ->
                //println("Element: $element")
                val vdaEntry: VDAEntry = jsonWorker.decodeFromJsonElement(element)
                this.vdaCache.add(vdaEntry)
            }
            this.importedVDA = true
            println("Updated VDA cache")
        }
        catch (e: Exception) {
            println("Failed to update VDA cache, error: $e")
            e.printStackTrace()
        }
    }

     fun worldSkillsFor(team: Team): WSEntry {
        return try {
            this.wsCache.first {
                it.team.id == team.id
            }
        } catch (e: NoSuchElementException) {
            WSEntry()
        }
    }

     fun vdaFor(team: Team): VDAEntry {
        return try {
            this.vdaCache.first {
                it.teamNumber == team.number
            }
        } catch (e: NoSuchElementException) {
            VDAEntry()
        }
    }

}

@Serializable
class Season {
    var id: Int = 0
    var name: String = ""
}

enum class AllianceColor {
    RED, BLUE
}

enum class Round(val value: Int) {
    NONE(0), PRACTICE(1), QUALIFICATION(2), R128(9), R64(8), R32(7), R16(6), QUARTERFINALS(3), SEMIFINALS(4), FINALS(5)
}

@Serializable
data class AllianceMember(
    val team: ShortTeam,
    val sitting: Boolean
)

@Serializable
data class MatchAlliance(
    val color: String,
    @kotlinx.serialization.Transient val allianceColor: AllianceColor = if (color == "red") AllianceColor.RED else AllianceColor.BLUE,
    val score: Int,
    @SerialName("teams") val members: List<AllianceMember>
)

@Serializable
data class ShortEvent(
    val id: Int,
    val name: String,
    val code: String
)

@Serializable
data class Match(
    val id: Int,
    val event: ShortEvent,
    val division: Division,
    val round: Int,
    @kotlinx.serialization.Transient val roundType: Round = Round.entries.find { it.value == round } ?: Round.NONE,
    val instance: Int,
    @SerialName("matchnum") val matchNum: Int,
    val scheduled: String?,
    @kotlinx.serialization.Transient val scheduledDate: Date? = RoboScoutAPI.roboteventsDate(scheduled ?: "", true),
    val started: String?,
    @kotlinx.serialization.Transient val startedDate: Date? = RoboScoutAPI.roboteventsDate(started ?: "", true),
    val field: String?,
    val session: Int,
    val scored: Boolean,
    val name: String,
    @kotlinx.serialization.Transient val shortName: String = name.replace("Qualifier", "Q").replace("Practice", "P").replace("Final", "F").replace("#", ""),
    val alliances: List<MatchAlliance>,
    @kotlinx.serialization.Transient val redAlliance: MatchAlliance = alliances.find { it.color == "red" }!!,
    @kotlinx.serialization.Transient val blueAlliance: MatchAlliance = alliances.find { it.color == "blue" }!!,
    @kotlinx.serialization.Transient val redScore: Int = redAlliance.score,
    @kotlinx.serialization.Transient val blueScore: Int = blueAlliance.score
) {

    fun allianceFor(team: Team): AllianceColor? {
        redAlliance.members.find { it.team.id == team.id }?.let { return AllianceColor.RED }
        blueAlliance.members.find { it.team.id == team.id }?.let { return AllianceColor.BLUE }
        return null
    }

    fun winningAlliance(): AllianceColor? {
        return when {
            redScore > blueScore -> AllianceColor.RED
            blueScore > redScore -> AllianceColor.BLUE
            else -> null
        }
    }

    fun completed(): Boolean {
        return !(started == null || (startedDate ?: Date()).time > Date().time - 300000) && redScore != 0 && blueScore != 0
    }
}

@Serializable
class Division {
    var id: Int = 0
    var name: String = ""
}

@Serializable
data class ShortTeam(
    val id: Int,
    val name: String,
    val code: String?
)

@Serializable
data class SkillsEvent(
    val id: Int,
    val name: String,
    val code: String?
)

@Serializable
data class TeamRanking(
    val id: Int,
    val team: ShortTeam,
    val event: Event,
    val division: Division,
    val rank: Int,
    val wins: Int,
    val losses: Int,
    val ties: Int,
    val wp: Int,
    val ap: Int,
    val sp: Int,
    @SerialName("high_score") val highScore: Int,
    @SerialName("average_points") val averagePoints: Double,
    @SerialName("total_points") val totalPoints: Int
)

@Serializable
data class TeamSkillsEntry(
    val id: Int,
    val event: SkillsEvent,
    val team: ShortTeam,
    val type: String,
    val rank: Int,
    val score: Int,
    val attempts: Int?
)

class TeamSkillsRanking(
    var driverId: Int = 0,
    var programmingId: Int = 0,
    var team: ShortTeam,
    var event: SkillsEvent,
    var rank: Int,
    var combinedScore: Int = 0,
    var driverScore: Int = 0,
    var programmingScore: Int = 0,
    var driverAttempts: Int = 0,
    var programmingAttempts: Int = 0
)

@Serializable
class Event {

    var id: Int = 0
    var sku: String = ""
    var name: String = ""
    var start: String = ""
    @kotlinx.serialization.Transient var startDate: Date? = null
    var end: String = ""
    @kotlinx.serialization.Transient var endDate: Date? = null
    var season: Season = Season()
    var location: Location = Location()
    @kotlinx.serialization.Transient var matches: MutableMap<Division, MutableList<Match>> = mutableMapOf<Division, MutableList<Match>>()
    var teams: MutableList<Team> = mutableListOf<Team>()
    @kotlinx.serialization.Transient var teamIDs: IntArray = intArrayOf()
    @kotlinx.serialization.Transient var teamObjects = ArrayList<Team>()
    var divisions: MutableList<Division> = mutableListOf<Division>()
    var rankings: MutableMap<Division, MutableList<TeamRanking>> = mutableMapOf<Division, MutableList<TeamRanking>>()
    @kotlinx.serialization.Transient var skillsRankings: MutableList<TeamSkillsRanking> = mutableListOf<TeamSkillsRanking>()
    @kotlinx.serialization.Transient var livestreamLink: String? = null

    init {
        this.startDate = RoboScoutAPI.roboteventsDate(this.start, true)
        this.endDate = RoboScoutAPI.roboteventsDate(this.end, true)
    }

    constructor(id: Int = 0, fetch: Boolean = true) {
        this.id = id
        if (fetch) {
            fetchInfo()
        }
    }

    constructor(sku: String, fetch: Boolean = true) {
        this.sku = sku
        if (fetch) {
            fetchInfo()
        }
    }

     fun fetchInfo() {

         if (this.id == 0 && this.sku == "") {
            return
         }

         var res: List<JsonObject>

         runBlocking {
             res = RoboScoutAPI.roboteventsRequest("/events/", if (id != 0) mapOf("id" to id.toString()) else mapOf("sku" to sku))
         }

         if (res.isEmpty()) {
             return
         }

         val event: Event = jsonWorker.decodeFromJsonElement(res[0])

         this.id = event.id
         this.sku = event.sku
         this.name = event.name
         this.start = event.start
         this.startDate = RoboScoutAPI.roboteventsDate(event.start, true)
         this.end = event.end
         this.endDate = RoboScoutAPI.roboteventsDate(event.end, true)
         this.season = event.season
         this.location = event.location
         this.teams = event.teams
         this.teamIDs = event.teamIDs
         this.teamObjects = event.teamObjects
         this.divisions = event.divisions
         // TODO: Add livestream link
    }

    suspend fun fetchTeams() {
        val teams = mutableListOf<Team>()
        val data = RoboScoutAPI.roboteventsRequest("/events/${this.id}/teams")
        for (team in data) {
            val cachedTeam: Team = jsonWorker.decodeFromJsonElement(team)
            teams.add(cachedTeam)
            this.teamIDs += cachedTeam.id
            this.teamObjects.add(cachedTeam)
        }
        this.teams = teams
    }

    fun getTeam(id: Int): Team? {
        return this.teams.find { it.id == id }
    }

    suspend fun fetchRankings(division: Division) {
        val data = RoboScoutAPI.roboteventsRequest("/events/${this.id}/divisions/${division.id}/rankings")
        this.rankings[division] = mutableListOf<TeamRanking>()
        for (ranking in data) {
            val teamRanking: TeamRanking = jsonWorker.decodeFromJsonElement(ranking)
            this.rankings[division]!!.add(teamRanking)
        }
    }

    suspend fun fetchMatches(division: Division) {
        val data = RoboScoutAPI.roboteventsRequest("/events/${this.id}/divisions/${division.id}/matches")
        this.matches[division] = mutableListOf<Match>()
        for (match in data) {
            val fetchedMatch: Match = jsonWorker.decodeFromJsonElement(match)
            this.matches[division]!!.add(fetchedMatch)
        }
        matches[division]?.sortBy { it.instance }
        matches[division]?.sortBy { it.roundType }
    }

    suspend fun fetchSkillsRankings() {
        val data = RoboScoutAPI.roboteventsRequest("/events/${this.id}/skills")
        this.skillsRankings = mutableListOf<TeamSkillsRanking>()
        var index = 0
        while (index < data.size) {
            val teamSkillsEntry1 = jsonWorker.decodeFromString<TeamSkillsEntry>(data[index].toString())
            val bundle = mutableListOf(teamSkillsEntry1)
            if (index + 1 < data.size) {
                val teamSkillsEntry2 = jsonWorker.decodeFromString<TeamSkillsEntry>(data[index + 1].toString())
                if (teamSkillsEntry1.team.id == teamSkillsEntry2.team.id) {
                    bundle.add(teamSkillsEntry2)
                    index++
                }
            }
            val teamSkillsEntry2 = bundle[1]
            val teamSkillsRanking = TeamSkillsRanking(
                driverId = teamSkillsEntry1.id,
                programmingId = teamSkillsEntry2.id,
                team = teamSkillsEntry1.team,
                event = teamSkillsEntry1.event,
                rank = teamSkillsEntry1.rank,
                combinedScore = teamSkillsEntry1.score + teamSkillsEntry2.score,
                driverScore = teamSkillsEntry1.score,
                programmingScore = teamSkillsEntry2.score,
                driverAttempts = teamSkillsEntry1.attempts ?: 0,
                programmingAttempts = teamSkillsEntry2.attempts ?: 0
            )
            this.skillsRankings.add(teamSkillsRanking)
            index++
        }
    }
    companion object {
        fun sortTeamsByNumber(teams: List<Team>): List<Team> {
            // Teams can be:
            // 229V, 4082B, 10C, 2775V, 9364C, 9364A
            // These teams are first sorted by the letter part of their team.number, then by the number part
            // The sorted list for the above teams:
            // 10C, 229V, 2775V, 4082B, 9364A, 9364C
            // Sort by letter part (remove all non-letter characters and sort)
            val sortedTeams = teams.sortedBy { it.number.replace(Regex("[^A-Za-z]"), "") }
            // Sort by number part (remove all non-number characters and sort)
            return sortedTeams.sortedBy { it.number.replace(Regex("[^0-9]"), "").toInt() }
        }
    }

}

@Serializable
class Location {
    var venue: String? = ""
    @SerialName("address_1") var address1: String? = ""
    @SerialName("address_2") var address2: String? = ""
    var city: String? = ""
    var region: String? = ""
    var postcode: String? = ""
    var country: String? = ""

    override fun toString(): String {
        return "${this.city}, ${this.region}, ${
            this.country?.replace(
                "United States",
                "USA"
            )
        }"
    }
}

@Serializable
class Team : MutableState<Team> {

    var id: Int = 0
    var events: MutableList<Event> = mutableListOf<Event>()
    var eventCount: Int = 0
    @SerialName("team_name") var name: String = ""
    var number: String = ""
    var organization: String = ""
    @SerialName("robot_name") var robotName: String? = ""
    var location: Location = Location()
    var grade: String = ""
    var registered: Boolean = false

    constructor(id: Int = 0, fetch: Boolean = true) {
        this.id = id
        if (fetch) {
            this.fetchInfo()
        }
    }

    constructor(number: String, fetch: Boolean = true) {
        this.number = number
        if (fetch) {
            this.fetchInfo()
        }
    }

     fun fetchInfo() {

        var res: List<JsonObject>

        if (this.id != 0) {
            runBlocking {
                res = RoboScoutAPI.roboteventsRequest("/teams/$id", mapOf("program" to 1))
            }
        }
        else if (this.number.isNotEmpty()) {
            runBlocking {
                res = RoboScoutAPI.roboteventsRequest("/teams", mapOf("number" to number, "program" to 1))
            }
        }
        else {
            return
        }

        if (res.isEmpty()) {
            return
        }

        val team: Team = jsonWorker.decodeFromJsonElement(res[0])

        this.id = team.id
        this.name = team.name
        this.number = team.number
        this.organization = team.organization
        this.robotName = team.robotName
        this.location = team.location
        this.grade = team.grade
        this.registered = team.registered
    }

    suspend fun matchesAt(event: Event): List<Match> {
        val data = RoboScoutAPI.roboteventsRequest("/teams/${this.id}/matches", mapOf("event" to event.id))
        val matches = mutableListOf<Match>()
        for (match in data) {
            val fetchedMatch: Match = jsonWorker.decodeFromJsonElement(match)
            matches.add(fetchedMatch)
        }
        matches.sortBy { it.instance }
        matches.sortBy { it.roundType }
        return matches
    }

     suspend fun fetchEvents(season: Int? = null) {
        val data = RoboScoutAPI.roboteventsRequest("/events", mapOf("team" to id, "season" to (season ?: 181)))
        events.clear()
        for (event in data) {
            val fetchedEvent: Event = jsonWorker.decodeFromJsonElement(event)
            events.add(fetchedEvent)
        }
    }

    suspend fun averageQualifiersRanking(season: Int? = null): Double {
        val data = RoboScoutAPI.roboteventsRequest("/teams/${this.id}/rankings/", mapOf("season" to (season ?: 181)))
        var total = 0
        for (comp in data) {
            total += comp["rank"]!!.jsonPrimitive.int
        }
        if (data.isEmpty()) {
            return 0.0
        }
        this.eventCount = data.size
        return total.toDouble() / data.size.toDouble()
    }

    override var value: Team
        get() = this
        set(value) {
            this.id = value.id
            this.events = value.events
            this.eventCount = value.eventCount
            this.name = value.name
            this.number = value.number
            this.organization = value.organization
            this.robotName = value.robotName
            this.location = value.location
            this.grade = value.grade
            this.registered = value.registered
        }

    override fun component1(): Team {
        return this
    }

    override fun component2(): (Team) -> Unit {
        return { this.value = it }
    }

}