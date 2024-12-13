package org.example.kotlinTelegramBot

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0)

class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer(private val currentAnswerCount: Int = 3, private val countOfQuestionWords: Int = 4) {
    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learned = dictionary.filter { it.correctAnswersCount >= currentAnswerCount }.size
        val total = dictionary.size
        val percent = learned * 100 / total

        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < currentAnswerCount }
        if (notLearnedList.isEmpty()) return null

        val shuffledWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList = dictionary.filter { it.correctAnswersCount >= currentAnswerCount }.shuffled()
            notLearnedList.shuffled().take(countOfQuestionWords) +
                    learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

        val correctAnswers = shuffledWords.random()

        question = Question(
            variants = shuffledWords,
            correctAnswer = correctAnswers,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val currentAnswerId = it.variants.indexOf(it.correctAnswer)
            if (currentAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): MutableList<Word> {
        try {
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary(dictionary: MutableList<Word>) {
        val file = File("words.txt")
        file.writeText("")

        for (word in dictionary) {
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}


