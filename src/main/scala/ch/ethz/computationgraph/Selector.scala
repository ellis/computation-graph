package ch.ethz.computationgraph

import scala.reflect.runtime.universe.Type

trait Selector
case class Selector_Entity(tpe: Type, id: String, isOptional: Boolean = false) extends Selector
case class Selector_List(tpe: Type, ids: Seq[String], isOptional: Boolean = false) extends Selector
case class Selector_All(tpe: Type) extends Selector
