/**
 * 
 */
package sim.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.DatatypeLiteral;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.PlainLiteral;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.util.RDFTool;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.data.ApplicationId;
import sim.data.Context;
import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.SystemId;
import sim.data.SystemMetrics;
import sim.server.atomic.AtomicMetricsGenerator;
import sim.server.data.CompoundMetric;
import sim.server.data.Metric;
import sim.server.util.SPARQLQueryContentAnalyzer;


/**
 * @author valer
 *
 */
public class RdfDatabase implements MetricsVisitor {

	private static final Logger logger = LoggerFactory.getLogger(RdfDatabase.class);
	
	public static final String QUERY_CONTENT = "QueryContent";
	
	private Model model;

	private String simNS;
	private String rdfNS;
	private String rdfsNS;	
	private String xsdNS;
	
	private URI typePredicateURI;
	private URI rdfLiURI;
	private URI hasSystemMetricURI;
	private URI hasNameURI;
	private URI hasDataValueURI;
	private URI hasTimeStampURI;
	
	private URI hasMethodMetricURI;
	private URI hasMethodNameURI;
	private URI hasClassNameURI;
	private URI hasExceptionURI;
	private URI hasEndedWithErrorURI;
	private URI hasBeginExecutionTimeURI;
	private URI hasEndExecutionTimeURI;
	
	private URI hasParentContextURI;
	private URI hasContextURI;
	private URI hasMetricsURI;
	
	private URI longDatatypeURI;
	private URI doubleDatatypeURI;
	private URI dateTimeDatatypeURI;
	private URI booleanDatatypeURI;
	private URI integerDatatypeURI;

	private URI hasMethodExecutionURI;
	private URI isMethodExecutionOfURI;
	private URI hasMeasurementURI;
	private URI isMeasurementOfURI;
	private URI includesURI;
	
	private URI methodTypeURI;
	private URI methodExecutionTypeURI;
	
	private URI systemTypeURI;
	private URI applicationTypeURI;
	
	private URI bagTypeURI;
	
	private static final HashMap<String, URI> methodURICache = new HashMap<String, URI>();
	private static final HashMap<String, URI> systemURICache = new HashMap<String, URI>();
	private static final HashMap<String, URI> applicationURICache = new HashMap<String, URI>();
	
	private URI queryBeginExecutionTimeURI;
	private URI queryEndExecutionTimeURI;
	private URI queryErrorStatusURI;
	private URI queryTotalResponseTimeURI;
	private URI queryThreadUserCPUTimeURI;
	private URI queryThreadSystemCPUTimeURI;
	private URI queryThreadTotalCPUTimeURI;
	private URI queryThreadCountURI;
	private URI queryThreadBlockCountURI;
	private URI queryThreadBlockTimeURI;
	private URI queryThreadWaitCountURI;
	private URI queryThreadWaitTimeURI;
	private URI queryThreadGccCountURI;
	private URI queryThreadGccTimeURI;
	private URI queryProcessTotalCPUTimeURI;
	
	private URI workflowTotalResponseTimeURI;
	private URI workflowThreadUserCPUTimeURI;
	private URI workflowThreadSystemCPUTimeURI;
	private URI workflowThreadTotalCPUTimeURI;
	private URI workflowThreadCountURI;
	private URI workflowThreadBlockCountURI;
	private URI workflowThreadBlockTimeURI;
	private URI workflowThreadWaitCountURI;
	private URI workflowThreadWaitTimeURI;
	private URI workflowThreadGccCountURI;
	private URI workflowThreadGccTimeURI;
	private URI workflowProcessTotalCPUTimeURI;

	private URI pluginBeginExecutionTimeURI;
	private URI pluginEndExecutionTimeURI;
	private URI pluginErrorStatusURI;
	private URI pluginTotalResponseTimeURI;
	private URI pluginThreadUserCPUTimeURI;
	private URI pluginThreadSystemCPUTimeURI;
	private URI pluginThreadTotalCPUTimeURI;
	private URI pluginThreadCountURI;
	private URI pluginThreadBlockCountURI;
	private URI pluginThreadBlockTimeURI;
	private URI pluginThreadWaitCountURI;
	private URI pluginThreadWaitTimeURI;
	private URI pluginThreadGccCountURI;
	private URI pluginThreadGccTimeURI;
	private URI pluginProcessTotalCPUTimeURI;
	
