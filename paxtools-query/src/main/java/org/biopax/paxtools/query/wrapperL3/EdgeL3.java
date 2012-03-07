package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.query.model.AbstractEdge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

/**
 * @author Ozgun Babur
 */
public class EdgeL3 extends AbstractEdge
{
	boolean transcription;

	public EdgeL3(Node source, Node target, Graph graph)
	{
		super(source, target, graph);
		transcription = false;
	}

	public boolean isTranscription()
	{
		return transcription;
	}

	public void setTranscription(boolean transcription)
	{
		this.transcription = transcription;
	}
}
