package de.olafhartig.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

public class RDFtoCDTListConverter implements StreamRDF
{
	protected final Set<Node> predicates;
	protected final StreamRDF outStream;

	/** to collect all triples that have one of the given predicates */
	protected List<Triple> startTriples = new ArrayList<>();
	/** to collect all nodes that have rdf:type rdf:List */
	protected Set<Node> listNodes = new HashSet<>();
	/** to collect all rdf:first triples */
	protected Map<Node,Node> firstMap = new HashMap<>();
	/** to collect all rdf:rest triples */
	protected Map<Node,Node> restMap = new HashMap<>();

	public RDFtoCDTListConverter( final Set<Node> predicates, final StreamRDF outStream ) {
		assert predicates != null;
		assert outStream != null;

		this.predicates= predicates;
		this.outStream = outStream;
	}

	public RDFtoCDTListConverter( final Node predicate, final StreamRDF outStream ) {
		this( Collections.singleton(predicate), outStream);
		assert predicate != null;
	}

	@Override
	public void base( final String base ) {
		outStream.base(base);
	}

	@Override
	public void prefix( final String prefix, final String iri ) {
		outStream.prefix(prefix, iri);
	}

	@Override
	public void start() {
		outStream.start();
	}

	@Override
	public void triple( final Triple t ) {
		if ( t.getPredicate().equals(RDF.first.asNode()) ) {
			final Node prev = firstMap.put( t.getSubject(), t.getObject() );
			if ( prev != null && ! prev.equals(t.getObject()) )
				throw new IllegalArgumentException("One list location (" + t.getSubject().toString() + ") has multiple first elements (" + prev.toString() + " and " + t.getObject().toString() + ").");
		}
		else if ( t.getPredicate().equals(RDF.rest.asNode()) ) {
			final Node prev = restMap.put( t.getSubject(), t.getObject() );
			if ( prev != null && ! prev.equals(t.getObject()) )
				throw new IllegalArgumentException("One list location (" + t.getSubject().toString() + ") has multiple rest elements (" + prev.toString() + " and " + t.getObject().toString() + ").");
		}
		else if ( t.getPredicate().equals(RDF.type.asNode()) && t.getObject().equals(RDF.List.asNode()) ) {
			listNodes.add( t.getSubject() );
		}
		else if ( predicates.contains(t.getPredicate()) ) {
			startTriples.add(t);
		}
		else {
			outStream.triple(t);
		}
	}

	/** Not implemented. */
	@Override
	public void quad( final Quad q ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void finish() {
		while ( startTriples.size() > 0 ) {
			final Triple t = startTriples.remove( startTriples.size()-1 );
			outStream.triple( rewriteListTriple(t) );
		}

		for ( final Node n : listNodes ) {
			final Triple t = Triple.create( n, RDF.type.asNode(), RDF.List.asNode() );
			outStream.triple(t);
		}
		listNodes.clear();

		for ( final Map.Entry<Node, Node> e : firstMap.entrySet() ) {
			final Triple t = Triple.create( e.getKey(), RDF.first.asNode(), e.getValue() );
			outStream.triple(t);
		}
		firstMap.clear();

		for ( final Map.Entry<Node, Node> e : restMap.entrySet() ) {
			final Triple t = Triple.create( e.getKey(), RDF.rest.asNode(), e.getValue() );
			outStream.triple(t);
		}
		restMap.clear();

		outStream.finish();
	}

	protected Triple rewriteListTriple( final Triple t ) {
		final Node cdtList = createCDTList(t);
		return Triple.create( t.getSubject(), t.getPredicate(), cdtList );
	}

	protected Node createCDTList( final Triple t ) {
		Node listPos = t.getObject();
		final List<CDTValue> cdtList = new ArrayList<>();
		while ( ! listPos.equals(RDF.nil.asNode()) ) {
			listNodes.remove(listPos);

			final Node listElmt = firstMap.remove(listPos);
			final Node nextListPos = restMap.remove(listPos);

			if ( listElmt == null )
				throw new IllegalArgumentException("The list element at position " + (cdtList.size()+1) + "(" + listPos.toString() + ") in the list of triple (" + t.toString() + ") has no rdf:first statement.");
			if ( nextListPos == null )
				throw new IllegalArgumentException("The list element at position " + (cdtList.size()+1) + "(" + listPos.toString() + ") in the list of triple (" + t.toString() + ") has no rdf:rest statement.");

			cdtList.add( CDTFactory.createValue(listElmt) );
			listPos = nextListPos;
		}

		return NodeFactory.createLiteralByValue(cdtList, CompositeDatatypeList.type);
	}

}
