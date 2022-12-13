
/*Represents an abstract document.This document has an:
 * 
 * Integer ID
 * String  Title
 * 
 * Can produce a stream of text representing the document's content.
 * 
 * NOTICE THAT THIS IS AN INTERFACE
 * 
 */


package cecs429.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document {
	/**
	 * The ID used by the index to represent the document.
	 */
	int getId();
	

	public double getByteSize();


	public String getDocumentName();

	/**
	 * Gets a stream over the content of the document.
	 */
	Reader getContent();
	
	/**
	 * The title of the document, for displaying to the user.
	 */
	String getTitle();
	/**
	 * Gets a stream over the author of the document.
	 */
	Reader getAuthor();
	/**
	 * The author names of the document, for displaying to the user.
	 */
	String getAuthorNames();
}
