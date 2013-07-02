package ch.ethz.reactivesim

import scala.language.implicitConversions
import scala.reflect.Manifest

sealed trait Lookup[A] {
	val clazz: Class[_]
	val selector: Selector
}
case class Lookup_Entity[A : Manifest](id: String) extends Lookup[A] {
	val clazz = manifest[A].erasure
	val selector = Selector_Entity(id)
}
case class Lookup_List[A : Manifest](ids: Seq[String], isOptional: Boolean = false) extends Lookup[List[A]] {
	val clazz = manifest[List[A]].erasure
	val selector = Selector_List(ids)
}
case class Lookup_All[A : Manifest]() extends Lookup[List[A]] {
	val clazz = manifest[List[A]].erasure
	val selector = Selector_All(manifest[A].erasure)
}

trait Dsl {
	
	def input()(fn: Unit => RsResult[List[CallResultItem]]): Call = {
		Call(
			fn = (inputs: List[Object]) => {
				fn()
			},
			selectors = List[Selector]()
		)
	}
	
	def input[A](a: Lookup[A])(fn: (A) => RsResult[List[CallResultItem]]): Call = {
		Call(
			fn = (inputs: List[Object]) => {
				val a1 = inputs(0).asInstanceOf[A]
				fn(a1)
			},
			selectors = List[Selector](a.selector)
		)
	}
	
	def as[A : Manifest](id: String): Lookup_Entity[A] = Lookup_Entity[A](id)
	
	def output(items: CallResultItem*): RsResult[List[CallResultItem]] = RsSuccess(items.toList)
	
	implicit def pairToResultEntity(pair: (String, Object)) = CallResultItem_Entity(pair._1, pair._2)
	implicit def callToResult(call: Call) = CallResultItem_Call(call)
	
}