package org.example.kotlinTelegramBot

import java.io.File

class Statistics(
    val totalCount: Int,
    val wordLearned: Int,
    val percent: Int
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer {
    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {

        val totalCount = dictionary.size
        val wordLearned = dictionary.count { it.correctAnswersCount >= CORRECT_ANSWERS }
        val percent = (wordLearned / totalCount) * 100

        return Statistics(totalCount, wordLearned, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS }
        if (notLearnedList.isEmpty()) return null
        val shuffledWords = notLearnedList.shuffled().take(WORDS_COUNT)
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


    private fun saveDictionary(dictionary: MutableList<Word>) {
        val file = File("words.txt")
        file.writeText("")

        for (word in dictionary) {
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}


