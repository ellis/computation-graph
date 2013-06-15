package ch.ethz.computationgraph

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.TypeTag
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
/*
class ComputationGraphSpec extends FunSpec with GivenWhenThen {
	
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
			val cg = new ComputationGraph
			cg.processCall(call0, List(0))
			assert(cg.db.selectEntity(typeOf[String], "output0", List(0)) === None)
			assert(cg.db.selectEntity(typeOf[String], "output0", List(1)) === Some("Hello, World!"))
		}
		
		it("calls should only be invoked when all inputs are available") {
			val cg = new ComputationGraph
			cg.processCall(call1, List(0))
			assert(cg.db.selectEntity(typeOf[String], "message", List(1)) === None)
			cg.db.storeEntity(typeOf[String], "name", List(0), "John")
			cg.processCall(call1, List(0))
			assert(cg.db.selectEntity(typeOf[String], "message", List(1)) === Some("Hello, John!"))
		}
		
		it("dependent functions should be automatically called") {
		}
	}
}*/