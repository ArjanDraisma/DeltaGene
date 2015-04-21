# DeltaGene
DeltaGene is a simple program that compares a list of genes against a multitude of lists. Includes a connection to the Human Phenotype Ontology database.


Support

!!!I have no plans to maintain this code after June 30, 2015!!!
I may work on it as a hobby at some point, but other than that the 
program is provided as is.


Summary

DeltaGene is a small program that compares lists of genes. It downloads
the latest HPO database (http://www.human-phenotype-ontology.org/) and
uses it to provide the user with autocompletion and genes associated 
with certain phenotypes.


Install

You will need Java version 1.6 or later to run DeltaGene. You can extract
DeltaGene anywhere on your computer and run it. DeltaGene will create
a directory called HPO in the same folder that it is in to store the 
HPO files.
 

Source

The source of DeltaGene is included in the .jar file, next to the class
files. I (the original developer) have some experience with 
object-oriented programs, but am hardly professional in any language
let alone Java. I am certain that as a result my code lacks the touch
of a professional programmer. If you do decide to dive into the dark
crevices of the source code, know that I have done my best to keep with 
Java standards, document my code, handle exceptions and improve
performance of the program.

Areas I know are probably ugly, confusing or amateur:

- Any component of the GUI
- The generateResults() function(s)
- The Browser class
