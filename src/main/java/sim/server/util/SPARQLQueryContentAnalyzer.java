package sim.server.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;

public class SPARQLQueryContentAnalyzer {
	private static final Logger log = LoggerFactory.getLogger(SPARQLQueryContentAnalyzer.class);

	private String queryContent;
	private int querySizeInCharacters;
	private int queryNamespaceNb;
	private Set<String> queryNamespaceKeys;
	

	private Set<String> queryNamespaceValues;
	private int queryVariablesNb;
	private int queryDataSetSourcesNb;
	private List<String> queryDataSetSources;
	private int queryOperatorsNb;
	private int queryResultOrderingNb;
	private int queryResultLimitNb;
	private int queryResultOffsetNb;

	public SPARQLQueryContentAnalyzer(String QueryContent) {
		this.queryContent = QueryContent;
		this.querySizeInCharacters = 0;
	}


	public boolean parseQuery() {
		// parses the QueryContent and fills up all the fields in this class

		try {
			Query query = QueryFactory.create(queryContent);
			
			//set the QuerySizeInCharacters
			this.querySizeInCharacters = queryContent.length();
			
			parsePrefixInformation(query);

			parseVariablesInformation(query);

			parseDataSetSourcesInformation(query);

			parseOrderByStatement(query);

			parseLimitStatement(query);

			parseOffsetStatement(query);
			
			return true;

		} catch (Exception e) {
			log.error("error parsing query {}", queryContent, e);
			return false;
		}
	}
	
	public void parsePrefixInformation(Query query) throws Exception {
		// gets the information related to the prefixes, i.e
		// QueryDataSetSourcesNb and QueryDataSetSources

		// display the number of prefix statements along with the namespaces
		// they represent
		PrefixMapping prefixMapping = query.getPrefixMapping();

		Map<String, String> prefixEquivalents = prefixMapping.getNsPrefixMap();

		// get only the prefixes that are used in the query;

		Set<String> keySetTmp = prefixEquivalents.keySet();

		// find what keys are used in the query;
		// some prefixes may appear in the prefix declaration but they are not
		// effectively used in the corpus of the query
		// hence further parsing is needed to the the exact list of prefixes;

		if (!query.isSelectType()) {
			throw new Exception("NOT A SELECT Query!");
		}
		String QueryBody = query.toString().toLowerCase();

		int selectIndex = QueryBody.indexOf("select");
		if (selectIndex < 0) {
			throw new Exception("NOT A SELECT Query!");
		}

		String selectBody = QueryBody.substring(selectIndex);
		Iterator<String> it = keySetTmp.iterator();
		int keyValueIndex = -1;
		Set<String> keys = new HashSet<String>();
		Set<String> values = new HashSet<String>();

		while (it.hasNext()) {
			// Return the value to which this map maps the specified key.
			String crtKey = it.next();
			String crtValue = prefixEquivalents.get(crtKey);
			keyValueIndex = selectBody.indexOf(crtKey);
			if (keyValueIndex > 0) {
				keys.add(crtKey);
				values.add(crtValue);
			} else {
				keyValueIndex = selectBody.indexOf(crtValue);
				if (keyValueIndex > 0) {
					keys.add(crtKey);
					values.add(crtValue);
				}
			}
		}
		this.queryNamespaceNb = keys.size();
		this.queryNamespaceKeys = keys;
		this.queryNamespaceValues = values;
	}

	public void parseVariablesInformation(Query query) {
		// finds all the information related to variables in the query

		/*
		 * /*display the number of variables contained in the query and the
		 * variables
		 */
		Element queryBlock = query.getQueryPattern();

		Set<String> variablesSet = new HashSet<String>();

		// save the variables from where clause - they might not contain all the
		// variables from select
		Set<Var> variablesInWhereClause = queryBlock.varsMentioned();

		// save the variables from where clause to the list containing all the
		// variables
		Iterator<Var> its = variablesInWhereClause.iterator();
		while (its.hasNext())
			variablesSet.add(its.next().toString().substring(1));

		// save the variables from select clause to the list containing all the
		// variables
		for (int i = 0; i < query.getResultVars().size(); i++)
			variablesSet.add(query.getResultVars().get(i));

	
		this.queryVariablesNb = variablesSet.size();
	}

	public void parseDataSetSourcesInformation(Query query) {
		// extract the from clauses
		/*
		 * display the number of FROM sources contained in the query and the
		 * sources
		 */

		if (query.hasDatasetDescription()) {
			List<String> graphUris = query.getGraphURIs();

			this.queryDataSetSourcesNb = graphUris.size();
			this.queryDataSetSources = graphUris;
		}
	}

	public void parseOrderByStatement(Query query) {

		/*
		 * /*display the number of fields in the ORDER BY statement and the
		 * fields
		 */

		if (query.hasOrderBy()) {
			List<SortCondition> orderByFields = query.getOrderBy();
			
			this.queryResultOrderingNb = orderByFields.size();
		}
	}

	public void parseLimitStatement(Query query) {
		/*
		 * /*display the value of n from the LIMIT n statement
		 */
		if (query.hasLimit()) {
			long limit = query.getLimit();

			this.queryResultLimitNb = (int) limit;
		}
	}

	public void parseOffsetStatement(Query query) {
		/*
		 * display the value of m from the OFFSET m statement
		 */
		if (query.hasOffset()) {
			long offset = query.getOffset();

			this.queryResultOffsetNb = (int) offset;
		}
	}

	
	/**
	 * @return the queryContent
	 */
	public String getQueryContent() {
		return queryContent;
	}

	public int getQuerySizeInCharacters() {
		return querySizeInCharacters;
	}


	/**
	 * @return the queryNamespaceNb
	 */
	public int getQueryNamespaceNb() {
		return queryNamespaceNb;
	}

	/**
	 * @return the queryVariablesNb
	 */
	public int getQueryVariablesNb() {
		return queryVariablesNb;
	}

	/**
	 * @return the queryDataSetSourcesNb
	 */
	public int getQueryDataSetSourcesNb() {
		return queryDataSetSourcesNb;
	}

	/**
	 * @return the queryDataSetSources
	 */
	public List<String> getQueryDataSetSources() {
		return queryDataSetSources;
	}

	/**
	 * @return the queryOperatorsNb
	 */
	public int getQueryOperatorsNb() {
		return queryOperatorsNb;
	}

	/**
	 * @return the queryResultOrderingNb
	 */
	public int getQueryResultOrderingNb() {
		return queryResultOrderingNb;
	}


	/**
	 * @return the queryResultLimitNb
	 */
	public int getQueryResultLimitNb() {
		return queryResultLimitNb;
	}

	/**
	 * @return the queryResultOffsetNb
	 */
	public int getQueryResultOffsetNb() {
		return queryResultOffsetNb;
	}
	
	/**
	 * @return the queryNamespaceKeys
	 */
	public Set<String> getQueryNamespaceKeys() {
		return queryNamespaceKeys;
	}

	public Set<String> getQueryNamespaceValues() {
		return queryNamespaceValues;
	}


}
