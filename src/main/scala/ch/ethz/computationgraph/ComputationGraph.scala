package ch.ethz.computationgraph

import scala.collection.mutable.HashMap
import scalaz._
import Scalaz._
import grizzled.slf4j.Logger

class ComputationGraph {
	private val logger = Logger[this.type]

	val db = new EntityBase
	val selectorToValue_m = new HashMap[Selector, Object]
	
	def processCall(call: Call, time: List[Int]) {
		call.args.map(selector => lookup(selector, time)).sequence match {
			case None =>
			case Some(args) =>
				try {
					val results = call.fn(args)
					processResults(results, time)
				} catch {
					case ex: Throwable =>
						logger.error(ex.getMessage())
				}
		}
	}
	
	def lookup(selector: Selector, time: List[Int]): Option[Object] = {
		selector match {
			case s: Selector_Entity => db.selectEntity(s, time)
			case _ => None
		}
	}
	
	def incTime(time: List[Int]): List[Int] = {
		time match {
			case Nil => Nil
			case n :: Nil => (n + 1) :: Nil
			case n :: rest => n :: incTime(rest)
		}
	}
	
	def processResults(results: List[CallResultItem], time: List[Int]) {
		val time1 = incTime(time)
		logger.debug(results)
		for (result <- results) {
			result match {
				case r: CallResultItem_Entity =>
					db.storeEntity(r.tpe, r.id, time1, r.entity)
				case r: CallResultItem_Event =>
				case r: CallResultItem_Call =>
					r.call
			}
		}
	}
}