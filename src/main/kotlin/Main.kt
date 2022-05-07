import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main() {
    UserInterface().initiation()
    UserInterface().startMenu()
}

class ListTask {
    object MainList {
        var item = mutableListOf<Entry>()
    }
}

class UserInterface {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listType = Types.newParameterizedType(List::class.java, Entry::class.java)!!
    private val adapter = moshi.adapter<List<Entry>>(listType)!!

    private fun listPrint() {
        if (ListTask.MainList.item.isEmpty()) {
            println("No tasks have been input")
        } else {
            prinTable()
            cycle@ for (count in 0 until ListTask.MainList.item.size) {
                println(buildString {
                    append(
                        numPrn(count + 1)
                                + ListTask.MainList.item[count].date.padStart(11, ' ').padStart(12, '|')
                            .padEnd(13, ' ')
                                + ListTask.MainList.item[count].time.padStart(6, ' ').padStart(7, '|')
                            .padEnd(8, ' ').padEnd(8, ' ')
                                + ListTask.MainList.item[count].priority.color.padStart(11, ' ').padStart(12, ' ')
                            .padStart(13, '|').padEnd(14, ' ')
                                + dueTag(ListTask.MainList.item[count].date.toLocalDate()).color.padStart(11, ' ')
                            .padStart(12, ' ')
                            .padStart(13, '|').padEnd(14, ' ')
                                + taskPrn(count)
                                + "+----+------------+-------+---+---+--------------------------------------------+"
                    )
                })
            }
        }
    }

    private fun taskPrn(count: Int): String {
        var resString = ""
        val list = ListTask.MainList.item[count].body
        if (list.size == 1 && list[0].length <= 44) {
            resString += "|${list[0].padEnd(44, ' ')}|\n"
        } else {
            val newList = list[0].chunked(44).toMutableList()
            resString += "|${newList[0].padEnd(44, ' ')}|\n"
            newList.removeAt(0)
            newList.forEach {
                val w = buildString {
                    append(
                        "|    |            |       |   |   "
                                + "|${it.padEnd(44, ' ')}|\n"
                    )
                }
                resString += w
            }
            for (entry in 1..list.lastIndex) {
                val strings = list[entry].chunked(44)
                strings.forEach {
                    val w = buildString {
                        append(
                            "|    |            |       |   |   "
                                    + "|${it.padEnd(44, ' ')}|\n"
                        )
                    }
                    resString += w
                }
            }
        }
        return resString
    }

    private fun numPrn(number: Int): String {
        return if (number < 10) {
            "$number".padStart(2, ' ').padStart(3, '|').padEnd(5, ' ')
        } else "$number".padStart(3, ' ').padStart(4, '|').padEnd(5, ' ')
    }

    fun initiation() {
        if (File(("tasklist.json")).exists()) {
            val file = File("tasklist.json").readText(Charsets.UTF_8)
            val result = adapter.fromJson(file)
            ListTask.MainList.item = result?.toMutableList() ?: return
        } else return
    }

    private fun saveJson() {
        val file = File("tasklist.json")
        file.writeText(adapter.toJson(ListTask.MainList.item))
    }

    fun startMenu() {
        while (true) {
            println("Input an action (add, print, edit, delete, end):")
            when (readln()) {
                "add" -> addItem()
                "print" -> listPrint()
                "delete" -> delItem()
                "edit" -> editItem()
                "end" -> {
                    saveJson()
                    println("Tasklist exiting!")
                    exitProcess(0)
                }
                else -> {
                    println("The input action is invalid")
                }
            }
        }
    }

    private fun getTaskNum(): Int {
        if (ListTask.MainList.item.isEmpty()) {
            println("No tasks have been input")
            startMenu()
        }
        listPrint()
        while (true) {
            println("Input the task number (1-${ListTask.MainList.item.size}):")
            val reg = "\\d".toRegex()
            val inputLine = readln()
            when {
                inputLine.matches(reg) -> {
                    if (inputLine.toInt() - 1 in ListTask.MainList.item.indices) {
                        return inputLine.toInt() - 1
                    } else println("Invalid task number")
                }
//                else -> println("Invalid task number")
            }
        }
    }

