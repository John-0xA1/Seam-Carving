package seamcarving

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val parsedArgs = parseArgs(args)
    val inFile = parsedArgs["-in"] ?: error("no -in arg provided")
    val outFile = parsedArgs["-out"] ?: error("no -out arg provided")
    val widthShrink = parsedArgs["-width"]?.toInt() ?: 0
    val heightShrink = parsedArgs["-height"]?.toInt() ?: 0

    var image = openImage(inFile)

    println(">>> start width shrinking")

    repeat (widthShrink) {
        val energies = PixelEnergies(image)
        val energyGraph = EnergyGraph(energies)
        val seam = energyGraph.findVerticalSeam()
        image = removeVerticalSeam(image, seam)
        println("$it / $widthShrink")
    }

    println(">>> start height shrinking")

    repeat (heightShrink) {
        val energies = PixelEnergies(image)
        val energyGraph = EnergyGraph(energies)
        val seam = energyGraph.findHorizontalSeam()
        image = removeHorizontalSeam(image, seam)
        println("$it / $heightShrink")
    }

    saveImage(image, outFile)
}

fun removeVerticalSeam(image: BufferedImage, seam: PointList): BufferedImage {
    val reducedImage = BufferedImage(image.width - 1, image.height, image.type)

    for (y in 0 until reducedImage.height) {
        val seamPoint = seam[y]
        for (x in 0 until reducedImage.width) {
            if (x < seamPoint.x)
                reducedImage.setRGB(x, y, image.getRGB(x, y))
            else
                reducedImage.setRGB(x, y, image.getRGB(x + 1, y))
        }
    }

    return reducedImage
}

fun removeHorizontalSeam(image: BufferedImage, seam: PointList): BufferedImage {
    val reducedImage = BufferedImage(image.width, image.height - 1, image.type)

    for (x in 0 until reducedImage.width) {
        val seamPoint = seam[x]
        for (y in 0 until reducedImage.height) {
            if (y < seamPoint.y)
                reducedImage.setRGB(x, y, image.getRGB(x, y))
            else
                reducedImage.setRGB(x, y, image.getRGB(x, y + 1))
        }
    }

    return reducedImage
}

fun parseArgs(args: Array<String>): Map<String, String> {
    val result = HashMap<String, String>()
    for (i in args.indices step 2) {
        val paramName = args[i]
        val paramValue = args[i + 1]
        result[paramName] = paramValue
    }
    return result
}

fun openImage(filePath: String): BufferedImage {
    return ImageIO.read(File(filePath))
}

fun saveImage(image: BufferedImage, fileName: String) {
    ImageIO.write(image, "png", File(fileName))
}