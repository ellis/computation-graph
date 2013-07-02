package ch.ethz.reactivesim

//import scala.language.implicitConversions

//sealed trait OutputItem
//case class OutputItem_

class Example2 extends Dsl {
	val A = Container("A")
	val B = Container("B")
	
	def exec(command: RobotCommand): Call = {
		command match {
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
					RsSuccess(List[CallResultItem](CallResultItem_Entity(container.id, state)))
				}
				Call(fn, selectors)
		}
	}
	
	def x(): RsResult[List[CallResultItem]] = x(0, 10)
	
	def x(index: Int, volume: Double): RsResult[List[CallResultItem]] = {
		output(
			exec(Aspirate(volume, A)),
			exec(Dispense(volume, B)),
			exec(Measure(B)),
			check(index, 10)
		)
	}
	
	def check(index: Int, target: Double): Call = {
		input(as[java.lang.Double](s"measurement$index")) {
			(measurement) =>
			val f = measurement / target
			if (f > 1.1)
				output(exec(AlertUser("Threshold exceeded")))
			else if (f < 0.9)
				x(index + 1, target - measurement)
			else
				RsSuccess(Nil)
		}
	}
	
}

object Example2 {
	def main(args: Array[String]) {
		val rs = new ReactiveSim
		val e = new Example2
		val calls: List[Call] = e.x().getOrElse(Nil).collect({case CallResultItem_Call(call) => call})
		calls.foreach(rs.addCall)
		rs.setInitialState(e.A.id, ContainerState(20))
		rs.setInitialState(e.B.id, ContainerState(0))
		println(rs.graph)
		println()
		
		rs.run()
		println("cg:")
		println(rs.graph)
		println()
		
		rs.setInitialState("measurement0", 8.0: java.lang.Double)
		
		rs.run()
		println("cg:")
		println(rs.graph)
		println()
		
		rs.setInitialState("measurement1", 10.0: java.lang.Double)
		
		rs.run()
		println("cg:")
		println(rs.graph)
		println()
	}
}
