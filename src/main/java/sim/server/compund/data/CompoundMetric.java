package sim.server.compund.data;

import org.ontoware.rdf2go.model.node.URI;

import sim.data.Metrics;

import java.util.Set;

/**
 * A compound metric is a metric that is composed from other metrics (e.g. atomic metrics, 
 * compound metrics, etc.). A compound metric is defined by an aggregation function and a 
 * set of constituents metrics. 
 *  
 * @author ioantoma
 * 
 */

public interface CompoundMetric extends Metric{
	
	/**
	 * @return returns the ontological type of the metric
	 */
	public URI getType();

	/**
	 * @return returns the aggregation function of the compound metric
	 */
	public URI getAggregationFunction();

	/**
	 * @return returns the aggregation function of the compound metric
	 */
	public Set<Metric> getConstituentMetrics();
	
	/**
	 * add a metric to a compound metric
	 * */
	public void addConstituentMetric(Metric metric);
	

}
