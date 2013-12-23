package com.rethinkscala.net


import com.rethinkscala.reflect.Reflector
import com.rethinkscala.{GeneratesKeys, Term, Document}
import com.rethinkscala.ast.{After, WithLifecycle}

object Translate {
  def translate[Out](implicit ct: Manifest[Out]): Translate[Out] = new BaseTranslate[Out] {}


}

trait Translate[Out] {
  lazy val fromMap = new MapConversion[Out] {
    protected def _convert(json: String)(implicit ct: Manifest[Out]) = {
      val out = Reflector.fromJson[Out](json)
      val doc = out.asInstanceOf[Document]
      doc.underlying = Reflector.fromJson[Map[String, Any]](json)
      doc.raw = json
      out
    }
  }

  def write(value: Out): Any

  def read(json: String, term: Term)(implicit ct: Manifest[Out]): Out

}

trait WithConversion[Out] {

  def convert(json: String, term: Term)(implicit ct: Manifest[Out]): Out = {

    val rtn = _convert(json)

    if (rtn.isInstanceOf[GeneratesKeys]) {
      val gks = rtn.asInstanceOf[GeneratesKeys]

      term match {
        case wlf: WithLifecycle[_] => wlf(After(gks.generatedKeys))

        case _ =>
      }

    }
    rtn

  }

  protected def _convert(json: String)(implicit ct: Manifest[Out]): Out
}

trait WithIterableConversion[Out] extends WithConversion[Iterable[Out]] {
  protected def _convert(value: Iterable[Map[String, _]], json: String)(implicit ct: Manifest[Out]) = {
    val isDocument = classOf[Document] isAssignableFrom ct.runtimeClass
    val out = Reflector.fromJson[Iterable[Out]](json)

    if (isDocument) {
      val seq = value.toSeq
      out.zipWithIndex foreach {
        case (o: Document, i) => o.underlying = seq(i)
      }
    }
    out
  }
}

trait MapConversion[Out] extends WithConversion[Out] {
  def asMap(json: String) = Reflector.fromJson[Map[String, Any]](json)
}


trait BinaryConversion extends MapConversion[Boolean] {
  private[rethinkscala] val resultField: String


  protected def _convert(json: String)(implicit mf: Manifest[Boolean]): Boolean = asMap(json).get(resultField).getOrElse(0) == 1
}

trait DocumentConversion[Out <: Document] extends MapConversion[Out] {

  protected def _convert(json: String)(implicit ct: Manifest[Out]): Out = {
    val out = Reflector.fromJson[Out](json)
    out.underlying = Reflector.fromJson[Map[String, Any]](json)
    out.raw = json
    out
  }

}

trait BaseTranslate[Out] extends Translate[Out] {

  type Conversion = WithConversion[Out]

  def write(value: Out): Any = value

  def read(json: String, term: Term)(implicit ct: Manifest[Out]): Out = {

    val isDocument = classOf[Document] isAssignableFrom ct.runtimeClass
    term match {
      case c: Conversion => c.convert(json, term)

      case _ => if (isDocument) fromMap.convert(json, term) else Reflector.fromJson[Out](json)

    }

  }
}

