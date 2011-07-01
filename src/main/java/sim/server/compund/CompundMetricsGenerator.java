package sim.server.compund;

import java.util.HashMap;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;


import sim.data.Metrics;
import sim.server.RdfDatabase;
import sim.server.compund.data.CompoundMetric;
import sim.server.compund.data.CompoundMetricImpl;
import sim.server.compund.data.Metric;


public class CompundMetricsGenerator {

	public static final URI COUNT = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Count");
	public static final URI AVERAGE = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Average");
	public static final URI SUM = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Sum");
	
	private String queryPrefixes = 			
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
		"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> " +
		"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX sim:<http://www.larkc.eu/ontologies/IMOntology.rdf#> " +
		"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
		"PREFIX xml:<http://www.w3.org/XML/1998/namespace> ";

	private RdfDatabase rdfDatabase;
	private HashMap<URI, CompoundMetric> compoundMetricsDefinition;
	
	public CompundMetricsGenerator(RdfDatabase rdfDatabase){
		this.rdfDatabase = rdfDatabase;
		compoundMetricsDefinition = new HashMap<URI, CompoundMetric>();
		
		rdfDatabase.open();
		readSchemaCompundMetrics();
		rdfDatabase.close();
	}
	
	private void readSchemaCompundMetrics(){		
		String queryString = queryPrefixes +
			"Select distinct ?a ?b "+
			"Where { "+
				"?c ?d sim:hasAggregationFunction. "+ 
				"?c owl:allValuesFrom ?b. "+
				"?a rdfs:subClassOf ?c. "+
				"?a rdfs:subClassOf sim:CompoundMetric.}"; 

		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
		while(cr.hasNext()){
			QueryRow qr = cr.next();
			URI type = qr.getValue("a").asURI();
			URI aggregationFunction = qr.getValue("b").asURI(); 
			CompoundMetric compoundMetric = new CompoundMetricImpl(type, aggregationFunction);
			compoundMetricsDefinition.put(type,compoundMetric);
		}		
	}

	public int countCompoundMetrics(){	
		return compoundMetricsDefinition.size();
	}
	
	public CompoundMetric generateCompundMetric(URI type, List<Metric> metrics){
		float value = 0;
		CompoundMetric compoundMetric = compoundMetricsDefinition.get(type);
		for(Metric m:metrics)
			compoundMetric.addConstituentMetric(m);
		
		if(compoundMetric.getAggregationFunction().equals(COUNT)){
			value = metrics.size();
		} 
		if (compoundMetric.getAggregationFunction().equals(SUM)){
			float sum = 0;
			for(Metric m:metrics)
				sum +=m.getValue();
			value = sum;
		}
		if (compoundMetric.getAggregationFunction().equals(AVERAGE)){
			float sum = 0;
			for(Metric m:metrics){
				sum +=m.getValue();
			}
			value = sum/metrics.size();
		}
		compoundMetric.setValue(value);
		
		return compoundMetric;
	}
			
}
