/**
 * 
 */
package sim.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.DatatypeLiteral;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.PlainLiteral;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.util.RDFTool;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.http.HTTPRepository;

import sim.data.ApplicationId;
import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.SystemId;
import sim.data.SystemMetrics;

/**
 * @author valer
 *
 */
public class RdfDatabase implements MetricsVisitor {

	private Model model;

	boolean visited = false;
	
	private String simNS;
	private String rdfNS;
	private String xsdNS;
	
	private URI typePredicateURI;
	private URI hasSystemMetricURI;
	private URI hasNameURI;
	private URI hasDataValueURI;
	private URI hasTimeStampURI;
	
	private URI hasMethodNameURI;
	
	private URI longDatatypeURI;
	private URI doubleDatatypeURI;
	private URI dateTimeDatatypeURI;
	
	public RdfDatabase() {
	}
	
	public void open() {
		this.model = new RepositoryModel(new HTTPRepository("http://localhost:8080/openrdf-sesame", "sim"));
		//this.model = RDF2Go.getModelFactory().createModel();
		this.model.open();
		this.model.setAutocommit(false);
		
		simNS = model.getNamespace("sim");
		if (simNS == null) {
			model.setNamespace("sim", "http://www.larkc.eu/ontologies/IMOntology.rdf#");
			model.commit();
			simNS = model.getNamespace("sim");
		}
		rdfNS = model.getNamespace("rdf");
		if (rdfNS == null) {
			model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			model.commit();
			rdfNS = model.getNamespace("rdf");
		}
		xsdNS = model.getNamespace("xsd");
		if (xsdNS == null) {
			model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
			model.commit();
			xsdNS = model.getNamespace("xsd");
		}
		
		typePredicateURI = model.createURI(rdfNS + "type");
		hasSystemMetricURI = model.createURI(simNS + "hasSystemMetric");
		hasNameURI = model.createURI(simNS + "hasName");
		hasDataValueURI = model.createURI(simNS + "hasDataValue");
		hasTimeStampURI = model.createURI(simNS + "hasTimeStamp");
		
		hasMethodNameURI = model.createURI(simNS + "hasMethodName");
		
		longDatatypeURI = model.createURI(xsdNS + "long");
		doubleDatatypeURI = model.createURI(xsdNS + "double");
		dateTimeDatatypeURI = model.createURI(xsdNS + "dateTime");
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

	@Override
	public void visit(MethodMetrics methodMetrics) {
		visited = true;
		
		List<Statement> statements = new ArrayList<Statement>();
		
		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(methodMetrics.getCreationTime());
		
		URI idSystemURI = addSystem(methodMetrics.getSystemId(), statements);
		URI idApplicationURI = addApplication(methodMetrics.getApplicationId(), statements);
		
		URI idURI = model.createURI(simNS + UUID.randomUUID().toString().replace("-", ""));
		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "MethodMetric")));
		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
		statements.add(model.createStatement(idSystemURI, hasSystemMetricURI, idURI));
		statements.add(model.createStatement(idApplicationURI, hasSystemMetricURI, idURI));
		statements.add(model.createStatement(idURI, hasMethodNameURI, model.createPlainLiteral(methodMetrics.getMethodName())));

		
	}
	
	@Override
	public void visit(SystemMetrics systemMetrics) {
		visited = true;
		
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
		URI idURI = model.createURI(simNS + UUID.randomUUID().toString().replace("-", ""));
		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + "SystemLoadAverage")));
		statements.add(model.createStatement(idURI, hasDataValueURI, value));
		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
		statements.add(model.createStatement(idSystemURI, hasSystemMetricURI, idURI));
		
		return statements;
	}

	private URI addSystem(SystemId systemId, List<Statement> statements) {
		//System metric
		URI idSystemURI = model.createURI(simNS + systemId.getId());
		Statement systemTypeStatement = model.createStatement(idSystemURI, typePredicateURI, model.createURI(simNS + "System"));
		ClosableIterator<Statement> systemStatement = model.findStatements(systemTypeStatement);
		if (!systemStatement.hasNext()) {
			statements.add(model.createStatement(idSystemURI, typePredicateURI, model.createURI(simNS + "System")));
			statements.add(model.createStatement(idSystemURI, hasNameURI, getStringTypeURI(systemId.getName())));
		}
		systemStatement.close();
		
		return idSystemURI;
	}

	private URI addApplication(ApplicationId applicationId, List<Statement> statements) {
		//System metric
		URI idApplicationURI = model.createURI(simNS + applicationId.getId());
		Statement applicationTypeStatement = model.createStatement(idApplicationURI, typePredicateURI, model.createURI(simNS + "Application"));
		ClosableIterator<Statement> applicationStatement = model.findStatements(applicationTypeStatement);
		if (!applicationStatement.hasNext()) {
			statements.add(model.createStatement(idApplicationURI, typePredicateURI, model.createURI(simNS + "Application")));
			statements.add(model.createStatement(idApplicationURI, hasNameURI, getStringTypeURI(applicationId.getName())));
		}
		applicationStatement.close();
		
		return idApplicationURI;
	}

}
