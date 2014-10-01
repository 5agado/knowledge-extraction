knowledge-extraction
====================

From Natural Language Text to Graph Database.

Extract information from big volumes of English language text, process it and store the results in a graph database for easy-to-do computation. Knowledge is represented as triplets of the form subject-action-object.

## Project Notes
###TOOLS FOR NLP:
* NLTK
* OpenNLP, UIMA
* CoreNLP
* GATE, RapidMiner
* MAHOUT
	
###RELATION EXTRACTION:
* UIMA (DKPro-core)
* Alchemy
* OpenIE (ReVerb, Ollie)
* RelEx
* ??StandfordNLP
	
	
###GENERAL IDEAS
- "person was here", or "person is that"
- difference between generic "retrieval" and the specific one (ex: based on query)
- extract relations, defining the strongest one (by mean of reinforcing its value for each
founded same entry) or finding contradiction (with percentage of validity/strength) 
- filter sentences before processing based on some param (e.g. person)
- "Pagerank": return most important entity of the text based on the number on relations to it
- filter triplets by classification (Person, Place ect.)
- construct knowledge base, level of confidence with the validity of something

## License

Released under version 2.0 of the [Apache License].

[Apache license]: http://www.apache.org/licenses/LICENSE-2.0
