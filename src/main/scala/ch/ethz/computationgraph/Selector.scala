package ch.ethz.computationgraph

trait Selector
case class Selector_Entity(id: String, isOptional: Boolean = false) extends Selector
case class Selector_List(ids: Seq[String], isOptional: Boolean = false) extends Selector
case class Selector_All(clazz: Class[_]) extends Selector
