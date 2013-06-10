package ch.ethz.computationgraph

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.TypeTag
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen

private case class ClassA(s: String, n: Int)

class EntityBaseSpec extends FunSpec with GivenWhenThen {
	describe("DataBase") {
		val tpeA = typeOf[ClassA]
		
		it("should read back the same entities as those stored, with time=Nil") {
			val db = new EntityBase
			val l = List[(String, ClassA)](
				"1" -> ClassA("_", 0),
				"2" -> ClassA("a", 1),
				"3" -> ClassA("b", 2)
			)
			for ((id, entity) <- l) {
				db.storeEntity(typeOf[ClassA], id, Nil, entity)
				val selector = Selector_Entity(typeOf[ClassA], id)
				val jsval2_? = db.selectEntity(selector, Nil)
				assert(jsval2_? === Some(entity))
			}
		}
		
		it("should read back equivalent entities as those stored with time != Nil") {
			val db = new EntityBase
			val jsval0 = ClassA("_", 0)
			val jsval1 = ClassA("a", 1)
			val jsval2 = ClassA("b", 2)
			val selector0 = Selector_Entity(tpeA, "ID")

			// Database is empty, so element shouldn't be found.
			assert(db.selectEntity(selector0, Nil).isEmpty)
			
			// Set object at time 0
			db.storeEntity(tpeA, "ID", List(0), jsval0)
			// Object should now be found at time 0
			assert(db.selectEntity(selector0, List(0)) === Some(jsval0))

			// Update object at time 1
			db.storeEntity(tpeA, "ID", List(1), jsval1)
			// Updated object should be found at time 1
			assert(db.selectEntity(tpeA, "ID", List(1)) === Some(jsval1))
			
			// Update object again at time 2
			db.storeEntity(tpeA, "ID", List(2), jsval2)
			// Updated object should be found at time 2
			assert(db.selectEntity(tpeA, "ID", List(2)) === Some(jsval2))

			// No object set at time=Nil, so shouldn't find one
			assert(db.selectEntity(tpeA, "ID", Nil).isEmpty)
			// Should still find original object at time 0
			assert(db.selectEntity(tpeA, "ID", List(0)) === Some(jsval0))
			// Should also find original object at time 0.1
			assert(db.selectEntity(tpeA, "ID", List(0, 1)) === Some(jsval0))
			// Should still jsval1 at time 1
			assert(db.selectEntity(tpeA, "ID", List(1)) === Some(jsval1))
			// Should find jsval2 at time 3
			assert(db.selectEntity(tpeA, "ID", List(3)) === Some(jsval2))
		}
		
		it("should handle state changes") {
			val time11 = List(1, 1)
			val time1122 = List(1, 1, 2, 2)
			val time1123 = List(1, 1, 2, 3)
			val time1124 = List(1, 1, 2, 4)
			val jsval1 = ClassA("a", 1)
			val jsval2 = ClassA("b", 2)
			val selector = Selector_Entity(tpeA, "ID")
			
			val db = new EntityBase
			db.storeEntity(tpeA, "ID", time11, jsval1)
			assert(db.selectEntity(selector, List(0)) === None)
			assert(db.selectEntity(selector, time11) === Some(jsval1))
			assert(db.selectEntity(selector, time1122) === Some(jsval1))

			//info(db.toString)
			db.storeEntity(tpeA, "ID", time1123, jsval2)
			//info(db.toString)
			assert(db.selectEntity(selector, Nil) === None)
			assert(db.selectEntity(selector, List(0)) === None)
			assert(db.selectEntity(selector, time11) === Some(jsval1))
			assert(db.selectEntity(selector, time1122) === Some(jsval1))
			assert(db.selectEntity(selector, time1123) === Some(jsval2))
			assert(db.selectEntity(selector, time1124) === Some(jsval2))
		}
		/*
		it("should read back all entities in table with getAll()") {
			val jsvalA = JsObject("s" -> JsString("a"), "n" -> JsNumber(1))
			val jsvalB = JsObject("s" -> JsString("b"), "n" -> JsNumber(2))
			val jsvalC = JsObject("s" -> JsString("c"), "n" -> JsNumber(3))
			val jsvalX = JsObject("s" -> JsString("x"), "n" -> JsNumber(9))

			val db = new DataBase
			
			db.set(TKP("TABLE", "A", Nil), jsvalA)
			db.set(TKP("TABLE", "B", Nil), jsvalB)
			db.set(TKP("TABLE", "C", Nil), jsvalC)
			db.set(TKP("TABLEX", "X", Nil), jsvalX)

			// Object should now be found at time 0
			assert(db.getAll("TABLE").toSet === Set(jsvalA, jsvalB, jsvalC))
		}
		*/
	}
}