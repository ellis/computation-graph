package ch.ethz.computationgraph

import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.TypeTag
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen

private case class ClassA(s: String, n: Int)

class EntityBaseSpec extends FunSpec with GivenWhenThen {
	describe("DataBase") {
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
		
		/*
		it("should read back equivalent JsValues as those set with time != Nil") {
			val jsval0 = ClassA("_", 0)
			val jsval1 = ClassA("a", 1)
			val jsval2 = ClassA("b", 2)
			val tkp = TKP("TABLE", "KEY", Nil)

			val db = new EntityBase

			// Database is empty, so element shouldn't be found.
			assert(db.get(tkp).isError)
			
			// Set object at time 0
			db.setAt(tkp, List(0), jsval0)
			// Object should now be found at time 0
			assert(db.getAt(tkp, List(0)) === RqSuccess(jsval0))

			// Update object at time 1
			db.setAt(tkp, List(1), jsval1)
			// Updated object should be found at time 1
			assert(db.getAt(tkp, List(1)) === RqSuccess(jsval1))
			
			// Update object again at time 2
			db.setAt(tkp, List(2), jsval2)
			// Updated object should be found at time 2
			assert(db.getAt(tkp, List(2)) === RqSuccess(jsval2))

			// No object set at time=Nil, so shouldn't find one
			assert(db.get(tkp).isError)
			// Should still find original object at time 0
			assert(db.getAt(tkp, List(0)) === RqSuccess(jsval0))
			// Should also find original object at time 0.1
			assert(db.getAt(tkp, List(0, 1)) === RqSuccess(jsval0))
			// Should still jsval1 at time 1
			assert(db.getAt(tkp, List(1)) === RqSuccess(jsval1))
			// Should find jsval2 at time 3
			assert(db.getAt(tkp, List(3)) === RqSuccess(jsval2))
		}
		
		it("should handle state changes when adding a new field") {
			val tkp = TKP("vesselState", "P1(A01)", Nil)
			val time11 = List(1, 1)
			val time1122 = List(1, 1, 2, 2)
			val time1122_+ = List(1, 1, 2, 2, Int.MaxValue)
			val jsval1 = JsonParser("""{"id":"P1(A01)","content":{"water":"100ul"}}""")
			val jsval2 = JsonParser("""{"id":"P1(A01)","content":{"water":0.00275},"isInitialVolumeKnown":null}""")
			
			val db = new DataBase
			db.setAt(tkp, List(0), jsval1)
			assert(db.getBefore(tkp, time11) === RqSuccess(jsval1))
			assert(db.getBefore(tkp, time1122) === RqSuccess(jsval1))

			info(db.toString)
			db.setAt(tkp, time1122_+, jsval2)
			info(db.toString)
			assert(db.getAt(tkp, time1122_+) === RqSuccess(jsval2))
			assert(db.getAt(tkp, List(0)) === RqSuccess(jsval1))
			assert(db.getBefore(tkp, time1122_+) === RqSuccess(jsval1))
			assert(db.getBefore(tkp, time1122) === RqSuccess(jsval1))
		}

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