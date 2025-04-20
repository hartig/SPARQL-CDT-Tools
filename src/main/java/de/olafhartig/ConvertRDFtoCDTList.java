package de.olafhartig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModLangOutput;
import arq.cmdline.ModTime;
import de.olafhartig.utils.RDFtoCDTListConverter;

public class ConvertRDFtoCDTList extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModLangOutput modLangOut =   new ModLangOutput();

	protected final ArgDecl argPredicate = new ArgDecl( ArgDecl.HasValue, "predicate" );
	protected final ArgDecl argInSyntax = new ArgDecl( ArgDecl.HasValue, "insyntax" );
	protected final ArgDecl argInFile = new ArgDecl( ArgDecl.HasValue, "infile" );
	protected final ArgDecl argOutFile = new ArgDecl( ArgDecl.HasValue, "outfile" );

	public static void main( final String[] args ) {
		new ConvertRDFtoCDTList(args).mainRun();
	}

	protected ConvertRDFtoCDTList( final String[] argv ) {
		super(argv);

		addModule(modTime);

		addModule(modLangOut);
		add( argOutFile, "--outfile", "File for the output data (if no such file is specified, then the tool writes to stdout)" );

		getUsage().startCategory("Input control");
		add( argInSyntax, "--insyntax", "Specifies syntax of the input (if no syntax is specified, the syntax is guessed from the file extension of the input file)");
		add( argInFile, "--infile", "File with the input data (if no such file is specified, then the tool reads from stdin)" );

		getUsage().startCategory("Conversion-related arguments");
		add( argPredicate, "--predicate", "Specifies the predicate IRI(s) to be considered for the conversion (this argument can be provided multiple times)");
	}

	@Override
	protected String getSummary() {
		return "Usage: " + getCommandName() + " " +
				"--predicate=<predicate URI> " +
				"[ --insyntax=<syntax name> ] " +
				"[ --infile=<input file> ] " +
				"[ --outfile=<output file> ] ";
	}

	@Override
	protected String getCommandName() {
		return "convert-rdf-lists";
	}

	@Override
	protected void exec() {
		final InputStream inStream = determineInputStream();
		final Lang inSyntax = determineInputSyntax();

		final RDFParser parser = RDFParser.create()
				.source(inStream)
				.lang(inSyntax)
				.build();

		final Set<Node> predicates = determinePredicates();
		final StreamRDF outRDFStream = createOutputRDFStream();

		final StreamRDF converter = new RDFtoCDTListConverter(predicates, outRDFStream);

		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		parser.parse(converter);

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Overall Processing Time: " + modTime.timeStr(time) + " sec");
		}
	}


	protected StreamRDF createOutputRDFStream() {
		if ( modLangOut.compressedOutput() ) {
			throw new CmdException("Compression of output not supported.");
		}

		final RDFFormat format = modLangOut.getOutputStreamFormat();
		if ( format == null ) {
			throw new CmdException("Non-streaming output not supported.");
		}

		final OutputStream outStream = determineOutputStream();
		final RDFFormat outSyntax = modLangOut.getOutputStreamFormat();

		return StreamRDFWriter.getWriterStream(outStream, outSyntax);
	}

	protected OutputStream determineOutputStream() {
		if ( ! contains(argOutFile) ) {
			return System.out;
		}

		final String filename = getValue(argOutFile);
		final File file = new File(filename);

		try {
			return new FileOutputStream(file);
		}
		catch ( final FileNotFoundException e ) {
			throw new CmdException( "There is a problem with the specified output file (" + filename + "): " + e.getMessage() );

		} 
	}

	protected InputStream determineInputStream() {
		if ( ! contains(argInFile) ) {
			return System.in;
		}

		final String filename = getValue(argInFile);
		final File file = new File(filename);

		try {
			return new FileInputStream(file);
		}
		catch ( final FileNotFoundException e ) {
			throw new CmdException( "There is a problem with the specified input file (" + filename + "): " + e.getMessage() );
		} 
	}

	protected Lang determineInputSyntax() {
		if ( contains(argInSyntax) ) {
			final String syntax = getValue(argInSyntax);
			final Lang lang = RDFLanguages.nameToLang(syntax);
			if ( lang == null )
				throw new CmdException("Cannot detemine the syntax from '" + syntax + "'");
			return lang;
		}

		if ( ! contains(argInFile) ) {
			throw new CmdException("Input syntax must be specified when reading from stdin.");
		}

		final String filename = getValue(argInFile);
		final Lang lang = RDFLanguages.filenameToLang(filename);
		if ( lang == null ) {
			throw new CmdException("Cannot guess the input syntax for the given input file.");
		}

		return lang;
	}

	protected Set<Node> determinePredicates() {
		if ( ! contains(argPredicate) )
			throw new CmdException("No predicate URIs specified.");

		//containsMultiple(argPredicate);
		final Set<Node> predicates = new HashSet<>();
		for ( final String p : getValues(argPredicate) ) {
			try {
				new URI(p);
			}
			catch ( final Exception e ) {
				throw new CmdException("One of the given predicate URIs does not seem to be a URI (" + p + ")");
			}

			predicates.add( NodeFactory.createURI(p) );
		}

		return predicates;
	}

}
