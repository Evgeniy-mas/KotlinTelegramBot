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
             "callback_data": "$LEARN_WORDS_CLICKED"
             },
             {
             "text": "Статистика",
             "callback_data": "$STATISTICS_BUTTON"
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


    fun sendQuestion(question: Question, chatId: String): String? {
val index = question.variants.mapIndexed { index, _ ->
    CALLBACK_DATA_ANSWER_PREFIX + index
}
        val sendMessage = "$API_TELEGRAM$botToken/sendMessage"
        val sendQuestion = """
            {"chat_id":$chatId,
            "text": "${question.correctAnswer.original}",
            "reply_markup": {"inline_keyboard": 
            [
            [
             {
             "text": "${question.variants.map { it.translate }[0]}",
             "callback_data": "$index"
             },
             {
             "text": "${question.variants.map { it.translate }[1]}",
             "callback_data": "$index"
             }
             ],
             [{
             "text": "${question.variants.map { it.translate }[2]}",
             "callback_data": "$index"
             },
             {
             "text": "${question.variants.map { it.translate }[3]}",
             "callback_data": "$index"
             }
           ]
         ]
       }            
     }

""".trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestion))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}











const val API_TELEGRAM = "https://api.telegram.org/bot"
const val STATISTICS_BUTTON = "statistics_clicked"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"