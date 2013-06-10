package ch.ethz.computationgraph

import scala.language.existentials
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.collection._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.math.Ordering
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.Type
import scala.reflect.runtime.universe.TypeTag
import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.typeTag
import scala.util.Try
import scalaz._
import grizzled.slf4j.Logger


class EntityBase {
	private val logger = Logger[this.type]

	private implicit val listIntOrdering = ListIntOrdering
	private val idToTimeToEntity_m = new HashMap[String, (Type, SortedMap[List[Int], Object])]

	def storeEntity(tpe: Type, id: String, time: List[Int], entity: Object) {
		idToTimeToEntity_m.get(id) match {
			case None =>
				idToTimeToEntity_m(id) = (tpe, SortedMap(time -> entity))
			case Some((tpe0, timeToEntity_m0)) =>
				if (tpe0 != tpe)
					logger.error(s"type mismatch for id `$id`")
				else
					idToTimeToEntity_m(id) = (tpe, timeToEntity_m0 + (time -> entity))
		}
	}
	
	def selectEntity(tpe: Type, id: String, time: List[Int], isOptional: Boolean = false): Option[Object] = {
		idToTimeToEntity_m.get(id) match {
			case None => None
			case Some((tpe0, timeToEntity_m0)) =>
				if (tpe0 != tpe) {
					logger.error(s"type mismatch for id `${id}`")
					None
				}
				else
					entityAtOrBeforeTime(timeToEntity_m0, time)
		}
	}

	def selectEntity(selector: Selector_Entity, time: List[Int]): Option[Object] =
		selectEntity(selector.tpe, selector.id, time, selector.isOptional)
	
	private def entityAtOrBeforeTime(timeToEntity_m: SortedMap[List[Int], Object], time: List[Int]): Option[Object] = {
		var entity_? : Option[Object] = None
		for ((time0, entity0) <- timeToEntity_m) {
			if (ListIntOrdering.compare(time0, time) <= 0)
				entity_? = Some(entity0)
			else
				return entity_?
		}
		entity_?
	}

}