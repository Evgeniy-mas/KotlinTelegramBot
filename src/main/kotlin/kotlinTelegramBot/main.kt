package org.example.kotlinTelegramBot


data class Word(val original: String, val translate: String, var correctAnswersCount: Int = 0)


fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + variants + "\n 0 - выйти в меню"
}

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {
        println("Меню:")
        println("1 - Учить слова, 2 - Статистика, 0 - Выход")

        val userChoice = readln().toIntOrNull()

        when (userChoice) {
            1 -> while (true) {
                val question = trainer.getNextQuestion()
                if (question == null) {
                    println("Все слова выучены!")
                    break
                } else {
                    println(question.asConsoleString())
                }
                val userAnswerInput = readln().toIntOrNull()
                if (userAnswerInput == 0) break

                if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                    println("Правильно\n")
                } else {
                    println("Неправильно! ${question.correctAnswer.original} " +
                            "это ${question.correctAnswer.translate}\\n\"")
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()

                println(
                    "Выбран пункт: Статистика\n" +
                            "Выучено ${statistics.wordLearned} из ${statistics.totalCount}| ${statistics.percent}%\n"
                )
            }

            0 -> return
            else -> {
                println("Введите число 1, 2 или 0:")
            }
        }
    }
}

const val CORRECT_ANSWERS = 3
const val WORDS_COUNT = 4