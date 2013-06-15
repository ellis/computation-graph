package ch.ethz.computationgraph

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.reflect.runtime.universe.Type
import scalaz._
import Scalaz._
import grizzled.slf4j.Logger
import scala.collection.mutable.ArrayBuffer
import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import scala.collection.SortedMap

trait Command
case class Command_SetEntity(time: List[Int], id: String, entity: Object) extends Command
case class Command_AddCall(time: List[Int], call: Call) extends Command

trait GraphNode
case class CallNode(time: List[Int], call: Call) extends GraphNode {
	override def toString = time.mkString(".")
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

case class X(
	val g: Graph[GraphNode, UnDiEdge],
	val db: EntityBase3,
	val timeToCall: Map[List[Int], Call],
	val timeToIdToEntity: SortedMap[List[Int], Map[String, Object]],
	val timeToStatus: Map[List[Int], CallStatus.Value]
) {
	def +(cmd: Command): X = {
		cmd match {
			case Command_SetEntity(time, id, entity) =>
				val g2 = g + EntityNode(id)
				val db2 = db.setEntity(time, id, entity)
				val timeToIdToEntity2 = db2.getEntities
				val timeToStatus2 = calcCallStatus(g2, db2, timeToCall, timeToIdToEntity2)
				new X(
					g2,
					db2,
					timeToCall,
					timeToIdToEntity2,
					timeToStatus2
				)
			case Command_AddCall(time, call) =>
				val g2 = g ++ addCall(time, call)
				val db2 = db.registerCall(time, None)
				val timeToCall2 = timeToCall + (time -> call)
				val timeToIdToEntity2 = db2.getEntities
				val timeToStatus2 = calcCallStatus(g2, db2, timeToCall2, timeToIdToEntity2)
				new X(
					g2,
					db2,
					timeToCall2,
					timeToIdToEntity2,
					timeToStatus2
				)
		}
	}
	
	def addCall(call: Call): X = {
		val time =
			if (timeToCall.isEmpty) List(1)
			else List(timeToCall.keys.max(ListIntOrdering).head + 1)
		this + Command_AddCall(time, call)
	}
	
	def setImmutableEntity(id: String, entity: Object): X = {
		this + Command_SetEntity(Nil, id, entity)
	}
	
	def setInitialState(id: String, entity: Object): X = {
		this + Command_SetEntity(List(0), id, entity)
	}

	/**
	 * When a call is added, its selectors are added to the selector/call graph
	 */
	private def addCall(time: List[Int], call: Call): Graph[GraphNode, UnDiEdge] = {
		val callNode = CallNode(time, call)
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
		g: Graph[GraphNode, UnDiEdge],
		db: EntityBase3,
		timeToCall: Map[List[Int], Call],
		timeToIdToEntity: SortedMap[List[Int], Map[String, Object]]
	): Map[List[Int], CallStatus.Value] = {
		timeToCall.map(pair => {
			val (time, call) = pair
			val status = timeToIdToEntity.get(time) match {
				case None => CallStatus.Waiting
				case Some(idToEntities) =>
					val ready =
						// Get entities required by the call node
						g.get(CallNode(time, call)).diPredecessors.toOuterNodes.collect({case n: EntityNode => n})
						// check whether the database has values for them all
						.forall(n => idToEntities.contains(n.id))
					if (ready) CallStatus.Ready else CallStatus.Waiting
			}
			time -> status
		})
	}
}

object X {
	def apply(): X =
		new X(Graph(), new EntityBase3(Map(), Map(), SortedMap()(ListIntOrdering)), Map(), SortedMap()(ListIntOrdering), Map())
}

/*
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
*/

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