	public RdfDatabase() {
	}
	
	public void open() {
		this.model = new RepositoryModel(new HTTPRepository("http://" + Main.storage_server_domain + ":" + Main.storage_server_port + "/openrdf-sesame", Main.storage_repository_id));
		this.model.open();
		this.model.setAutocommit(false);
		
		simNS = model.getNamespace("sim");
		if (simNS == null) {
			model.setNamespace("sim", "http://www.larkc.eu/ontologies/IMOntology.rdf#");
			model.commit();
			simNS = model.getNamespace("sim");
			if (simNS == null) {
				simNS = "http://www.larkc.eu/ontologies/IMOntology.rdf#"; //this is for OWLIM, the get returned null check http://www.mail-archive.com/owlim-discussion@ontotext.com/msg00772.html
			}
		}
		rdfNS = model.getNamespace("rdf");
		if (rdfNS == null) {
			model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			model.commit();
			rdfNS = model.getNamespace("rdf");
		}
		rdfsNS = model.getNamespace("rdfs");
		if (rdfsNS == null) {
			model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			model.commit();
			rdfsNS = model.getNamespace("rdfs");
		}

		xsdNS = model.getNamespace("xsd");
		if (xsdNS == null) {
			model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
			model.commit();
			xsdNS = model.getNamespace("xsd");
		}
		
		typePredicateURI = model.createURI(rdfNS + "type");
		rdfLiURI = model.createURI(rdfNS + "li");
		hasSystemMetricURI = model.createURI(simNS + "hasSystemMetric");
		hasNameURI = model.createURI(simNS + "hasName");
		hasDataValueURI = model.createURI(simNS + "hasDataValue");
		hasTimeStampURI = model.createURI(simNS + "hasTimeStamp");
		
		hasMethodMetricURI = model.createURI(simNS + "hasMethodMetric");
		hasMethodNameURI = model.createURI(simNS + "hasMethodName");
		hasClassNameURI = model.createURI(simNS + "hasClassName");
		hasExceptionURI = model.createURI(simNS + "hasException");
		hasEndedWithErrorURI = model.createURI(simNS + "hasEndedWithError");
		hasBeginExecutionTimeURI = model.createURI(simNS + "hasBeginExecutionTime");
		hasEndExecutionTimeURI = model.createURI(simNS + "hasEndExecutionTime");
		
		hasParentContextURI = model.createURI(simNS + "hasParentContext");
		hasContextURI = model.createURI(simNS + "hasContext");
		hasMetricsURI = model.createURI(simNS + "hasMetrics");
		
		longDatatypeURI = model.createURI(xsdNS + "long");
		doubleDatatypeURI = model.createURI(xsdNS + "double");
		dateTimeDatatypeURI = model.createURI(xsdNS + "dateTime");
		booleanDatatypeURI = model.createURI(xsdNS + "boolean");
		integerDatatypeURI = model.createURI(xsdNS + "int");
		
		hasMethodExecutionURI = model.createURI(simNS + "hasMethodExecution");
		isMethodExecutionOfURI = model.createURI(simNS + "isMethodExecutionOf");
		hasMeasurementURI = model.createURI(simNS + "hasMeasurement");
		isMeasurementOfURI = model.createURI(simNS + "isMeasurementOf");
		includesURI = model.createURI(simNS + "includes");
		
		methodTypeURI = model.createURI(simNS + "Method");
		methodExecutionTypeURI = model.createURI(simNS + "MethodExecution");
		
		systemTypeURI = model.createURI(simNS + "System");
		applicationTypeURI = model.createURI(simNS + "Application");
		
		bagTypeURI = model.createURI(simNS + "Bag");
		
		queryBeginExecutionTimeURI = model.createURI(simNS + "QueryBeginExecutionTime");
		queryEndExecutionTimeURI = model.createURI(simNS + "QueryEndExecutionTime");	
		queryErrorStatusURI = model.createURI(simNS + "QueryErrorStatus");
		
		
		queryTotalResponseTimeURI = model.createURI(simNS + "QueryTotalResponseTime");
		queryThreadUserCPUTimeURI = model.createURI(simNS + "QueryThreadUserCPUTime");
		queryThreadSystemCPUTimeURI = model.createURI(simNS + "QueryThreadSystemCPUTime");
		queryThreadTotalCPUTimeURI = model.createURI(simNS + "QueryThreadTotalCPUTime");
		queryThreadCountURI = model.createURI(simNS + "QueryThreadCount");
		queryThreadBlockCountURI = model.createURI(simNS + "QueryThreadBlockCount");
		queryThreadBlockTimeURI = model.createURI(simNS + "QueryThreadBlockTime");
		queryThreadWaitCountURI = model.createURI(simNS + "QueryThreadWaitCount");
		queryThreadWaitTimeURI = model.createURI(simNS + "QueryThreadWaitTime");
		queryThreadGccCountURI = model.createURI(simNS + "QueryThreadGccCount");
		queryThreadGccTimeURI = model.createURI(simNS + "QueryThreadGccTime");
		queryProcessTotalCPUTimeURI = model.createURI(simNS + "QueryProcessTotalCPUTime");

		workflowTotalResponseTimeURI = model.createURI(simNS + "WorkflowTotalResponseTime");
		workflowThreadUserCPUTimeURI = model.createURI(simNS + "WorkflowThreadUserCPUTime");
		workflowThreadSystemCPUTimeURI = model.createURI(simNS + "WorkflowThreadSystemCPUTime");
		workflowThreadTotalCPUTimeURI = model.createURI(simNS + "WorkflowThreadTotalCPUTime");
		workflowThreadCountURI = model.createURI(simNS + "WorkflowThreadCount");
		workflowThreadBlockCountURI = model.createURI(simNS + "WorkflowThreadBlockCount");
		workflowThreadBlockTimeURI = model.createURI(simNS + "WorkflowThreadBlockTime");
		workflowThreadWaitCountURI = model.createURI(simNS + "WorkflowThreadWaitCount");
		workflowThreadWaitTimeURI = model.createURI(simNS + "WorkflowThreadWaitTime");
		workflowThreadGccCountURI = model.createURI(simNS + "WorkflowThreadGccCount");
		workflowThreadGccTimeURI = model.createURI(simNS + "WorkflowThreadGccTime");
		workflowProcessTotalCPUTimeURI = model.createURI(simNS + "WorkflowProcessTotalCPUTime");
		
		pluginBeginExecutionTimeURI = model.createURI(simNS + "PluginBeginExecutionTime");
		pluginEndExecutionTimeURI = model.createURI(simNS + "PluginEndExecutionTime");
		pluginErrorStatusURI = model.createURI(simNS + "PluginErrorStatus");
		pluginTotalResponseTimeURI = model.createURI(simNS + "PluginTotalResponseTime");
		pluginThreadUserCPUTimeURI = model.createURI(simNS + "PluginThreadUserCPUTime");
		pluginThreadSystemCPUTimeURI = model.createURI(simNS + "PluginThreadSystemCPUTime");
		pluginThreadTotalCPUTimeURI = model.createURI(simNS + "PluginThreadTotalCPUTime");
		pluginThreadCountURI = model.createURI(simNS + "PluginThreadCount");
		pluginThreadBlockCountURI = model.createURI(simNS + "PluginThreadBlockCount");
		pluginThreadBlockTimeURI = model.createURI(simNS + "PluginThreadBlockTime");
		pluginThreadWaitCountURI = model.createURI(simNS + "PluginThreadWaitCount");
		pluginThreadWaitTimeURI = model.createURI(simNS + "PluginThreadWaitTime");
		pluginThreadGccCountURI = model.createURI(simNS + "PluginThreadGccCount");
		pluginThreadGccTimeURI = model.createURI(simNS + "PluginThreadGccTime");
		pluginProcessTotalCPUTimeURI = model.createURI(simNS + "PluginProcessTotalCPUTime");
	}
	
