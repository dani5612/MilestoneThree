package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {

	private List<QueryComponent> mComponents;
	String type = "positive";
	
	public AndQuery( List<QueryComponent> components) {
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
			postings2 = mComponents.get(componentNumber + 1).getPostings(index);

			//if this is not our first merge

			if(postings1 == null || postings2 == null){

				System.out.println("AND went wrong, returning null");
				return null;

			}


			if(mComponents.get(componentNumber).getType().equals("negative") ||  mComponents.get(componentNumber + 1).getType().equals("negative")  ){

				if(mComponents.get(componentNumber).getType().equals("negative")){

					postings1 =  mComponents.get(componentNumber + 1).getPostings(index);
					postings2 =  mComponents.get(componentNumber).getPostings(index);

				}

				while(pointer1 < postings1.size()  && pointer2 <  postings2.size() ){

					//if they both match, add them
					if(postings1.get(pointer1).getDocumentId() == postings2.get(pointer2).getDocumentId() ) {
						
						pointer1++;
						pointer2++;
					}
					//if they do not match, just keep on moving
					else if(postings1.get(pointer1).getDocumentId() < postings2.get(pointer2).getDocumentId() ){
						result.add(postings1.get(pointer1));
						pointer1++;


					}
					else if(postings1.get(pointer1).getDocumentId() > postings2.get(pointer2).getDocumentId() ){
						pointer2++;
					}	

				}
				if(pointer2 >=  postings2.size()){

					System.out.println("hewwo");
					while(pointer1 < postings1.size() ){

						result.add(postings1.get(pointer1));
						pointer1++;


					}



				}




			}
			else{
				//get the next element int he list of components
			
				while(pointer1 < postings1.size()  && pointer2 <  postings2.size() ){

					//if they both match, add them
					if(postings1.get(pointer1).getDocumentId() == postings2.get(pointer2).getDocumentId() ) {
						result.add(postings1.get(pointer1));
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

				}

				
			}


		}

		return result;





		/*for(QueryComponent component : mComponents){
		
			This guy did not work because we were modifying the actual postings list.
			Was a good idea but can't do it	
		
			if (result == null){

				result = component.getPostings(index);

			}
			else if (result != null){
				int resultPtr = 0;
				int componentPtr = 0;
				while(true){

					if(result.get(resultPtr).getDocumentId() == component.getPostings(index).get(componentPtr).getDocumentId() ){
						componentPtr++;
						resultPtr++;
						continue;
					}
					else if(result.get(resultPtr).getDocumentId() < component.getPostings(index).get(componentPtr).getDocumentId() ){
						result.remove(resultPtr);
						resultPtr++;
						continue;
					} 
					else if (result.get(resultPtr).getDocumentId() > component.getPostings(index).get(componentPtr).getDocumentId() )  {
						componentPtr++;

					}

				}
				


			}


		}
		*/
		

		

		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		
		
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}


}
