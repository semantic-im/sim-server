package sim.server.compund.data;

import java.util.HashSet;
import java.util.Set;

import org.ontoware.rdf2go.model.node.URI;

import sim.data.MetricsVisitor;

/**
 * Implementation for {@link CompundMetric}.
 * 
 * @author ioantoma
 * 
 */

public class CompoundMetricImpl implements CompoundMetric{

	private static final long serialVersionUID = 2951565471226614739L;

	private double value;
	private URI id;
	private URI type;
	private URI aggregationFunction;
	private Set<Metric> constituentMetrics;
	
	public CompoundMetricImpl(URI type, URI aggregationFunction) {
		this.type = type;
		this.aggregationFunction = aggregationFunction;
		this.constituentMetrics = new HashSet<Metric>();
		this.value = 0;
	}
	
	
	@Override
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public URI getAggregationFunction() {
		return aggregationFunction;
	}
	
	@Override
	public URI getType() {
		// TODO Auto-generated method stub
		return type;
	} 


	@Override
	public Set<Metric> getConstituentMetrics() {
		return constituentMetrics;
	}


	@Override
	public void addConstituentMetric(Metric metric) {
		constituentMetrics.add(metric);
	}


	@Override
	public double getValue() {
		return value;
	}	
	
	@Override
	public void accept(MetricsVisitor visitor) {
		// TODO Auto-generated method stub	
	}


	@Override
	public void setValue(double value) {
		this.value = value;
	}


	@Override
	public URI getId() {
		return id;
	}


	@Override
	public void setId(URI id) {
		this.id= id;
	}
		
}
