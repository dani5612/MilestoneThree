/*
 * Abstracts a collection of documents without specifying where those documents come from.
 * or what kind of documents they are.
 * 
 * Again, notice this is an INTERFACE.
 * 
 * Maybe adding an I in front of the name of files that are
 * interfaces could help in the future.
 * 
 */


package cecs429.documents;

/**
 * Represents a collection of documents used to build an index.
 */
public interface DocumentCorpus {
	/**
	 * Gets all documents in the corpus.
	 */
	Iterable<Document> getDocuments();
	
	/**
	 * The number of documents in the corpus.
	 */
	int getCorpusSize();
	
	/**
	 * Returns the document with the given document ID.
	 */
	Document getDocument(int id);
}
