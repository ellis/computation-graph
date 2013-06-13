package ch.ethz.computationgraph

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.reflect.runtime.universe.Type
import scalaz._
import Scalaz._
import grizzled.slf4j.Logger
import scala.collection.mutable.ArrayBuffer
import scalax.collection.Graph, scalax.collection.GraphPredef._, scalax.collection.GraphEdge._

trait Command
case class Command_SetEntity(tpe: Type, id: String, time: List[Int], entity: Object) extends Command
case class Command_AddCall(call: Call, time: List[Int]) extends Command

trait GraphNode
case class CallNode(time: List[Int]) extends GraphNode {
	override def toString = time.map(_ + 1).mkString(".")
}
case class EntityNode(id: String) extends GraphNode {
	override def toString = id
}
case class SelectorNode(tpe: Type) extends GraphNode {
	override def toString = tpe.toString+"*" //@"+time.map(_ + 1).mkString(".")
}

object CallStatus extends Enumeration {
	val Waiting, Ready, Success, Error = Value
}

class X(
	timeToCall: Map[List[Int], Call],
	db: EntityBase2,
	g: Graph[GraphNode, UnDiEdge]
) {
	def +(cmd: Command): X = {
		cmd match {
			case Command_SetEntity(tpe, id, time, entity) =>
				val db2 = db.addEntity(tpe, id, time, entity)
				new X(
					timeToCall,
					db2,
					g
				)
			case Command_AddCall(call, time) =>
				val timeToCall2 = timeToCall + (time -> call)
				val g2 = g ++ addCall(call, time)
				new X(
					timeToCall2,
					db,
					g2
				)
		}
	}

	/**
	 * When a call is added, its selectors are added to the selector/call graph
	 */
	private def addCall(call: Call, time: List[Int]): Graph[GraphNode, UnDiEdge] = {
		val callNode = CallNode(time)
		val g0 = Graph[GraphNode, UnDiEdge](callNode)
		call.args.flatMap(selector => processSelector(callNode, selector)).foldLeft(g0)(_ + _)
	}
	
	private def processSelector(callNode: CallNode, selector: Selector): Graph[GraphNode, UnDiEdge] = {
		selector match {
			case s: Selector_Entity =>
				Graph[GraphNode, UnDiEdge](EntityNode(s.id) ~> callNode)
			case s: Selector_List =>
				Graph.from(Nil, s.ids.map(id => DiEdge[GraphNode](EntityNode(id), callNode)))
			case s: Selector_All => // FIXME: add handling for Selector_ALL
				Graph[GraphNode, UnDiEdge](SelectorNode(s.tpe) ~> callNode)
		}
	}
}

class ComputationGraphBuilder {
	val db = new EntityBase
	var g = Graph[GraphNode, UnDiEdge]()
	private val calls = new ArrayBuffer[Call]
	private val callToTime = new HashMap[Call, List[Int]]
	private val timeToCall = new HashMap[List[Int], Call]
	
	/**
	 * Add a top-level call
	 */
	def addCall(call: Call) {
		calls += call
		addCall(call, List(calls.size))
	}
	
	/**
	 * When a call is added, its selectors are added to the selector/call graph
	 */
	def addCall(call: Call, time: List[Int]) {
		val callNode = CallNode(time)
		g = g + callNode
		callToTime(call) = time
		timeToCall(time) = call
		for (selector <- call.args) {
			processSelector(callNode, selector)
		}
	}
	
	private def processSelector(callNode: CallNode, selector: Selector) {
		selector match {
			case s: Selector_Entity =>
				g = g ++ Graph[GraphNode, UnDiEdge](EntityNode(s.id) ~> callNode)
			case s: Selector_List =>
				s.ids.foreach(id => g = g ++ Graph[GraphNode, UnDiEdge](EntityNode(id) ~> callNode))
			case s: Selector_All => // FIXME: add handling for Selector_ALL
				g = g ++ Graph[GraphNode, UnDiEdge](SelectorNode(s.tpe) ~> callNode)
		}
	}
	
	def run() {
		
	}
	
	/*
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
	*/
	
	
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