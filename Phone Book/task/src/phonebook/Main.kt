package phonebook

import java.io.File
import java.io.FileNotFoundException
import java.util.Hashtable
import kotlin.math.floor
import kotlin.math.sqrt

val SPACES = "\\s+".toRegex()

enum class SORTSEARCHALGO{
    UNSORTED,
    BUBBLE_JUMP,
    QUICK_BINARY,
    HASH
}

class PhoneBook {

    fun getSize(): Int {
        return phoneBook.size - 1
    }

    companion object Search {
        var bubbleSortTime = 0L
        var sortingThreshold = 0L
        var hashTableTime = 0L
        private fun convertMsToMinSecMs(total: Long): String {
            val min = total / 1000 / 60
            val sec = total / 1000 % 60
            val ms = total % 1000

            return "$min min. $sec sec. $ms ms."
        }

        fun evaluateSearch(phoneBook: PhoneBook, searchData: File, algo: SORTSEARCHALGO) {
            var found = 0
            var lines = 0
            when(algo) {
                SORTSEARCHALGO.UNSORTED -> {
                    val totalStart = System.currentTimeMillis()
                    println("Start searching (linear search)...")
                    searchData.forEachLine {
                        if (phoneBook.linearSearch(it.trim())) {
                            ++found
                        }
                        ++lines
                    }
                    val totalEnd = System.currentTimeMillis()
                    sortingThreshold = (totalStart - totalEnd) * 10
                    println("Found $found / $lines entries. Time taken: ${convertMsToMinSecMs(totalEnd - totalStart)}")
                    println()
                }

                SORTSEARCHALGO.BUBBLE_JUMP -> {
                    val totalStart = System.currentTimeMillis()
                    println("Start searching (bubble sort + jump search)...")

                    val bubbleSortDone = phoneBook.bubbleSort()
                    val searchStart = System.currentTimeMillis()
                    if (bubbleSortDone) {
                        searchData.forEachLine {
                            if (phoneBook.jumpSearch(it.trim())) {
                                ++found
                            }
                            ++lines
                        }
                    } else {
                        searchData.forEachLine {
                            if (phoneBook.linearSearch(it.trim())) {
                                ++found
                            }
                            ++lines
                        }
                    }
                    val searchEnd = System.currentTimeMillis()
                    val totalEnd = System.currentTimeMillis()
                    println("Found $found / $lines entries. Time taken: ${convertMsToMinSecMs(totalEnd - totalStart)}")
                    println("Sorting time: ${convertMsToMinSecMs(bubbleSortTime)} ${if (!bubbleSortDone) " - STOPPED, moved to linear search" else ""}")
                    println("Searching time: ${convertMsToMinSecMs(searchEnd - searchStart)}")
                    println()
                }
                SORTSEARCHALGO.QUICK_BINARY -> {
                    val totalStart = System.currentTimeMillis()
                    println("Start searching (quick sort + binary search)...")

                    val sortStart = System.currentTimeMillis()
                    phoneBook.quickSort(0, phoneBook.getSize())
                    val sortEnd = System.currentTimeMillis()
                    val searchStart = System.currentTimeMillis()
                        searchData.forEachLine {
                            if (phoneBook.binarySearch(it.trim(), 0, phoneBook.getSize()) != -1) {
                                ++found
                            }
                            ++lines
                        }

                    val searchEnd = System.currentTimeMillis()
                    val totalEnd = System.currentTimeMillis()
                    println("Found $found / $lines entries. Time taken: ${convertMsToMinSecMs(totalEnd - totalStart)}")
                    println("Sorting time: ${convertMsToMinSecMs(sortEnd - sortStart)}")
                    println("Searching time: ${convertMsToMinSecMs(searchEnd - searchStart)}")
                    println()
                }
                else -> {
                    val totalStart = System.currentTimeMillis()
                    println("Start searching (hash table)...")

                    val searchStart = System.currentTimeMillis()
                    searchData.forEachLine {
                        if (phoneBook.phoneBookHashMap.containsKey(it.trim())) {
                            ++found
                        }
                        ++lines
                    }

                    val searchEnd = System.currentTimeMillis()
                    val totalEnd = System.currentTimeMillis()
                    println("Found $found / $lines entries. Time taken: ${convertMsToMinSecMs(hashTableTime + searchEnd - searchStart)}")
                    println("Creating time: ${convertMsToMinSecMs(hashTableTime)}")
                    println("Searching time: ${convertMsToMinSecMs(searchEnd - searchStart)}")
                    println()
                }
            }
        }
    }

    private val phoneBook = mutableListOf<Pair<String, String>>()
    private val phoneBookHashMap = Hashtable<String, String>()

