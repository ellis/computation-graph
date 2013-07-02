package ch.ethz.reactivesim

class ReactiveSim {
	private var cg = ComputationGraph()
	
	def graph = cg
	
	/**
	 * Add a top-level call to the computation graph
	 */
	def addCall(call: Call) {
		cg = cg.addCall(call)
	}
	
	/**
	 * Set the initial value for a given id
	 */
	def setInitialState(id: String, entity: Object) {
		cg = cg.setInitialState(id, entity)
	}
	
	/**
	 * Run the computation as far as it can go
	 */
	def run() {
		cg = cg.run()
	}
	
	/**
	 * Get list of errors and warnings from the computation
	 */
	def messages(): List[String] = {
		val l: List[(List[Int], String)] =
			cg.timeToErrors.toList.flatMap(pair => pair._2.map(s => pair._1 -> s"ERROR: Call ${pair._1.mkString(".")}: $s")) ++
			cg.timeToWarnings.toList.flatMap(pair => pair._2.map(s => pair._1 -> s"WARNING: Call ${pair._1.mkString(".")}: $s"))
		l.sortBy(_._1)(ListIntOrdering).map(_._2)
	}
}