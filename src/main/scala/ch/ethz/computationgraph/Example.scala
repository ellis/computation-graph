package ch.ethz.computationgraph

case class Container(id: String)
case class ContainerState(volume: Double)

trait RobotCommand
case class Aspirate(volume: Double, container: Container) extends RobotCommand
case class Dispense(volume: Double, container: Container) extends RobotCommand
case class Measure(container: Container) extends RobotCommand
case class AlertUser(message: String) extends RobotCommand

trait RobotEvent
case class AspriateEvent()
case class DispenseEvent()
case class MeasureEvent()

class Example {
	val A = Container("A")
	val B = Container("B")
	
	def exec(command: RobotCommand): CallResultItem_Call = {
		val call = command match {
			case Aspirate(volume, container) =>
				val selectors = List[Selector](Selector_Entity(container.id))
				val fn = (inputs: List[Object]) => {
					val state = inputs(0).asInstanceOf[ContainerState]
					List[CallResultItem](CallResultItem_Entity(container.id, state.copy(volume = state.volume - volume)))
				}
				Call(fn, selectors)
			case Dispense(volume, container) =>
				val selectors = List[Selector](Selector_Entity(container.id))
				val fn = (inputs: List[Object]) => {
					val state = inputs(0).asInstanceOf[ContainerState]
					List[CallResultItem](CallResultItem_Entity(container.id, state.copy(volume = state.volume + volume)))
				}
				Call(fn, selectors)
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
			val measurement = inputs(0).asInstanceOf[Double]
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
