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

/**
 * Waiting waiting for inputs to become available
 * Ready inputs are all available
 * Next can be executed in the next step
 */
object CallStatus extends Enumeration {
	val Waiting, Ready, Next, Success, Error = Value
}

class X(
	val timeToCall: Map[List[Int], Call],
	val db: EntityBase2,
	val g: Graph[GraphNode, UnDiEdge]
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
	
	private def calcCallStatus(
		timeToCall: Map[List[Int], Call],
		db: EntityBase2,
		g: Graph[GraphNode, UnDiEdge],
		time: List[Int]
	) {
		val ready = g.get(CallNode(time)).incoming.filter(_.isInstanceOf[EntityNode]).forall(n => db.contains(n.asInstanceOf[EntityNode].id, time))
		
	}
	
	/**
	 * Return all ready nodes which don't depend on state,
	 * plus the next node which depends on state after exclusively successful nodes.
	 */
	private def makePendingComputationList(node_l: List[Node]): List[NodeState] = {
		val order_l = state_m.values.toList.sortBy(_.node.time)(ListIntOrdering).dropWhile(_.status == Status.Success)
		// Find first node which isn't ready and depends on state
		val blocker_? = order_l.find(state => state.status == Status.NotReady && state.node.input_l.exists(_.kc.key.table.endsWith("State")))
		val timeEnd = blocker_?.map(_.node.time).getOrElse(List(Int.MaxValue))
		println()
		println(s"makePending (<= $timeEnd)")
		order_l.foreach(state => 
			println(state.node.time + " " + state.status.toString.take(1) + " " + state.node.id + ": " + state.node.contextKey_?.map(_.id + " ").getOrElse("") + state.node.desc)
		)
		println()
		
		order_l.filter(state => state.status == Status.Ready && ListIntOrdering.compare(state.node.time, timeEnd) <= 0)
		/*
		//val order_l = state_m.toList.sortBy(_._1.path)(ListIntOrdering).map(_._2).dropWhile(_.status == Status.Success)
		order_l match {
			case Nil => Nil
			case next :: _ =>
				val timeNext = next.node.time
				val (prefix_l, suffix_l) = order_l.span(_.node.time == timeNext)
				val next_l = prefix_l.filter(_.status == Status.Ready)
				val ready_l = suffix_l.filter(state => {
					// Node is ready
					state.status == Status.Ready &&
					// And it doesn't depend on state
					state.node.input_l.forall(kco => !kco.kc.key.table.endsWith("State"))
				})
				// Take next node if it's ready (regardless of whether it depends on state)
				next_l ++ ready_l
		}*/
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