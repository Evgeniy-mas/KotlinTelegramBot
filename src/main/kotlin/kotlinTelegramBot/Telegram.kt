package org.example.kotlinTelegramBot

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val messageAnswerRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    val trainer = LearnWordsTrainer()
    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val telegram = TelegramBotService(botToken)
        val updates = telegram.getUpdates(updateId)
        println(updates)

        val matchResultId: MatchResult? = updateIdRegex.find(updates)
        val updateString = matchResultId?.groups?.get(1)?.value ?: continue
        updateId = updateString.toInt() + 1

        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)

        val matchResultMessage: MatchResult? = messageAnswerRegex.find(updates)
        val chatId = matchResultMessage?.groups?.get(1)?.value ?: continue

        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (text == "Hello".lowercase()) {
            telegram.sendMessage(chatId, "Hello")
        }
        if (text == "/start".lowercase()) {
            telegram.sendMenu(chatId)
        }
        if (data?.lowercase() == STATISTICS_BUTTON) {
            val statistic = trainer.getStatistics()
            telegram.sendMessage(
                chatId, "Выбран пункт: Статистика\n" +
                        "Выучено ${statistic.learned} слов из ${statistic.total}| ${statistic.percent}%\n"
            )
        }
        if (data?.lowercase() == LEARN_WORDS_CLICKED) {
            checkNextQuestionAndSend(trainer, telegramBotService, chatId)
        }

    }
}






fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: String, )
{
    val question = trainer.getNextQuestion()
    if (question == null) {
        telegramBotService.sendMessage(chatId,"Все слова выучены!")
    }
    else {
        telegramBotService.sendQuestion(question,chatId)}
    }



