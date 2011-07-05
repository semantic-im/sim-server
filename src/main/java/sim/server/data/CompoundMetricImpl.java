package sim.server.data;

import java.util.HashSet;
import java.util.Set;

import org.ontoware.rdf2go.model.node.URI;
import sim.server.RdfDatabase;

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
	
	private long creationTime;
	
	public CompoundMetricImpl(URI type, URI aggregationFunction) {
		this.type = type;
		this.aggregationFunction = aggregationFunction;
		this.constituentMetrics = new HashSet<Metric>();
		this.value = 0;
		this.creationTime = System.currentTimeMillis();
	}
	
	
	@Override
	public long getCreationTime() {
		return creationTime;
	}
	
	@Override
	public URI getAggregationFunction() {
		return aggregationFunction;
	}
	
	@Override
	public URI getType() {
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
	
	public void accept(RdfDatabase visitor) {
		visitor.visit(this);
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


	@Override
	public String getMethodId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setMethodId(String methodId) {
		// TODO Auto-generated method stub
	}

}
