 
package sim.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.data.Context;
import sim.data.MethodMetrics;
import sim.data.MetricsVisitor;
import sim.data.PlatformMetrics;
import sim.data.SystemId;
import sim.data.SystemMetrics;
import sim.server.data.CompoundMetric;
import sim.server.util.SPARQLQueryContentAnalyzer;

/**
 * @author silviu
 * 
 */

public class SqlDatabase implements MetricsVisitor {

	private static final Logger logger = LoggerFactory.getLogger(SqlDatabase.class);

	private Connection conn;

	private PreparedStatement stmtGetMetricId;

	private PreparedStatement stmtInsertPlugin;

	private PreparedStatement stmtInsertWorkflowMetric;

	private PreparedStatement stmtInsertPluginMetric;

	private PreparedStatement stmtInsertPluginIntoWorkflow;

	private PreparedStatement stmtInsertPlatformMetric;

	private PreparedStatement stmtInsertQuery;

	private PreparedStatement stmtInsertQueryMetric;

	private PreparedStatement stmtInsertPlatformAndApplication;
	
	private PreparedStatement stmtInsertSystem;
	
	private PreparedStatement stmtInsertSystemMetric;

	private PreparedStatement stmtInsertWorkflowInstance;
	private PreparedStatement stmtUpdateWorkflowInstanceChangeDescription;

	private PreparedStatement stmtInsertQueryIntoWorkflow;

	private PreparedStatement stmtInsertWorkflowIntoPlatform;
	
	private HashMap<String, Integer> metricIDCache = new HashMap<String, Integer>();
	private HashSet<String> existingPlatformInstances = new HashSet<String>();
	private HashMap<String, TreeSet<String>> workflowContextIdToWorkflowPluginSet = new HashMap<String, TreeSet<String>>();

	private int getMetricID(String metric) throws SQLException {
		Integer id = metricIDCache.get(metric);
		if (id == null) {
			stmtGetMetricId.setString(1, metric);
			ResultSet rs = stmtGetMetricId.executeQuery();
			if (!rs.next()) {
				throw new SQLException("Metric "+metric+" does not exist in Database");
			}
			int newId = rs.getInt(1);
			metricIDCache.put(metric, newId);
			return newId;
		}
		return id;
	}

	


	
	private String workflowPluginSetToWorkflowDescription(SortedSet<String> pluginSet) {
		StringBuilder ret = new StringBuilder();
		for (String s : pluginSet) {
			ret.append(s);
			ret.append("; ");
		}
		return ret.toString();
	}

	private void ensureWorkflowContextIdExist(String workflowContextId) throws SQLException {
		if (!workflowContextIdToWorkflowPluginSet.containsKey(workflowContextId)) {
			workflowContextIdToWorkflowPluginSet.put(workflowContextId, new TreeSet<String>());

			// create a workflow instance with no plugins yet
			stmtInsertWorkflowInstance.setString(1, workflowContextId);
			stmtInsertWorkflowInstance.setString(2, ";");
			stmtInsertWorkflowInstance.execute();
		}
	}
	
	private void insertPluginIntoWorkflowDescription(String pluginName, String workflowContextId, String pluginContextId) throws SQLException {
		TreeSet<String> pluginSet = workflowContextIdToWorkflowPluginSet.get(workflowContextId);
		if(pluginSet==null) {
			ensureWorkflowContextIdExist(workflowContextId);
			pluginSet = workflowContextIdToWorkflowPluginSet.get(workflowContextId);
		}		
		if(!pluginSet.contains(pluginName)) {
			stmtInsertPluginIntoWorkflow.setString(1, workflowContextId);
			stmtInsertPluginIntoWorkflow.setString(2, pluginContextId);
			stmtInsertPluginIntoWorkflow.execute();
			
			pluginSet.add(pluginName);
			stmtUpdateWorkflowInstanceChangeDescription.setString(2, workflowContextId);
			stmtUpdateWorkflowInstanceChangeDescription.setString(1, workflowPluginSetToWorkflowDescription(pluginSet));
			stmtUpdateWorkflowInstanceChangeDescription.execute();
		}
		
	}
		


