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
				CallResultItem_Entity("output0", "Hello, World!")
			)
		},
		args = Nil
	)
	
	val call1 = Call(
		fn = (args: List[Object]) => {
			List(
				CallResultItem_Entity("message", s"Hello, ${args.head}!")
			)
		},
		args = Selector_Entity("name") :: Nil
	)
	
	val t1 = List(1)
	
	describe("ComputationGraph") {
		val tpeA = typeOf[ClassA]
		
		it("call `call0` should be placed in the graph at time 1") {
			val x0 = X()
			val x1 = x0.addCall(call0)
			assert(x1.g.nodes.toNodeInSet === Set(CallNode(t1, call0)))
		}
		
		it("call `call1` should be ready once its input is available") {
			val x0 = X()
			val x1 = x0.addCall(call1)
			println(x1.g)
			val cn1 = CallNode(t1, call1)
			assert(x1.g.nodes.toNodeInSet ===
				Set(
					cn1,
					EntityNode("name")
				)
			)
			assert(x1.timeToStatus(t1) === CallStatus.Waiting)
			
			// Set the input entity value to "World"
			val x2 = x1.setImmutableEntity("name", "World")
			// Graph nodes should be unchanged
			assert(x2.g.nodes.toNodeInSet ===
				Set(
					cn1,
					EntityNode("name")
				)
			)
			// The call should now be ready
			assert(x2.timeToStatus(t1) === CallStatus.Ready)
			
			val x3 = x2.step()
			println(x3.g)
			// Graph nodes be now also contain `message`
			assert(x3.g.nodes.toNodeInSet ===
				Set(
					cn1,
					EntityNode("name"),
					EntityNode("message")
				)
			)
			assert(x3.db.getEntity(t1, "message") === None)
			assert(x3.db.getEntity(List(2), "message") === Some("Hello, World!"))
			// The call should have succeeded
			assert(x2.timeToStatus(t1) === CallStatus.Success)
		}
		
		it("dependent functions should be automatically called") {
		}
	}
}