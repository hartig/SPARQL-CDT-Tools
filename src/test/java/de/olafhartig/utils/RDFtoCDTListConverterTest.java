package de.olafhartig.utils;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

public class RDFtoCDTListConverterTest
{
	@Test
	public void testOneList() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList (1 2 3) . "
				+ "ex:b ex:other ex:c . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[1, 2, 3]'^^cdt:List . "
				+ "ex:b ex:other ex:c . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}

	@Test
	public void testTwoListsDifferentSubjects() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList (1 2 3) . "
				+ "ex:b ex:hasList (4 5 6) . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[1, 2, 3]'^^cdt:List . "
				+ "ex:b ex:hasList '[4, 5, 6]'^^cdt:List . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}

	@Test
	public void testTwoListsSameSubject() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList (1 2 3) . "
				+ "ex:a ex:hasList (4 5 6) . "
				+ "ex:b ex:other ex:c . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[1, 2, 3]'^^cdt:List . "
				+ "ex:a ex:hasList '[4, 5, 6]'^^cdt:List . "
				+ "ex:b ex:other ex:c . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}

	@Test
	public void testTwoListsIgnoreOne() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList (1 2 3) . "
				+ "ex:b ex:hasIrrelevantList (4 5 6) . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[1, 2, 3]'^^cdt:List . "
				+ "ex:b ex:hasIrrelevantList (4 5 6) . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}

	@Test
	public void testEmptyList() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList () . "
				+ "ex:b ex:other ex:c . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[]'^^cdt:List . "
				+ "ex:b ex:other ex:c . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}

	@Test
	public void testListWithURI() {
		final String inputAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList ( ex:b 42 ex:c) . "
				+ "ex:b ex:other ex:c . ";

		final Node predicate = NodeFactory.createURI("http://example.org/hasList");

		final String expectedResultAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix cdt:	 <http://w3id.org/awslabs/neptune/SPARQL-CDTs/> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:a ex:hasList '[<http://example.org/b>, 42, <http://example.org/c>]'^^cdt:List . "
				+ "ex:b ex:other ex:c . ";

		testRunner(inputAsTurtle, predicate, expectedResultAsTurtle);
	}


	protected void testRunner( final String inputAsTurtle, final Node predicate, final String expectedResultAsTurtle ) {
		final RDFParser expectedResultParser = RDFParser.create()
				.fromString(expectedResultAsTurtle)
				.lang(Lang.TURTLE)
				.build();
		final Graph expectedResultGraph = GraphFactory.createDefaultGraph();
		expectedResultParser.parse(expectedResultGraph);

		final Graph result = GraphFactory.createDefaultGraph();
		final MyTriplesCollector collector = new MyTriplesCollector(result);
		final RDFtoCDTListConverter converter = new RDFtoCDTListConverter(predicate, collector);

		final RDFParser parser = RDFParser.create()
				.fromString(inputAsTurtle)
				.lang(Lang.TURTLE)
				.build();
		parser.parse(converter);

		assertTrue( result.isIsomorphicWith(expectedResultGraph) );
	}

	protected class MyTriplesCollector extends StreamRDFBase {
		protected final Graph g;

		public MyTriplesCollector( final Graph g ) { this.g = g; }

		@Override
		public void triple( final Triple t ) { g.add(t); }

		public Graph getGraph() { return g; }
	}

}