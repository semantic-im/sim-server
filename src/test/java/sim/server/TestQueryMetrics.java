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

import junit.framework.TestCase;
import sim.data.ApplicationId;
import sim.data.Context;
import sim.data.MethodMetrics;
import sim.data.MethodMetricsImpl;
import sim.data.SystemId;
import sim.server.util.SPARQLQueryContentAnalyzer;

/**
 * ioantoma 
 */

public class TestQueryMetrics extends TestCase {
	
	String query = "";
	
	public TestQueryMetrics() {
	}

	@Override
	protected void setUp() throws Exception {
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> "+
		"SELECT DISTINCT ?name "+
		"WHERE { " +
		"?x rdf:type foaf:Person . "+
		"?x foaf:name ?name "+
		"}" +
		"ORDER BY ?name "+
		"LIMIT 100";

		super.setUp();
	}
	
	public void testParseQuery(){				
		SPARQLQueryContentAnalyzer sqa = new SPARQLQueryContentAnalyzer(query);
		sqa.parseQuery();		
		
		assertEquals(2, sqa.getQueryNamespaceNb());
		assertEquals(0, sqa.getQueryDataSetSourcesNb());
		assertEquals(0, sqa.getQueryOperatorsNb());
		assertEquals(100, sqa.getQueryResultLimitNb());
		assertEquals(0, sqa.getQueryResultOffsetNb());
		assertEquals(1, sqa.getQueryResultOrderingNb());
		assertEquals(200, sqa.getQuerySizeInCharacters());
		assertEquals(2, sqa.getQueryVariablesNb());		
	}
	
	public void testQueryMetrics(){				
		
		//add into the context information about the query
		Context context = Context.create("", "", null);
		context.put(RdfDatabase.QUERY_CONTENT, query);

		//setup the RdfDatabase and connect
		RdfDatabase rdfDatabase = new RdfDatabase();
		Main.storage_server_domain = "localhost";
		Main.storage_server_port  = 8080;
		Main.storage_repository_id = "sim";
		
		rdfDatabase.open();
		//process Context; as the context is set to QueryContext metrics related 
		//to query will be extracted from the query content and will be stored in 
		//the repository
		rdfDatabase.visit(context);
		rdfDatabase.close();
	}	
	
}
