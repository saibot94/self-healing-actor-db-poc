# Autonomous systems project

This is the implementation for `autonomous systems`, ALFP, year 2, West University of Timisoara.

# Implementation

The implementation features an actor system that implements a distributed database, as well as an HTTP server
to interact with this database.

The following functionality is available and provided by the system:

- distributed storage across multiple partitions
- automatic recovery of failed actors and reloading of data, in order to avoid loss


## REST API

The following functionality is provided:

* ` PUT /data ` - with query params key and value, add a piece of data at that specific key
. If the key already existed, then the previous data is removed.
* `GET /data ` - query param key; gets the value at that key or an empty response
in case nothing is there
* `DELETE /data` - query param `id`; allows the user to remove a piece of data from
the collection
* `GET /actors` - get the names and last heartbeat time for all data storage actors
* `DELETE /actors` - query param `id`; cause the system to kill one of the actors,
if the id is a correct one. The system will then discover that the actor is dead
and proceed with replacing it with a new instance
* `GET /data/all` - get all data from all actors, grouped by partition into different
lists


## Teacher's questions:

Design the architecture and a few functionalities.

Some questions:
* how and when is it noticed that data has been lost
* who, when and what must be done in order to finish the data

These at the presentation.


### Copyright

Cristian Schuszter, 2018
All rights reserved