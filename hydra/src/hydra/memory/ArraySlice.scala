package hydra.memory

class ArraySlice(val mem: Array[Double], val startPos: Int, val length: Int) {

  def apply(i: Int): Double           = mem(startPos + i)
  def update(i: Int, v: Double): Unit = mem(startPos + i) = v

}

object ArraySlice {
  def apply(arr: Array[Double]): ArraySlice = new ArraySlice(arr, 0, arr.length)
}
