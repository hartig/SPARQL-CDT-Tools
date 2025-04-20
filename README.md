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
TODO: describe how to use this tool
