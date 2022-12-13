package cecs429.querying;

import java.util.ArrayList;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.BasicTokenProcessorV2;


/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;

	String type = "positive";

	public String getType(){

        return type;

    }
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {

		ArrayList<String> stemmedQuery = new ArrayList<String>();

		BasicTokenProcessorV2 queryProcessor = new BasicTokenProcessorV2();
		stemmedQuery =  queryProcessor.processTokenList(mTerm);


		if(stemmedQuery.size() == 1){

			for(String element : stemmedQuery){


				if(index.getPostings(element) == null ){
					System.out.println("Your Query has no results");
				}
				else if(index.getPostings(element) != null){
					return index.getPostings(element);
				}	
			}
		}

		else if(stemmedQuery.size() > 1){

			int maxLength = 0;
			String longestString = null;
			for(String s : stemmedQuery){
				if(s.length() > maxLength){
					maxLength = s.length();
					longestString = s;
				}
			}

			if(index.getPostings(longestString) == null ){
				System.out.println("Your Query has no results");
				return null;

			}
			else if(index.getPostings(longestString) != null ){
				return index.getPostings(longestString);
			}
			
		}


		return index.getPostings(mTerm);


	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
