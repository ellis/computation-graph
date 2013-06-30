package ch.ethz.computationgraph

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

//sealed trait OutputItem
//case class OutputItem_

class Example2 {
	val A = Container("A")
	val B = Container("B")
	
	def input()(fn: Unit => List[CallResultItem]): Call = {
		Call(
			fn = (inputs: List[Object]) => {
				fn()
			},
			args = List[Selector]()
		)
	}
	
	def input[A](a: Lookup[A])(fn: (A) => List[CallResultItem]): Call = {
		Call(
			fn = (inputs: List[Object]) => {
				val a1 = inputs(0).asInstanceOf[A]
				fn(a1)
			},
			args = List[Selector](a.selector)
		)
	}
	
	def as[A : Manifest](id: String): Lookup_Entity[A] = Lookup_Entity[A](id)
	
	def output(items: CallResultItem*): List[CallResultItem] = items.toList
	
	implicit def pairToResultEntity(pair: (String, Object)) = CallResultItem_Entity(pair._1, pair._2)
	
	def exec(command: RobotCommand): CallResultItem_Call = {
		val call = command match {
			case Aspirate(volume, container) =>
				input (as[ContainerState](container.id)) {
					(state) =>
					output(container.id -> state.copy(volume = state.volume - volume))
				}
			case Dispense(volume, container) =>
				input (as[ContainerState](container.id)) {
					(state) =>
					output(container.id -> state.copy(volume = state.volume + volume))
				}
			case Measure(container) =>
				val selectors = List[Selector](Selector_Entity(container.id))
				val fn = (inputs: List[Object]) => {
					val state = inputs(0).asInstanceOf[ContainerState]
					List[CallResultItem](CallResultItem_Entity(container.id, state))
				}
				Call(fn, selectors)
		}
		CallResultItem_Call(call)
	}
	
	def x(): List[CallResultItem] = x(0, 10)
	
	def x(index: Int, volume: Double): List[CallResultItem] = {
		exec(Aspirate(volume, A)) :: exec(Dispense(volume, B)) :: exec(Measure(B)) :: check(index, 10) :: Nil
	}
	
	def check(index: Int, target: Double): CallResultItem = {
		val selectors = List[Selector](Selector_Entity(s"measurement$index"))
		val fn = (inputs: List[Object]) => {
			val measurement = inputs(0).asInstanceOf[java.lang.Double]
			val f = measurement / target
			if (f > 1.1)
				exec(AlertUser("Threshold exceeded")) :: Nil
			else if (f < 0.9)
				x(index + 1, target - measurement)
			else
				Nil
		}
		CallResultItem_Call(Call(fn, selectors))
	}
	
	
}

object Example2 {
	def main(args: Array[String]) {
		val e = new Example2
		val call = Call(
			fn = (inputs: List[Object]) => {
				e.x()
			},
			args = Nil
		)
		var cg = ComputationGraph()
			.addCall(call)
			.setInitialState(e.A.id, ContainerState(20))
			.setInitialState(e.B.id, ContainerState(0))
		println(cg)
		println()
		
		cg = cg.run()
		println("cg:")
		println(cg)
		println()
		
		cg = cg.setInitialState("measurement0", 8.0: java.lang.Double)
		
		cg = cg.run()
		println("cg:")
		println(cg)
		println()
		
		cg = cg.setInitialState("measurement1", 10.0: java.lang.Double)
		
		cg = cg.run()
		println("cg:")
		println(cg)
		println()
	}
}
