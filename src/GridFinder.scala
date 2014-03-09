package tw.g0v

import javax.imageio._
import java.awt.image._
import java.io._
import scala.collection._

object BMP {
  def imageToBits(image: BufferedImage): Vector[BitSet] = {
    val width = image.getWidth
    val height = image.getHeight
    def toBitsIter(currentRow: Int, accum: Vector[BitSet]): Vector[BitSet] = {
      def rowIter(currentColumn: Int, bitSet: BitSet): BitSet = {
        if (currentColumn == width) bitSet
        else {
          val rgb = image.getRGB(currentColumn, currentRow) & 0x00ffffff
          val isBlack = (rgb == 0)
          val nextBitSet = if (isBlack) bitSet + currentColumn else bitSet
          rowIter(currentColumn + 1, nextBitSet)
        }
      }

      if (currentRow >= height) accum
      else toBitsIter(currentRow + 1, accum :+ rowIter(0, BitSet()))
    }
    toBitsIter(0, Vector[BitSet]())
  }
}

class BMP(val bits: Vector[BitSet], val width: Int, val height: Int) extends AnyRef {

  def this(image: BufferedImage) = 
    this(BMP.imageToBits(image), image.getWidth, image.getHeight)

  def apply(x: Int, y: Int): Boolean = bits(y)(x)

  def components: Vector[Vector[(Int, Int)]] = {
    var results = Vector[Vector[(Int, Int)]]()
    
    val traversed = Array.fill(width)(BitSet.empty)
    
    for (y <- 1 to height - 1) {
      for (x <- 1 to width - 1) {
        if (bits(y)(x) && !traversed(y)(x)) {
          var component = Vector[(Int, Int)]()
          var q = scala.collection.mutable.Queue[(Int, Int)]((x, y))

          while (!q.isEmpty) {
            val (x, y) = q.dequeue
            component = component :+ (x, y)
            traversed(y) = traversed(y) + x

            List((1, 0), (-1, 0), (0, 1), (0, -1))
            .map(d => (x + d._1, y + d._2))
            .filter(c => (0 <= c._1 && c._1 < width) && (0 <= c._2 && c._2 <= height))
            .filter(c => !traversed(c._2)(c._1) && bits(c._2)(c._1))
            .foreach(c => {
              q.enqueue(c)
              traversed(c._2) = traversed(c._2) + c._1
            })
          }
          results = results :+ component
        } 
      }
    }
    results
  }
}

object GridFinder extends App {
  val bmp = new BMP(ImageIO.read(new File("images/origin.png")))
  val largestComponent = bmp.components.maxBy(_.size)

  val output = new BufferedImage(bmp.width, bmp.height, BufferedImage.TYPE_4BYTE_ABGR)
  for (y <- 0 to bmp.height - 1) {
    for (x <- 0 to bmp.width - 1) {
      if (bmp(x, y)) {
        output.setRGB(x, y, 0xFF000000) // black
      } else {
        output.setRGB(x, y, 0xFFFFFFFF) // white
      }
    }
  }
  largestComponent.foreach {case (x, y) => output.setRGB(x, y, 0xFFFF0000)}
  ImageIO.write(output, "png", new File("output.png"))
  // println(bmp.components.filter(component => component.size > 10).map(_.size).sorted.mkString("\n"))

}