	public void open() {
		if (conn != null)
			return; // already open
		Properties connectionProps = new Properties();
		connectionProps.put("user", Main.storage_sql_user_name);
		connectionProps.put("password", Main.storage_sql_password);
		String connectionString = "jdbc:" + Main.storage_sql_dbms + "://" + Main.storage_sql_server + ":"
				+ Main.storage_sql_port + "/" + Main.storage_sql_database;
		logger.debug("Opening mysql connection to: " + connectionString);
		try {
			conn = DriverManager.getConnection(connectionString, connectionProps);

			// prepare statements
			stmtGetMetricId = conn.prepareStatement("select idMetric from metrics where MetricName=?");

			stmtInsertPlugin = conn.prepareStatement("insert ignore into plugins values(?,?)");

			stmtInsertQueryMetric = conn
					.prepareStatement("insert ignore into queries_metrics values(?,?,?,?)");

			stmtInsertWorkflowMetric = conn
					.prepareStatement("insert ignore into workflows_metrics values(?,?,?,?)");

			stmtInsertPluginMetric = conn
					.prepareStatement("insert ignore into plugins_metrics values(?,?,?,?)");

			stmtInsertPlatformMetric = conn
					.prepareStatement("insert ignore into platforms_metrics values(?,?,?,?)");

			stmtInsertQuery = conn.prepareStatement("insert ignore into queries values(?,?)");

			stmtInsertPlatformAndApplication = conn
					.prepareStatement("insert ignore into platforms values(?,?,?)");

			stmtInsertWorkflowInstance = conn.prepareStatement("insert ignore into workflows values(?,?)");
			stmtUpdateWorkflowInstanceChangeDescription = conn
					.prepareStatement("update workflows set WorkflowDescription=? where idWorkflow=?");

			stmtInsertPluginIntoWorkflow = conn
					.prepareStatement("insert ignore into workflows_plugins values(?,?)");

			stmtInsertQueryIntoWorkflow = conn
					.prepareStatement("insert ignore into queries_workflows values(?,?)");

			stmtInsertWorkflowIntoPlatform = conn
					.prepareStatement("insert ignore into platforms_workflows values(?,?)");
			
			stmtInsertSystem = conn.prepareStatement("insert ignore into systems values(?,?,?,?)");
			
			stmtInsertSystemMetric = conn.prepareStatement("insert ignore into systems_metrics values(?,?,?,?)");

		} catch (SQLException e) {
			logger.error("Exception encounter while trying to open the SQL connection: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void close() {
		logger.debug("Closing SQL Connection");
		try {
			metricIDCache.clear();
			existingPlatformInstances.clear();
			workflowContextIdToWorkflowPluginSet.clear();
			stmtGetMetricId.close();
			stmtInsertPlugin.close();
			stmtInsertQueryMetric.close();
			stmtInsertWorkflowMetric.close();
			stmtInsertPluginMetric.close();
			stmtInsertPlatformMetric.close();
			stmtInsertQuery.close();
			stmtInsertPlatformAndApplication.close();
			stmtInsertWorkflowInstance.close();
			stmtUpdateWorkflowInstanceChangeDescription.close();
			stmtInsertPluginIntoWorkflow.close();
			stmtInsertQueryIntoWorkflow.close();
			stmtInsertWorkflowIntoPlatform.close();
			conn.close();
			conn = null;
		} catch (SQLException e) {
			logger.error("Exception encounter while trying to close the SQL connection: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void visit(MethodMetrics methodMetrics) {
		try {
			processMetric(methodMetrics);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void visit(SystemMetrics systemMetrics) {
		SystemId systemId = systemMetrics.getSystemId();
		
		String sysIdString = systemId.getId();
		
		try {
			stmtInsertSystem.setString(1, sysIdString);
			stmtInsertSystem.setString(2, systemId.getName());
			stmtInsertSystem.setLong(3, systemId.getCpuCount());
			stmtInsertSystem.setLong(4, systemId.getTotalMemory());
			stmtInsertSystem.execute();
			
			stmtInsertSystemMetric.setString(1, sysIdString);
			stmtInsertSystemMetric.setTimestamp(4, new Timestamp(systemMetrics.getCreationTime()));
			
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemLoadAverage"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSystemLoadAverage());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTotalFreeMemory"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTotalSystemFreeMemory());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTotalUsedMemory"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTotalSystemUsedMemory());
			stmtInsertSystemMetric.execute();
	
			
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTotalUsedSwap"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTotalSystemUsedSwap());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTotalUsedSwap"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTotalSystemUsedSwap());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemOpenFileDescrCnt"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSystemOpenFileDescriptors());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemSwapIn"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSwapIn());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemSwapOut"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSwapOut());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIORead"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIORead());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIOWrite"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIOWrite());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemUserCPULoad"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getUserPerc());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemCPULoad"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSysPerc());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIdleCPULoad"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIdlePerc());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemWaitCPULoad"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getWaitPerc());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIrqCPULoad"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIrqPerc());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemUserCPUTime"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getUser());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemCPUTime"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getSys());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIdleCPUTime"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIdle());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemWaitCPUTime"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getWait());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemIrqCPUTime"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getIrq());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemThreadsCount"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getThreadsCount());
			stmtInsertSystemMetric.execute();
	
			
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemProcessesCount"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getProcessesCount());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemRunningProcessesCount"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getRunningProcessesCount());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTcpInbound"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTcpInbound());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemTcpOutbound"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getTcpOutbound());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemNetworkSent"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getNetworkSent());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemNetworkReceived"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getNetworkReceived());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemLoopbackNetworkSent"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getLoopbackNetworkSent());
			stmtInsertSystemMetric.execute();
	
			stmtInsertSystemMetric.setInt(2, getMetricID("SystemLoopbackNetworkReceived"));
			stmtInsertSystemMetric.setString(3, ""+systemMetrics.getLoopbackNetworkReceived());
			stmtInsertSystemMetric.execute();
		}catch(SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}



	@Override
	public void visit(Context context) {
		if (context == null)
			return;
		String contextId = context.getId();
		String contextName = context.getName();
		try {
			if ("WorkflowExecution".equals(contextName)) {
				logger.debug("Context got WorkflowExecution");
				
				
				
				stmtInsertQueryIntoWorkflow.setString(1, context.getParentContextId());
				stmtInsertQueryIntoWorkflow.setString(2, contextId);
				stmtInsertQueryIntoWorkflow.execute();
				
				stmtInsertWorkflowIntoPlatform.setString(1, context.getSystemId().getId());
				stmtInsertWorkflowIntoPlatform.setString(2, contextId);
				stmtInsertWorkflowIntoPlatform.execute();
				
				stmtInsertWorkflowMetric.setString(1, contextId);
				stmtInsertWorkflowMetric.setTimestamp(4, new Timestamp(context.getCreationTime()));
					
				if (context.containsKey("WorkflowNumberOfPlugins")) {
					logger.debug("Context got WorkflowNumberOfPlugins");
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowNumberOfPlugins"));
					stmtInsertWorkflowMetric.setString(3, context.get("WorkflowNumberOfPlugins").toString());
					stmtInsertWorkflowMetric.execute();
				}
				
				Object numberOfExceptions = context.get("NumberOfExceptions");
				if (numberOfExceptions != null) {
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowNumberOfExceptions"));
					stmtInsertWorkflowMetric.setString(3, numberOfExceptions.toString());
					stmtInsertWorkflowMetric.execute();
				}
				
				Object numberOfMalformedSpqarqlQueryExceptions = context.get("NumberOfMalformedSparqlQueryExceptions");
				if (numberOfMalformedSpqarqlQueryExceptions != null) {
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowNumberOfMalformedSparqlQueryExceptions"));
					stmtInsertWorkflowMetric.setString(3, numberOfMalformedSpqarqlQueryExceptions.toString());
					stmtInsertWorkflowMetric.execute();
				}

				Object numberOfOutOfMemoryExceptions = context.get("NumberOfOutOfMemoryExceptions");
				if (numberOfOutOfMemoryExceptions != null) {
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowNumberOfOutOfMemoryExceptions"));
					stmtInsertWorkflowMetric.setString(3, numberOfOutOfMemoryExceptions.toString());
					stmtInsertWorkflowMetric.execute();
				}

				
				Object dataLayerInserts = context.get("DataLayerInserts");
				if (dataLayerInserts != null) {
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowDataLayerInserts"));
					stmtInsertWorkflowMetric.setString(3, dataLayerInserts.toString());
					stmtInsertWorkflowMetric.execute();
				}
				

				Object dataLayerSelects = context.get("DataLayerISelects");
				if (dataLayerSelects != null) {
					stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowDataLayerSelects"));
					stmtInsertWorkflowMetric.setString(3, dataLayerSelects.toString());
					stmtInsertWorkflowMetric.execute();
				}
			}

			if ("PluginExecution".equals(contextName)) {
				logger.debug("Context got PluginExecution");
				String pluginName = (String) context.get("PluginName");
				logger.debug("got context plugin name=" + pluginName);
				
				stmtInsertPlugin.setString(1, contextId);
				stmtInsertPlugin.setString(2, pluginName);
				stmtInsertPlugin.execute();
				
				String workflowContextId = context.getParentContextId();				
				insertPluginIntoWorkflowDescription(pluginName, workflowContextId, contextId);
				
				
				stmtInsertPluginMetric.setString(1, contextId);
				stmtInsertPluginMetric.setTimestamp(4, new Timestamp(context.getCreationTime()));

				Object pluginInputSizeInTriples = context.get("PluginInputSizeInTriples");
				if (pluginInputSizeInTriples != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginInputSizeInTriples"));
					stmtInsertPluginMetric.setString(3, pluginInputSizeInTriples.toString());
					stmtInsertPluginMetric.execute();
				}

				Object pluginOutputSizeInTriples = context.get("PluginOutputSizeInTriples");
				if (pluginOutputSizeInTriples != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginOutputSizeInTriples"));
					stmtInsertPluginMetric.setString(3, pluginOutputSizeInTriples.toString());
					stmtInsertPluginMetric.execute();
				}

				Object pluginCacheHit = context.get("PluginCacheHit");
				if (pluginCacheHit != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginCacheHit"));
					stmtInsertPluginMetric.setString(3, pluginCacheHit.toString());
					stmtInsertPluginMetric.execute();
				}
				
				Object numberOfExceptions = context.get("NumberOfExceptions");
				if (numberOfExceptions != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginNumberOfExceptions"));
					stmtInsertPluginMetric.setString(3, numberOfExceptions.toString());
					stmtInsertPluginMetric.execute();
				}
				
				Object numberOfMalformedSpqarqlQueryExceptions = context.get("NumberOfMalformedSparqlQueryExceptions");
				if (numberOfMalformedSpqarqlQueryExceptions != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginNumberOfMalformedSparqlQueryExceptions"));
					stmtInsertPluginMetric.setString(3, numberOfMalformedSpqarqlQueryExceptions.toString());
					stmtInsertPluginMetric.execute();
				}

				Object numberOfOutOfMemoryExceptions = context.get("NumberOfOutOfMemoryExceptions");
				if (numberOfOutOfMemoryExceptions != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginNumberOfOutOfMemoryExceptions"));
					stmtInsertPluginMetric.setString(3, numberOfOutOfMemoryExceptions.toString());
					stmtInsertPluginMetric.execute();
				}

				
				Object dataLayerInserts = context.get("DataLayerInserts");
				if (dataLayerInserts != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginDataLayerInserts"));
					stmtInsertPluginMetric.setString(3, dataLayerInserts.toString());
					stmtInsertPluginMetric.execute();
				}
				

				Object dataLayerSelects = context.get("DataLayerISelects");
				if (dataLayerSelects != null) {
					stmtInsertPluginMetric.setInt(2, getMetricID("PluginDataLayerSelects"));
					stmtInsertPluginMetric.setString(3, dataLayerSelects.toString());
					stmtInsertPluginMetric.execute();
				}


			}

			/*
			 * check if the context contains information about SPARQL query; if
			 * that's the case the SPARQL query is parsed and query atomic
			 * metrics are created for this query
			 */

			if ("Query".equals(contextName)) {
				logger.debug("Context got QueryContent");

				String query = "";
				if(context.get("QueryContent")==null) {
					logger.warn("Query context does not contain QueryContet, skipping...");
				} else {
					query = context.get("QueryContent").toString();
				}
				
				
				stmtInsertQuery.setString(1, contextId);
				stmtInsertQuery.setString(2, query.length()<1000?query:query.substring(0, 1000));
				stmtInsertQuery.execute();
				
				stmtInsertQueryMetric.setString(1, contextId);
				stmtInsertQueryMetric.setTimestamp(4, new Timestamp(context.getCreationTime()));

				SPARQLQueryContentAnalyzer sqa = new SPARQLQueryContentAnalyzer(query);
				
				if (sqa.parseQuery()) {

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryDataSetSourcesNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryNamespaceNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryNamespaceNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryNamespaceNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryOperatorsNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryOperatorsNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryResultLimitNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryResultLimitNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryResultOffsetNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryResultOffsetNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryResultOrderingNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryResultOrderingNb());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QuerySizeInCharacters"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQuerySizeInCharacters());
					stmtInsertQueryMetric.execute();

					stmtInsertQueryMetric.setInt(2, getMetricID("QueryVariablesNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryVariablesNb());
					stmtInsertQueryMetric.execute();
					
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryLiteralsNb"));
					stmtInsertQueryMetric.setString(3, "" + sqa.getQueryLiteralsNb());
					stmtInsertQueryMetric.execute();
					
					if (sqa.getQueryDataSetSources() != null && sqa.getQueryDataSetSources().size() > 0) {
						StringBuilder sb = new StringBuilder("[");
						boolean firstTime = true;
						for(String dataSource:sqa.getQueryDataSetSources()) {
							if (firstTime)
								firstTime = false;
							else
								sb.append(", ");
							sb.append(dataSource);
						}
						
						stmtInsertQueryMetric.setInt(2, getMetricID("QueryDataSetSources"));
						stmtInsertQueryMetric.setString(3, sb.toString());
						stmtInsertQueryMetric.execute();
					}

					if (sqa.getQueryNamespaceValues() != null && sqa.getQueryNamespaceValues().size() > 0) {
						StringBuilder sb = new StringBuilder("[");
						boolean firstTime = true;
						for(String namespace:sqa.getQueryNamespaceValues()) {
							if (firstTime)
								firstTime = false;
							else
								sb.append(", ");
							sb.append(namespace);
						}
						sb.append("]");
						stmtInsertQueryMetric.setInt(2, getMetricID("QueryNamespaceValues"));
						stmtInsertQueryMetric.setString(3, sb.toString());
						stmtInsertQueryMetric.execute();
					}
				}

				
				Object queryResultSizeInCharacters = context.get("QueryResultSizeInCharacters");
				if (queryResultSizeInCharacters != null) {
					logger.debug("QueryResultSizeInCharacters");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryResultSizeInCharacters"));
					stmtInsertQueryMetric.setString(3, queryResultSizeInCharacters.toString());
					stmtInsertQueryMetric.execute();
				}
				
				Object numberOfExceptions = context.get("NumberOfExceptions");
				if (numberOfExceptions != null) {
					logger.debug("QueryNumberOfExceptions ");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryNumberOfExceptions"));
					stmtInsertQueryMetric.setString(3, numberOfExceptions.toString());
					stmtInsertQueryMetric.execute();
				}
				Object numberOfMalformedSpqarqlQueryExceptions = context.get("NumberOfMalformedSparqlQueryExceptions");
				if (numberOfMalformedSpqarqlQueryExceptions != null) {
					logger.debug("QueryNumberOfMalformedSparqlQueryExceptions");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryNumberOfMalformedSparqlQueryExceptions"));
					stmtInsertQueryMetric.setString(3, numberOfMalformedSpqarqlQueryExceptions.toString());
					stmtInsertQueryMetric.execute();
				}
				
				Object numberOfOutOfMemoryExceptions = context.get("NumberOfOutOfMemoryExceptions");
				if (numberOfOutOfMemoryExceptions != null) {
					logger.debug("QueryNumberOfOutOfMemoryExceptions");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryNumberOfOutOfMemoryExceptions"));
					stmtInsertQueryMetric.setString(3, numberOfOutOfMemoryExceptions.toString());
					stmtInsertQueryMetric.execute();
				}

				
				Object dataLayerInserts = context.get("DataLayerInserts");
				if (dataLayerInserts != null) {
					logger.debug("QueryDataLayerInserts");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryDataLayerInserts"));
					stmtInsertQueryMetric.setString(3, dataLayerInserts.toString());
					stmtInsertQueryMetric.execute();
				}
				

				Object dataLayerSelects = context.get("DataLayerISelects");
				if (dataLayerSelects != null) {
					logger.debug("DataLayerISelects");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryDataLayerSelects"));
					stmtInsertQueryMetric.setString(3, dataLayerSelects.toString());
					stmtInsertQueryMetric.execute();
				}
				
				Object queryErrorMessage = context.get("QueryErrorMessage");
				if (queryErrorMessage != null) {
					logger.debug("QueryErrorMessage");
					stmtInsertQueryMetric.setInt(2, getMetricID("QueryErrorMessage"));
					stmtInsertQueryMetric.setString(3, queryErrorMessage.toString());
					stmtInsertQueryMetric.execute();
				}
				
				
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void visit(CompoundMetric compoundMetric) {
		// nothing in SQLDB for this
	}

	@Override
	public void visit(PlatformMetrics pm) {
		logger.debug("visit(PlatformMetrics)");
		// context and time stamp are constant throughout
		try {
		
			SystemId systemId = pm.getSystemId();
			String sysIdString = systemId.getId();
			if (!existingPlatformInstances.contains(sysIdString)) {
				existingPlatformInstances.add(sysIdString);

				stmtInsertPlatformAndApplication.setString(1, sysIdString);
				stmtInsertPlatformAndApplication.setString(2, systemId.getName());
				stmtInsertPlatformAndApplication.setString(3, pm.getApplicationId().getName());
				stmtInsertPlatformAndApplication.execute();
		}
			
			logger.debug("visit(PlatformMetrics) " + pm.getSystemId());
			stmtInsertPlatformMetric.setString(1, pm.getSystemId().getId());
			stmtInsertPlatformMetric.setTimestamp(4, new Timestamp(pm.getCreationTime()));

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformAvgCPUUsage"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getAvgCpuUsage());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformCPUTime"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getCpuTime());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformTotalCPUTime"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getTotalCpuTime());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformCPUUsage"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getCpuUsage());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformGccCount"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getGccCount());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformTotalGccCount"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getTotalGccCount());
			stmtInsertPlatformMetric.execute();

			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformGccTime"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getGccTime());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformTotalGccTime"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getTotalGccTime());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformUptime"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getUptime());
			stmtInsertPlatformMetric.execute();

			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformAllocatedMemory"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getAllocatedMemory());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformUsedMemory"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getUsedMemory());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformFreeMemory"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getFreeMemory());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformUnallocatedMemory"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getUnallocatedMemory());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformThreadsCount"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getThreadsCount());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformThreadsStarted"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getThreadsStarted());
			stmtInsertPlatformMetric.execute();
			
			stmtInsertPlatformMetric.setInt(2, getMetricID("PlatformTotalThreadsStarted"));
			stmtInsertPlatformMetric.setString(3, "" + pm.getTotalThreadsStarted());
			stmtInsertPlatformMetric.execute();
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void processMetric(MethodMetrics methodMetrics) throws SQLException {
		String contextId = methodMetrics.getContextId();
		if (contextId == null)
			return;
		long timeStamp = methodMetrics.getBeginExecutionTime();
		String methodID = methodMetrics.getMethod().getClassName() + "."
				+ methodMetrics.getMethod().getMethodName().replace("<init>", "new");

		/**
		 * if the method execution is the method execution of
		 * eu.larkc.core.endpoint.sparql.SparqlHandler.handle we write query
		 * related atomic metrics
		 */
		if (methodID.equals("eu.larkc.core.endpoint.sparql.SparqlHandler.handle")) {
			logger.debug("MethodMetrics got QueryMetrics" + contextId);
		
			stmtInsertQueryMetric.setString(1, contextId);
			stmtInsertQueryMetric.setTimestamp(4, new Timestamp(timeStamp));

			// write an instance of QueryBeginExecutionTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryBeginExecutionTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getBeginExecutionTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryEndExecutionTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryEndExecutionTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getEndExecutionTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryErrorStatus
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryErrorStatus"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.endedWithError());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryTotalResponseTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryTotalResponseTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getWallClockTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadUserCPUTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadUserCPUTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadUserCpuTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadSystemCPUTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadSystemCPUTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadSystemCpuTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadTotalCPUTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadTotalCPUTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadTotalCpuTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadCount
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadCount"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadCount());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadBlockCount
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadBlockCount"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadBlockCount());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadBlockTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadBlockTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadBlockTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadWaitCount
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadWaitCount"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadWaitCount());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadWaitTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadWaitTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadWaitTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadGccCount
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadGccCount"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadGccCount());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryThreadGccTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryThreadGccTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getThreadGccTime());
			stmtInsertQueryMetric.execute();

			// write an instance of QueryProcessTotalCPUTime
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryProcessTotalCPUTime"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getProcessTotalCpuTime());
			stmtInsertQueryMetric.execute();
			
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryAllocatedMemoryBefore"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getAllocatedMemoryBefore());
			stmtInsertQueryMetric.execute();
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryAllocatedMemoryAfter"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getAllocatedMemoryAfter());
			stmtInsertQueryMetric.execute();
			
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryUsedMemoryBefore"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getUsedMemoryBefore());
			stmtInsertQueryMetric.execute();
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryUsedMemoryAfter"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getUsedMemoryAfter());
			stmtInsertQueryMetric.execute();
					
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryFreeMemoryBefore"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getFreeMemoryBefore());
			stmtInsertQueryMetric.execute();
	
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryFreeMemoryAfter"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getFreeMemoryAfter());
			stmtInsertQueryMetric.execute();
	
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryUnallocatedMemoryBefore"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryBefore());
			stmtInsertQueryMetric.execute();
			
			stmtInsertQueryMetric.setInt(2, getMetricID("QueryUnallocatedMemoryAfter"));
			stmtInsertQueryMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryAfter());
			stmtInsertQueryMetric.execute();
		}

		/**
		 * if the method execution is the method execution of
		 * eu.larkc.core.executor.Executor.execute we write workflow related
		 * atomic metrics
		 */
		if (methodID.equals("eu.larkc.core.executor.Executor.execute")
				|| methodID.equals("eu.larkc.core.executor.Executor.getNextResults")) {
			logger.debug("MethodMetrics got WorkflowMetrics");
			
			// context and time stamp are constant throughout
			stmtInsertWorkflowMetric.setString(1, contextId);
			stmtInsertWorkflowMetric.setTimestamp(4, new Timestamp(timeStamp));

			// write an instance of WorkflowTotalResponseTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowTotalResponseTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getWallClockTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadUserCPUTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadUserCPUTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadUserCpuTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadSystemCPUTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadSystemCPUTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadSystemCpuTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadTotalCPUTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadTotalCPUTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadTotalCpuTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadCount
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadCount"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadCount());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadBlockCount
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadBlockCount"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadBlockCount());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadBlockTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadBlockTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadBlockTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadWaitCount
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadWaitCount"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadWaitCount());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadWaitTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadWaitTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadWaitTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadGccCount
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadGccCount"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadGccCount());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowThreadGccTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowThreadGccTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getThreadGccTime());
			stmtInsertWorkflowMetric.execute();

			// write an instance of WorkflowProcessTotalCPUTime
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowProcessTotalCPUTime"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getProcessTotalCpuTime());
			stmtInsertWorkflowMetric.execute();
			
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowAllocatedMemoryBefore"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getAllocatedMemoryBefore());
			stmtInsertWorkflowMetric.execute();
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowAllocatedMemoryAfter"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getAllocatedMemoryAfter());
			stmtInsertWorkflowMetric.execute();
			
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowUsedMemoryBefore"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getUsedMemoryBefore());
			stmtInsertWorkflowMetric.execute();
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowUsedMemoryAfter"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getUsedMemoryAfter());
			stmtInsertWorkflowMetric.execute();
					
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowFreeMemoryBefore"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getFreeMemoryBefore());
			stmtInsertWorkflowMetric.execute();
	
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowFreeMemoryAfter"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getFreeMemoryAfter());
			stmtInsertWorkflowMetric.execute();
	
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowUnallocatedMemoryBefore"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryBefore());
			stmtInsertWorkflowMetric.execute();
			
			stmtInsertWorkflowMetric.setInt(2, getMetricID("WorkflowUnallocatedMemoryAfter"));
			stmtInsertWorkflowMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryAfter());
			stmtInsertWorkflowMetric.execute();
		}

		/**
		 * if the method execution is the method execution of
		 * eu.larkc.plugin.Plugin.invoke we write plugin related atomic metrics
		 */
		if (methodID.equals("eu.larkc.plugin.Plugin.invoke")) {
			logger.debug("MethodMetrics got PluginMetrics");
			// context and time stamp are constant throughout
			
			stmtInsertPluginMetric.setString(1, contextId);
			stmtInsertPluginMetric.setTimestamp(4, new Timestamp(timeStamp));

			// write an instance of PluginBeginExecutionTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginBeginExecutionTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getBeginExecutionTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginEndExecutionTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginEndExecutionTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getEndExecutionTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginErrorStatus
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginErrorStatus"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.endedWithError());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginTotalResponseTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginTotalResponseTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getWallClockTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadUserCPUTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadUserCPUTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadUserCpuTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadUserCPUTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadSystemCPUTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadSystemCpuTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadTotalCPUTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadTotalCPUTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadTotalCpuTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadCount
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadCount"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadCount());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadBlockCount
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadBlockCount"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadBlockCount());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadBlockTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadBlockTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadBlockTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadWaitCount
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadWaitCount"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadWaitCount());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadWaitTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadWaitTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadWaitTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadGccCount
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadGccCount"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadGccCount());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginThreadGccTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginThreadGccTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getThreadGccTime());
			stmtInsertPluginMetric.execute();

			// write an instance of PluginProcessTotalCPUTime
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginProcessTotalCPUTime"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getProcessTotalCpuTime());
			stmtInsertPluginMetric.execute();
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginAllocatedMemoryBefore"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getAllocatedMemoryBefore());
			stmtInsertPluginMetric.execute();
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginAllocatedMemoryAfter"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getAllocatedMemoryAfter());
			stmtInsertPluginMetric.execute();
			
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginUsedMemoryBefore"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getUsedMemoryBefore());
			stmtInsertPluginMetric.execute();
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginUsedMemoryAfter"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getUsedMemoryAfter());
			stmtInsertPluginMetric.execute();
					
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginFreeMemoryBefore"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getFreeMemoryBefore());
			stmtInsertPluginMetric.execute();
	
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginFreeMemoryAfter"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getFreeMemoryAfter());
			stmtInsertPluginMetric.execute();
	
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginUnallocatedMemoryBefore"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryBefore());
			stmtInsertPluginMetric.execute();
			
			stmtInsertPluginMetric.setInt(2, getMetricID("PluginUnallocatedMemoryAfter"));
			stmtInsertPluginMetric.setString(3, "" + methodMetrics.getUnallocatedMemoryAfter());
			stmtInsertPluginMetric.execute();
		}
	}
}
