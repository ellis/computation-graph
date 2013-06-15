package ch.ethz.computationgraph

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.TypeTag
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen

class ComputationGraphBuilderSpec extends FunSpec with GivenWhenThen {
	
	val call0 = Call(
		fn = (args: List[Object]) => {
			List(
				CallResultItem_Entity(typeOf[String], "output0", "Hello, World!")
			)
		},
		args = Nil
	)
	
	val call1 = Call(
		fn = (args: List[Object]) => {
			List(
				CallResultItem_Entity(typeOf[String], "message", s"Hello, ${args.head}!")
			)
		},
		args = Selector_Entity(typeOf[String], "name") :: Nil
	)
	
	val t1 = List(1)
	
	describe("ComputationGraph") {
		val tpeA = typeOf[ClassA]
		
		it("call `call0` should be placed in the graph at time 1") {
			val x0 = X()
			val x1 = x0.addCall(call0)
			assert(x1.g.nodes.toNodeInSet === Set(CallNode(t1, call0)))
		}
		
		it("call `call1` should ready once its input is available") {
			val x0 = X()
			val x1 = x0.addCall(call1)
			println(x1.g)
			assert(x1.g.nodes.toNodeInSet ===
				Set(
					CallNode(t1, call1),
					EntityNode("name")
				)
			)
			assert(x1.timeToStatus(t1) === CallStatus.Waiting)
			
			// Set the input entity value to "John"
			val x2 = x1.setImmutableEntity("name", "John")
			// Graph nodes should be unchanged
			assert(x1.g.nodes.toNodeInSet ===
				Set(
					CallNode(t1, call1),
					EntityNode("name")
				)
			)
			// The call should now be ready
			assert(x2.timeToStatus(t1) === CallStatus.Ready)
		}
		
		it("dependent functions should be automatically called") {
		}
	}
}