package cu.rst.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;


import org.apache.log4j.Logger;
import org.jgraph.JGraph;
import org.jgraph.graph.Edge;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DirectedPseudograph;

import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import cu.rst.graph.Graph;
import cu.rst.alg.*;
import cu.rst.graph.FeedbackHistoryGraphEdge;
import cu.rst.graph.TG;
import cu.rst.graph.TrustEdge;
import cu.rst.graph.RG;
import cu.rst.graph.ReputationEdge;
import cu.rst.graph.ReputationEdgeFactory;
import cu.rst.graph.Agent;
import cu.rst.graph.FHG;
import cu.rst.graph.Feedback;
import cu.rst.graph.TestbedEdge;

public class Util
{
	static Logger logger = Logger.getLogger(Util.class.getName());
	
	public static void assertNotNull(Object o) throws NullPointerException
	{
		if(o == null) throw new NullPointerException();
	}
	
	public static void assertFileExists(Object o) throws FileNotFoundException
	{
		if(o instanceof String)
		{
			File f = new File ((String)o);
			if(!f.exists())  throw new FileNotFoundException();
		}else
		{
			throw new FileNotFoundException("Input file name is not a String");
		}
	}
	
	public static boolean isPresent(ArrayList objects, Object o)
	{
		assertNotNull(objects);
		assertNotNull(o);
		
		for(Object o2 : objects)
		{
			if(o2.equals(o)) return true;
		}
		return false;
	}
		
	  
	public static double round(double Rval, int Rpl) 
	{  
		double p = (double)Math.pow(10,Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (double)tmp/p;
	}
	
	public static FHG generateFHG(String arffFileName) throws Exception
	{
		FHG fhg = new FHG();
	
		DataSource source;
		try
		{
			source = new DataSource(arffFileName);
			Instances instances = source.getDataSet();
			logger.debug("Number of instances in arff file is " + instances.numInstances());
			
			Enumeration enu = instances.enumerateInstances();
			//get all the feedback lines
			
			while(enu.hasMoreElements())
			{
				Instance temp = (Instance)enu.nextElement();
				logger.info("Parsing " + temp);
				String[] feedbackInstance = new String[3];
				//go through each feedback line
				
				if(temp.numValues()!=3) throw new Exception("Feedback line does not have 3 elements. This is illegal.");
				
				for(int i=0;i<temp.numValues();i++)
				{
					//number of values == 3
					feedbackInstance[i] = temp.stringValue(i);					
				}
				Agent assessor = new Agent(new Integer(feedbackInstance[0]));
				Agent assessee = new Agent(new Integer(feedbackInstance[1]));
				Double value = new Double(feedbackInstance[2]);
				
				Feedback f = new Feedback(assessor, assessee, value);
				fhg.addFeedback(f);
				logger.info("Added " + f );
				
			}

		} catch (Exception e)
		{
			logger.info("Error parsing arff file '" + arffFileName +"'.");
			logger.info(e.getStackTrace());
			throw e;
		}
		
		return fhg;
				
	}
	
	public static RG generateRG(String arffFileName) throws Exception
	{
		RG repGraph = new RG(new ReputationEdgeFactory());
		
		Util.assertNotNull(arffFileName);
		Util.assertFileExists(arffFileName);
		
		DataSource source;
		try
		{
			source = new DataSource(arffFileName);
			Instances instances = source.getDataSet();
			logger.debug("Number of instances in arff file is " + instances.numInstances());
			
			Enumeration enu = instances.enumerateInstances();
			//get all the feedback lines
			
			while(enu.hasMoreElements())
			{
				Instance temp = (Instance)enu.nextElement();
				System.out.println("Parsing " + temp);
				String[] repInstance = new String[3];
				//go through each feedback line
				
				if(temp.numValues()!=3) throw new Exception("Reputation line does not have 3 elements. This is illegal.");
				
				
				
//				for(int i=0;i<temp.numValues();i++)
//				{
//					//number of values == 3
//					if(i<2) repInstance[i] = String.valueOf(temp.value(i));
//					else if(i==2)  repInstance[i] = String.valueOf(temp.value(i));			
//				}
				
				String srcId = temp.stringValue(0);
				String sinkId = temp.stringValue(1);
				String rep = temp.stringValue(2);
				
				Agent src = new Agent(Integer.valueOf(srcId));
				Agent sink = new Agent(Integer.valueOf(sinkId));
				double reputation = Double.valueOf(rep);
				
				if(!repGraph.containsVertex(src))
				{
					repGraph.addVertex(src);
				}
				
				if(!repGraph.containsVertex(sink))
				{
					repGraph.addVertex(sink);
				}
				
				repGraph.addEdge(src, sink, (double)reputation);
				ReputationEdge repEdge = (ReputationEdge) repGraph.getEdge(src, sink);
				repEdge.setReputation(reputation);
//				System.out.println(repGraph);
			}
			
			return repGraph;

		} catch (Exception e)
		{
			logger.error("Error parsing arff file '" + arffFileName +"'.");
			logger.error(e);
			throw new Exception("Error parsing arff file.", e);
		}
			
	}
	
	public static void writeToArff(FHG fhg, String fileName) throws Exception
	{
		
		//header
		FastVector attributes = new FastVector();
		Attribute assessorID = new Attribute("assessorID");
		Attribute assesseeID = new Attribute("assesseeID");
		Attribute feedbackValue = new Attribute("feedbackValue");
		attributes.addElement(assessorID);
		attributes.addElement(assesseeID);
		attributes.addElement(feedbackValue);
		
		Instances data = new Instances("mydataset", attributes, 0);
		for(Feedback f : fhg.m_feedbacks)
		{
			//instances
			double[] values = new double[data.numAttributes()];
			values[0] = f.getAssesor().id;
			values[1] = f.getAssesee().id;
			values[2] = f.value;
			data.add(new Instance(1.0, values));
		}
		System.out.println(data);
//		FileWriter writer = new FileWriter(fileName);
//		writer.write(data.toString());
//		writer.close();
	}
	
	public static void displayJGraph(Graph g) throws Exception
	{
		mxGraph m_graph;
		mxGraphComponent m_graphComponent;
		HashMap<String, mxCell> jGraphCells;
		mxOrganicLayout  layout;
		
		m_graph = new mxGraph();
		m_graph.setMinimumGraphSize(new mxRectangle(0,0,400,400));
		layout = new mxOrganicLayout (m_graph);
		m_graph.setCellsDeletable(false);
		m_graph.setCellsDisconnectable(false);
		m_graph.setCellsEditable(false);
		m_graph.setAutoOrigin(true);
		m_graph.setAutoSizeCells(true);
		m_graph.setCellsLocked(false);
		m_graph.setAllowDanglingEdges(false);
		m_graph.setCellsCloneable(false);
		m_graphComponent = new mxGraphComponent(m_graph);
		m_graphComponent.setAutoExtend(true);
		m_graphComponent.setExportEnabled(true);
		m_graphComponent.setCenterZoom(true);

		
		//key = agent id, value = ref to the cell in jgraph. a cell can be vertex or edge
		jGraphCells = new HashMap<String, mxCell>();
		
		Object parent = m_graph.getDefaultParent();
		m_graph.getModel().beginUpdate();
		
		try
		{
			for(TestbedEdge e : (Set<TestbedEdge>)g.edgeSet())
			{
				String srcIdString = new Integer(((Agent)e.src).id).toString();
				String sinkIdString = new Integer(((Agent)e.sink).id).toString();
				
				Object srcj = jGraphCells.get(srcIdString);
				
				if(srcj == null)
				{
					//if not present in our hashmap, then the cell doesnt exist in the jgraph
					//so add it and store the reference of the cell
					logger.info("Adding src agent " + srcIdString + " to the view.");
					mxCell o = (mxCell) m_graph.insertVertex(parent, srcIdString, srcIdString, 20, 20, 20, 20, "ROUNDED");
					jGraphCells.put(srcIdString, o);
				}
				
				Object sinkj = jGraphCells.get(sinkIdString);
				
				if(sinkj == null)
				{
					//if not present in our hashmap, then the cell doesnt exist in the jgraph
					//so add it and store the reference of the cell
					logger.info("Adding sink agent " + sinkIdString + " to the view.");
					mxCell o = (mxCell) m_graph.insertVertex(parent, sinkIdString, sinkIdString, 20, 20, 20, 20, "ROUNDED");
					jGraphCells.put(sinkIdString, o);
				}
				
				String edgeKey = srcIdString + "-"+ sinkIdString;
				
				if(!jGraphCells.containsKey(edgeKey))
				{
					//guaranteed jgraphVertices.get(srcIdString) and jgraphVertices.get(sinkIdString) will not be null
					//and that the cells are already present in jraph 
					mxCell o = (mxCell) m_graph.insertEdge(parent, null, e.toString(), jGraphCells.get(srcIdString), jGraphCells.get(sinkIdString));
					jGraphCells.put(edgeKey, o);
				}
				else
				{
					//edge exists in graph. remove it and add it again with the updated edge info
					mxCell cell1 = jGraphCells.get(edgeKey);
					m_graph.removeCells(new Object[]{cell1});
					mxCell cell2 = (mxCell) m_graph.insertEdge(parent, null, e.toString(), jGraphCells.get(srcIdString), jGraphCells.get(sinkIdString));
					jGraphCells.remove(edgeKey);
					jGraphCells.put(edgeKey, cell2);
					
				}
			}
		}
		catch(Exception e)
		{
			logger.error(e.toString());
		}
		finally
		{
			m_graph.getModel().endUpdate();
			m_graph.setCellsDeletable(false);
			layout.execute(parent);
			m_graph = layout.getGraph();
		}

		JFrame frame = new JFrame();
		frame.getContentPane().add(m_graphComponent);
		frame.setVisible(true);
		

	}
	
	public static void display(List<Graph> graphs) throws Exception
	{
		long time = (new Date()).getTime();
		File f = new File("output/" + time);
		f.mkdirs();
		for(Graph g : graphs)
		{
			write(g, "output/" + time + "/" + g.getName(), "/usr/local/bin/dot");
		}
	}
	
	public static void display(Graph g) throws Exception
	{
		long time = (new Date()).getTime();
		File f = new File("output/" + time);
		f.mkdirs();
		write(g, "output/" + time + "/" + g.getName(), "/usr/local/bin/dot");
	}
	
	public static void write(Graph g, String fn, String graphVizLocation) throws Exception
	{
		Util.assertNotNull(g);
		FileWriter fw = new FileWriter(fn + ".dot");
		if(g instanceof FHG)
		{
			FHG fhg = ((FHG) g);
			fw.write("digraph G {");
			
			for(Object o : fhg.edgeSet())
			{
				FeedbackHistoryGraphEdge e = (FeedbackHistoryGraphEdge)o;
				fw.write(((Agent)e.src).id + " -> ");
				fw.write(((Agent)e.sink).id + " [label=\"{");
				
				for(int i=0; i<e.feedbacks.size();i++)
				{
					Feedback f = e.feedbacks.get(i);
					fw.write(new Double(f.value).toString());
					if(i != e.feedbacks.size()-1) fw.write(", ");	
				}
				
				fw.write("}\"];");
				
			}
			fw.write("}");
			fw.close();
			
			//invoke Graphviz to output a jpeg file of the graph.
			Runtime.getRuntime().exec(graphVizLocation + " -Tjpg " + fn + ".dot -o " + fn + ".jpg");
			
		}
		else if(g instanceof RG)
		{
			RG rg = (RG)g;
			fw.write("digraph G {");
			
			for(Object o : rg.edgeSet())
			{
				ReputationEdge e = (ReputationEdge)o;
				fw.write(((Agent)e.src).id + " -> ");
				fw.write(((Agent)e.sink).id + " [label=\"" + roundTwoDecimals(e.getReputation()) + "\"];");
			}
			fw.write("}");
			fw.close();
			
			//invoke Graphviz to output a jpeg file of the graph.
			Runtime.getRuntime().exec(graphVizLocation + " -Tjpg " + fn + ".dot -o " + fn + ".jpg");

			
		}
		else if(g instanceof TG)
		{
			TG tg = (TG)g;
			fw.write("digraph G {");
			
			for(Object o : tg.edgeSet())
			{
				TrustEdge e = (TrustEdge) o;
				fw.write(((Agent)e.src).id + " -> ");
				fw.write(((Agent)e.sink).id +  " [label=\"" + "\"];");
			}
			fw.write("}");
			fw.close();
			
			//invoke Graphviz to output a jpeg file of the graph.
			Runtime.getRuntime().exec(graphVizLocation + " -Tjpg " + fn + ".dot -o " + fn + ".jpg");
		}
		
		
	}
	
	public static Graph peerTrust(Graph g) throws Exception
	{
		assertNotNull(g);
		PeerTrust pt = new PeerTrust();
		return pt.execute(g);
	}
	
	public static Graph eigenTrust(Graph g) throws Exception
	{
		assertNotNull(g);
		EigenTrustv1 et = new EigenTrustv1();
		return et.execute(g);
	}
	
	public static double spearman(Graph g1, Graph g2) throws Exception
	{
		assertNotNull(g1);
		assertNotNull(g2);
		Spearman sp = new Spearman();
		return sp.execute(g1, g2);
	}
	
	public static Graph normalize(Graph g) throws Exception
	{
		assertNotNull(g);
		Normalizer nm = new Normalizer();
		return nm.execute(g);
	}
	
	public static Graph discretize(Graph g) throws Exception
	{
		assertNotNull(g);
		Discretizer dsc = new Discretizer();
		return dsc.execute(g);
	}
	
	public static Graph appleseed(Graph g) throws Exception
	{
		assertNotNull(g);
		Appleseed as = new Appleseed();
		return as.execute(g);
	}
	
	public static Graph eigenTrustPart1(Graph g) throws Exception
	{
		assertNotNull(g);
		EigenTrustPart1 et1 = new EigenTrustPart1();
		return et1.execute(g);
	}
	
	
	
	public static double roundTwoDecimals(double d) 
	{
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        try
        {
        	return Double.valueOf(twoDForm.format(d));
        }
        catch(Exception e)
        {
        	//Likely a NaN, return -1;
        	return -1;
        }
       
	}
	
	
	
}
