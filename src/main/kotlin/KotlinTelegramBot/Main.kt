package org.example.KotlinTelegramBot

import java.io.File

fun main() {
    val dictionary = loadDictionary()

    while (true) {
        println("Меню:")
        println("1 - Учить слова\n" +
                "2 - Статистика\n" +
                "0 - Выход")

        val userChoice = readln().toIntOrNull()

        when (userChoice) {
            1 -> while (true) {
                val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS }
                if (notLearnedList.isEmpty()) {
                    println("Все слова выучены! \n")
                    break
                }

                val countLearn = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS }
                val shuffledWords = countLearn.shuffled().take(WORDS_COUNT)
                val correctAnswers = shuffledWords.random()

                val questionWords = shuffledWords.mapIndexed { index, word ->
                    "${index + 1} - ${word.translate}"
                }.joinToString("\n", "${correctAnswers.original}\n", "\n\n0 - меню")

                println(questionWords)

                val userAnswerInput = readln().toIntOrNull()
            }

            2 -> {
                val totalCount = dictionary.size
                val wordLearned = dictionary.count { it.correctAnswersCount >= CORRECT_ANSWERS }
                val percent = (wordLearned.toDouble() / totalCount) * 100

                println("Выбран пункт: Статистика\n" +
                        "Выучено $wordLearned из $totalCount слов | ${percent.toInt()}%\n")
            }

            0 -> return
            else -> {
                println("Введите число 1, 2 или 0:")
            }
        }
    }
}

fun loadDictionary(): MutableList<Word> {

    val dictionary: MutableList<Word> = mutableListOf()
    val file = File("words.txt")
    file.createNewFile()

    val list: List<String> = file.readLines()
    for (line in list) {
        val splitLines = line.split("|")

        val correctAnswer: Int = splitLines.getOrNull(2)?.toIntOrNull() ?: 0

        val word = Word(original = splitLines[0], translate = splitLines[1], correctAnswer)
        dictionary.add(word)
    }
    return dictionary
}

data class Word(val original: String, val translate: String, var correctAnswersCount: Int = 0)

const val CORRECT_ANSWERS = 3
const val WORDS_COUNT = 4
