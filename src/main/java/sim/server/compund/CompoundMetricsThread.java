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
package sim.server.compund;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.server.Main;
import sim.server.RdfDatabase;
import sim.server.data.CompoundMetric;

/**
 * This is the thread writing the compound metrics to rdf store. It is running
 * periodically.
 * 
 * @author ioantoma
 * 
 */
public class CompoundMetricsThread implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(CompoundMetricsThread.class);

	private static int ONE_MINUTE = 60000;
	private static int FIVE_MINUTES = 300000;
	private static int ONE_HOUR = 3600000;

	private CompundMetricsGenerator cmg = null;

	private Set<String> plugins = new HashSet<String>();
	private Set<String> workflows = new HashSet<String>();;

	/*
	 * Initializae the compound metrics thread
	 */
	public CompoundMetricsThread() {
		RdfDatabase rdfDatabase = new RdfDatabase();

		cmg = new CompundMetricsGenerator(rdfDatabase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		long endDateTime = System.currentTimeMillis();
//		long startDateTime = endDateTime - ONE_HOUR;
//		long startDateTime = endDateTime - ONE_MINUTE;
		long startDateTime = endDateTime - FIVE_MINUTES;
	
		Set<CompoundMetric> cm = new HashSet<CompoundMetric>();
		
		cmg.getRdfDatabase().open();
		plugins = cmg.getRegisteredPlugins();
		workflows = cmg.getRegisteredWorkflowIds();

		cm.add(cmg.generateQueriesPerTimeInterval(startDateTime, endDateTime));
		cm.add(cmg.generateQueriesSuccessRatePerTimeInterval(startDateTime,
				endDateTime));
		cm.add(cmg.generateQueriesFailureRatePerTimeInterval(startDateTime,
				endDateTime));

		for (String workflowID : workflows) {
			cm.add(cmg.generateWorkflowsPerTimeInterval(startDateTime, endDateTime,
					workflowID));
//			cm.add(cmg.generateWorkflowAvgDurationPerTimeInterval(startDateTime,
//					endDateTime, workflowID));
		}

		
		for (String pluginName : plugins) {
			cm.add(cmg.generatePlatformPluginAvgExecutionTimePerTimeInterval(
					startDateTime, endDateTime, pluginName));
			cm.add(cmg.generatePlatformPluginAvgThreadsStartedPerTimeInterval(
					startDateTime, endDateTime, pluginName));
			cm.add(cmg.generatePlatformPluginTotalExecutionTimePerTimeInterval(
					startDateTime, endDateTime, pluginName));
			cm.add(cmg.generatePlatformPluginTotalThreadsStartedPerTimeInterval(
					startDateTime, endDateTime, pluginName));
		}

		
		for (String workflowID : workflows) {
			for (String pluginName : plugins) {
				cm.add(cmg.generateWorkflowPluginAvgExecutionTimePerTimeInterval(
						startDateTime, endDateTime, pluginName, workflowID));
				cm.add(cmg.generateWorkflowPluginAvgThreadsStartedPerTimeInterval(
						startDateTime, endDateTime, pluginName, workflowID));
				cm.add(cmg.generateWorkflowPluginTotalExecutionTimePerTimeInterval(
						startDateTime, endDateTime, pluginName, workflowID));
			}
		}
		

		for(CompoundMetric m:cm)
			cmg.getRdfDatabase().visit(m);

		cm.removeAll(cm);
		cmg.getRdfDatabase().close();	
				
	}

}