	public void close() {
		this.model.close();
	}

	private PlainLiteral getStringTypeURI(String value) {
		return model.createPlainLiteral(value);
	}

	private DatatypeLiteral getLongTypeURI(long value) {
		return model.createDatatypeLiteral(String.valueOf(value), longDatatypeURI);
	}

	private DatatypeLiteral getDoubleTypeURI(double value) {
		return model.createDatatypeLiteral(String.valueOf(value), doubleDatatypeURI);
	}

	private DatatypeLiteral getDateTimeTypeURI(long dateTimeLong) {
		Date dateTime = new Date(dateTimeLong);
		return model.createDatatypeLiteral(RDFTool.dateTime2String(dateTime), dateTimeDatatypeURI);
	}

	private DatatypeLiteral getBooleanTypeURI(boolean value) {
		return model.createDatatypeLiteral(String.valueOf(value), booleanDatatypeURI);
	}

	private DatatypeLiteral getIntegerTypeURI(int value) {
		return model.createDatatypeLiteral(String.valueOf(value), integerDatatypeURI);
	}

	@Override
	public void visit(MethodMetrics methodMetrics) {
		List<Statement> statements = new ArrayList<Statement>();
		
		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(methodMetrics.getCreationTime());
		
		URI idSystemURI = addSystem(methodMetrics.getSystemId(), statements);
		URI idApplicationURI = addApplication(methodMetrics.getMethod().getApplicationId(), statements);

		String methodID = methodMetrics.getMethod().getClassName() + "." + methodMetrics.getMethod().getMethodName().replace("<init>", "new");
		
		URI idMethodURI = methodURICache.get(methodID);
		if(idMethodURI == null){		
			idMethodURI = model.createURI(simNS + methodID);			
			statements.add(model.createStatement(idMethodURI, typePredicateURI, methodTypeURI));
			statements.add(model.createStatement(idMethodURI, hasMethodNameURI, model.createPlainLiteral(methodMetrics.getMethod().getMethodName())));
			statements.add(model.createStatement(idMethodURI, hasClassNameURI, model.createPlainLiteral(methodMetrics.getMethod().getClassName())));
			methodURICache.put(methodID, idMethodURI);
		}
		
		URI idMethodMetricsURI = generateURI();
		statements.add(model.createStatement(idMethodMetricsURI, typePredicateURI, methodExecutionTypeURI));
		statements.add(model.createStatement(idMethodMetricsURI, isMethodExecutionOfURI, idMethodURI));
		statements.add(model.createStatement(idMethodMetricsURI, hasBeginExecutionTimeURI, getLongTypeURI(methodMetrics.getBeginExecutionTime())));
		statements.add(model.createStatement(idMethodMetricsURI, hasEndExecutionTimeURI, getLongTypeURI(methodMetrics.getEndExecutionTime())));
		statements.add(model.createStatement(idMethodMetricsURI, hasEndedWithErrorURI, getBooleanTypeURI(methodMetrics.endedWithError())));
		if (methodMetrics.getException() != null) {
			statements.add(model.createStatement(idMethodMetricsURI, hasExceptionURI, model.createPlainLiteral(methodMetrics.getException())));
		}
		
		URI idContextURI;
		if (methodMetrics.getContextId() == null)
			idContextURI = null;
		else
			idContextURI = model.createURI(simNS + methodMetrics.getContextId());

		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "WallClockTime", getLongTypeURI(methodMetrics.getWallClockTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadCount", getLongTypeURI(methodMetrics.getThreadCount())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime())));
		statements.addAll(createMethodMetricStatements(idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime())));
		
		
		/**
		 * if the method execution is the method execution of specific methods that correspond to
		 * atomic metrics than the corresponding atomic metrics are written in the RDF storage
		 */
		List<Statement> atomicMetricsStatements = processMetric(methodMetrics);
		if(atomicMetricsStatements.size()>0)		
			statements.addAll(atomicMetricsStatements);

		model.addAll(statements.iterator());
		model.commit();
	}
	
