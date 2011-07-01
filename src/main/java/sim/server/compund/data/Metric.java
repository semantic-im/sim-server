package sim.server.compund.data;

import org.ontoware.rdf2go.model.node.URI;

import sim.data.Metrics;

/**
 * A generic metric 
 *  
 * @author ioantoma
 * 
 */

public interface Metric extends Metrics{
	
	/**
	 * @return returns the ontological type of the metric
	 */
	public URI getType();
	
	/**
	 * @return returns the value of the metric
	 */
	public double getValue();
	
	/**
	 * @return sets the value of the metric
	 */
	public void setValue(double value);
	
	/**
	 * @return returns the id of the metric
	 */
	public URI getId();
	
	/**
	 * @return sets the id of the metric
	 */
	public void setId(URI uri);
	

}
