- [ ] ComputationGraph: remove callnode children when its marked as Ready or Waiting
- [ ] ComputationGraph: `run` method
- [ ] Example: robot pipetting liquid
- [ ] ComputationGraph: remove node
- [ ] ComputationGraph: remove entity
- [ ] ComputationGraph: handle optional selectors
- [ ] ComputaitonGraph: consider whether more information can be put in the graph rather than in extra maps
- [ ] DSL
- [ ] ComputationGraph: needs a result monad for errors and warnings
- [ ] ReactiveSim: handles ComputationGraph and modifications
- [ ] ReactiveSim: handle commands, events, and measurements
- [ ] New terminology: immutable entity -> parameter
- [ ] ReactiveSim: create function to run until measurement required
- [ ] Create visual representation of ComputationGraph

Robot:

Actions and percepts are discrete events that happen at pre-determined
times.

Time-based simulations may have new percepts coming in at any time
as signals.

Some data entities are calculated, so they update at any time step
where the inputs change.

Some calculations require history data, so not just the current value.

ACTIONS
DATA
CALCULATIONS
MEASUREMENTS
