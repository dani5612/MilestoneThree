package cecs429.indexing;

import java.util.*;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full corpus vocabulary and number of documents
 * prior to construction.
 */
public class TermDocumentIndex implements Index {
	private final boolean[][] mMatrix;
	private final List<String> mVocabulary;
	private int mCorpusSize;
	
	/**
	 * Constructs an empty index with with given vocabulary set and corpus size.
	 * @param vocabulary a collection of all terms in the corpus vocabulary.
	 * @param corpuseSize the number of documents in the corpus.
	 */
	public TermDocumentIndex(Collection<String> vocabulary, int corpuseSize) {
		mMatrix = new boolean[vocabulary.size()][corpuseSize];
		mVocabulary = new ArrayList<String>();
		mVocabulary.addAll(vocabulary);
		mCorpusSize = corpuseSize;
		
		Collections.sort(mVocabulary);
	}
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		int vIndex = Collections.binarySearch(mVocabulary, term);
		if (vIndex >= 0) {
			mMatrix[vIndex][documentId] = true;
		}
	}
	
	@Override
	public List<Posting> getPostings(String term) {

		
		List<Posting> results = new ArrayList<>();

		if(mVocabulary.contains(term)){
			//Gives us where in the matrix we can find the term
			int termRow = Collections.binarySearch(mVocabulary, term);
			
			System.out.println("you entered: " + mVocabulary.get(termRow));
			//we search in the place where we found the term
			//and write down the columns that hit
			for(int column = 0; column < mMatrix[termRow].length; column++ ){

				//System.out.println(mMatrix[termRow][column]);

				if(mMatrix[termRow][column]){

					
					Posting hit = new Posting(column);
					results.add(hit);
					
				}

			}
			return results;
		}
		
		//for(int i = 0; i < results.size(); i++){

		//	System.out.println(results.get(i));

		//}
		

		//System.out.println(vIndex);
		// TODO: implement this method.
		// Binary search the mVocabulary array for the given term.
		// Walk down the mMatrix row for the term and collect the document IDs (column indices)
		// of the "true" entries.
		
		System.out.println("The term you entered is not part of the Vocabulary.");
		return results;
	}
	
	public List<String> getVocabulary() {
		return Collections.unmodifiableList(mVocabulary);
	}

	@Override
	public Double getLdForDocId(int docId, int LdType) {
		// TODO Auto-generated method stub
		return null;
	}

}
