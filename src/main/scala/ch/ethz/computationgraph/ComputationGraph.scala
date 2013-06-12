package ch.ethz.computationgraph

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scalaz._
import Scalaz._
import grizzled.slf4j.Logger
import scala.collection.mutable.ArrayBuffer
import scalax.collection.Graph, scalax.collection.GraphPredef._, scalax.collection.GraphEdge._

trait Command

case class Command_AddEntity
case class Command_AddCall(call: Call, time: List[Int]) extends Command

class ComputationGraphBuilder {
	val db = new EntityBase
	val selectorToValue_m = new HashMap[Selector, Object]
	val call_l = new ArrayBuffer[Call]
	val callChildren_m = new HashMap[Call, List[Call]]
	val selectorToCall_l = new HashSet[(Selector, Call)]
	
	/**
	 * When a call is added, its selectors are added to the selector/call graph
	 */
	def addCall(call: Call, time: List[Int]) {
		call_l += call
		for (selector <- call.args)
			selectorToCall_l += (selector -> call)
	}
}

class ComputationGraph {
	private val logger = Logger[this.type]

	val db = new EntityBase
	val selectorToValue_m = new HashMap[Selector, Object]
	val call_l = new ArrayBuffer[Call]
	val callChildren_m = new HashMap[Call, List[Call]]
	
	def addCall(call: Call) {
		call_l += call
	}
	
	def run() {
		
	}
	
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