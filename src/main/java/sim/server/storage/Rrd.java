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
package sim.server.storage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;


/**
 * @author ioantoma
 * 
 */
public class Rrd implements Storage{

	private String location;
	private String dataSource;
	private RrdDb rrdDB;

	public Rrd(String location){
		this.location = location;
	}

	/*
	 * For each metric a RRDDatabase is created with the name of the metric
	 */
	public void createMetricRRDDatabase(String dataSource, double xfilesFactor){
		this.dataSource = dataSource;
		RrdDef rrdDef = new RrdDef(location);
		rrdDef.setStartTime(System.currentTimeMillis());
		rrdDef.addDatasource(dataSource, DsType.COUNTER, 600, 0, Double.NaN);

		rrdDef.addArchive(ConsolFun.AVERAGE, xfilesFactor, 1, 600);
		rrdDef.addArchive(ConsolFun.AVERAGE, xfilesFactor, 6, 700);	
		
		System.out.println(rrdDef.dump());
		System.out.println("Estimated file size: " + rrdDef.getEstimatedSize());
		 
		try {
			rrdDB = new RrdDb(rrdDef);	
			rrdDB.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String read(String metric) throws IOException{
		if(rrdDB.isClosed())
			rrdDB = new RrdDb(location);		
		FetchRequest fetchRequest = rrdDB.createFetchRequest(ConsolFun.AVERAGE, rrdDB.getLastUpdateTime(), rrdDB.getLastUpdateTime());
		FetchData fetchData = fetchRequest.fetchData();
		String rez = fetchData.dump();
		return rez;
	}

	public String read(String metric, long startTime, long endTime) throws IOException{
		if(rrdDB.isClosed())
			rrdDB = new RrdDb(location);
		FetchRequest fetchRequest = rrdDB.createFetchRequest(ConsolFun.AVERAGE, startTime, endTime);
		FetchData fetchData = fetchRequest.fetchData();
		String rez = fetchData.dump();		
		return rez;
	}

	public long write(String metric, long data) throws IOException {
		if(rrdDB.isClosed())
			rrdDB = new RrdDb(location);			
		Sample sample = rrdDB.createSample();
		
		long time = System.currentTimeMillis();
		if (System.currentTimeMillis()<=rrdDB.getLastUpdateTime())
			time = rrdDB.getLastUpdateTime()+1;
		
		sample.setTime(time);
		sample.setValue(dataSource, data);
		sample.update();
		rrdDB.close();
		return time;
	}

	public void createGraphRrd(long startTime, long endTime) throws IOException {
		RrdGraphDef graphDef = new RrdGraphDef();
		graphDef.setTimeSpan(startTime, endTime);
		graphDef.datasource("metrics", location, dataSource, ConsolFun.AVERAGE);
		graphDef.line("metrics", new Color(0xFF, 0, 0), null, 2);
		RrdGraph graph = new RrdGraph(graphDef);
		BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
		graph.render(bi.getGraphics());
	}	
}