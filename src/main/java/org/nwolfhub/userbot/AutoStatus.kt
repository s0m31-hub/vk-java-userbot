package org.nwolfhub.userbot

import com.google.gson.JsonParser
import org.nwolfhub.vk.requests.Request
import org.nwolfhub.vk.requests.StatusSet
import org.nwolfhub.vk.requests.UsersGet
import org.nwolfhub.vkUser.Vk
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class AutoStatus {
    companion object {
        public fun init(vk: Vk) {
            val context = AnnotationConfigApplicationContext(Configurator::class.java)
            val phrasesArr = context.getBean("statuses") as Array<*>
            if(phrasesArr.isNotEmpty()) {
                Thread { work(vk, phrasesArr as Array<String>) }.start()
            }
        }

        private fun changeVars(src:String, vk: Vk):String {
            var toReturn = src;
            toReturn.replace("{followers}", "{subscribers}")
            var response = ""
            if(toReturn.contains("{friends}") || toReturn.contains("{subscribers}")) {
                response = vk.makeRequest(Request("users.get", "fields=counters"))
            }
            //time
            toReturn = toReturn.replace("{time}", SimpleDateFormat("hh:mm").format(Date(System.currentTimeMillis())))
            //date
            toReturn = toReturn.replace("{date}", SimpleDateFormat(toReturn.split("{date}{pattern ")[1].split("}")[0]).format(Date(System.currentTimeMillis())))
            //friends
            var friends = 0
            if(toReturn.contains("{friends}")) {
                friends = JsonParser.parseString(response).asJsonObject.get("response").asJsonArray[0].asJsonObject.get("counters").asJsonObject.get("friends").asInt
            }
            toReturn = toReturn.replace("{friends}", friends.toString())
            //subs
            toReturn = toReturn.replace("{subscribers}", JsonParser.parseString(response).asJsonObject.get("response").asJsonArray[0].asJsonObject.get("counters").asJsonObject.get("followers").asInt.toString())
            return toReturn
        }

        private fun work(vk: Vk, phrases: Array<String>) {
            val r = Random
            while (true) {
                //phrases[r.nextInt(phrases.size)]
                vk.makeRequest(StatusSet(changeVars(phrases[r.nextInt(phrases.size)], vk)))
                Thread.sleep(60000)
            }
        }
    }
}