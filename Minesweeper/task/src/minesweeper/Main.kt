package minesweeper

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.random.nextInt

const val AUTOMATED = false

class MineField(private val size: Int = 9) {

    class Explosion: java.lang.RuntimeException("You stepped on a mine and failed!")
    var hasExploded = false

    enum class Mark(val mark: Char) {
        UNEXPLORED('.'),
        UNEXPLORED_MARKED('*'),

        EXPLORED_NO_MINES_AROUND('/'),
        EXPLORED_MINES_AROUND('$'),
        MINE('X')
    }
    inner class Cell(val location: Pair<Int, Int>) {
        val row: Int = location.first
        val column: Int = location.second

        var hasMine: Boolean = false
            /*set(value) {
                field = value
                if (field) {
                    neighborCells?.forEach { neighborCell -> neighborCell.addAdjacentMine(this) }
                } else {
                    neighborCells?.forEach { neighborCell -> neighborCell.dropAdjacentMine(this) }
                }
            }*/

        private var status: Mark = Mark.UNEXPLORED
        val content: Char
            get() {
                val mark = when {
                hasExploded && hasMine -> Mark.MINE.mark
                status == Mark.EXPLORED_MINES_AROUND -> getNeighborMines(this).size.digitToChar()
                else -> status.mark}
                return mark
            }

        private var markedAsMine = false
            set(value) {
                field = value
                status = if (field) Mark.UNEXPLORED_MARKED else Mark.UNEXPLORED
            }

        val isMarked: Boolean
            get() = markedAsMine

        val isExplored: Boolean
            get() {
                return status in Mark.EXPLORED_NO_MINES_AROUND..Mark.EXPLORED_MINES_AROUND
            }

        val nextColumn = min(column + 1, cellRange.last)
        val previousRow = max(row - 1, 0)
        val previousColumn = max(column - 1, 0)
        val nextRow = min(row + 1, cellRange.last)



        private var neighbors: Set<Cell>? = null
        fun neighborCells(): Set<Cell> {
            neighbors = if (neighbors == null) getNeighborCells(this) else neighbors
            return neighbors!!
        }


        private fun neighborMines() = getNeighborMines(this)

        /*fun neighborCells(): Set<Cell> {
            val result = with(board) {
                val neighborLocations = (previousRow..nextRow).map { neighborRow ->
                    (previousColumn..nextColumn).map { neighborColumn ->
                        neighborRow to neighborColumn
                    }
                }

                 neighborLocations
                    .flatten()
                    .filter { it != location }
                    .map { getCellBy(it) }
                    .toSet()
            }
            return result
        }

        fun neighborMines() = neighborCells().filter { it.hasMine }.toSet()*/

//        val adjacentMines = mutableSetOf<Cell>()

        /*fun addAdjacentMine(mineCell: Cell) {
            adjacentMines += mineCell
        }
        fun dropAdjacentMine(mineCell: Cell) {
            adjacentMines -= mineCell
        }
        fun hasAdjacentMines() = adjacentMines.isNotEmpty()*/


        /*val isValidLocation =
            location.first in cellRange && location.second in cellRange*/

        fun explore(cell: Cell = this, isCurrent: Boolean = false) {
            cell.run {
                if (isCurrent && hasMine) explode()
                else {
                    val neighbors = neighborCells()
                    val neighborMines = neighborMines()
                    status =
                        if (neighborMines.isNotEmpty()) Mark.EXPLORED_MINES_AROUND
//                        if (hasAdjacentMines()) Mark.EXPLORED_MINES_AROUND
                        else Mark.EXPLORED_NO_MINES_AROUND
                    if (status == Mark.EXPLORED_NO_MINES_AROUND) {
                        exploreNeighbors()
                    }
                    status
                }
            }
        }

        private fun exploreNeighbors() {
            val neighborsNotMines = neighborCells() - neighborMines()
            neighborsNotMines
                .filter { it.status in Mark.UNEXPLORED..Mark.UNEXPLORED_MARKED }
                .forEach { cell -> explore(cell) }
        }

        private fun explode(): Mark {
            hasExploded = true
            throw Explosion()
        }

        fun toggleMarkAsMine() {
            markedAsMine = !markedAsMine
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Cell) return false

            if (row != other.row) return false
            if (column != other.column) return false

            return true
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + column
            return result
        }

