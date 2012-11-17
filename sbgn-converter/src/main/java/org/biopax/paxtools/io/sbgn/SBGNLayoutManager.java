package org.biopax.paxtools.io.sbgn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ivis.layout.*;
import org.ivis.layout.util.*;
import org.ivis.layout.cose.CoSELayout;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;
import org.sbgn.bindings.Bbox;

/**
 * Class for applying layout by ChiLay component to Sbgn generated by paxtools.
 * @author: istemi Bahceci
 * */

public class SBGNLayoutManager
{
	
	private Layout layout;
	private VCompound root;
	
	// mapping between view and layout level 
	private HashMap <VNode, LNode> viewToLayout;
	private HashMap <LNode, VNode> layoutToView;
	private HashMap <Glyph,VNode>  glyphToVNode;
	private HashMap <String, Glyph> idToGLyph;
	
	private HashMap<String, Glyph> idToCompartmentGlyphs;
	
	/**
	 * Converts the given model to SBGN, and writes in the specified file.
	 *
	 * @param  Sbgn sbgn object to which layout will be applied
	 * @return Layout applied sbgn object
	 */
	public Sbgn createLayout(Sbgn sbgn)
	{
		viewToLayout = new HashMap();
		layoutToView = new HashMap();
		glyphToVNode = new HashMap();
		idToGLyph = new HashMap();
		idToCompartmentGlyphs = new HashMap();
		
		// Using Compound spring  embedder layout
		this.layout = new CoSELayout();
		
		LGraphManager graphMan = this.layout.getGraphManager(); 
		LGraph lRoot = graphMan.addRoot();
		this.root = new VCompound(new Glyph());
		lRoot.vGraphObject = this.root;
		
		// Create Vnodes for ChiLay layout component
		createVNodes(root, sbgn.getMap().getGlyph());
		
		
		
		for (VNode vNode: this.root.children) 
		{ 
			this.createNode(vNode, null, this.layout); 
		}
		
		for (VNode vNode: this.root.children) 
		{
			Glyph tmpGlyph = vNode.glyph;
			
			if(tmpGlyph.getCompartmentRef() != null)
			{
				Glyph containerCompartment = (Glyph)tmpGlyph.getCompartmentRef();
				idToCompartmentGlyphs.get(containerCompartment.getId()).getGlyph().add(tmpGlyph);
			}
				
		}
		
		
		// Create LEdges for ChiLay layout component
		createLedges(sbgn.getMap().getArc(), this.layout);
		

		
		// Apply layout
		this.layout.runLayout();
		
		for (VNode vNode: this.root.children) 
		{ 
			updateCompoundBounds(vNode.glyph, vNode.glyph.getGlyph()); 
			/*LNode tmpLNode = viewToLayout.get(vNode);
			
			tmpLNode.setWidth(vNode.glyph.getBbox().getW());
			tmpLNode.setHeight(vNode.glyph.getBbox().getH());*/
		}
		

		for (Glyph compGlyph: idToCompartmentGlyphs.values()) 
		{
			compGlyph.getGlyph().clear();
		}
		
		/*GraphMLWriter writer = new GraphMLWriter("output.graphml");
		writer.saveGraph(this.layout.getGraphManager());*/
		
		return sbgn;
	}
	
	void printAllMap(Glyph parent, int nestingLevel)
	{
		boolean  isContainerGlyph = true;
		
		for(int i = 0; i < nestingLevel; i++)
			System.out.print(" ");
		if(isContainerGlyph)
		{
			System.out.println(parent.getId());
			nestingLevel++;
			isContainerGlyph = false;
		}

		for(Glyph glyph: parent.getGlyph())
		{
			for(int i = 0; i < nestingLevel; i++)
				System.out.print(" ");
			
			
			printAllMap(glyph,nestingLevel);
				
		}
	}
	
	public void updateCompoundBounds(Glyph parent,List<Glyph> childGlyphs)
	{		
		float PAD = (float) 2.0;
		float minX = Float.MAX_VALUE; float minY = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE; float maxY = Float.MIN_VALUE;
		
		for (Glyph tmpGlyph:childGlyphs) 
		{
			if(tmpGlyph.getClazz() != "unit of information" && tmpGlyph.getClazz() != "state variable" )
			{
				if(tmpGlyph.getGlyph().size() > 0)
					updateCompoundBounds(tmpGlyph, tmpGlyph.getGlyph());
				
	            float w = tmpGlyph.getBbox().getW();
				float h = tmpGlyph.getBbox().getH();
				
	            // Verify MIN and MAX x/y again:
	            minX = Math.min(minX, (tmpGlyph.getBbox().getX()));
	            minY = Math.min(minY, (tmpGlyph.getBbox().getY()));
	            maxX = Math.max(maxX, (tmpGlyph.getBbox().getX())+w);
	            maxY = Math.max(maxY, (tmpGlyph.getBbox().getY())+h);
	            
	            if (minX == Float.MAX_VALUE) minX = 0;
	            if (minY == Float.MAX_VALUE) minY = 0;
	            if (maxX == Float.MIN_VALUE) maxX = 0;
	            if (maxY == Float.MIN_VALUE) maxY = 0;
	            
	            parent.getBbox().setX(minX - PAD);
	            parent.getBbox().setY(minY - PAD);
	            parent.getBbox().setW(maxX -  parent.getBbox().getX() + PAD);
	            parent.getBbox().setH(maxY -  parent.getBbox().getY() + PAD);
			}
			

		}
	}
	
	public boolean isChildless(Glyph tmpGlyph)
	{
		boolean checker = true;
		for(Glyph glyph: tmpGlyph.getGlyph() )
		{
			if (glyph.getClazz() !=  "state variable" && glyph.getClazz() !=  "unit of information"  ) 
			{
				checker = false;
				break;
			}
		}
		return checker;
	}
	
