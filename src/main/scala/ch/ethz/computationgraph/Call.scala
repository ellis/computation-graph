package ch.ethz.computationgraph

import scala.reflect.runtime.universe.Type

case class Call(fn: List[Object] => List[CallResultItem])

trait CallResultItem
case class CallResultItem_Entity(tpe: Type, id: String, entity: Object) extends CallResultItem
case class CallResultItem_Event(tpe: Type, id: String, fn: Object => Object) extends CallResultItem
case class CallResultItem_Call(call: Call) extends CallResultItem
