package com.sunkensplashstudios.VRCRoboScout

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.utils.io.printStack
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.util.Date

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

class RoboScoutAPI {

    init {

    }

    companion object {

        public final fun roboteventsUrl(): String {
            return "https://www.robotevents.com/api/v2"
        }

        public final fun roboteventsAccessKey(): String {
            return BuildConfig.ROBOTEVENTS_API_KEY
        }

        public final suspend fun roboteventsRequest(requestUrl: String, params: Map<String, Any> = emptyMap<String, Any>()): List<JsonObject> {
            var data = mutableListOf<JsonObject>()
            var request_url = this.roboteventsUrl() + requestUrl
            var page = 1
            var cont = true
            var params = params.toMutableMap()

            while (cont) {

                try {
                    val response = client.get(request_url) {
                        header("Authorization", "Bearer ${RoboScoutAPI.roboteventsAccessKey()}")
                        url {
                            params.forEach { param ->
                                parameters.append(param.key, param.value.toString())
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

}

@Serializable
class Season {
    var id: Int = 0
    var name: String = ""
}

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
    var venue: String = ""
    var address: String = ""
    var city: String = ""
    var region: String = ""
    var postcode: Int = 0
    var country: String = ""
    var teams: List<Team> = listOf(Team())
    @kotlinx.serialization.Transient var teams_map: Map<Int, Team> = emptyMap<Int, Team>()
    @kotlinx.serialization.Transient var livestream_link: String? = null

    constructor(id: Int = 0, fetch: Boolean = true) {
        this.id = id
        if (fetch) {

        }
    }

    constructor(sku: String, fetch: Boolean = true) {
        this.sku = sku
        if (fetch) {
            fetchInfo()
        }
    }

    public fun fetchInfo() {

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

        var id: Int = 0
        this.id = event.id
        var sku: String = ""
        this.sku = event.sku
        var name: String = ""
        this.name = event.name
        var start: String = ""
        this.start = event.start
        // TODO: this.startDate
        var end: String = ""
        this.end = event.end
        // TODO: this.endDate
        var season: Int = 0
        this.season = event.season
        var venue: String = ""
        this.venue = event.venue
        var address: String = ""
        this.address = event.address
        var city: String = ""
        this.city = event.city
        var region: String = ""
        this.region = event.region
        var postcode: Int = 0
        this.postcode = event.postcode
        var country: String = ""
        this.country = event.country
        var teams: List<Team> = listOf(Team())
        this.teams = event.teams
        // TODO: this.teams_map
        this.teams_map = event.teams_map
        // TODO: this.livestream_link
    }

}

@Serializable
class Team {

    var id: Int = 0
    var events: MutableList<Event> = mutableListOf<Event>()
    var event_count: Int = 0
    var team_name: String = ""
    var number: String = ""
    var organization: String = ""
    var robot_name: String = ""
    var city: String = ""
    var region: String = ""
    var country: String = ""
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

    public fun fetchInfo() {

        var res: List<JsonObject>

        if (this.id != 0) {
            runBlocking {
                res = RoboScoutAPI.roboteventsRequest("/teams/$id")
            }
        }
        else if (this.number.isNotEmpty()) {
            runBlocking {
                res = RoboScoutAPI.roboteventsRequest("/teams", mapOf("number" to number))
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
        this.team_name = team.team_name
        this.number = team.number
        this.organization = team.organization
        this.robot_name = team.robot_name
        this.city = team.city
        this.region = team.region
        this.country = team.country
        this.grade = team.grade
        this.registered = team.registered
    }

    public fun fetchEvents(season: Int? = null) {
        var res: List<JsonObject>

        runBlocking {
            res = RoboScoutAPI.roboteventsRequest("/events", mapOf("team" to id, "season" to (season ?: 181)))
        }

        for (event in res) {
            val fetchedEvent: Event = jsonWorker.decodeFromJsonElement(event)
            events.add(fetchedEvent)
        }
    }

}