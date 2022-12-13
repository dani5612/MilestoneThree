/*
 * An interface defining the operations of a search engine index. It 
 * provides two simple methods: 
 * getPostings which retrieves the postings list for a given term
 * getVocabulary which retrieves a sorted list od the index's complete vocabulary
 * 
 * 
 * 
 */


package cecs429.indexing;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {

	Double getLdForDocId(int docId, int LdType);


	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<Posting> getPostings(String term);
	
	/**
	 * A sorted list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
}
