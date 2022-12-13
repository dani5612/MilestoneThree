/*
 * 
 * A simple wraper around a Document ID	
 * 
 * 
*/

package cecs429.indexing;

import java.util.ArrayList;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {

	private int mDocumentId;
	private ArrayList<Integer> positions = new ArrayList<Integer>();
	private int tftd;

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public Posting(int documentId, Integer position) {

		mDocumentId = documentId;
		this.positions.add(position);
		
	}

	public void addPosition(Integer position){

		this.positions.add(position);

	}

	public void addtftd(int tftd){

		this.tftd = tftd;


	}

	public int gettftd(){

		return this.tftd;
		
	}

	
	public int getDocumentId() {
		return mDocumentId;
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

}
