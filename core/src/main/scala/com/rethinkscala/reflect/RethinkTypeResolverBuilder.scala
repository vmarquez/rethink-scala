package com.rethinkscala.reflect

import java.util

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.`type`.CollectionLikeType
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder
import com.fasterxml.jackson.databind.jsontype.{NamedType, TypeDeserializer}
import com.fasterxml.jackson.databind.{DeserializationConfig, JavaType}
import com.rethinkscala._

/**
 * Created with IntelliJ IDEA.
 * User: keyston
 * Date: 3/29/14
 * Time: 6:43 PM
 *
 */

class RethinkTypeResolverBuilder extends StdTypeResolverBuilder {


  val classGroupResultRecordName = classOf[GroupResultRecord[_]].getName
  val classGroupRecord = classOf[GroupResult[_]]


  val classOfPoint = classOf[Point]
  val classOfPolygon = classOf[Polygon]
  val classOfGeometryType = classOf[GeometryType]

  override def buildTypeDeserializer(config: DeserializationConfig, baseType: JavaType, subtypes: util.Collection[NamedType]): TypeDeserializer = {
    if (_idType == JsonTypeInfo.Id.NONE) {
      return null
    }
    println(baseType)

    baseType match {
      case ct: CollectionLikeType if classGroupRecord.isAssignableFrom(ct.getRawClass) => GroupResultTypeDeserializer(baseType, null, _typeProperty, _typeIdVisible, _defaultImpl)


      case _ => null
    }


  }


}
