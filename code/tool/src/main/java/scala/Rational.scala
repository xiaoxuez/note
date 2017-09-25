package scala

/**
  * Created by xiaoxuez on 2017/8/23.
  * 定义一个分数的类，以练习。
  */
class Rational(n:Int, d:Int) {  //类参数，可直接写在类名后面的括号中，
  // Scala 编译器会收集这两个类参数并创造一个带同样的两个参数的主构造器: primary constructor。

  require(d != 0)
  println("Created "+n+"/"+d)
  //直接写在大括号中非字段的部分或者方法定义的代码，会直接编译进主构造器，故在新建Rational对象的时候就会直接执行这两行代码
  override def toString: String = n + "/" + d

//  def add(that: Rational) = n * that.d + d * that.n;
  // n,d应该只是个方法参数，强行一点说就是类的方法参数，虽然在这里作用域是足够的，但是that.n或者that.d这样就不允许了，所以需要类的字段
  private val g = gcd(n.abs, d.abs);
  //注意的是，n.abs 不能写成 n.abs()，提示Unit does not take parameters，因为abs定义时是def abs: Int = ...就没有定义方法参数，故方法参数类型为Unit

  val number :Int = n / g;
  val denom :Int = d / g;
  def add(that:Rational): Rational = new Rational(number * that.denom + that.number * denom, that.denom * denom)

  def lessThan(that: Rational) =
    this.number * that.denom < that.number * this.denom

  //this指向当前对象
  def max(that: Rational) = if (this.lessThan(that)) that else this


  //从构造器,主构造器之外的构造器被称为从构造器，Scala的从构造器开始于def this(...)
  //这个规则的根 结果就是每一个 Scala 的构造器调用终将结束于对类的 主构造器的调用。因此主构造器是类的唯一入口点。
  def this(n: Int) = this(n, 1)

  private def gcd(a: Int, b: Int): Int = {
    if (b == 0) a else gcd(b, a % b)
  }

  //定义操作符
  def +(that: Rational): Rational =
    new Rational(
      number * that.denom + that.number * denom,
      denom * that.denom
    )

  def +(i: Int): Rational =   //方法重载
    new Rational(number + i * denom, denom)

  def *(that: Rational): Rational =
    new Rational(number * that.number, denom * that.denom)

  /**
    * 定义操作符的时候突然想起了unary_, unary_+被用做定义一元的‘+’操作符的方法名
    */

}
