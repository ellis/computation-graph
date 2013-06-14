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
	
	describe("ComputationGraph") {
		val tpeA = typeOf[ClassA]
		
		it("Call to `call0` should store output entity at next time step") {
			val x0 = X()
			val x1 = x0.addCall(call0)
			println(x1.g)
		}
		
		it("calls should only be invoked when all inputs are available") {
			val x0 = X()
			val x1 = x0.addCall(call1)
			println(x1.g)
			//cgb.db.storeEntity(typeOf[String], "name", List(0), "John")
		}
		
		it("dependent functions should be automatically called") {
		}
	}
}