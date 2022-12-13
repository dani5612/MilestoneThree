package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class NotQuery implements QueryComponent {

	private QueryComponent mComponent;
    private String type = "negative";
	
	public NotQuery( QueryComponent component) {
		mComponent = component;
	}
	
    public String getType(){

        return type;

    }

	@Override
	public List<Posting> getPostings(Index index) {
		
		return mComponent.getPostings(index);	
		
	}
	
	@Override
	public String toString() {
		return "-";
	}


}