    fun loadPhoneBook(filePath: String) {
        try {
            loadPhoneBookData(File(filePath))
        } catch (except: Exception) {
            when (except) {
                is FileNotFoundException -> println("File Not Found Exception")
                is AccessDeniedException -> println("Access Denied Exception")
                is NoSuchFileException -> println("No Such File Exception")
                else -> println(except.message)
            }
        }
    }

    fun loadHashTable(filePath: String) {
        try {
            val hashStart = System.currentTimeMillis()
            loadPhoneBookHashData(File(filePath))
            hashTableTime = System.currentTimeMillis() - hashStart
        } catch (except: Exception) {
            when (except) {
                is FileNotFoundException -> println("File Not Found Exception")
                is AccessDeniedException -> println("Access Denied Exception")
                is NoSuchFileException -> println("No Such File Exception")
                else -> println(except.message)
            }
        }
    }

    fun bubbleSort(): Boolean {
        val sortStart = System.currentTimeMillis()
        val phoneBookSize = phoneBook.size
        val startTime = System.currentTimeMillis()
        for (firstIndex in 0 until phoneBookSize) {
            for (secondIndex in 0 until phoneBookSize - firstIndex - 1) {
                if (phoneBook[secondIndex].first > phoneBook[secondIndex + 1].first) {
                    val temp = phoneBook[secondIndex]
                    phoneBook[secondIndex] = phoneBook[secondIndex + 1]
                    phoneBook[secondIndex + 1] = temp

                }
            }
            val endTime = System.currentTimeMillis()

            if (endTime - startTime > sortingThreshold) {
                bubbleSortTime = System.currentTimeMillis() - sortStart
                return false
            }
        }
        bubbleSortTime = System.currentTimeMillis() - sortStart
        return true
    }

    fun quickSort (start: Int, end: Int) {
        if (start < end) {
            val pi = partition(start, end)
            quickSort(start, pi - 1)
            quickSort(pi + 1, end)
        }
    }

    fun binarySearch(value: String, start: Int, end: Int): Int {
        if (end >= start) {
            val mid = (start + end) / 2

            if (value == phoneBook[mid].first) {
                return mid
            }
            if (phoneBook[mid].first > value) {
                return binarySearch(value, start, mid - 1)
            }

            return binarySearch(value, mid + 1, end)
        }

        return -1
    }

    private fun partition(start: Int, end: Int): Int {
        val pivot = phoneBook[end]
        var index = start - 1

        for (newIndex in start until end) {
            if (phoneBook[newIndex].first < pivot.first) {
                ++index
                swap(index, newIndex)
            }
        }
        swap(index + 1, end)

        return index + 1
    }

    private fun swap(start: Int, end: Int){
        val temp = phoneBook[start]
        phoneBook[start] = phoneBook[end]
        phoneBook[end] = temp
    }

    fun jumpSearch(value: String): Boolean {
        val blockSize = floor(sqrt(phoneBook.size.toDouble())).toInt()
        var startIndex = 0
        var lastIndex = 0
        var count = 0

        for (index in (0..phoneBook.size step blockSize)) {
            count++
            val currElem = phoneBook[index]
            return if (value == currElem.first) {
                true
            } else if (value > currElem.first) {
                continue
            } else if (value < currElem.first && index == 0) {
                false
            } else {
                startIndex = index - blockSize
                lastIndex = index - 1
                break
            }
        }

        for (index in lastIndex downTo startIndex) {
            count++
            val currVal = phoneBook[index]
            if (value == currVal.first) {
                return true
            }
        }
        return false
    }

    private fun loadPhoneBookData(file: File) {
        file.forEachLine { line ->
            val (phoneNumber, name) = line.split(SPACES, 2)
            phoneBook.add(name.trim() to  phoneNumber)
        }
    }

    private fun loadPhoneBookHashData(file: File) {
        file.forEachLine { line ->
            val (phoneNumber, name) = line.split(SPACES, 2)
            phoneBookHashMap[name.trim()] = phoneNumber
        }
    }

    fun linearSearch(name: String): Boolean {
        for (entry in phoneBook) {
            if (entry.first == name) {
                return true
            }
        }
        return false
    }
}

fun main() {

    val phoneBook = PhoneBook()
    phoneBook.loadPhoneBook("/Users/mandal/Downloads/directory.txt")

    val searchData = File("/Users/mandal/Downloads/find.txt")

    PhoneBook.evaluateSearch(phoneBook, searchData, SORTSEARCHALGO.UNSORTED)
    PhoneBook.evaluateSearch(phoneBook, searchData, SORTSEARCHALGO.BUBBLE_JUMP)
    PhoneBook.evaluateSearch(phoneBook, searchData, SORTSEARCHALGO.QUICK_BINARY)


    phoneBook.loadHashTable("/Users/mandal/Downloads/directory.txt")
    PhoneBook.evaluateSearch(phoneBook, searchData, SORTSEARCHALGO.HASH)

}

