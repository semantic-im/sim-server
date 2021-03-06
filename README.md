ABOUT
=======

SIM-Server (Semantic Instrumentation and Monitoring - Server) is responsible for
receiving data from SIM-Agents and mapping it to the metrics ontology database.
Some of the metrics are also keept into a rrd database.


STORAGE INFORMATION
===================

As mentioned above incoming metrics are stored in RDF and RRD format. Following is a description of the data storage structure for both.


RDF
---

### System Metrics

All the custom entities are located in the http://www.larkc.eu/ontologies/IMOntology.rdf# namespace. This namespace has the empty prefix
in the ontology.

The following system metric types are stored : :SystemLoadAverage, :TotalSystemFreeMemory, :TotalSystemUsedMemory, :TotalSystemUsedSwap, 
:SystemOpenFileDescriptorCount, :SwapIn, :SwapOut, :IORead, :IOWrite, :UserCPULoad, :SystemCPULoad, :IdleCPULoad, :WaitCPULoad, :IrqCPULoad, 
:UserCPUTime, :SystemCPUTime, :IdleCPUTime, :WaitCPUTime, :IrqCPUTime. Those entities have the type owl:Class and subClassOf :SystemMetric

To store a metric value a new instance of the above entities is created. For each metric we have the following properties : :hasDataValue,
:hastimeStamp.

Each system metric has an instance of :System entity which is used to tell for which machine the metric was recorded. The :System instance 
has an id (the resource name) and a name (the name of the machine) 

The :hasDataValue is used to save the value of the metric.

The :hasTimeStamp property is for setting the timestamp of the metric.

