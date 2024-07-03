package com.sunkensplashstudios.VRCRoboScout

import androidx.compose.runtime.MutableState
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
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
import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.simple.SimpleMatrix
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

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

class RoboScoutAPIError: Throwable() {
    companion object {
        fun missingData(message: String): Exception {
            return Exception("Missing data: $message")
        }
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

    var wsCache: CopyOnWriteArrayList<WSEntry> = CopyOnWriteArrayList<WSEntry>()
    var vdaCache: CopyOnWriteArrayList<VDAEntry> = CopyOnWriteArrayList<VDAEntry>()
    var regionsMap: MutableMap<String, Int> = mutableMapOf<String, Int>()
    var importedWS: Boolean = false
    var importedVDA: Boolean = false
    var seasonsCache: List<MutableList<Season>> = listOf()
    var selectedSeasonId: Int = BuildConfig.DEFAULT_V5_SEASON_ID
    var gradeLevel: String = "High School"

    companion object {

        fun roboteventsUrl(): String {
            return "https://www.robotevents.com/api/v2"
        }

        fun roboteventsAccessKey(): String {
            if (BuildConfig.DEBUG) {
                return BuildConfig.ROBOTEVENTS_API_KEY
            }
            else {
                val key = (0..9).random()
                return when (key) {
                    0 -> BuildConfig.key0
                    1 -> BuildConfig.key1
                    2 -> BuildConfig.key2
                    3 -> BuildConfig.key3
                    4 -> BuildConfig.key4
                    5 -> BuildConfig.key5
                    6 -> BuildConfig.key6
                    7 -> BuildConfig.key7
                    8 -> BuildConfig.key8
                    9 -> BuildConfig.key9
                    else -> ""
                }
            }
        }

        fun roboteventsDate(date: String, localize: Boolean): Date? {
            try {
                val formatter = SimpleDateFormat()
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
            try {
                if (date == null) return ""
                val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                return outputFormat.format(date)
            }
            catch (e: java.text.ParseException) {
                println("Could not format date: $e")
                return ""
            }
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
                            params["program"] = listOf(1, 4)
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

        suspend fun roboteventsCompetitionScraper(params: MutableMap<String, Any> = mutableMapOf()): List<String> {
            var params = params

            println(params)

            if (!params.containsKey("page")) {
                params["page"] = 1
            }

            if (!params.containsKey("country_id")) {
                params["country_id"] = "*"
            }

            when (params["level_class_id"] as? Int) {
                4 -> params["level_class_id"] = 9
                5 -> params["level_class_id"] = 12
                6 -> params["level_class_id"] = 13
            }

            val competition = if (API.seasonsCache[0].find { it.id == params["seasonId"]} != null) "vex-robotics-competition" else "college-competition"

            val requestUrl = "https://www.robotevents.com/robot-competitions/$competition"

            val skuArray = mutableListOf<String>()

            val response = client.get(requestUrl) {
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
                }
                headers {
                    append("User-Agent", "Bond, James Bond")
                }
            }

            println("RobotEvents Scraper (page ${params["page"] as? Int ?: 0}): ${response.call.request.url}")
            //println(response.bodyAsText())

            val pattern = "$requestUrl/RE-[A-Z0-9]*([+-]?(?=\\.\\d|\\d)(?:\\d+)?\\.?\\d*)(?:[Ee]([+-]?\\d+))?([+-]?(?=\\.\\d|\\d)(?:\\d+)?\\.?\\d*)(?:[Ee]([+-]?\\d+))?\\.html"
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val matches = regex.findAll(response.bodyAsText())

            for (match in matches) {
                skuArray.add(match.value.replace("$requestUrl/", "").replace(".html", ""))
            }
            println("Matches: $skuArray")

            return skuArray
        }
    }

    suspend fun generateseasonsCache() {
        this.seasonsCache = listOf(mutableListOf(), mutableListOf())
        val data = roboteventsRequest("/seasons/")

        for (seasonData in data) {
            val season = jsonWorker.decodeFromJsonElement<Season>(seasonData)
            val gradeLevelIndex = if (season.program.id == 1) 0 else if (season.program.id == 4) 1 else -1
            if (gradeLevelIndex != -1) {
                this.seasonsCache[gradeLevelIndex].add(season)
            }
        }

        println("Season ID map generated")
        /*for (gradeLevel in this.seasonsCache) {
            for (season in gradeLevel) {
                println("ID: ${season.id}, Name: ${season.name}")
            }
        }*/
    }

    fun selectedProgramId(): Int {
        return if (this.gradeLevel == "College") 4 else 1
    }

    fun selectedSeasonId(): Int {
        return this.selectedSeasonId
    }

    fun activeSeasonId(): Int {
        return if (this.seasonsCache.isNotEmpty()) {
            if (this.gradeLevel != "College") {
                try {
                    this.seasonsCache[0].first().id
                }
                catch (e: NoSuchElementException) {
                    BuildConfig.DEFAULT_V5_SEASON_ID
                }
            }
            else {
                try {
                    this.seasonsCache[1].first().id
                }
                catch (e: NoSuchElementException) {
                    BuildConfig.DEFAULT_VU_SEASON_ID
                }
            }
        }
        else {
            if (this.gradeLevel != "College") {
                BuildConfig.DEFAULT_V5_SEASON_ID
            }
            else {
                BuildConfig.DEFAULT_VU_SEASON_ID
            }
        }
    }

    suspend fun updateWorldSkillsCache(season: Int? = null) {

        this.importedWS = false
        this.wsCache.clear()

        try {
            val response = client.get("https://www.robotevents.com/api/seasons/${season ?: API.selectedSeasonId()}/skills") {
                url {
                    parameters.append("grade_level", gradeLevel)
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

    suspend fun vdaFor(team: Team, fetchRobotEventsMatchStatistics: Boolean = false): VDAEntry {
        val vda = try {
            this.vdaCache.first {
                it.teamNumber == team.number
            }
        } catch (e: NoSuchElementException) {
            VDAEntry()
        }
        if (fetchRobotEventsMatchStatistics) {
            var totalWins = 0
            var totalLosses = 0
            var totalTies = 0
            var totalAP = 0
            var totalWP = 0

            val seasonIndex = API.seasonsCache[if (API.selectedProgramId() == 4) 1 else 0].indexOfFirst { it.id == API.selectedSeasonId() }
            val season = API.seasonsCache[if (team.grade == "College") 1 else 0][seasonIndex]

            val reRankingsData = roboteventsRequest("/teams/${team.id}/rankings", mapOf("season" to season.id))
            val reRankings = reRankingsData.map { jsonWorker.decodeFromJsonElement<TeamRanking>(it) }
            for (eventRankings in reRankings) {
                totalWins += eventRankings.wins
                totalLosses += eventRankings.losses
                totalTies += eventRankings.ties
                totalAP += eventRankings.ap
                totalWP += eventRankings.wp
            }

            val matches = team.matchesForSeason(season.id)
            for (match in matches.filterNot { listOf(Round.PRACTICE, Round.QUALIFICATION).contains(it.roundType) }) {
                if (match.winningAlliance() == match.allianceFor(team)) {
                    totalWins += 1
                } else if (match.winningAlliance() != null) {
                    totalLosses += 1
                } else {
                    totalTies += 1
                }
            }

            vda.totalWins = totalWins.toDouble()
            vda.totalLosses = totalLosses.toDouble()
            vda.totalTies = totalTies.toDouble()
            vda.totalMatches = (totalWins + totalLosses + totalTies).toDouble()
            vda.totalWinningPercent = (totalWins / vda.totalMatches) * 100
            vda.apPerMatch = totalAP / vda.totalMatches
            vda.wpPerMatch = totalWP / vda.totalMatches
            vda.awpPerMatch = (totalWP - 2 * totalWins - totalTies) / vda.totalMatches
        }
        return vda
    }

}

@Serializable
class Program {
    var id: Int = 0
    var name: String = ""
    var code: String = ""
}

@Serializable
class Season {
    var id: Int = 0
    var name: String = ""
    @kotlinx.serialization.Transient var shortName: String = name.replace("VRC ", "").replace("V5RC ", "").replace("VEXU ", "").replace("VURC ", "")
    var program: Program = Program()
    var start: String = ""
    var end: String = ""
    @kotlinx.serialization.Transient var startDate: Date? = RoboScoutAPI.roboteventsDate(start, true)
    @kotlinx.serialization.Transient var endDate: Date? = RoboScoutAPI.roboteventsDate(end, true)
}

@Serializable
class ShortSeason {
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
) {
    operator fun get(i: Int): AllianceMember {
        return members[i]
    }
}

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
        return (this.redScore != 0 || this.blueScore != 0) || (this.startedDate != null && (startedDate.time < Date().time - 300000) && this.redScore == 0 && this.blueScore == 0)
    }
}

@Serializable
data class TeamWinner(
    val division: Division,
    val team: ShortTeam
)
@Serializable
data class Award(
    val id: Int,
    val event: ShortEvent,
    val order: Int,
    var title: String,
    val qualifications: List<String>,
    val designation: String?,
    val classification: String?,
    val teamWinners: List<TeamWinner>,
    val individualWinners: List<String>
) {
    init {
        if (!this.title.contains("(WC)")) {
            this.title = title.replace("\\([^()]*\\)".toRegex(), "")
        }
    }
}

@Serializable
class Division(
    var id: Int? = 0,
    var name: String = ""
)

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
    @SerialName("high_score") val highScore: Int?,
    @SerialName("average_points") val averagePoints: Double?,
    @SerialName("total_points") val totalPoints: Int?
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

data class TeamPerformanceRatings(
    val team: Team,
    val event: Event,
    val opr: Double,
    val dpr: Double,
    val ccwm: Double
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
    var season: ShortSeason = ShortSeason()
    var program: Program = Program()
    var location: Location = Location()
    @kotlinx.serialization.Transient var matches: MutableMap<Division, MutableList<Match>> = mutableMapOf<Division, MutableList<Match>>()
    var teams: MutableList<Team> = mutableListOf<Team>()
    @kotlinx.serialization.Transient var teamIDs: IntArray = intArrayOf()
    @kotlinx.serialization.Transient var teamObjects = ArrayList<Team>()
    @kotlinx.serialization.Transient var teamPerformanceRatings: MutableMap<Division, MutableMap<Int, TeamPerformanceRatings>> = mutableMapOf<Division, MutableMap<Int, TeamPerformanceRatings>>()
    var divisions: MutableList<Division> = mutableListOf<Division>()
    @kotlinx.serialization.Transient var rankings: MutableMap<Division, MutableList<TeamRanking>> = mutableMapOf<Division, MutableList<TeamRanking>>()
    @kotlinx.serialization.Transient var skillsRankings: MutableList<TeamSkillsRanking> = mutableListOf<TeamSkillsRanking>()
    @kotlinx.serialization.Transient var awards: MutableMap<Division, MutableList<Award>> = mutableMapOf<Division, MutableList<Award>>()
    @kotlinx.serialization.Transient var livestreamLink: String? = null

    init {
        try {
            this.startDate = RoboScoutAPI.roboteventsDate(this.start, true)
            this.endDate = RoboScoutAPI.roboteventsDate(this.end, true)
        }
        catch (_: java.text.ParseException) { }
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

    @Throws(RoboScoutAPIError::class)
    suspend fun calculateTeamPerformanceRatings(division: Division) {
        this.teamPerformanceRatings[division] = mutableMapOf<Int, TeamPerformanceRatings>()

        if (this.teams.isEmpty()) {
            this.fetchTeams()
        }

        if (this.matches[division] == null || this.matches[division]!!.isEmpty()) {
            this.fetchMatches(division)
        }

        var m = arrayOf(doubleArrayOf())
        var scores = arrayOf(doubleArrayOf())
        var oppScores = arrayOf(doubleArrayOf())

        val divisionTeams = mutableListOf<Team>()

        if (!this.matches.keys.contains(division)) {
            this.matches[division] = mutableListOf()
        }

        if (this.matches[division]!!.isEmpty()) {
            return
        }

        val addedTeams = mutableListOf<Int>()
        for (match in this.matches[division]!!) {
            val matchTeams = mutableListOf<Team>()
            matchTeams.addAll(match.redAlliance.members.map { allianceMember ->
                this.getTeam(allianceMember.team.id) ?: Team()
            })
            matchTeams.addAll(match.blueAlliance.members.map { allianceMember ->
                this.getTeam(allianceMember.team.id) ?: Team()
            })
            for (team in matchTeams) {
                if (!addedTeams.contains(team.id) && this.getTeam(id = team.id) != null) {
                    divisionTeams.add(this.getTeam(id = team.id)!!)
                }
                addedTeams.add(team.id)
            }
        }

        for (match in this.matches[division]!!) {

            if (match.round != Round.QUALIFICATION.value) {
                continue
            }
            if (false/* && (UserSettings.getPerformanceRatingsCalculationOption() != "via" && !match.completed())*/) {
                continue
            }

            val red = mutableListOf<Double>()
            val blue = mutableListOf<Double>()

            for (team in divisionTeams) {
                if (match.redAlliance[0].team.id == team.id || match.redAlliance[1].team.id == team.id) {
                    red.add(1.0)
                } else {
                    red.add(0.0)
                }
                if (match.blueAlliance[0].team.id == team.id || match.blueAlliance[1].team.id == team.id) {
                    blue.add(1.0)
                } else {
                    blue.add(0.0)
                }
            }
            m += red.toDoubleArray()
            m += blue.toDoubleArray()
            scores += doubleArrayOf(match.redScore.toDouble())
            scores += doubleArrayOf(match.blueScore.toDouble())
            oppScores += doubleArrayOf(match.blueScore.toDouble())
            oppScores += doubleArrayOf(match.redScore.toDouble())
        }

        if (m.isEmpty() || scores.isEmpty() || oppScores.isEmpty()) {
            throw RoboScoutAPIError.missingData("matches")
        }

        m = m.drop(1).toTypedArray()
        scores = scores.drop(1).toTypedArray()
        oppScores = oppScores.drop(1).toTypedArray()

        val mM = SimpleMatrix(m)
        val mScores = SimpleMatrix(scores)
        val mOppScores = SimpleMatrix(oppScores)

        val result = DMatrixRMaj(mM.numRows(), mM.numCols())
        CommonOps_DDRM.pinv(mM.ddrm, result)
        val pinv = SimpleMatrix(result)

        val mOPRs = pinv.mult(mScores)
        val mDPRs = pinv.mult(mOppScores)

        fun convertToList(matrix: SimpleMatrix): List<Double> {
            val list = mutableListOf<Double>()
            for (i in 0 until matrix.numRows()) {
                list.add(matrix[i, 0])
            }
            return list
        }

        val OPRs = convertToList(mOPRs)
        val DPRs = convertToList(mDPRs)

        var i = 0
        for (team in divisionTeams) {
            this.teamPerformanceRatings[division]!![team.id] = TeamPerformanceRatings(team, this, OPRs[i], DPRs[i], OPRs[i] - DPRs[i])
            i += 1
        }
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

    suspend fun fetchAwards(division: Division) {
        val data = RoboScoutAPI.roboteventsRequest("/events/${this.id}/awards")
        this.awards[division] = mutableListOf<Award>()
        for (award in data) {
            val fetchedAward: Award = jsonWorker.decodeFromJsonElement(award)
            this.awards[division]!!.add(fetchedAward)
        }
        this.awards[division] = this.awards[division]!!.sortedBy { it.order }.toMutableList()
    }

    companion object {
        fun sortTeamsByNumber(teams: List<Team>, gradeLevel: String): List<Team> {
            if (gradeLevel != "College") {
                // Teams can be:
                // 229V, 4082B, 10C, 2775V, 9364C, 9364A
                // These teams are first sorted by the letter part of their team.number, then by the number part
                // The sorted list for the above teams:
                // 10C, 229V, 2775V, 4082B, 9364A, 9364C
                // Sort by letter part (remove all non-letter characters and sort)
                val sortedTeams = teams.sortedBy { it.number.replace(Regex("[^A-Za-z]"), "") }
                // Sort by number part (remove all non-number characters and sort)
                return sortedTeams.sortedBy { it.number.replace(Regex("[^0-9]"), "").toIntOrNull() }
            }
            else {
                // Teams can be:
                // UCF, GATR1, GATR2, BLRS2, PYRO
                // These teams are first sorted by the number part of their team.number, then by the letter part
                // The sorted list for the above teams:
                // BLRS2, GATR1, GATR2, PYRO, UCF
                // Sort by number part (remove all non-number characters and sort)
                // If there is no number part, the team should go above all teams with the same letter part that have a number part
                val sortedTeams = teams.sortedBy { it.number.replace(Regex("[^0-9]"), "").toIntOrNull() }
                // Sort by letter part (remove all non-letter characters and sort)
                return sortedTeams.sortedBy { it.number.replace(Regex("[^A-Za-z]"), "") }
            }
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
        return "${this.city}, ${if (this.region != null) this.region + ", " else ""}${
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
    @kotlinx.serialization.Transient var awards: MutableList<Award> = mutableListOf<Award>()
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
                res = RoboScoutAPI.roboteventsRequest("/teams/$id")
            }
        }
        else if (this.number.isNotEmpty()) {
            runBlocking {
                res = RoboScoutAPI.roboteventsRequest("/teams", mapOf("number" to number, "grade" to listOf("Middle School", "High School", "College")))
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

    suspend fun matchesForSeason(season: Int): List<Match> {
        val data = RoboScoutAPI.roboteventsRequest("/teams/${this.id}/matches", mapOf("season" to season))
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
        val data: List<JsonObject>
        if (season == null) {
            val seasonIndex = API.seasonsCache[if (API.selectedProgramId() == 4) 1 else 0].indexOfFirst { it.id == API.selectedSeasonId() }
            data = RoboScoutAPI.roboteventsRequest("/events", mapOf("team" to id, "season" to (API.seasonsCache[if (this.grade == "College") 1 else 0][seasonIndex].id)))
        }
        else {
            data = RoboScoutAPI.roboteventsRequest("/events", mapOf("team" to id, "season" to season))
        }
        events.clear()
        for (event in data) {
            val fetchedEvent: Event = jsonWorker.decodeFromJsonElement(event)
            events.add(fetchedEvent)
        }
    }

    suspend fun fetchAwards(season: Int? = null) {
        val data = RoboScoutAPI.roboteventsRequest("/teams/${this.id}/awards", mapOf("season" to (season ?: API.selectedSeasonId())))
        awards.clear()
        for (award in data) {
            val fetchedAward: Award = jsonWorker.decodeFromJsonElement(award)
            awards.add(fetchedAward)
        }
        this.awards.sortBy { it.order }
    }

    suspend fun averageQualifiersRanking(season: Int? = null): Double {
        val data = RoboScoutAPI.roboteventsRequest("/teams/${this.id}/rankings/", mapOf("season" to (season ?: API.selectedSeasonId())))
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