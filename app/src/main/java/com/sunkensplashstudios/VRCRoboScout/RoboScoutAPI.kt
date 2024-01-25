package com.sunkensplashstudios.VRCRoboScout

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val dotenv = dotenv {
    directory = "/assets"
    filename = "env"
}
val API = RoboScoutAPI()

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

            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }

            while (cont) {

                params.put("page", page)

                println("RobotEvents API request (page ${page}): $request_url")

                val response = client.get(request_url) {
                    header("Authorization", "Bearer ${RoboScoutAPI.roboteventsAccessKey()}")
                    url {
                        if (params.get("per_page") == null) {
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
data class Team(val id: Int, val number: String)