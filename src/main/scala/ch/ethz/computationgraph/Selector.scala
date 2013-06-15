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

trait Selector
case class Selector_Entity(id: String, isOptional: Boolean = false) extends Selector
case class Selector_List(ids: Seq[String], isOptional: Boolean = false) extends Selector
case class Selector_All(clazz: Class[_]) extends Selector
