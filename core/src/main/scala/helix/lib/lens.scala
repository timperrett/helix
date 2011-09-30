package helix
package lib

case class Lens[A,B](get: A => B, set: (A,B) => A) extends Immutable {
  /** A Lens[A,B] can be used as a function from A => B */
  def apply(whole:A): B = get(whole)

  /** Modify the value viewed through the lens */
  def mod(a:A, f: B => B) : A = set(a, f(get(a)))
  
  /** Lenses can be composed */
  def compose[C](that: Lens[C,A]) = Lens[C,B](
    c => get(that.get(c)),
    (c, b) => that.mod(c, set(_, b))
  )
  def andThen[C](that: Lens[B,C]) = that compose this
  
  /** Two lenses that view a value of the same type can be joined */
  def |||[C](that: Lens[C,B]) = Lens[Either[A,C],B](
    { case Left(a) => get(a)
      case Right(b) => that.get(b)
    },
    { case (Left(a),  b) => Left (set(a,b))
      case (Right(c), b) => Right(that.set(c,b))
    }
  )

  /** Two disjoint lenses can be paired */
  def ***[C,D](that: Lens[C,D]) = Lens[(A,C),(B,D)](
    ac => (get(ac._1), that.get(ac._2)),
    (ac,bd) => (set(ac._1,bd._1),that.set(ac._2,bd._2))
  )
}