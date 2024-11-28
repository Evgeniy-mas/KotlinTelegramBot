package org.example.kotlinTelegramBot

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$API_TELEGRAM$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: String, text: String): String? {
        val encoded = URLEncoder.encode(text, "UTF-8")
        println(encoded)
        val sendMessage = "$API_TELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: String): String? {
        val sendMessage = "$API_TELEGRAM$botToken/sendMessage"
        val sendMenuBody = """
             {"chat_id":$chatId,
             "text": "Основное меню",
             "reply_markup": {
             "inline_keyboard": [
             [
             {
             "text": "Изучить слова",
             "callback_data": "learn_words_clicked"
             },
             {
             "text": "Статистика",
             "callback_data": "statistics_clicked"
            }
          ]
        ]
      }           
       }
            
        """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}

const val API_TELEGRAM = "https://api.telegram.org/bot"