    private fun editItem() {
        val num = getTaskNum()
        while (true) {
            println("Input a field to edit (priority, date, time, task):")
            when (readln()) {
                "priority" -> {
                    ListTask.MainList.item[num].priority = setPriority()
                    success()
                }
                "date" -> {
                    ListTask.MainList.item[num].date = setDate()
                    success()
                }
                "time" -> {
                    ListTask.MainList.item[num].time = setTime(
                        (ListTask.MainList
                            .item[num].date.toLocalDate())
                    )
                    success()
                }
                "task" -> {
                    ListTask.MainList.item[num].body = setMsg()
                    success()
                }
                else -> {
                    println("Invalid field")
                }
            }
        }
    }

    private fun delItem() {
        ListTask.MainList.item.removeAt(getTaskNum())
        println("The task is deleted")
        startMenu()
    }

    private fun addItem() {
        val tPriority = setPriority()
        val date = setDate()
        val time = setTime(date.toLocalDate())
        val taskMsg = setMsg()
        ListTask.MainList.item.add(Entry(tPriority, date, time, taskMsg))
    }

    private fun setMsg(): MutableList<String> {
        val reg = Regex("\\s+")
        val inTask = mutableListOf<String>()
        println("Input a new task (enter a blank line to end):")
        while (true) {
            val inputLine = readLine()!!.trimEnd().trimStart().replace(reg, " ")
            if (inputLine.isEmpty() && inTask.size == 0) {
                println("The task is blank")
                startMenu()
            } else
                if (inputLine.isEmpty() && inTask.size != 0) {
                    return inTask
                } else inTask.add(inputLine)
        }
    }

    private fun setTime(date: LocalDate): String {
        while (true) {
            println("Input the time (hh:mm):")
            val inputLine = readLine()!!.trimEnd().trimStart()
            val data = inputLine.split(":").toMutableList()
            try {
                return LocalDateTime(
                    date.year,
                    date.month,
                    date.dayOfMonth,
                    data[0].toInt(),
                    data[1].toInt()
                ).toString().slice(11..15)
            } catch (e: IllegalArgumentException) {
                println("The input time is invalid")
            }
        }
    }

    private fun setDate(): String {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val inputLine = readLine()!!.trimEnd().trimStart()
            val data = inputLine.split("-").toMutableList()
            try {
                return (LocalDate(data[0].toInt(), data[1].toInt(), data[2].toInt())).toString()
            } catch (e: IllegalArgumentException) {
                println("The input date is invalid")
            }
        }
    }

    private fun setPriority(): Priority {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            when (readLine()!!.trimEnd().trimStart().uppercase(Locale.getDefault())) {
                "C" -> return Priority.Critical
                "H" -> return Priority.High
                "N" -> return Priority.Normal
                "L" -> return Priority.Low
            }
        }
    }

    private fun dueTag(date: LocalDate): Tag {
        val currentDate = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        val numberOfDays = currentDate.daysUntil(date)
        return when {
            numberOfDays == 0 -> Tag.Today
            numberOfDays < 0 -> Tag.Overdue
            else -> Tag.InTime
        }
    }

    private fun success() {
        println("The task is changed")
        startMenu()
    }

    private fun prinTable() {
        val header = """+----+------------+-------+---+---+--------------------------------------------+
| N  |    Date    | Time  | P | D |                   Task                     |
+----+------------+-------+---+---+--------------------------------------------+"""
        println(header)
    }

}

data class Entry(
    var priority: Priority,
    var date: String,
    var time: String,
    var body: MutableList<String>
)

enum class Tag(val color: String) {
    InTime("\u001B[102m \u001B[0m"),
    Today("\u001B[103m \u001B[0m"),
    Overdue("\u001B[101m \u001B[0m")
}

enum class Priority(val color: String) {
    Critical("\u001B[101m \u001B[0m"),
    High("\u001B[103m \u001B[0m"),
    Normal("\u001B[102m \u001B[0m"),
    Low("\u001B[104m \u001B[0m");
}