	/**
	 * Recursively creates VNodes from Glyphs of Sbgn. 
	 * 
	 * @param parent Parent of the glyphs that are passed as second arguement.
	 * @param glyphs Glyphs that are child of parent which is passed as first arguement.
	 * 
	 * */
	public void createVNodes(VCompound parent,List<Glyph> glyphs)
	{
		for(Glyph glyph: glyphs )
		{	
			if (glyph.getClazz() !=  "state variable" && glyph.getClazz() !=  "unit of information"  ) 
			{
				
				if(glyph.getClazz() == "compartment")
				{
					idToCompartmentGlyphs.put(glyph.getId(), glyph);
				}
				
				if(!this.isChildless(glyph))
				{
					VCompound v = new VCompound(glyph);

					idToGLyph.put(glyph.getId(), glyph);
					glyphToVNode.put(glyph, v);
		
					parent.children.add(v);
					
					createVNodes(v, glyph.getGlyph());
				}
				
				else
				{
					VNode v = new VNode(glyph);

					idToGLyph.put(glyph.getId(), glyph);
					glyphToVNode.put(glyph, v);
					
					parent.children.add(v);
				}
				
			}
		}
	}
	
	/**
	 *  Clears the port indicator of ID of given port, and retrieves corresponding LNode for this ID. Briefly, finds the LNode which is parent of the VNode that 
	 *  corresponds to the glyph object which is container of this port.
	 * 
	 * @param p port object that by its ID  the corresponding LNode will be retrieved.
	 * @return  LNode which is parent of the VNode that corresponds to the glyph object which is container of this port p.
	 * 
	 * */
	public LNode getLNodeByPort(Port p)
	{
		
		String tmpStr = p.getId();
		
		// Clear port indicator. 
		// ( CONVENTION: It is assumed that, ports are declared by prepending '.' at the end of the ID of container glyphs and also appending proper name e.g. "INPUT", ".1")
		if (tmpStr.lastIndexOf(".") > -1) 
		{
			tmpStr = tmpStr.substring(0, tmpStr.lastIndexOf("."));
		}
		
		
		// Access hashmaps to first get corresponding container glyph and corresponding VNode
		Glyph tmpG = idToGLyph.get(tmpStr);
		VNode tmpV = glyphToVNode.get(tmpG);

		// Return corresponding LNode
		return viewToLayout.get(tmpV);
	}
	
	/**
	 * Creates LNodes from Arcs of Sbgn and adds it to the passed layout object. 
	 * 
	 * @param parent arcs list of arc objects from which the LEdges will be constructed for ChiLay Layout component. 
	 * @param layout layout object to which the created LEdges added.
	 * 
	 * */
	public void createLedges(List<Arc> arcs, Layout layout)
	{
		for(Arc arc: arcs )
		{
			LEdge lEdge = layout.newEdge(null); 
			LNode sourceLNode;
			LNode targetLNode;
			
			// If source is port, first clear port indicators else retrieve it from hashmaps
			if (arc.getSource() instanceof Port ) 
			{
				sourceLNode = getLNodeByPort((Port)arc.getSource());
				arc.setSource(layoutToView.get(sourceLNode).glyph);
			}
			else
			{
				sourceLNode = this.viewToLayout.get(glyphToVNode.get(arc.getSource()));
			}
			
			
			// If target is port, first clear port indicators else retrieve it from hashmaps
			if (arc.getTarget() instanceof Port) 
			{
				targetLNode = getLNodeByPort((Port)arc.getTarget());
				arc.setTarget(layoutToView.get(targetLNode).glyph);
			}
			else
			{
				targetLNode = this.viewToLayout.get(glyphToVNode.get(arc.getTarget()));
			}
			
	
			// Add edge to the layout
			this.layout.getGraphManager().add(lEdge, sourceLNode, targetLNode);
		}
	}
	
	
	/**
	 * Helper function for creating LNode objects from VNode objects and adds them to the given layout.
	 * 
	 * @param vNode  VNode object from which a corresponding LNode object will be created.
	 * @param parent parent of vNode, if not null vNode will be added to layout as child node.
	 * @param layout layout object to which the created LNodes added.
	 * */
	
	public void createNode(VNode vNode,VNode parent,Layout layout)
	{
		LNode lNode = layout.newNode(vNode); 
		lNode.setWidth(vNode.glyph.getBbox().getW());
		lNode.setHeight(vNode.glyph.getBbox().getH());
		lNode.setLocation(vNode.glyph.getBbox().getX(), vNode.glyph.getBbox().getY());
		
		LGraph rootLGraph = layout.getGraphManager().getRoot();
		
		this.viewToLayout.put(vNode, lNode); 
		this.layoutToView.put(lNode, vNode);
		
		// if the vNode has a parent, add the lNode as a child of the parent l-node. 
		// otherwise, add the node to the root graph. 
		if (parent != null) 
		{ 
			LNode parentLNode = this.viewToLayout.get(parent); 
			parentLNode.getChild().add(lNode); 
			
		} 
		else 
		{ 
			rootLGraph.add(lNode); 
		}		
		
		
		if (vNode instanceof VCompound) 
		{ 
			VCompound vCompound = (VCompound) vNode; 
			// add new LGraph to the graph manager for the compound node 
			layout.getGraphManager().add(layout.newGraph(null), lNode); 
			// for each VNode in the node set create an LNode 
			for (VNode vNode2: vCompound.getChildren()) 
			{ 
				this.createNode(vNode2, vCompound, layout); 
				
			} 
				
			lNode.updateBounds();
		}
	}
}