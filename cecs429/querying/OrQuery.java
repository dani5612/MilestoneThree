package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	String type = "positive";
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	public String getType(){

        return type;

    }

	@Override
	public List<Posting> getPostings(Index index) {

		List<Posting> result = new ArrayList<Posting>();
		List<Posting> postings1 = new ArrayList<Posting>();
		List<Posting> postings2 = new ArrayList<Posting>();
		boolean firstMerge = true;

		//-1 to prevent going out of bounds
		for(int componentNumber = 0; componentNumber < mComponents.size() -1; componentNumber++ ){

			int pointer1 = 0;
			int pointer2 = 0;

			//if this is not our first merge
			if(!firstMerge){
				//make space for a new merge
				postings1.clear();
				//add all the current results that we will be comparing
				postings1.addAll(result);
				//clear the results for now
				result.clear();
			}
			else if(firstMerge){
				postings1 = mComponents.get(componentNumber).getPostings(index);
				firstMerge = false;
			} 

			//get the next element int he list of components
			postings2 = mComponents.get(componentNumber + 1).getPostings(index);

			if(postings1 == null || postings2 == null){

				System.out.println("Phrase went wrong, returning null");
				return null;

			}

			while(pointer1 < postings1.size()  && pointer2 <  postings2.size() ){

				//if they both match, add them
				if(postings1.get(pointer1).getDocumentId() == postings2.get(pointer2).getDocumentId() ) {
					result.add(postings1.get(pointer1));
					pointer1++;
					pointer2++;
				}
				//if they do not match, just keep on moving
				else if(postings1.get(pointer1).getDocumentId() < postings2.get(pointer2).getDocumentId() ){
					result.add(postings1.get(pointer1));
					pointer1++;
				}
				else if(postings1.get(pointer1).getDocumentId() > postings2.get(pointer2).getDocumentId() ){
					result.add(postings2.get(pointer2));
					pointer2++;
				}	

			}
			//for the or, we need to add whatever is left of the list that did not finish
			if(pointer1 >= postings1.size()){
				while(pointer2 <  postings2.size()){
					result.add(postings2.get(pointer2));
					pointer2++;
				}
			}
			else if(pointer2 >= postings2.size()){
				while(pointer1 <  postings1.size()){
					result.add(postings1.get(pointer1));
					pointer1++;
				}
			}
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
