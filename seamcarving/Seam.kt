package seamcarving

import java.util.*
import kotlin.collections.ArrayList

typealias PointList = List<Point>

data class Point(val x: Int, val y: Int)

class EnergyGraph(private val energies: PixelEnergies) {

    fun findVerticalSeam(): PointList {
        val graphData = toVerticalSeamGraphData(energies)
        val seam = findSeam(graphData)
        return verticalSeamToPointList(seam)
    }

    fun findHorizontalSeam(): PointList {
        val graphData = toHorizontalSeamGraphData(energies)
        val seam = findSeam(graphData)
        return horizontalSeamToPointList(seam)
    }

    private fun verticalSeamToPointList(seam: Seam): PointList {
        val result = ArrayList<Point>()
        for (y in seam.indices) {
            val x = seam[y]
            result.add(Point(x, y))
        }
        return result
    }

    private fun horizontalSeamToPointList(seam: Seam): PointList {
        val result = ArrayList<Point>()
        for (x in seam.indices) {
            val y = seam[x]
            result.add(Point(x, y))
        }
        return result
    }

    private fun findSeam(graphData: List<List<GraphPoint>>): Seam {
        setSmallestDistances(graphData, Point(0, 0))
        val shortestPath = getShortestPath(
                graphData,
                Point(0, 0),
                Point(graphData.lastIndex, graphData[0].lastIndex)
        )
        return toSeam(shortestPath, graphData[0].lastIndex)
    }

    private fun toVerticalSeamGraphData(energies: PixelEnergies): List<List<GraphPoint>> {
        val result = initGraphPointList(energies.imageWidth, energies.imageHeight)
        energies.forEach { energy, x, y ->
            result[x][y + 1] = GraphPoint(x, y + 1, energy)
        }
        return result
    }

    private fun toHorizontalSeamGraphData(energies: PixelEnergies): List<List<GraphPoint>> {
        val result = initGraphPointList(energies.imageHeight, energies.imageWidth)
        energies.forEach { energy, x, y ->
            result[y][x + 1] = GraphPoint(y, x + 1, energy)
        }
        return result
    }

    private fun initGraphPointList(width: Int, height: Int): List<MutableList<GraphPoint>> {
        val result = ArrayList<MutableList<GraphPoint>>(width)
        for (x in 0 until width) {
            val resultRow = ArrayList<GraphPoint>(height + 2)
            for (y in 0 until height + 2) {
                resultRow.add(GraphPoint(x, y, 0.0))
            }
            result.add(resultRow)
        }
        return result
    }

    private fun setSmallestDistances(graphData: List<List<GraphPoint>>, source: Point) {
        val reachable = PriorityQueue<GraphPoint>(compareBy { it.smallestDistance })
        var point: GraphPoint? = graphData[source.x][source.y]
        point?.smallestDistance = point!!.value
        while (point != null) {
            val neighbors: List<GraphPoint> = getNeighbors(graphData, point)
            neighbors.forEach {
                val distance = point!!.smallestDistance + it.value
                if (it.smallestDistance > distance) {
                    it.smallestDistance = distance
                    it.previous = point
                }
            }
            reachable.addAll(neighbors.filter { !it.processed && !reachable.contains(it) })
            point.processed = true
            point = if (reachable.size > 0)
                reachable.remove()
            else
                null
        }
    }

    private fun getNeighbors(graphData: List<List<GraphPoint>>, point: GraphPoint): List<GraphPoint> {
        val points = ArrayList<GraphPoint>()
        val isFirstLine = point.y == 0
        val isLastLine = point.y == graphData[0].lastIndex
        if (isFirstLine || isLastLine) {
            addPointIfExists(points, graphData, point.x + 1, point.y)
        }
        addPointIfExists(points, graphData, point.x - 1, point.y + 1)
        addPointIfExists(points, graphData, point.x + 0, point.y + 1)
        addPointIfExists(points, graphData, point.x + 1, point.y + 1)
        return points
    }

    private fun addPointIfExists(points: MutableList<GraphPoint>, graphData: List<List<GraphPoint>>, x: Int, y: Int) {
        if (x < 0 || y < 0 || x > graphData.lastIndex || y > graphData[0].lastIndex)
            return
        points.add(graphData[x][y])
    }

    private fun getShortestPath(graphData: List<List<GraphPoint>>, source: Point, target: Point): List<GraphPoint> {
        val reversedPath = ArrayList<GraphPoint>()
        var point: GraphPoint? = graphData[target.x][target.y]
        while (point != null) {
            reversedPath.add(point)
            point = point.previous
        }
        reversedPath.add(graphData[source.x][source.y])
        return reversedPath.reversed()
    }

    private fun toSeam(shortestPath: List<GraphPoint>, maxY: Int): Seam {
        return shortestPath.mapNotNull {
            if (it.y == maxY || it.y == 0) null
            else it.x
        }
    }
}

private typealias Seam = List<Int>

private data class GraphPoint(
        val x: Int,
        val y: Int,
        val value: Double,
        var processed: Boolean = false,
        var previous: GraphPoint? = null,
        var smallestDistance: Double = Double.POSITIVE_INFINITY
)