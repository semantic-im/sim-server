package sim.server.atomic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.URI;

import sim.server.RdfDatabase;
import sim.server.data.Metric;
import sim.server.data.MetricImpl;

public class AtomicMetricsGenerator {
	
	private String queryPrefixes = 			
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
		"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> " +
		"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX sim:<http://www.larkc.eu/ontologies/IMOntology.rdf#> " +
		"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
		"PREFIX xml:<http://www.w3.org/XML/1998/namespace> ";

	private RdfDatabase rdfDatabase;
	
	
	/**
	 * A HashMap having as a key the URI of an atomic metric 
	 * and the atomic metric
	 */
	private HashMap<URI, Metric> atomicMetricsDefinition;

	
	public AtomicMetricsGenerator(RdfDatabase rdfDatabase){
		this.rdfDatabase = rdfDatabase;
		atomicMetricsDefinition = new HashMap<URI, Metric>();

		rdfDatabase.open();
		readSchemaAtomicMetrics();
		rdfDatabase.close();
	}

	public int countAtomicMetrics(){	
		return atomicMetricsDefinition.size();
	}

	private void readSchemaAtomicMetrics(){		
		String queryString = queryPrefixes +
			"Select distinct ?a ?b"+
			"Where { "+
				"?a rdfs:subClassOf ?c. "+
				"?a rdfs:subClassOf sim:AtomicMetric." +
				"?c owl:onProperty sim:hasMethodId." +
				"?c owl:hasValue ?b.}"; 

		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
		while(cr.hasNext()){
			QueryRow qr = cr.next();
			URI type = qr.getValue("a").asURI();
			
			Metric atomicMetric = new MetricImpl(type);
			
			atomicMetricsDefinition.put(type,atomicMetric);
			
		}		
	}

	public HashMap<URI, Metric> getAtomicMetricsDefinition() {
		return atomicMetricsDefinition;
	}

	public RdfDatabase getRdfDatabase() {
		return rdfDatabase;
	}
	
}
