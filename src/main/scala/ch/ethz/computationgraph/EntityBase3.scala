/*

Copyright 2013 Ellis Whitehead

This file is part of ComputationGraph.

ComputationGraph is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ComputationGraph is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationGraph.  If not, see <http://www.gnu.org/licenses/>
*/
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
	idToEntity_? : Option[Map[String, Object]]
)

case class EntityBase3(
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
	
	def setEntities(time: List[Int], pairs: (String, Object)*): EntityBase3 = {
		time match {
			case Nil =>
				new EntityBase3(
					immutables ++ pairs,
					initials,
					calls
				)
			case 0 :: Nil =>
				new EntityBase3(
					immutables,
					initials ++ pairs,
					calls
				)
			case _ =>
				calls.get(time) match {
					case None =>
						logger.error(s"tried to set entity on an unregistered call: time=$time")
						this
					case Some(call) =>
						val call2 = call.copy(idToEntity_? = Some(call.idToEntity_?.getOrElse(Map()) ++ pairs))
						new EntityBase3(
							immutables,
							initials,
							calls + (time -> call2)
						)
				}
		}
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
						val call2 = call.copy(idToEntity_? = Some(call.idToEntity_?.getOrElse(Map()) + (id -> entity)))
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
			val idToEntity = next
			next = call.idToEntity_? match {
				case None => Map()
				case Some(idToEntity) => next ++ idToEntity
			}
			time -> (idToEntity ++ immutables)
		})
		// The final time is a one-number list, whose value is higher than any calls in the database
		val timeEnd = List(calls.keys.lastOption.getOrElse(List(0)).head + 1)
		val end = timeEnd -> (next ++ immutables)
		SortedMap((m + end).toSeq : _*)(ListIntOrdering)
	}
	
	def getEntity(time: List[Int], id: String): Option[Object] = {
		time match {
			case Nil => immutables.get(id)
			case List(0) => initials.get(id)
			case _ =>
				val timeToIdToEntity = getEntities()
				timeToIdToEntity.get(time).flatMap(_.get(id))
		}
	}
}

object EntityBase3 {
	val zero = new EntityBase3(Map(), Map(), SortedMap()(ListIntOrdering)) 
}
