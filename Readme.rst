=================
computation-graph
=================

Structure
---------

There are four important concepts:

* Data (immutable, state)
* Call
* Selector
* Event

There are three types of data: immutable entities, initial states, and non-initial states.
All three are represented by a 4-tuple (id, type, time, value).

A call is composed of a function and a list of input selectors.

There are three types of selectors: single data item, list of data items, all items of a given type.
The first two can be optional, meaning that the function will be called even if the items are unavailable.

Functions can return data, events, and calls.  Also constraints.

Graph structures
----------------

Data map
Call tree
Selector set
Selector/value map
Event list
Call/Output cache

Entity data structure
---------------------

Immutable entities are available to all calls.
In contrast, the availability of mutable entities is more complex.
Entities which have been set at time 1 (the "initial state" of the system) are available to call 1.
Since the first call might change any of the mutable entities, they are all masked from the call 2 until call 1 has completed.
If the programmer specifies constraints for which entities call 1 can change, however, then all other mutable entities are available to call 2
even before call 1 completes.

Missing data
------------

When data is missing, defaults can be supplied by special functions.

Use cases
---------

* Functional Reactive Programming
* Undo/redo
* Exploration of parameter variations
* Visualizing

  * code execution
  * simulation over time
  * computation state

* Optimization (interactive or automated)

Demonstration
-------------

* Typical GUI example of typing text and fetching data
* Undo/redo
* Tracking alternate paths
* Example of tracing changes to an image (e.g. example from "Make flow tangible" at Bret Victor, Learnable Programming)

Enhancements
------------

* Selector_Field -- select a field from an entity rather than the entire entity

Related topics
--------------

* Event sources (<http://martinfowler.com/eaaDev/EventSourcing.html>)
* CQRS (<http://martinfowler.com/bliki/CQRS.html>)
* Functional Reactive Programming (FRP)
* AngularJS
* Bret Victor - Learnable Programming (http://worrydream.com/LearnableProgramming/)

