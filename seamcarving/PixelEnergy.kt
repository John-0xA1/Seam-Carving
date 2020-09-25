package seamcarving;

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

class PixelEnergies(image: BufferedImage) {
    val imageWidth = image.width
    val imageHeight = image.height
    private val energies = MutableList(imageWidth) { MutableList(imageHeight) { 0.0 } }

    init {
        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                energies[x][y] = calculatePixelEnergy(image, x, y)
            }
        }
    }

    fun forEach(callback: (energy: Double, x: Int, y: Int) -> Unit) {
        for (x in energies.indices) {
            for (y in energies[x].indices) {
                callback(energies[x][y], x, y)
            }
        }
    }

    private fun calculatePixelEnergy(image: BufferedImage, x: Int, y: Int): Double {
        val squareXGrad = squareXGradient(image, x, y)
        val squareYGrad = squareYGradient(image, x, y)
        return sqrt(squareXGrad + squareYGrad)
    }

    private fun squareXGradient(image: BufferedImage, x: Int, y: Int): Double {
        val pixelX = when (x) {
            0 -> x + 1
            image.width - 1 -> x - 1
            else -> x
        }

        val color1 = Color(image.getRGB(pixelX - 1, y))
        val color2 = Color(image.getRGB(pixelX + 1, y))
        return squareGradient(color1, color2)
    }

    private fun squareYGradient(image: BufferedImage, x: Int, y: Int): Double {
        val pixelY = when (y) {
            0 -> y + 1
            image.height - 1 -> y - 1
            else -> y
        }

        val color1 = Color(image.getRGB(x, pixelY - 1))
        val color2 = Color(image.getRGB(x, pixelY + 1))
        return squareGradient(color1, color2)
    }

    private fun squareGradient(color1: Color, color2: Color): Double {
        val deltaRed = color2.red - color1.red
        val deltaGreen = color2.green - color1.green
        val deltaBlue = color2.blue - color1.blue

        return 1.0 * deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue
    }
}