package ch.ethz.computationgraph

import scala.collection.mutable.HashMap

class ComputationGraph {
	val db = new EntityBase
	val selectorToValue_m = new HashMap[Selector, Object]
	
	def processCall(call: Call, time: List[Int]) {
		val values = call.args.map(selector => lookup(selector, time))
	}
	
	def lookup(selector: Selector, time: List[Int]): Option[Object] = {
		selector match {
			case s: Selector_Entity => db.selectEntity(s, time)
			case _ => None
		}
	}
}