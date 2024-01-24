package com.sunkensplashstudios.VRCRoboScout

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header

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
            return ""
        }

        public final suspend fun roboteventsRequest(requestUrl: String, params: Map<String, Any> = emptyMap<String, Any>()): Map<String, Any> {
            var data: MutableMap<String, Any> = mutableMapOf<String, Any>()
            var request_url = this.roboteventsUrl() + requestUrl
            var page = 1
            var cont = true
            var params = params.toMutableMap()

            val client = HttpClient(CIO)

            while (cont) {

                params.put("page", page)

                println("RobotEvents API request (page ${page}): $request_url")

                val response = client.get(request_url) {
                    header("Authorization", "Bearer ${BuildConfig.ROBOTEVENTS_API_KEY}")
                    url {
                        if (params.get("per_page") == null) {
                            parameters.append("per_page", "250")
                            parameters.append("page", page.toString())
                        }
                    }
                }

                println(response.status)

                cont = false

            }

            return emptyMap<String, Any>()
        }

    }

}