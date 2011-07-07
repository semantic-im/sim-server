/*
 * Copyright 2010 Softgress - http://www.softgress.com/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package sim.server;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import junit.framework.TestCase;
import sim.server.compund.CompundMetricsGenerator;
import sim.server.data.CompoundMetric;
import sim.server.data.Metric;
import sim.server.data.MetricImpl;

/**
 * ioantoma 
 */

public class TestCompoundMetrics extends TestCase {
	
	private CompundMetricsGenerator cmg; 

	public TestCompoundMetrics() {
		RdfDatabase rdfDatabase = new RdfDatabase();
		Main.storage_server_domain = "localhost";
		Main.storage_server_port  = 8080;
		Main.storage_repository_id = "sim";

		cmg = new CompundMetricsGenerator(rdfDatabase);
	}
	
	public void testCountCompuntMetrics(){				
		assertEquals(10,cmg.countCompoundMetrics());		
	}	
	
	public void testGenerateCompoundMetric(){
		URI uri = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#QueriesInvocation");
		Metric m1 = new MetricImpl(uri);
		Metric m2 = new MetricImpl(uri);
		Metric m3 = new MetricImpl(uri);
		Metric m4 = new MetricImpl(uri);
		Metric m5 = new MetricImpl(uri);
		
		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(m1);
		metrics.add(m2);
		metrics.add(m3);
		metrics.add(m4);
		metrics.add(m5);
		
		CompoundMetric cm1 = cmg.generateCompundMetric(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#QueriesPerTimeInterval"), metrics);
				
		assertEquals(new Double(5.0).doubleValue(),cm1.getValue());
		
		uri = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowTotalResponseTime");
		m1 = new MetricImpl(uri);
		m1.setValue(1000);
		m2 = new MetricImpl(uri);
		m2.setValue(1100);
		m3 = new MetricImpl(uri);
		m3.setValue(1200);
		m4 = new MetricImpl(uri);
		m4.setValue(1000);
		m5 = new MetricImpl(uri);
		m5.setValue(1200);
		
		
		metrics = new ArrayList<Metric>();
		metrics.add(m1);
		metrics.add(m2);
		metrics.add(m3);
		metrics.add(m4);
		metrics.add(m5);
		
		CompoundMetric cm2 = cmg.generateCompundMetric(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowAvgDurationPerTimeInterval"), metrics);
		assertEquals(new Double(1100.0).doubleValue(),cm2.getValue());
	}
	
	
	
}
