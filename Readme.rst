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

Missing data
------------

When data is missing, defaults can be supplied by special functions.

Demonstration
-------------

* Typical GUI example of typing text and fetching data
* Example of tracing changes to an image (e.g. example from "Make flow tangible" at Bret Victor, Learnable Programming)

Enhancements
------------

* Selector_Field -- select a field from an entity rather than the entire entity

Related topics
--------------

* Functional Reactive Programming (FRP)
* AngularJS
* Bret Victor - Learnable Programming (http://worrydream.com/LearnableProgramming/)

