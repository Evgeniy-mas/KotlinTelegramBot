package org.example.kotlinTelegramBot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = LearnWordsTrainer()
    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val telegram = TelegramBotService(botToken)
        val responseString: String = telegram.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (message == "Hello".lowercase()) {
            telegram.sendMessage(chatId, "Hello", json)
        }
        if (message == "/start".lowercase()) {
            telegram.sendMenu(json, chatId)
        }
        if (data?.lowercase() == STATISTICS_BUTTON) {
            val statistic = trainer.getStatistics()
            telegram.sendMessage(
                chatId, "Выбран пункт: Статистика\n" +
                        "Выучено ${statistic.learned} слов из ${statistic.total}| ${statistic.percent}%\n",
                json
            )
        }
        if (data?.lowercase() == LEARN_WORDS_CLICKED) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId, json)
        } else if (data?.lowercase() == BACK_TO_MENU) {
            telegram.sendMenu(json, chatId)
        }

        if (data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter("_").toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(chatId, "Правильно!", json)
                checkNextQuestionAndSend(trainer, telegramBotService, chatId, json)
            } else {
                val questionOriginal = trainer.question?.correctAnswer?.original
                val correctAnswer = trainer.question?.correctAnswer?.translate
                telegramBotService.sendMessage(
                    chatId, "Неправильно! " +
                            "$questionOriginal это $correctAnswer.",
                    json
                )
                checkNextQuestionAndSend(trainer, telegramBotService, chatId, json)
            }
        }
    }
}

fun checkNextQuestionAndSend(

    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
    json: Json,
) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId, "Все слова выучены!", json)
    } else {
        telegramBotService.sendQuestion(json, question, chatId)
    }
}



