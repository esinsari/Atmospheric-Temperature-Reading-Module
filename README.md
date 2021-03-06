# Atmospheric-Temperature-Reading-Module

MultiThreads with locks

Synchronization Method: Fine-Grained

Design of the module responsible for measuring the atmospheric temperature of the next generation Mars Rover, equipped with a multi-core CPU and 8 temperature sensors. The sensors are responsible for collecting temperature readings at regular intervals and storing them in shared memory space. The atmospheric temperature module has to compile a report at the end of every hour, comprising the top 5 highest temperatures recorded for that hour, the top 5 lowest temperatures recorded for that hour, and the 10-minute interval of time when the largest temperature difference was observed. The data storage and retrieval of the shared memory region must be carefully handled, as we do not want to delay a sensor and miss the interval of time when it is supposed to conduct temperature reading. 

- Design and implemention of a protocol using 8 threads that will offer a solution for this task. 
- Temperature readings are taken every 1 minute. 
- Simulate the operation of the temperature reading sensor by generating a random number from -100F to 70F at every reading.

