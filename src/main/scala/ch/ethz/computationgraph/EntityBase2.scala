/*package ch.ethz.computationgraph

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

class EntityBase2(
	timeToIdToEntity: SortedMap[List[Int], SortedMap[String, Object]],
	idToType: Map[String, Type]
) {
	def addEntity(tpe: Type, id: String, time: List[Int], entity: Object): EntityBase2 = {
		timeToIdToEntity.get(time) match {
			case None =>
				new EntityBase2(
					timeToIdToEntity + (time -> SortedMap(id -> entity)),
					idToType + (id -> tpe)
				)
			case Some(idToEntity) =>
				assert(idToType(id) =:= tpe)
				new EntityBase2(
					timeToIdToEntity + (time -> (idToEntity + (id -> entity))),
					idToType
				)
		}
	}
	
	def getEntity(tpe: Type, id: String, time: List[Int]): Option[Object] = {
		var entity_? : Option[Object] = None
		for ((time2, idToEntity) <- timeToIdToEntity if ListIntOrdering.compare(time2, time) <= 0) {
			idToEntity.get(id).foreach(entity => entity_? = Some(entity))
		}
		entity_?
	}
	
	def contains(id: String, time: List[Int]): Boolean = {
		for ((time2, idToEntity) <- timeToIdToEntity if ListIntOrdering.compare(time2, time) <= 0 && idToEntity.contains(id)) {
			return true
		}
		return false
	}
}
*/