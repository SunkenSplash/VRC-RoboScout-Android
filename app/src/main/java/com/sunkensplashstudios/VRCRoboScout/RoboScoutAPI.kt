package com.sunkensplashstudios.VRCRoboScout

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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

                params.put("page", page)

                println("RobotEvents API request (page ${page}): $request_url")

                val response = client.get(request_url) {
                    header("Authorization", "Bearer ${RoboScoutAPI.roboteventsAccessKey()}")
                    url {
                        if (params["per_page"] == null) {
                            parameters.append("per_page", "250")
                            parameters.append("page", page.toString())
                        }
                    }
                }

                val json = Json.parseToJsonElement(response.bodyAsText())

                json.jsonObject["data"]!!.jsonArray.forEach{ element ->
                    data.add(element.jsonObject)
                }

                cont = false

            }

            return data

        }

    }

}

@Serializable
class Team {

    var id: Int = 0
    var team_name: String = ""
    var number: String = ""
    var organization: String = ""
    var robot_name: String = ""
    var city: String = ""
    var region: String = ""
    var country: String = ""
    var grade: String = ""
    var registered: Boolean = false

    private fun update(team: Team) {
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

    constructor(id: Int) {
        if (id != 0) {
            runBlocking {
                val res = RoboScoutAPI.roboteventsRequest("/teams/$id")
                if (res.isEmpty()) return@runBlocking
                val team: Team = jsonWorker.decodeFromJsonElement(res[0])
                update(team)
            }
        }
    }

    constructor(number: String) {
        if (number.isNotEmpty()) {
            runBlocking {
                val res = RoboScoutAPI.roboteventsRequest("/teams/?number=$number")
                if (res.isEmpty()) return@runBlocking
                val team: Team = jsonWorker.decodeFromJsonElement(res[0])
                update(team)
            }
        }
    }
}