package ch.ethz.computationgraph

import scala.reflect.runtime.universe.Type

case class Call(fn: List[Object] => List[CallResultItem], args: List[Selector])

trait CallResultItem
case class CallResultItem_Entity(id: String, entity: Object) extends CallResultItem
case class CallResultItem_Event(id: String, fn: Object => Object) extends CallResultItem
case class CallResultItem_Call(call: Call) extends CallResultItem
