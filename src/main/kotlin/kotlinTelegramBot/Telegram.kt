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
    val trainers = HashMap<Long, LearnWordsTrainer>()

    val telegram = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)

        val responseString: String = telegram.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handelUpdate(it, telegram, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
    ) {

    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId, "Все слова выучены!")
    } else {
        telegramBotService.sendQuestion(question, chatId)
    }
}

fun handelUpdate(
    update: Update, telegram: TelegramBotService,
    trainers: HashMap<Long, LearnWordsTrainer>
) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message == "Hello".lowercase()) {
        telegram.sendMessage(chatId, "Hello")
    }
    if (message == "/start".lowercase()) {
        telegram.sendMenu(chatId)
    }
    if (data?.lowercase() == STATISTICS_BUTTON) {
        val statistic = trainer.getStatistics()
        telegram.sendMessage(
            chatId,
            "Выбран пункт: Статистика\n" +
                    "Выучено ${statistic.learned} слов из ${statistic.total}| ${statistic.percent}%\n",
            )
    }
    if (data?.lowercase() == LEARN_WORDS_CLICKED) {
        checkNextQuestionAndSend(trainer, telegram, chatId)
    } else if (data?.lowercase() == BACK_TO_MENU) {
        telegram.sendMenu(chatId)
    }

    if (data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val userAnswerIndex = data.substringAfter("_").toInt()
        if (trainer.checkAnswer(userAnswerIndex)) {
            telegram.sendMessage(chatId, "Правильно!")
            checkNextQuestionAndSend(trainer, telegram, chatId)
        } else {
            val questionOriginal = trainer.question?.correctAnswer?.original
            val correctAnswer = trainer.question?.correctAnswer?.translate
            telegram.sendMessage(
                chatId,
                "Неправильно! " +
                        "$questionOriginal это $correctAnswer.",

                )
            checkNextQuestionAndSend(trainer, telegram, chatId)
        }
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        telegram.sendMessage(chatId, "Прогресс сброшен!")
    }
}



