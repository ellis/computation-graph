package ch.ethz.reactivesim

class ReactiveSim {
	private var cg = ComputationGraph()
	
	def graph = cg
	
	def addCall(call: Call) {
		cg = cg.addCall(call)
	}
	
	def setInitialState(id: String, entity: Object) {
		cg = cg.setInitialState(id, entity)
	}
	
	def run() {
		cg = cg.run()
	}
	
	def messages(): List[String] = {
		val l: List[(List[Int], String)] =
			cg.timeToErrors.toList.flatMap(pair => pair._2.map(s => pair._1 -> s"ERROR: Call ${pair._1.mkString(".")}: $s")) ++
			cg.timeToWarnings.toList.flatMap(pair => pair._2.map(s => pair._1 -> s"WARNING: Call ${pair._1.mkString(".")}: $s"))
		l.sortBy(_._1)(ListIntOrdering).map(_._2)
	}
}