	@Override
	public void visit(SystemMetrics systemMetrics) {
		List<Statement> statements = new ArrayList<Statement>();
		
		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(systemMetrics.getCreationTime());
		
		//System metric
		URI idSystemURI = addSystem(systemMetrics.getSystemId(), statements);
		
		//SystemLoadAverage
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemLoadAverage", getDoubleTypeURI(systemMetrics.getSystemLoadAverage())));
		
		//TotalSystemFreeMemory
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemFreeMemory", getLongTypeURI(systemMetrics.getTotalSystemFreeMemory())));
		
		//TotalSystemUsedMemory
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemUsedMemory", getLongTypeURI(systemMetrics.getTotalSystemUsedMemory())));
		
		//TotalSystemUsedSwap
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "TotalSystemUsedSwap", getLongTypeURI(systemMetrics.getTotalSystemUsedSwap())));

		//SystemOpenFileDescriptors
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemOpenFileDescriptorCount", getLongTypeURI(systemMetrics.getSystemOpenFileDescriptors())));
		
		//SwapIn
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SwapIn", getLongTypeURI(systemMetrics.getSwapIn())));

		//SwapOut
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SwapOut", getLongTypeURI(systemMetrics.getSwapOut())));

		//IORead
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IORead", getLongTypeURI(systemMetrics.getIORead())));

		//IOWrite
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IOWrite", getLongTypeURI(systemMetrics.getIOWrite())));

		//UserPerc
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "UserCPULoad", getDoubleTypeURI(systemMetrics.getUserPerc())));

		//SysPerc
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemCPULoad", getDoubleTypeURI(systemMetrics.getSysPerc())));

		//IdlePerc //TODO
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IdleCPULoad", getDoubleTypeURI(systemMetrics.getIdlePerc())));

		//WaitPerc //TODO
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "WaitCPULoad", getDoubleTypeURI(systemMetrics.getWaitPerc())));
		
		//IrqPerc //TODO
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IrqCPULoad", getDoubleTypeURI(systemMetrics.getIrqPerc())));
		
		//User
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "UserCPUTime", getDoubleTypeURI(systemMetrics.getUser())));
		
		//Sys
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "SystemCPUTime", getDoubleTypeURI(systemMetrics.getSys())));
		
		//Idle
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IdleCPUTime", getDoubleTypeURI(systemMetrics.getIdle())));
		
		//Wait
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "WaitCPUTime", getDoubleTypeURI(systemMetrics.getWait())));
		
		//Irq
		statements.addAll(createSystemMetricStatements(idSystemURI, dateTimeLiteral, "IrqCPUTime", getDoubleTypeURI(systemMetrics.getIrq())));
		
		model.addAll(statements.iterator());
		model.commit();
	}

	private List<Statement> createSystemMetricStatements(URI idSystemURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
		List<Statement> statements = new ArrayList<Statement>();
		URI idURI = generateURI();
		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
		statements.add(model.createStatement(idURI, hasDataValueURI, value));
		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
		statements.add(model.createStatement(idSystemURI, hasSystemMetricURI, idURI));
		
		return statements;
	}

	private List<Statement> createMethodMetricStatements(URI idSystemURI, URI idApplicationURI, URI idContextURI, URI idMethodURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
		List<Statement> statements = new ArrayList<Statement>();
		URI idURI = generateURI();
		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
		statements.add(model.createStatement(idURI, hasDataValueURI, value));
		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
		if (idContextURI != null) {
			statements.add(model.createStatement(idURI, hasContextURI, idContextURI));
		}
		statements.add(model.createStatement(idSystemURI, hasMethodMetricURI, idURI));
		statements.add(model.createStatement(idApplicationURI, hasMethodMetricURI, idURI));
		statements.add(model.createStatement(idMethodURI, hasMethodMetricURI, idURI));
		
		return statements;
	}

	private URI addSystem(SystemId systemId, List<Statement> statements) {
		//System metric
		URI idSystemURI = systemURICache.get(systemId.getId());
		if (idSystemURI == null) {
			idSystemURI = model.createURI(simNS + systemId.getId());
			statements.add(model.createStatement(idSystemURI, typePredicateURI, systemTypeURI));
			statements.add(model.createStatement(idSystemURI, hasNameURI, getStringTypeURI(systemId.getName())));
		}
		return idSystemURI;
	}

	private URI addApplication(ApplicationId applicationId, List<Statement> statements) {
		URI idApplicationURI = applicationURICache.get(applicationId.getId());
		if (idApplicationURI == null) {
			idApplicationURI = model.createURI(simNS + applicationId.getId());
			statements.add(model.createStatement(idApplicationURI, typePredicateURI, applicationTypeURI));
			statements.add(model.createStatement(idApplicationURI, hasNameURI, getStringTypeURI(applicationId.getName())));
		}
		return idApplicationURI;
	}

	private URI addContext(Context context, List<Statement> statements) {
		if (context == null) {
			return null;
		}
		URI idContextURI = model.createURI(simNS + context.getId());
		statements.add(model.createStatement(idContextURI, typePredicateURI, model.createURI(simNS + context.getName())));
		if (context.getParentContextId() != null) {
			URI idParentContextURI = model.createURI(simNS + context.getParentContextId());
			statements.add(model.createStatement(idContextURI, hasParentContextURI, idParentContextURI));
		}
		URI idBagURI = model.createURI(simNS + context.getId() + "-metrics");
		statements.add(model.createStatement(idBagURI, typePredicateURI, bagTypeURI));
		statements.add(model.createStatement(idContextURI, hasMetricsURI, idBagURI));

		for (Entry<String, Object> entry : context.entrySet()) {
			URI idBagValueURI = generateURI();
			statements.add(model.createStatement(idBagValueURI, typePredicateURI, model.createURI(simNS + entry.getKey())));
			statements.add(model.createStatement(idBagValueURI, hasDataValueURI, model.createPlainLiteral(entry.getValue().toString())));
			statements.add(model.createStatement(idBagURI, rdfLiURI, idBagValueURI));
		}
		

		/*	check if the context contains information about SPARQL query;
		if that's the case the SPARQL query is parsed and method 
		metrics statements are created for this query
		 */
	
		if(context.containsKey(QUERY_CONTENT)){
			String query = context.get(QUERY_CONTENT).toString();
			SPARQLQueryContentAnalyzer sqa = new SPARQLQueryContentAnalyzer(query);
			if (sqa.parseQuery()) {
	
				URI idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryDataSetSourcesNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryDataSetSourcesNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
	
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryNamespaceNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryNamespaceNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
	
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryOperatorsNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryOperatorsNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
	
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultLimitNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultLimitNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
			
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultOffsetNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultOffsetNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
			
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryResultOrderingNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryResultOrderingNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
										
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QuerySizeInCharacters")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQuerySizeInCharacters())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
	
				idURI = generateURI();
				statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "QueryVariablesNb")));
				statements.add(model.createStatement(idURI, hasDataValueURI, getIntegerTypeURI(sqa.getQueryVariablesNb())));
				statements.add(model.createStatement(idURI, hasTimeStampURI, getDateTimeTypeURI(context.getCreationTime())));
				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
			}
		}
		
		return idContextURI;
	}

	@Override
	public void visit(Context context) {
		List<Statement> statements = new ArrayList<Statement>();	
		addContext(context, statements);
		model.addAll(statements.iterator());
		model.commit();			
	}

	public URI generateURI(){
		return model.createURI(simNS + UUID.randomUUID().toString());
	}
	
	public QueryResultTable sparqlSelect(String queryString){
		return model.sparqlSelect(queryString);
	}

	public URI createURI(String uriString){
		return model.createURI(simNS + uriString);
	}

	public String getSimNS() {
		return simNS;
	}

	public String getRdfNS() {
		return rdfNS;
	}

	public String getRdfsNS() {
		return rdfsNS;
	}

	public String getXsdNS() {
		return xsdNS;
	}
	
	public void visit(CompoundMetric compoundMetric) {
		List<Statement> statements = new ArrayList<Statement>();
				
		URI idCompoundMetricURI = generateURI();
		compoundMetric.setId(idCompoundMetricURI);
		
		statements.add(model.createStatement(idCompoundMetricURI, typePredicateURI, compoundMetric.getType()));
		for(Metric m : compoundMetric.getConstituentMetrics()){
			statements.add(model.createStatement(idCompoundMetricURI, includesURI, m.getId()));
		}		
		statements.add(model.createStatement(idCompoundMetricURI, hasDataValueURI, getDoubleTypeURI(new Double(compoundMetric.getValue()).doubleValue())));
		
		model.addAll(statements.iterator());
		model.commit();
	}

	
	private List<Statement> processMetric(MethodMetrics methodMetrics){
		
		List<Statement> statements = new ArrayList<Statement>();
		String methodID = methodMetrics.getMethod().getClassName() + "." + methodMetrics.getMethod().getMethodName().replace("<init>", "new");
		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(methodMetrics.getCreationTime());

		/**
		 * if the method execution is the method execution of 
		 * eu.larkc.core.endpoint.sparql.SparqlHandler.handle
		 * we write query related atomic metrics
		 */		
		if(methodID.equals("eu.larkc.core.endpoint.sparql.SparqlHandler.handle")){
						
			//write an instance of QueryBeginExecutionTime
			URI id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryBeginExecutionTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getBeginExecutionTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of QueryEndExecutionTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryEndExecutionTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getEndExecutionTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryErrorStatus
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryErrorStatusURI));
			statements.add(model.createStatement(id, hasDataValueURI, getBooleanTypeURI(methodMetrics.endedWithError())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryTotalResponseTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryTotalResponseTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getWallClockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of QueryThreadUserCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadUserCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadUserCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of QueryThreadSystemCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadSystemCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadSystemCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadBlockCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadBlockCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadBlockTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadBlockTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadWaitCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadWaitCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadWaitTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadWaitTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadGccCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadGccCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryThreadGccTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryThreadGccTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of QueryProcessTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, queryProcessTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getProcessTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
		}
		
		/**
		 * if the method execution is the method execution of 
		 * eu.larkc.core.executor.Executor.execute
		 * we write workflow related atomic metrics
		 */		
		if(methodID.equals("eu.larkc.core.executor.Executor.execute") || methodID.equals("eu.larkc.core.executor.Executor.getNextResults")){
		
			//write an instance of WorkflowTotalResponseTime
			URI id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowTotalResponseTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getWallClockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadUserCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadUserCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadUserCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of WorkflowThreadSystemCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadSystemCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadSystemCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadBlockCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadBlockCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadBlockTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadBlockTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadWaitCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadWaitCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadWaitTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadWaitTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadGccCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadGccCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadGccCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadGccCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowThreadGccTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowThreadGccTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of WorkflowProcessTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, workflowProcessTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getProcessTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
		}
		
		/**
		 * if the method execution is the method execution of 
		 * eu.larkc.plugin.Plugin.invoke
		 * we write plugin related atomic metrics
		 */		
		if(methodID.equals("eu.larkc.plugin.Plugin.invoke")){
			
			//write an instance of PluginBeginExecutionTime
			URI id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginBeginExecutionTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getBeginExecutionTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of PluingEndExecutionTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginEndExecutionTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getEndExecutionTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingErrorStatus
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginErrorStatusURI));
			statements.add(model.createStatement(id, hasDataValueURI, getBooleanTypeURI(methodMetrics.endedWithError())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingTotalResponseTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginTotalResponseTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getWallClockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of PluingThreadUserCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadUserCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadUserCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
			
			//write an instance of PluingThreadSystemCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadSystemCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadSystemCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadBlockCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadBlockCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadBlockTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadBlockTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadBlockTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadWaitCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadWaitCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadWaitTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadWaitTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadWaitTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadGccCount
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadGccCountURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccCount())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingThreadGccTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginThreadGccTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getThreadGccTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));

			//write an instance of PluingProcessTotalCPUTime
			id = generateURI();
			statements.add(model.createStatement(id, typePredicateURI, pluginProcessTotalCPUTimeURI));
			statements.add(model.createStatement(id, hasDataValueURI, getLongTypeURI(methodMetrics.getProcessTotalCpuTime())));
			statements.add(model.createStatement(id, hasTimeStampURI, dateTimeLiteral));
		}
		return statements;
	}
	
}