        override fun toString(): String {
            return "$status$location"
        }

    }



    private val numberOfCells = size * size
    val cellRange = 0 until size
    private val board = mutableSetOf<Cell>()
    /*Setup Board*/
    init {
        cellRange.forEach { row ->
            cellRange.forEach { column ->
                board += Cell(row to column)
            }
        }
    }

    private val mines: List<Cell>
        get() = board.filter { it.hasMine }

    init {
        run()
    }

    private fun getCellBy(location: Pair<Int, Int>): Cell {
        return board.first { it.location == location }
    }

    private fun setAsMine(location: Pair<Int, Int>) {
        board.first { it.location == location }.run { hasMine = true }
    }

    fun getNeighborCells(cell: Cell): Set<Cell> {
        val result = cell.run {
            val neighborLocations = (previousRow..nextRow).map { neighborRow ->
                (previousColumn..nextColumn).map { neighborColumn ->
                    neighborRow to neighborColumn
                }
            }

            neighborLocations
                .flatten()
                .filter { it != location }
                .map { getCellBy(it) }
                .toSet()
        }
        return result
    }

    fun getNeighborMines(cell: Cell) = cell.neighborCells().filter { it.hasMine }.toSet()

    fun allMinesAreMarked() = mines.all { it.isMarked }
    fun anyMinesNotMarked() = mines.any { !it.isMarked }
    fun allNonMinesAreExplored() = (board- mines.toSet()).all { it.isExplored }

    private val randomMineGenerator: Random
        get() = Random.Default

    private var numberOfMines = 0
        set(value) {
            field = if (value > numberOfCells) numberOfCells else value
//            mines.forEach(Cell::clearMine)
            while (mines.size < field) {
                setAsMine(randomMineGenerator.nextInt(size) to randomMineGenerator.nextInt(size))
            }
        }

    private fun collectNumberOfMinesFromStandardInput() {
        numberOfMines = readln().toInt()
    }
    private fun collectNumberOfMinesRandom(max: Int = size) {
        numberOfMines = Random.nextInt(0..max)
        println(numberOfMines)
    }

    private fun run(){
        initiateMines()
        showBattleField()
        startGame()
    }

    private fun initiateMines(){
        print("How many mines do you want on the field? ")
        if (AUTOMATED) {
            collectNumberOfMinesRandom()
        } else {
            collectNumberOfMinesFromStandardInput()
        }

    }

    private fun startGame() {
        try {
            while (anyMinesNotMarked() || allNonMinesAreExplored()) {
                getUserInput()
                showBattleField()
            }
            println("Congratulations! You found all the mines!")
        } catch (explosion: Explosion) {
            showBattleField()
            println(explosion.message)
        }

        /*while (mines.any { !it.isMarkedForRemoval } || marks.any { it !in mines.map { it.location } }) {
            print("Set/delete mines marks (x and y coordinates): ")
            val (x, y) = readln().split(" ").map(String::toInt).map(Int::dec)
            if (markCell(y to x))
                showBattleField()
        }
        println("Congratulations! You found all the mines!")*/
    }
    enum class UserAction(val input: String) {
        TOGGLE_MARK_MINE("mine"), EXPLORE("free");

        companion object {
            fun byInput(input: String) = values().first { it.input == input }
        }
    }

    private fun getUserInput() {
        print("Set/unset mines marks or claim a cell as free: ")
        val (columnString, rowString, actionString) = readln().split(" ")
        val location = getLocation(columnString, rowString)
        val cell = board.first { it.location == location }

        when (UserAction.byInput(actionString)) {
            UserAction.TOGGLE_MARK_MINE -> cell.toggleMarkAsMine()
            UserAction.EXPLORE -> cell.explore(isCurrent = true)

        }
    }

    private fun getLocation(column: String, row: String) = row.toInt().dec() to column.toInt().dec()

    private fun showBattleField() {
        val separatorRow = "—│${"—".repeat(size)}│\n"
        val topRow = " │${(1..size).joinToString("")}│\n"

        val renderedField = board.groupBy {it.location.first} .map {
            val index = it.key
            val rowCells = it.value
            "${index + 1}│${rowCells.joinToString("") { cell -> cell.content.toString() }}│\n"
        }.joinToString("")
        println(topRow + separatorRow + renderedField + separatorRow)
    }

}




fun main() {
    if (AUTOMATED) {
        for (i in 1..20) {
            MineField()
        }
    } else {
        MineField()
    }
}
