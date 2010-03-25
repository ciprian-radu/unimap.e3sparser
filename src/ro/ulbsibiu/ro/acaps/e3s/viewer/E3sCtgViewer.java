package ro.ulbsibiu.ro.acaps.e3s.viewer;
import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.VertexView;
import org.jgrapht.ext.JGraphModelAdapter;

import ro.ulbsibiu.acaps.ctg.CommunicationTaskGraph;
import ro.ulbsibiu.acaps.e3s.ctg.E3sBenchmarkData;
import ro.ulbsibiu.acaps.e3s.parser.E3sTgffFileParser;

import com.jgraph.components.labels.MultiLineVertexRenderer;

import de.susebox.jtopas.TokenizerException;

/**
 * @author Ciprian Radu
 *
 */
public class E3sCtgViewer extends JApplet {

	private E3sJGraphModelAdapter jgAdapter;
	
	private CommunicationTaskGraph ctg;
	
	public E3sCtgViewer(CommunicationTaskGraph ctg) {
		this.ctg = ctg;
	}
	
	private JGraphLayoutPanel initialize () {
        // create a visualization using JGraph, via an adapter
        jgAdapter = new E3sJGraphModelAdapter(ctg);
        JGraph jgraph = new JGraph(jgAdapter);
		
		// Overrides the global vertex renderer
		VertexView.renderer = new MultiLineVertexRenderer();
		JGraphLayoutPanel layoutPanel = new JGraphLayoutPanel(jgraph);
		
		return layoutPanel;
	}
	
	/**
	 * Initializes the applet by showing something interesting.
	 */
	public void start() {
		JGraphLayoutPanel layoutPanel = initialize();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(layoutPanel, BorderLayout.CENTER);
	}

	/**
	 * Starts the demo as an application.
	 * @throws TokenizerException
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, TokenizerException {
    	E3sTgffFileParser e3sFileParser = new E3sTgffFileParser(args[0]);
		e3sFileParser.parseTgffFile();
		List<E3sBenchmarkData> ctgs = e3sFileParser.getE3sCtgs();
		for (int i = ctgs.size() - 1; i >= 0; i--) {
			E3sCtgViewer app = new E3sCtgViewer(ctgs.get(i).getCtg());
			
			// Switch off D3D because of Sun XOR painting bug
			// See http://www.jgraph.com/forum/viewtopic.php?t=4066
			System.setProperty("sun.java2d.d3d", "false");
			JFrame frame = new JFrame("E3S CTG " + i);
			JGraphLayoutPanel layoutPanel = app.initialize();
			frame.getContentPane().add(layoutPanel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setSize(800, 600);
			frame.setVisible(true);
		}
	}

}
