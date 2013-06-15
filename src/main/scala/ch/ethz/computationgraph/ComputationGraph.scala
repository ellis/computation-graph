package ch.ethz.computationgraph

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
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
case class SelectorNode(clazz: Class[_]) extends GraphNode {
	override def toString = clazz.toString+"*" //@"+time.map(_ + 1).mkString(".")
}

/**
 * Check status should be rechecked
 * InputMissing waiting for inputs to become available
 * InputReady inputs are all available
 * Next can be executed in the next step
 */
object CallStatus extends Enumeration {
	val Check, Waiting, Ready, Success, Error = Value
}

case class X(
	val g: Graph[GraphNode, UnDiEdge],
	val db: EntityBase3,
	val timeToCall: Map[List[Int], Call],
	val timeToIdToEntity: SortedMap[List[Int], Map[String, Object]],
	val timeToStatus: SortedMap[List[Int], CallStatus.Value]
) {
	def +(cmd: Command): X = {
		cmd match {
			case Command_SetEntity(time, id, entity) =>
				val g2 = {
					if (ListIntOrdering.compare(time, List(0)) <= 0)
						g + EntityNode(id)
					else
						g + DiEdge[GraphNode](CallNode(time, timeToCall(time)), EntityNode(id))
				}
				val db2 = db.setEntity(time, id, entity)
				val timeToIdToEntity2 = db2.getEntities
				val timeToStatus1 = callStatusCheck(g2, time, id)
				val timeToStatus2 = calcCallStatus(g2, db2, timeToCall, timeToIdToEntity2, timeToStatus1)
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
				val timeToStatus2 = calcCallStatus(g2, db2, timeToCall2, timeToIdToEntity2, timeToStatus)
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
		// Create graph with either the new node, or the node with and incoming edge from its parent
		val g0 = {
			if (time.tail.isEmpty)
				Graph[GraphNode, UnDiEdge](callNode)
			else {
				val parent = CallNode(time.init, timeToCall(time.init))
				Graph[GraphNode, UnDiEdge](parent ~> callNode)
			}
		}
		// Add edges from entities and selectors to the call node
		call.args.flatMap(selector => processSelector(callNode, selector)).foldLeft(g0)(_ + _)
	}
	
	private def processSelector(callNode: CallNode, selector: Selector): Graph[GraphNode, UnDiEdge] = {
		selector match {
			case s: Selector_Entity =>
				Graph[GraphNode, UnDiEdge](EntityNode(s.id) ~> callNode)
			case s: Selector_List =>
				Graph.from(Nil, s.ids.map(id => DiEdge[GraphNode](EntityNode(id), callNode)))
			case s: Selector_All => // FIXME: add handling for Selector_ALL
				Graph[GraphNode, UnDiEdge](SelectorNode(s.clazz) ~> callNode)
		}
	}
	
	/**
	 * Change call status to CallStatus.Check for relevant nodes which depend on the entity with ID `id`.
	 * The relevant nodes are those after time `time` and before the entity is set again by another call. 
	 */
	private def callStatusCheck(
		g: Graph[GraphNode, UnDiEdge],
		time: List[Int],
		id: String
	): SortedMap[List[Int], CallStatus.Value] = {
		// List of times at which this entity is set after time `time`
		val timesSet = g.get(EntityNode(id)).diPredecessors.toOuterNodes.collect({case n: CallNode if ListIntOrdering.compare(n.time, time) > 0 => n.time})
		// Last time point we'll check
		val last = if (timesSet.isEmpty) List(Int.MaxValue) else timesSet.min(ListIntOrdering)
		// Call nodes which depend on the given entity
		val callNodes = g.get(EntityNode(id)).diSuccessors.toOuterNodes.collect({case n: CallNode => n})
		val checks = callNodes.filter(n => ListIntOrdering.compare(n.time, time) > 0 && ListIntOrdering.compare(n.time, last) <= 0)
		timeToStatus ++ checks.map(n => n.time -> CallStatus.Check)
	}
	
	private def calcCallStatus(
		g: Graph[GraphNode, UnDiEdge],
		db: EntityBase3,
		timeToCall: Map[List[Int], Call],
		timeToIdToEntity: SortedMap[List[Int], Map[String, Object]],
		timeToStatus: SortedMap[List[Int], CallStatus.Value]
	): SortedMap[List[Int], CallStatus.Value] = {
		// REFACTOR: map over timeToStatus, and make timeToStatus use a SortedMap
		SortedMap(timeToCall.keys.toSeq.sorted(ListIntOrdering).map(time => {
			val idToEntities = timeToIdToEntity(time)
			val call = timeToCall(time)
			val status = {
				val status0 = timeToStatus.getOrElse(time, CallStatus.Check)
				if (status0 == CallStatus.Check) {
					val ready =
						// Get entities required by the call node
						g.get(CallNode(time, call)).diPredecessors.toOuterNodes.collect({case n: EntityNode => n})
						// check whether the database has values for them all
						.forall(n => idToEntities.contains(n.id))
					
					if (ready) CallStatus.Ready else CallStatus.Waiting
				}
				else
					status0
			}
			time -> status
		}).toSeq : _*)(ListIntOrdering)
	}
	
	def step(): X = {
		timeToStatus.filter(_._2 == CallStatus.Ready).foldLeft(this) { (acc0, pair) =>
			val (time, status) = pair
			val call = acc0.timeToCall(time)
			val idToEntity = acc0.timeToIdToEntity(time)
			val args = call.args.map(_ match {
				case s: Selector_Entity => idToEntity(s.id)
				case s: Selector_List => s.ids.map(idToEntity)
				case s: Selector_All => Nil // FIXME: add handling for Selector_ALL
			})
			val results = call.fn(args)
			// Set status of call to Success
			val acc1 = acc0.copy(timeToStatus = acc0.timeToStatus + (time -> CallStatus.Success))
			// Add resulting commands
			val commands = processCallResults(time, results)
			val acc2 = commands.foldLeft(acc1) { (acc, cmd) => acc + cmd }
			acc2
		}
	}
	
	private def processCallResults(time: List[Int], results: List[CallResultItem]): List[Command] = {
		var childIndex = 0
		results.map(_ match {
			case CallResultItem_Entity(id, entity) =>
				Command_SetEntity(time, id, entity)
			case CallResultItem_Event(id, fn) =>
				val call = Call(
					fn = (args: List[Object]) => {
						val entity0 = args.head
						val entity1 = fn(entity0)
						List(CallResultItem_Entity(id, entity1))
					},
					args = Selector_Entity(id) :: Nil
				)
				childIndex += 1
				Command_AddCall(time ++ List(childIndex), call)
			case CallResultItem_Call(call) =>
				childIndex += 1
				Command_AddCall(time ++ List(childIndex), call)
		})
	}
}

object X {
	def apply(): X =
		new X(Graph(), new EntityBase3(Map(), Map(), SortedMap()(ListIntOrdering)), Map(), SortedMap()(ListIntOrdering), SortedMap()(ListIntOrdering))
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

/*
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
*/