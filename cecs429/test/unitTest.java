package cecs429.test;


import org.junit.*;
import static org.junit.Assert.*;

import java.util.List;
import cecs429.querying.BooleanQueryParser;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;

import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;

import cecs429.text.BasicTokenProcessorV2;
import cecs429.text.EnglishTokenStream;


import java.nio.file.Paths;

import java.util.*;
/*
 * The purpose of this class is to create its separate, stand alone index compared
 * to the TermDocumentIndexer class. It must pull from a separate directory.
 */
public class unitTest{

    private ArrayList<Integer> singlePhraseQueryResult = new ArrayList<Integer>();
    private ArrayList<Integer> PhraseQueryResult = new ArrayList<Integer>();
    private ArrayList<Integer> AndQueryResult = new ArrayList<Integer>();
    private ArrayList<Integer> OrQueryResult = new ArrayList<Integer>();
    private ArrayList<Integer> NotQueryResult = new ArrayList<Integer>();


    Index globalIndex;

    // Set up the index, collections, lists, whatever else in here!
    public void Test(){
										   
        if(true){

            testAndQuery();
            testOrQuery();
            testPhraseQuery();
            testSinglePhraseQuery();

        }


    }

    // Basic test to see if assert is true.
    // @Test
    // public void testEmptyCollection() {
    //     assertTrue(collection.isEmpty());
    // }
    // TODO:
    // Test the Phrase query
    @Test
    public void testSinglePhraseQuery(){


        String directory = "C:\\Users\\lapiz\\OneDrive\\Documentos\\CSULB\\FALL 2022\\CECS 429\\bunnies";


        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

        long start = System.nanoTime();
        System.out.println("Indexing. . .");
        // Index the documents of the corpus.
        Index index = indexCorpustxt(corpus) ;
        long end = System.nanoTime();
        long elaplsedTime = end - start ;
        double accurateTime = (double)elaplsedTime / 1_000_000_000; 
		System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");

        String  singlePhraseQuery = "bunny";

        BooleanQueryParser parser = new BooleanQueryParser();

        List<Posting> singlePhraseQueryPostings = parser.parseQuery(singlePhraseQuery).getPostings(index);

        //getting results for SinglePhrase
        if(singlePhraseQueryPostings == null){
            System.out.println("singlePhraseQueryPostings NOT postings found");	
        }
        else {
            for(Posting p : singlePhraseQueryPostings){
                singlePhraseQueryResult.add(p.getDocumentId());
            }
        }


        ArrayList <Integer> expectedResults = new ArrayList<Integer>();
        expectedResults.add(0);
        expectedResults.add(1);
        expectedResults.add(2);
        expectedResults.add(3);
        expectedResults.add(4);

        if(expectedResults.equals(singlePhraseQueryResult)){
            System.out.println("testSinglePhraseQuery successful");
        }
        else{
            System.out.println("testSinglePhraseQuery failed");
        }

        assertEquals(expectedResults, singlePhraseQueryResult);

        /*
         * bunny-->{docID:0[2,8],
         *          docID:1[2],
         *          docID:2[4],
         *          docID:3[6],
         *          docID:4[11]}
         */

    }
    // TODO:
    // Test the Phrase query




    @Test
    public void testNotQuery(){


        String directory = "C:\\Users\\lapiz\\OneDrive\\Documentos\\CSULB\\FALL 2022\\CECS 429\\bunnies";


        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

        long start = System.nanoTime();
        System.out.println("Indexing. . .");
        // Index the documents of the corpus.
        Index index = indexCorpustxt(corpus) ;
        long end = System.nanoTime();
        long elaplsedTime = end - start ;
        double accurateTime = (double)elaplsedTime / 1_000_000_000; 
		System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");

        String  NotQuery = "bunny -socks";

        BooleanQueryParser parser = new BooleanQueryParser();

        List<Posting> NotQueryPostings = parser.parseQuery(NotQuery).getPostings(index);

        //Not Query
        if(NotQueryPostings == null){
            System.out.println("NotQueryPostings NOT postings found");	
        }
        else {
            for(Posting p : NotQueryPostings){
                NotQueryResult.add(p.getDocumentId());
            }
        }

        ArrayList <Integer> expectedResults = new ArrayList<Integer>();
        expectedResults.add(0);
        expectedResults.add(1);
        expectedResults.add(3);
        expectedResults.add(4);

        if(expectedResults.equals(NotQueryResult)){
            System.out.println("OrQueryResult successful");
        }
        else{
            System.out.println("OrQueryResult failed");
        }


        /*
         * ' bunny -socks ' --> docID: 0 1 3 4
         */
    }


