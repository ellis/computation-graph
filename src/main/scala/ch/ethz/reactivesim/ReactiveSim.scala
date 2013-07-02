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
}