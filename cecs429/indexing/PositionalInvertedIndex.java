//Give me 6 hours to chop a tree and
//I will spend the first 4 hours
//sharpening my axe.

package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index {

    //Our index will consist of a HashMap that will map a word(string) to an array of postings
    private HashMap <String, ArrayList<Posting> > PositionalInvertedIndex;

    private List <String> vocabulary;




    public PositionalInvertedIndex (Collection<String> inputVocabulary){
        
        PositionalInvertedIndex = new HashMap<String, ArrayList<Posting> > ();
        this.vocabulary = new ArrayList<String>();
        this.vocabulary.addAll(inputVocabulary);
        Collections.sort(vocabulary);

    }

    

    public HashMap <String, ArrayList<Posting> > getPositionalInvertedIndex(){

        return this.PositionalInvertedIndex;

    }


    public void addTerm(String term, int docID, Integer Position){

        //if we straight up do not find the key, we need to just add it.
        //the requirement for addTerm is to keep it in constant time O(1).
        //using containsKey is okay because it runs in constant time.
        if(!PositionalInvertedIndex.containsKey(term)){

            //add the word to the vocabulary
            vocabulary.add(term);

            //since it is new, we need a new posting list to associate to the term
            ArrayList<Posting> newPostingsList = new ArrayList<Posting>();  
            //we add a the posting where we just found it to the new list.
            newPostingsList.add(new Posting(docID, Position));
            //we add the term and the new list to the inverted index.
            PositionalInvertedIndex.put(term, newPostingsList );

        }
        //doing an else here may also work! I am just paranoid.
        //if the inverted index contains the key, we add the posting (made of the docID) to the 
        //array list IF the posting has not been made yet. (The docID in the form of a posting is not there yet).
        else if (PositionalInvertedIndex.containsKey(term)) {

            //BEHOLD
            ////PositionalInvertedIndex.get(term).get(PositionalInvertedIndex.get(term).size() - 1);
            //This line of code is a riddle. BUT
            //we use PositionalInvertedIndex.get(term) to get the arraylist mapped to the term.
            //then we use:
            //PositionalInvertedIndex.get(term).get(~~~~) to get the value at index ~~~~ of that list. (1)
            //inside that .get we use:
            //PositionalInvertedIndex.get(term).size() - 1 to get last index. if the lenght is 100, 100-1 is the last index. (2)
            //we combine 1 and 2 to get the element at the last index.

            //If the value of the last element is equal to the docID we are trying to insert, we don't add it because we already took
            //note that the term is in that document.
            //If the value of the last element is NOT equal to the docID, this means it is the first time we see 
            //this term in the document. So we ADD IT as a new posting.! O(1) :)


            if (PositionalInvertedIndex.get(term).get(PositionalInvertedIndex.get(term).size() - 1).getDocumentId() == docID){
                //here we are adding the new posting to the index!
                PositionalInvertedIndex.get(term).get(PositionalInvertedIndex.get(term).size() - 1).addPosition(Position);
            } 
            else if (PositionalInvertedIndex.get(term).get(PositionalInvertedIndex.get(term).size() - 1).getDocumentId() != docID){
                //here we are adding the new posting to the index!
                PositionalInvertedIndex.get(term).add(new Posting(docID, Position));
            } 

        }


    }


    //We already did all the hard work adding to the posting list. So we can take it easy here. Check this out B)
    @Override
    public List<Posting> getPostings(String term) {
        //this guy is gonna give us the list of postings that we need!
        //we already have a postings list ready from our hashmap!
        //if the term is not at all in the index, we return null
        //and leave it to the TermDocumentIndexer to deal with the rest. 
        if(PositionalInvertedIndex.containsKey(term)){
            return PositionalInvertedIndex.get(term);
        }
        else {
            return null;
        }
    }




    @Override
    public List<String> getVocabulary() {
        //Tee hee, stole it from Neal.
        return Collections.unmodifiableList(vocabulary);
    }



    @Override
    public Double getLdForDocId(int docId, int LdType) {
        // TODO Auto-generated method stub
        return null;
    }




    
}
