package ch.ethz.computationgraph

import scala.language.existentials
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.collection.SortedMap
import scala.math.Ordering
import scala.util.Try
import scalaz._
import grizzled.slf4j.Logger

case class EntityBaseCall(
	includes_? : Option[Set[String]],
	excludes_? : Option[Set[String]],
	idToEntities_? : Option[Map[String, Object]]
)

class EntityBase3(
	immutables: Map[String, Object],
	initials: Map[String, Object],
	calls: SortedMap[List[Int], EntityBaseCall]
) {
	private val logger = Logger[this.type]

	/**
	 * @param constraints_? An optional list of IDs of entities which this call might modify.  If set to Some(Nil), then no entities will be modified.  If set to None, all entities may potentially be modified.
	 */
	def registerCall(time: List[Int], includes_? : Option[Iterable[String]]): EntityBase3 = {
		val call = EntityBaseCall(includes_?.map(_.toSet), None, None)
		new EntityBase3(
			immutables,
			initials,
			calls + (time -> call)
		)
	}
	
	def setEntity(time: List[Int], id: String, entity: Object): EntityBase3 = {
		time match {
			case Nil =>
				new EntityBase3(
					immutables + (id -> entity),
					initials,
					calls
				)
			case 0 :: Nil =>
				new EntityBase3(
					immutables,
					initials + (id -> entity),
					calls
				)
			case _ =>
				calls.get(time) match {
					case None =>
						logger.error(s"tried to set entity on an unregistered call: time=$time, id=$id")
						this
					case Some(call) =>
						val call2 = call.copy(idToEntities_? = Some(call.idToEntities_?.getOrElse(Map()) + (id -> entity)))
						new EntityBase3(
							immutables,
							initials,
							calls + (time -> call2)
						)
				}
		}
	}
	
	def getEntities(): SortedMap[List[Int], Map[String, Object]] = {
		var next = initials
		val m = calls.map(pair => {
			val (time, call) = pair
			val idToEntities = next
			next = call.idToEntities_? match {
				case None => Map()
				case Some(idToEntities) => next ++ idToEntities
			}
			time -> (idToEntities ++ immutables)
		})
		SortedMap(m.toSeq : _*)(ListIntOrdering)
	}
}
