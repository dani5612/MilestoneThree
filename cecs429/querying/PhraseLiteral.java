package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.text.Position;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.BasicTokenProcessorV3;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	String type = "positive";

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}

	public String getType(){

        return type;

    }
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {

		System.out.println("Howdy");
		

		List<Posting> result = new ArrayList<Posting>();
		List<Posting> postings1 = new ArrayList<Posting>();
		List<Posting> postings2 = new ArrayList<Posting>();
		boolean firstMerge = true;
		int awayDistance = 1;
		BasicTokenProcessorV3 processor = new BasicTokenProcessorV3();



		if(mTerms.size() == 1){


			return index.getPostings( processor.processToken( mTerms.get(0)) );

		}


		//-1 to prevent going out of bounds
		for(int componentNumber = 0; componentNumber < mTerms.size() -1; componentNumber++ ){

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

				

				postings1 = index.getPostings( processor.processToken( mTerms.get(componentNumber)) );
				firstMerge = false;
	
			} 


			//get the next element int he list of components
			postings2 = index.getPostings( processor.processToken(mTerms.get(componentNumber + 1) )   );


			if(postings1 == null || postings2 == null){

				System.out.println("Phrase went wrong, returning null");
				return null;

			}

			


			while(pointer1 < postings1.size()  && pointer2 <  postings2.size() ){

				//if they both match, add them
				if(postings1.get(pointer1).getDocumentId() == postings2.get(pointer2).getDocumentId() ) {
					
					int positionPtr1 = 0;
					int positionPtr2 = 0;

					while(positionPtr1 < postings1.get(pointer1).getPositions().size() && positionPtr2 < postings2.get(pointer2).getPositions().size()){

						if(postings2.get(pointer2).getPositions().get(positionPtr2) > postings1.get(pointer1).getPositions().get(positionPtr1) ) {

							if(postings2.get(pointer2).getPositions().get(positionPtr2) - postings1.get(pointer1).getPositions().get(positionPtr1) == awayDistance ){

								//It is a liner search, but it should not be bad at all.
								if (!result.contains(postings1.get(pointer1))){
									result.add(postings1.get(pointer1));
								}
								positionPtr1++;
								positionPtr2++;
	
							}
							else{
								positionPtr1++;
							}
						}
						else if (postings2.get(pointer2).getPositions().get(positionPtr2) < postings1.get(pointer1).getPositions().get(positionPtr1) ){
							positionPtr2++;
						}
						else if (postings2.get(pointer2).getPositions().get(positionPtr2) == postings1.get(pointer1).getPositions().get(positionPtr1) ){
							positionPtr2++;
							positionPtr1++;
						}
						//System.out.println("Pos1 " +  positionPtr1);
					}
					pointer1++;
					pointer2++;
				}
				//if they do not match, just keep on moving
				else if(postings1.get(pointer1).getDocumentId() < postings2.get(pointer2).getDocumentId() ){
					pointer1++;
				}
				else if(postings1.get(pointer1).getDocumentId() > postings2.get(pointer2).getDocumentId() ){
					pointer2++;
				}	

				//System.out.println("ptr1 " + pointer1);
				//System.out.println("ptr2 " + pointer2);
			}
			awayDistance++;
		}
		return result;


		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
	}
	
	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
