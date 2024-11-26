package org.example.kotlinTelegramBot

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val messageAnswerRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()

    while (true) {
        Thread.sleep(2000)
        val telegram = TelegramBotService()
        val updates = telegram.getUpdates(botToken, updateId)
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

        if (text == "Hello".lowercase()) {
            telegram.sendMessage(chatId, "Hello", botToken)
        }
    }
}