    @Test
    public void testPhraseQuery(){

        String directory = "C:\\Users\\lapiz\\OneDrive\\Documentos\\CSULB\\FALL 2022\\CECS 429\\bunnies";


        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

        long start = System.nanoTime();
        System.out.println("Indexing. . .");
        // Index the documents of the corpus.
        Index index = indexCorpustxt(corpus) ;
        long end = System.nanoTime();
        long elaplsedTime = end - start ;
        double accurateTime = (double)elaplsedTime / 1_000_000_000; 
		System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");

        String  PhraseQuery = "the \"vanilla cake\"";

        BooleanQueryParser parser = new BooleanQueryParser();

        List<Posting> PhraseQueryPostings = parser.parseQuery(PhraseQuery).getPostings(index);

        //gettingResults for Phrase
        if(PhraseQueryPostings == null){
            System.out.println("PhraseQueryPostings NOT postings found");	
        }
        else {
            for(Posting p : PhraseQueryPostings){
                PhraseQueryResult.add(p.getDocumentId());
            }
        }



        ArrayList <Integer> expectedResults = new ArrayList<Integer>();
        expectedResults.add(3);

        if(expectedResults.equals(PhraseQueryResult)){
            System.out.println("PhraseQueryResult successful");
        }
        else{
            System.out.println("PhraseQueryResult failed");
        }


        assertEquals(expectedResults, PhraseQueryResult);

        /*
         * 'the "vanilla cake' --> docID: 4
         * the, docID: 1,2,3,4,5
         * vanilla, docID: 4[10]
         * cake, docID:    4[11]
         * "vanilla cake", docID: 4
         */
    }
    // TODO:
    // Test the AND query
    @Test
    public void testAndQuery(){

        String directory = "C:\\Users\\lapiz\\OneDrive\\Documentos\\CSULB\\FALL 2022\\CECS 429\\bunnies";


        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

        long start = System.nanoTime();
        System.out.println("Indexing. . .");
        // Index the documents of the corpus.
        Index index = indexCorpustxt(corpus) ;
        long end = System.nanoTime();
        long elaplsedTime = end - start ;
        double accurateTime = (double)elaplsedTime / 1_000_000_000; 
		System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");

        String  AndQuery = "forest bunny";

        BooleanQueryParser parser = new BooleanQueryParser();

        List<Posting> AndQueryPostings = parser.parseQuery(AndQuery).getPostings(index);

        //getting results for AndQueryPostings
        if(AndQueryPostings == null){
            System.out.println("AndQueryPostings NOT postings found");	
        }
        else {
            for(Posting p : AndQueryPostings){
                AndQueryResult.add(p.getDocumentId());
            }
        }


        
        ArrayList <Integer> expectedResults = new ArrayList<Integer>();
        expectedResults.add(4);

        if(expectedResults.equals(AndQueryResult)){
            System.out.println("AndQueryResult successful");
        }
        else{
            System.out.println("AndQueryResult failed");
        }
        
        assertEquals(expectedResults, AndQueryResult);

        /*
         * 'forest AND bunny' --> docID: 
         */
    }

    // TODO:
    // Test the OR query
    @Test
    public void testOrQuery(){

        String directory = "C:\\Users\\lapiz\\OneDrive\\Documentos\\CSULB\\FALL 2022\\CECS 429\\bunnies";


        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

        long start = System.nanoTime();
        System.out.println("Indexing. . .");
        // Index the documents of the corpus.
        Index index = indexCorpustxt(corpus) ;
        long end = System.nanoTime();
        long elaplsedTime = end - start ;
        double accurateTime = (double)elaplsedTime / 1_000_000_000; 
		System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");

        String  OrQuery = "from + and";

        BooleanQueryParser parser = new BooleanQueryParser();

        List<Posting> OrQueryPostings = parser.parseQuery(OrQuery).getPostings(index);

        //getting results for OrQueryPostings
        if(OrQueryPostings == null){
            System.out.println("OrQueryPostings NOT postings found");	
        }
        else {
            for(Posting p : OrQueryPostings){
                OrQueryResult.add(p.getDocumentId());
            }
        }
        

        ArrayList <Integer> expectedResults = new ArrayList<Integer>();
        expectedResults.add(0);
        expectedResults.add(1);
        expectedResults.add(2);

        if(expectedResults.equals(OrQueryResult)){
            System.out.println("OrQueryResult successful");
        }
        else{
            System.out.println("OrQueryResult failed");
        }
        
        assertEquals(expectedResults, OrQueryResult);
        

        /*
         * ' from + and ' --> docID: 1,2,3
         */
    }












































































    private static Index indexCorpustxt(DocumentCorpus corpus) {

		//This guy will hold our vocabulary! The beautiful thing
		//about HashSets is that they don't take duplicates
		HashSet<String> vocabulary = new HashSet<>();

		//This guy will process all the words so they are nice and
		//we can add them to the index.
		BasicTokenProcessorV2 processor = new BasicTokenProcessorV2();

		//This guy is our cool iverted index!
		PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex(vocabulary);

		ArrayList<String> tempTokenResults = new ArrayList<String>();
		//Loop through the documents one by one! We will start with document 1 and so on.
		for (Document d : corpus.getDocuments()) {
			//for each document, we will need an english token stream
			//that will be able to get us a reader. This reader will get us
			//the contents of the file!
			EnglishTokenStream stream =  new EnglishTokenStream(d.getContent());
			//For each "token" in the stream, we will add it to the inverted index.
			Integer position = 1;

			for(String token: stream.getTokens()){
				//We add the term to our index. processor is the BasicTokenProcessor up there.
				//We pass the PROCESSED TOKEN to the add term function, along with the
				//documentID where the token is coming from.
				tempTokenResults = processor.processTokenList(token);

				for(String stemmedTerm : tempTokenResults){

					invertedIndex.addTerm(stemmedTerm, d.getId(), position);

				}
				
				position++;

			}
		}
		//and we return our cool Index B)
		return invertedIndex;

	}



}