Here is an example of system metric saved in RDF format (together with it's system) :

	:dad42564-fca2-4200-ba57-196f0b8569bc	rdf:type		:System
	:dad42564-fca2-4200-ba57-196f0b8569bc	:hasName		"Linux i386 2.6.32-31-generic"
	
	:be01d40d-444e-49d7-bf11-2a729515ec81	rdf:type		:SystemLoadAverage
	:be01d40d-444e-49d7-bf11-2a729515ec81	:hadDataValue		"1.37"^^xsd:double
	:be01d40d-444e-49d7-bf11-2a729515ec81	:hadTimeStamp		"2011-05-24T22:09:18"^^xsd:dateTime
	:dad42564-fca2-4200-ba57-196f0b8569bc	:hasSystemMetric	:be01d40d-444e-49d7-bf11-2a729515ec81


### Method Metrics

Each method metric is tied to this entities : :System, :Application, :Method and :Context
The :System entities is the same as the one from system metrics. The :Application entity is used to define the application for which the method metric was read, 
it has as id the name of the instance (resource id) and the name is the application name. The :Method entity defines the method of the metric. It properties are defined using the this properties : :hasMethodName, :hasClassName, :hasException, :endedWithError, :beginExecutionTime, :endExecutionTime 

:Context entity is used to suggest into which context the metric was read. The context has a name and and id, the name will be used to set the type of the context 
instance and the id to create the resource identifier. Also the context contains a map of key, values used to add additional parameters to context. The contexts can 
be nested, this is achieved in rdf storage using the :hasParentContext property.

The possible metrics for a method are : :WallClockTime, :ThreadUserCPUTime, :ThreadSystemCPUTime, :ThreadTotalCPUTime, :ThreadCount, :ThreadBlockCount, 
:ThreadWaitCount, :ThreadGccCount, :ThreadGccTime, :ProcessTotalCPUTime

The properties for each method metric are defined using : :hasDataValue, :hasTimeStamp, :hasContext

Following is an example of method metric :

	#System definition
	:dad42564-fca2-4200-ba57-196f0b8569bc	rdf:type		:System
	:dad42564-fca2-4200-ba57-196f0b8569bc	:hasName		"Linux i386 2.6.32-31-generic"

	#Application definition	
	:be01d40d-444e-49d7-bf11-2a729515ec81	rdf:type		:Application	
	:be01d40d-444e-49d7-bf11-2a729515ec81	:hasName		"LarkC 2.5"

	#Method definition	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	rdf:type		:Method	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	:hasMethodName		"main"	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	:hasClassName		"eu.larkc.core.Larkc"	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	:hasEndedWithError	"false"^^xsd:boolean	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	:hasBeginExecutionTime	"1306264158415"^^xsd:long	
	:a91fc2e6-b7c8-4242-8d2f-548df3af9f74	:hasEndExecutionTime	"1306264183372"^^xsd:long

	#Context definition	
	:88f7fa37-50e4-4591-980c-6fc134a284f0	rdf:type		:WorkflowExecution
	:075950eb-9f0b-4124-83c1-f4dc80723623	rdf:type		:Bag
	:dce426b0-3d6d-4fb9-b814-80cfcf0841a1	rdf:type		:PluginInputSizeInBytes
	:dce426b0-3d6d-4fb9-b814-80cfcf0841a1	:hasDataValue		"1000"
	:075950eb-9f0b-4124-83c1-f4dc80723623	rdf:li			:dce426b0-3d6d-4fb9-b814-80cfcf0841a1

	#Metric definition
	:2193b041-3d8b-485a-8509-2cec0585a838	rdf:type		:WallClockTime	
	:2193b041-3d8b-485a-8509-2cec0585a838	:hasDataValue		"24957"^^xsd:long	
	:2193b041-3d8b-485a-8509-2cec0585a838	:hasTimeStamp		"2011-05-24T22:09:18"^^xsd:dateTime
	:2193b041-3d8b-485a-8509-2cec0585a838	:hasContext		:88f7fa37-50e4-4591-980c-6fc134a284f0	
	:dad42564-fca2-4200-ba57-196f0b8569bc	:hasMethodMetric	:2193b041-3d8b-485a-8509-2cec0585a838	
	:be01d40d-444e-49d7-bf11-2a729515ec81	:hasMethodMetric	:2193b041-3d8b-485a-8509-2cec0585a838	
	:88f7fa37-50e4-4591-980c-6fc134a284f0	:hasMethodMetric	:2193b041-3d8b-485a-8509-2cec0585a838	


RRD
---

### System Metrics

The system metrics are saved in a rrd database with the name system_metrics.rrd located  in the current folder from where the sim server was ran.
The databases are created with a step of 60. The database step is the number of seconds betwwen database updates (a value is stored each 60 seconds).
The datasource names are : sysLoadAverage, 	totalSysFreeMemory, totalSysUsedMemory, totalSysUsedSwap, sysOpenFileDescrCnt, swapIn, swapOut, iORead, 
iOWrite, userCPULoad, systemCPULoad, idleCPULoad, waitCPULoad, irqCPULoad, userCPUTime, systemCPUTime, idleCPUTime, waitCPUTime, irqCPUTime
All data sources ahave the following parameters : type GAUGE, hearbeat 600, min value 0, max value unknown.

- GAUGE saves the actual value itself. There are no divisions or calculations. 
Memory consumption in a server is a typical example of gauge.
- hearbeat defines the minimum heartbeat, the maximum number of seconds that can go by before a DS value is considered unknown.
- min value is the minimum value that the datasource can have, anything below will me stored as NaN
- max value is the maximum value that the datasource can have, anything above will me stored as NaN

On the database two archives are stored for now (an archive is used to define data aggregation).

- AVERAGE archive, 0.5 XFileFactor, 1 step, 60 records.
- AVERAGE archive, 0.5 XFileFactor, 10 steps, 6*24 records.

XFileFactor is the accepted percentage of NaN values to consider the archive value correct (not NaN).
The steps are the number of recorded values needed to create an archive value.
The records number are the number of values keep in the archive.
For example, the second archive would use 10 database values (database has a step of 60 so 10 values will be recorded in 10*60 seconds = 10 minutes) to
create one archive value. The archive has 6*24 records that means we would store data for 10 minutes * 6 * 24 = 1 hour * 24 = 1 day


### Method Metrics

For each system, application and context of an instrumented method a new rrd database is created. The name of the database is systemid_applicationid_contextid.rrd
and is located in the current folder.
We have to create one database for each context because in rrd only numeric values are possible to store and it would be irrelevant to store mixed metrics
for different methods in one database, one would not be able to filter them by method.
The database and the datasources properties are defined as for system metric for now. The same archives definitions are use for methods as for the system.
The datasource names for each method metric are : wallClockTime, threadUserCPUTime, threadSystemCPUTime, threadTotalCPUTime, threadCount, threadBlockCount, 
threadWaitCount, threadGccCount, threadGccTime, processTotalCPUTime

	
