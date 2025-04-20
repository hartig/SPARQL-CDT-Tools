# SPARQL-CDT-Tools
This project contains tools related to a SPARQL extension for handling literals that capture generic types of composite values (lists, maps, etc.). For an overview of the extension, refer to the [informal description of the approach](https://w3id.org/awslabs/neptune/SPARQL-CDTs/spec/latest.html#description) in the related specification. The repository with artifacts for the specification is: https://github.com/awslabs/SPARQL-CDTs

## convert-rdf-lists
This tool converts RDF data that uses [RDF collections](https://www.w3.org/TR/rdf-mt/#collections) to RDF data in which the collections are replaced by [cdt:List literals](https://awslabs.github.io/SPARQL-CDTs/spec/latest.html#list-datatype). For example, consider the following RDF data (represented in Turtle format).
```turtle
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ex:   <http://example.org/>

ex:b ex:hasList _:b0 .
_:b0 rdf:type rdf:List ;
     rdf:first 1 ;
     rdf:rest _:b1 .
_:b1 rdf:first 2 ;
     rdf:rest _:b2 .
_:b2 rdf:first 3 ;
     rdf:rest rdf:nil .
ex:b ex:other ex:c .
```
By using the tool, this data can be converted into the following.
```turtle
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX cdt:  <http://w3id.org/awslabs/neptune/SPARQL-CDTs/>
PREFIX ex:   <http://example.org/>

ex:b    ex:hasList  "[1, 2, 3]"^^cdt:List .
ex:b    ex:other  ex:c .
```
The tool works in a streaming fashion (i.e., it uses a streaming parser for the input data and it produces output while consuming the input), it can read input data from a file or from stdin, in all sorts of RDF formats, and it can write the converted data to a file or to stdout. It is possible to specify the predicate URIs of the triples that refer to the relevant RDF collections to be converted (such as the predicate URI `ex:hasList` in the previous example); RDF collections referred to by other triples are ignored and not converted.

### Usage of convert-rdf-lists
To use the tool you need to have a relatively recent version of a Java Runtime Environment installed on your computer.

Then, [download the latest release package](https://github.com/hartig/SPARQL-CDT-Tools/releases), unpack it, enter the resulting directory in a command-line terminal, and execute a command such as the following (where `Example.ttl` would be the input file to be converted).
```bash
bin/convert-rdf-lists --infile=Example.ttl --predicate=http://example.org/hasList
```
Further arguments can be passed to the program, which are described in detail below. You can also have the list of supported arguments printed by executing the program with the argument `--help`. 
```bash
bin/convert-rdf-lists --help
```
### Arguments of convert-rdf-lists
**`--predicate`** This argument can be used to specify the predicate IRI(s) to be considered for the conversion (such as `ex:hasList` in the previous example). This argument is mandatory and can be provided multiple times to specify multiple predicate IRIs.

**`--insyntax`** This argument can be used to specify the syntax of the input. If this argument is omitted, then the syntax is guessed from the file extension of the input file (which is not possible if the input is meant to be read from stdin). Possible values for this argument are `Turtle`, `N-Triples`, `JSON-LD`, and `RDF/XML`.

**`--infile`** This argument can be used to specify the file with the input data. If this argument is omitted, then the tool reads the input from stdin. Hence, it is possible to pipe the output of other command-line programs as input to the tool.
```bash
cat Example.ttl | bin/convert-rdf-lists --insyntax=Turtle --predicate=http://example.org/hasList
```

**`--outfile`** This argument can be used to specify the file to which the converted data shall be written. If this argument is omitted, then the tool writes to stdout.

**`--stream`** This argument can be used to specify the syntax of the output when producing the output in a streaming fashion. If this argument is omitted, then the N-Triples syntax is used as the default. Possible values for this argument are `Turtle` and `N-Triples`.
