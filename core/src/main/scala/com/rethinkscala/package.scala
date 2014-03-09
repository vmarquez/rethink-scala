package com

import com.rethinkscala.ast._
import com.rethinkscala.net._
import scala.concurrent.{ExecutionContext, Future}
import com.rethinkscala.net.AsyncResultQuery
import com.rethinkscala.ast.Var
import com.rethinkscala.net.BlockingResultQuery
import scala.Some


/** Created by IntelliJ IDEA.
  * User: Keyston
  * Date: 3/19/13
  * Time: 7:32 PM
  */
package object rethinkscala extends ImplicitConversions {


  private[rethinkscala] trait DatumOrFunction


  implicit val stringToStrings = new ToAst[String] {
    type TypeMember = Strings
  }
  implicit val doubleToNumeric = new ToAst[Double] {
    type TypeMember = Numeric
  }
  implicit val intToNumeric = new ToAst[Int] {
    type TypeMember = Numeric
  }
  implicit val floatToNumeric = new ToAst[Float] {
    type TypeMember = Numeric
  }
  implicit val anyToTyped = new ToAst[Any] {
    type TypeMember = Typed
  }
  implicit val docToTyped = new ToAst[Document] {
    type TypeMember = Var
  }

  object Async {

    object Connection {
      def apply(version: Version) = AsyncConnection(version)

    }

    implicit def toDelegate[T](produce: Produce[T])(implicit connection: AsyncConnection) = Delegate(produce, connection)


  }

  object Blocking {


    object Connection {
      def apply(version: Version): BlockingConnection = BlockingConnection(version)

    }

    implicit def toDelegate[T](produce: Produce[T])(implicit connection: BlockingConnection) = Delegate(produce, connection)


  }

  trait BlockingContext[T] extends Function[BlockingConnection, T]

  trait AsyncContext[T] extends Function[AsyncConnection, Future[T]]

  implicit def toBlockingContext[T](f: BlockingConnection => T) = new BlockingContext[T] {
    def apply(v1: BlockingConnection) = f(v1)
  }

  implicit def toAsyncContext[T](f: AsyncConnection => Future[T]) = new AsyncContext[T] {
    def apply(v1: AsyncConnection) = f(v1)
  }


  def block[T](c: Connection)(f: BlockingContext[T]) = f(BlockingConnection(c))

  def async[T](c: Connection)(f: AsyncContext[T]) = f(AsyncConnection(c))


  class CanMap[-From, -To, Out]


  implicit val canMapAny = new CanMap[Any, Strings, String]

  // implicit val canMapAnyInt = new CanMap[Any, Numeric, Double]
  implicit val canMapAnyDocument = new CanMap[Any, MapTyped, Document]

  implicit val canMapStringToString = new CanMap[String, Strings, String]
  implicit val canMapStringToInt = new CanMap[String, Numeric, Int]


  implicit val canMapIntToInt = new CanMap[Int, Numeric, Int]

  implicit val canMapDocToInt = new CanMap[Document, Numeric, Double]


  class CanFunctional[-Raw, -Ast]

  implicit val stringToFunctional = new CanFunctional[String, Strings]
  implicit val intToFunction = new CanFunctional[Int, Numeric]

  /*implicit val canMapAnyArray = new CanMap[Any, ArrayTyped[Any]] {
    type
  } */

  class ToFunctional[T, A >: Var, Reduce <: Typed](seq: Sequence[T]) {

    def map[B, Inner](f: A => B)(implicit cm: CanMap[T, B, Inner]) = RMap[Inner](seq.underlying, FuncWrap(f))

    def reduce(base: T, f: (A, A) => Reduce) = Reduce[T](seq.underlying, f, Some(base))

    def reduce(f: (A, A) => Reduce) = Reduce[T](seq.underlying, f, None)
  }


  // implicit def doubleToFunctional[T <: Double](seq: Sequence[]) = new ToFunctional[T, Numeric, Numeric](seq)

  // implicit def stringToFunctional(seq: Sequence[String]) = new ToFunctional[String, Strings, Strings](seq)

  // implicit def anyToFunctional(seq: Sequence[Any]) = new ToFunctional[Any, Var, Typed](seq)
  implicit def toFunctional[T](seq: Sequence[T])(implicit ast: ToAst[T]) = new ToFunctional[T, ast.TypeMember, Typed](seq)

  implicit def tableToFunctional[T <: Document](seq: Table[T]) = new ToFunctional[T, Var, Typed](seq)

  // implicit def docToFunctional[T <: Sequence[U], U, R >: Var](seq: Sequence[U])(implicit cf: CanFunctional[U, R]) = new ToFunctional[U, R, Typed](seq)


  //implicit def docToFunctional[T <: Document](seq: Sequence[T]) = new ToFunctional[T, Var, Typed](seq)


}
