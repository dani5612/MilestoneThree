package edu.csulb;



import cecs429.querying.BooleanQueryParser;
import javafx.application.Platform;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.finnishStemmer;
import cecs429.RankedRetrieval.OKAPI;
import cecs429.RankedRetrieval.Retrieval;
import cecs429.RankedRetrieval.defaultRetrieval;
import cecs429.RankedRetrieval.tfIdf;
import cecs429.RankedRetrieval.wonky;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexing.Index;
import cecs429.indexing.InvertedIndex;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.indexing.SoundexIndex;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.DiskPositionalIndex;
//import cecs429.indexing.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.BasicTokenProcessorV2;
import cecs429.text.BasicTokenProcessorV3;
import cecs429.text.EnglishTokenStream;
//import javafx.*;
import javafx.application.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import javax.management.Query;



public class TermDocumentIndexer {

	static byte corpusType = 0;

	public static void main(String[] args) {
		
		String directory = "";
		Scanner scan = new Scanner(System.in);
		String query;
		String retrievalMode;
		DocumentCorpus corpus = null;
		Index index = null;
		//***Soundex index will be made beforehand...
		Index soundex = null;
		

		System.out.println("---------Welcome---------");

		System.out.println("Would you like to:");
		System.out.println("1. Build an index ");
		System.out.println("or");
		System.out.println("2.query an existing index");

		query = scan.nextLine();
		if(query.equals("1")){


			System.out.println("Please enter the type of");
			System.out.println("corpus that will be used.");
			System.out.println("\"json\"\nor");
			System.out.println("\"txt\"");
			String option = scan.nextLine();
			while (true){
			
				if(option.equals("json")) {
					setCorpusToJson(corpusType);
					break;
				}
				else if (option.equals("txt")){ 
					setCorpusToTxt(corpusType);
					break;
				}
				option = scan.nextLine();
				System.out.print("Please enter a valid option.\njson\ntxt");

			}

			System.out.println("Enter the location of the corpus.");
			 directory = scan.nextLine();


			if(corpusType == 1){
				// Create a DocumentCorpus to load .json documents from the project directory.
				corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(directory).toAbsolutePath(), ".json");
			}
			else if (corpusType == 2){

				corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

			}
			else{
				corpus = null;
				System.out.println("An error has occurred trying to load the copus. Exiting Program");
				System.exit(0);
			}

			System.out.println("Please enter the location of the index (.bin file)");
			String binFile = scan.nextLine();

			long start = System.nanoTime();
			System.out.println("Indexing. . .");
			Index invertedIndex = indexCorpustxt(corpus);
			// Index the documents of the corpus.
			System.out.println("Inverted index created");
			System.out.println("writing to disk. . .");
			DiskIndexWriter writer = new DiskIndexWriter();
			writer.writeIndex(invertedIndex, binFile);
			long end = System.nanoTime();
			long elaplsedTime = end - start ;
			
			double accurateTime = (double)elaplsedTime / 1_000_000_000; 
			System.out.println("Approximate time spent indexing the corpus: " + accurateTime + " seconds.");
			index = new DiskPositionalIndex(binFile);

			//***TODO: do the same for the soundex here??
			//NOTE: no in disk for now WIP, shares same corpus as regular index
			//soundex = indexCorpusSoundex(corpus);
		}
		else if (query.equals("2")){


			System.out.println("Please enter the type of");
			System.out.println("corpus that will be used.");
			System.out.println("\"json\"\nor");
			System.out.println("\"txt\"");
			String option = scan.nextLine();
			while (true){
			
				if(option.equals("json")) {
					setCorpusToJson(corpusType);
					break;
				}
				else if (option.equals("txt")){ 
					setCorpusToTxt(corpusType);
					break;
				}
				option = scan.nextLine();
				System.out.print("Please enter a valid option.\njson\ntxt");

			}

			System.out.println("Enter the location of the corpus.");
			 directory = scan.nextLine();


			if(corpusType == 1){
				// Create a DocumentCorpus to load .json documents from the project directory.
				corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(directory).toAbsolutePath(), ".json");

				corpus.getDocuments();
			}
			else if (corpusType == 2){

				corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directory).toAbsolutePath(), ".txt");

				corpus.getDocuments();

				System.out.println(corpus.getDocument(0).getTitle());

			}
			else{
				corpus = null;
				System.out.println("An error has occurred trying to load the copus. Exiting Program");
				System.exit(0);
			}

			

			System.out.println("Please enter the location of the index (.bin file)");
			String binFile = scan.nextLine();
			index = new DiskPositionalIndex(binFile);


		}

		System.out.println("Would you like to operate in");

		System.out.println("1. Boolean Retrieval\nor");
		System.out.println("2. Ranked retrieval");

		retrievalMode = scan.nextLine();


		while (true){

			ArrayList<String> stemmedQuery = new ArrayList<String>();

			BasicTokenProcessorV2 queryProcessor = new BasicTokenProcessorV2();
			// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
			while(true){
				System.out.println("Type a term for your search or enter :q to end the program, :stem to stem a term,");
				System.out.println(":index to change directory, :vocabulary to see the vocabulary, :t to test the search engine");
				System.out.println(":author to view matching author names");
				query = scan.nextLine(); 

				if(query.equals(":q")) {
					System.out.println("quit entered, ending program");
					scan.close();
					System.exit(0);
				}
				else if (query.equals(":stem")){
					System.out.println("enter a word to stem");
					String toStem = scan.nextLine();
					englishStemmer stemmer = new englishStemmer();
					stemmer.setCurrent(toStem);
					stemmer.stem();
					String stemmedToken = stemmer.getCurrent();
					System.out.println("stemmed token is: "+ stemmedToken);
					continue;
				}
				else if (query.equals(":index")){
					break;
				}
				else if (query.equals(":vocabulary")){
					int i = 0;
					System.out.println();
					System.out.println("Printing vocabulary terms");
					for(String term : index.getVocabulary()){

						
						System.out.println(term);
						if(i >= 1000){
							break;
						}
						i++;
					}
					System.out.println("printed first 1000 terms.");
					System.out.println("Total words in the vocabulary: "+ index.getVocabulary().size());
					continue;

				}
				else if (query.equals(":disk")){

					System.out.println("Enter the location of the index");


					String indexLocation = scan.nextLine();

					
					DiskIndexWriter writer = new DiskIndexWriter();

					try {
						writer.writeIndex(index, indexLocation);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					continue;

					//

				}
				else if (query.equals(":author")){
						//1. Convert the query to the hashkey
						//Keep the original query on the side
						System.out.println("Enter one singular author name: ");
						String authorName = scan.nextLine();
						String queryConverted = SoundexIndex.termConverter(authorName);
						System.out.println(authorName + " converted to hashkey: "+ queryConverted);
						//2. Find postings that match the query hashkey
						List<Posting> x = soundex.getPostings(queryConverted);
						if(x == null){
							System.out.println("No matches to the author.");
							continue;
						}
						else {
							//3. For every matching posting to the hashkey, print the document name + author
							for(Posting p : x){
								System.out.println("Title: " + corpus.getDocument(p.getDocumentId()).getTitle()
								+ "\nAuthor: " + corpus.getDocument(p.getDocumentId()).getAuthorNames());
							}
							System.out.println("------------------------------------------");
							System.out.println("Documents found: ------- " + x.size() );
						}
						continue;
				}
		
			
				if(retrievalMode.equals("1")){								
					BooleanQueryParser parser = new BooleanQueryParser();



					List<Posting> x = parser.parseQuery(query).getPostings(index);

					if(x == null){
						System.out.println("No postings found");
						continue;
					}
					else {
						for(Posting p : x){
							System.out.println("Document ID: "+ p.getDocumentId() +" Title: " + corpus.getDocument(p.getDocumentId()).getDocumentName());	
							//System.out.println("Positions: " + p.getPositions());
						}
						System.out.println("Documents found: ------- " + x.size() );
					}
				}
				else{




					ArrayList<List<String>> relevantResults = new ArrayList<List<String>>();
					ArrayList<String> testQueries = new ArrayList<String>();

					ArrayList<Double> defaultResponseTimes = new ArrayList<Double>();
					ArrayList<Double> tfidResponseTimes = new ArrayList<Double>();
					ArrayList<Double> OKAPIResponseTimes = new ArrayList<Double>();
					ArrayList<Double> WackyResponseTimes = new ArrayList<Double>();
					





					if( query.equals(":t") ){


						//C:\Users\lapiz\OneDrive\Documentos\CSULB\FALL 2022\CECS 429\MilestoneThree\relevance_parks\relevance\qrel
						System.out.println("Enter the path to the query-relevance file");
						String relevanceFilePath =  directory  + "\\relevance\\qrel";
						//C:\Users\lapiz\OneDrive\Documentos\CSULB\FALL 2022\CECS 429\MilestoneThree\relevance_parks\relevance\queries
						System.out.println("Enter the path to the test queries file");
						String testQueriesFilePath = directory  + "\\relevance\\queries";
						

						//We get the the relevant results for each query in an array of lists. EACH LIST CONTAINS THE RELEVANT DOCUMENTS FOR THE CORRESPODING QUERY
						try {
							
							Scanner fileScanner = new Scanner(new File(relevanceFilePath));
							//Gets us the relevant results forr eac query
							while(fileScanner.hasNextLine()){
								String line = fileScanner.nextLine();
								relevantResults.add(Arrays.asList(line.split(" ")));
							}
							fileScanner = new Scanner(new File(testQueriesFilePath));
							//Gets us the test queries
							while(fileScanner.hasNextLine()){
								String line = fileScanner.nextLine();
								testQueries.add(line);
							}
							fileScanner.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}		
						//We now have the relevant results and the test queries in their respective arrays


						//for each query in the test query collection, we are gonna get the average precision of each query
						//in each retrieval model

						//Values that will help us calculate the MRT, throughput, and MAP
						Double defaultMeanAveragePrecision = 0.0;
						double defaultMRT = 0.0;
						double defaultThroughput = 0.0;

						Retrieval defaultRetriever = new defaultRetrieval();
						ArrayList<Double> defaultPrecisions = new ArrayList<Double>();
						ArrayList<Double> defaultRecalls = new ArrayList<Double>();

						//each one is gonna have it's own MRT and throughput.
						Double tfidfMeanAveragePrecision = 0.0;
						double tfidfMRT = 0.0;
						double tfidfThroughput = 0.0;
						Retrieval tfidfRetriever = new  tfIdf();
						ArrayList<Double> tfidfPrecisions = new ArrayList<Double>();
						ArrayList<Double> tfidfRecalls = new ArrayList<Double>();

						//ALL OF THEM
						Double OKAPIMeanAveragePrecision = 0.0;
						double OKAPIMRT = 0.0;
						double OKAPIThroughput = 0.0;
						Retrieval OKAPIRetriever = new OKAPI();
						ArrayList<Double> OKAPIPrecisions = new ArrayList<Double>();
						ArrayList<Double> OKAPIRecalls = new ArrayList<Double>();

						//have their own mrt and throughput.
						Double wackyMeanAveragePrecision = 0.0;
						double wackyMRT = 0.0;
						double wackyThroughput = 0.0;
						Retrieval wackyRetriever = new wonky();
						ArrayList<Double> wackyPrecisions = new ArrayList<Double>();
						ArrayList<Double> wackyRecalls = new ArrayList<Double>();

						
						
						//A counter to be able to know which query we are on.
						int testQueryCounter = 0;
						//We iterate through the test queries

						int chosenQueryToGraph = 0;
						

						for(String testQuery: testQueries){
							
							//We process the query before we pass it down to the retrieval models
							String[] choppedQuery = testQuery.split(" ");

							ArrayList<String> processedQuery = new ArrayList<String>();

							BasicTokenProcessorV3 newTokenProcessor = new BasicTokenProcessorV3();

							for(String s :choppedQuery){

								if(s.length() > 0 ){
									processedQuery.add(newTokenProcessor.processToken( s ) );
								}

					
							}
							//at this point the query is now porcessed and we can get the maps with the retrieval models



							

							System.out.println("--------------testing query: " + testQuery + "-----------------");


							System.out.println("-------testing default retrieval model-------");
							//TEME HERE
							long defaultStart = System.nanoTime();
							//default retrieval model
							PriorityQueue<Map.Entry<Integer, Double>> defaultRetrievals = defaultRetriever.getTopIdsWithScores(index, corpus.getCorpusSize(), processedQuery);
							//Helps us calculate the average precission for the query we are running in the default retrieval model.
							//This one changes for each query. Thus, we have it in the loop.
							Double defaultAveragePrecision = 0.0;

							long defaultEnd = System.nanoTime();
							long elaplsedTime = defaultEnd - defaultStart ;
							double defaultAccurateTime = (double)elaplsedTime / 1_000_000_000;

							if(testQueryCounter == 0){
								double defaultThroughputAT30 = (double)30 / defaultAccurateTime ;
								System.out.println("DEFAULT THROUGHPUT AT 30 " + defaultThroughputAT30);
							}


							defaultMRT += defaultAccurateTime;
							
							//Counter to tell us how many docs we have retrieved so far
							int defaultRetrievalCounter = 1;
							//Counter to tell us how many relevant docs we have found so far
							int defaultRelevantFound = 0;

							
							while(!defaultRetrievals.isEmpty()){
								Map.Entry<Integer,Double> retrieval = defaultRetrievals.poll() ;
								String currentDoc =  corpus.getDocument(retrieval.getKey()).getDocumentName();
								currentDoc = currentDoc.substring(0, currentDoc.lastIndexOf('.'));

								if (currentDoc.charAt(0) ==  '0'  ) {

									int whereItStarts = 0;
									while (currentDoc.charAt(whereItStarts) == '0') {
										whereItStarts++;

									}
									currentDoc = currentDoc.substring(whereItStarts);
								}

								System.out.println(defaultRetrievalCounter + " Current doc: " + currentDoc);
								if(relevantResults.get(testQueryCounter).contains(currentDoc)){

									defaultRelevantFound++;
									System.out.println("relevant doc found " + currentDoc + ".json at index" + defaultRetrievalCounter);
									defaultAveragePrecision += (double)defaultRelevantFound / (double)defaultRetrievalCounter;

								}

								if(testQueryCounter == chosenQueryToGraph){
									defaultPrecisions.add((double)defaultRelevantFound / (double)defaultRetrievalCounter);
									defaultRecalls.add((double)defaultRelevantFound / (double)relevantResults.get(testQueryCounter).size());
								}
								
								



								if(defaultRetrievalCounter==50){
									break;
								}

								defaultRetrievalCounter++;

							}

							//Notice how the here we are only dividing by the number of relevant docs for the query we are running.
							//This is because the summantion of the average precission gets calulated inside the loop
							//This part is just the division part of the AP formula.
							defaultAveragePrecision/= relevantResults.get(testQueryCounter).size(); 
							System.out.println("AP: " + defaultAveragePrecision);
							System.out.println("relevant size " + relevantResults.get(testQueryCounter).size());

							//The MeanAveragePrecision is outside the loop for the queries, so it will be sotred after each 
							//iteration of the queries.It won't reset!
							defaultMeanAveragePrecision += defaultAveragePrecision;


							System.out.println("-------testing tfidf retrieval model-------");

							//TEME HERE. We have to time right before we get the results.
							long tfidfStart = System.nanoTime();
							PriorityQueue<Map.Entry<Integer, Double>> tfidfRetrievals = tfidfRetriever.getTopIdsWithScores(index, corpus.getCorpusSize(), processedQuery);

							//Helps us calculate the average precission for the query we are running in the tfidf retrieval model.
							//This one changes for each query. Thus, we have it in the loop.
							Double tfidfAveragePrecision = 0.0;

							long tfidfEnd = System.nanoTime();
							elaplsedTime = tfidfEnd - tfidfStart ;
							double tfidfAccurateTime = (double)elaplsedTime / 1_000_000_000;

							tfidfMRT += tfidfAccurateTime;

							//Counter to tell us how many docs we have retrieved so far
							int tfidfRetrievalCounter = 1;

							//Counter to tell us how many relevant docs we have found so far
							int tfidfRelevantFound = 0;



							while(!tfidfRetrievals.isEmpty()){
								Map.Entry<Integer,Double> retrieval = tfidfRetrievals.poll() ;
								String currentDoc =  corpus.getDocument(retrieval.getKey()).getDocumentName();
								currentDoc = currentDoc.substring(0, currentDoc.lastIndexOf('.'));

								if (currentDoc.charAt(0) ==  '0'  ) {

									int whereItStarts = 0;
									while (currentDoc.charAt(whereItStarts) == '0') {
										whereItStarts++;
									}
									currentDoc = currentDoc.substring(whereItStarts);
								}

								System.out.println(tfidfRetrievalCounter + " Current doc: " + currentDoc);
								if(relevantResults.get(testQueryCounter).contains(currentDoc)){

									tfidfRelevantFound++;
									System.out.println("relevant doc found " + currentDoc + ".json at index" + tfidfRetrievalCounter);
									tfidfAveragePrecision += (double)tfidfRelevantFound / (double)tfidfRetrievalCounter;

								}

								if(testQueryCounter == chosenQueryToGraph){
									tfidfPrecisions.add((double)tfidfRelevantFound / (double)tfidfRetrievalCounter);
									tfidfRecalls.add((double)tfidfRelevantFound / (double)relevantResults.get(testQueryCounter).size());
								}

								if(tfidfRetrievalCounter==50){
									break;
								}

								tfidfRetrievalCounter++;
							}



							//Notice how the here we are only dividing by the number of relevant docs for the query we are running.
							//This is because the summantion of the average precission gets calulated inside the loop
							//This part is just the division part of the AP formula.
							tfidfAveragePrecision/= relevantResults.get(testQueryCounter).size();
							System.out.println("AP: " + tfidfAveragePrecision);
							System.out.println("relevant size " + relevantResults.get(testQueryCounter).size());
							tfidfMeanAveragePrecision += tfidfAveragePrecision;



							System.out.println("-------testing OKAPI BM25 retrieval model-------");

							//TEME HERE. We have to time right before we get the results.
							long OKAPIStart = System.nanoTime();

							PriorityQueue<Map.Entry<Integer, Double>> OKAPIRetrievals = OKAPIRetriever.getTopIdsWithScores(index, corpus.getCorpusSize(), processedQuery);

							//Helps us calculate the average precission for the query we are running in the BM25 retrieval model.
							//This one changes for each query. Thus, we have it in the loop.
							Double OKAPIAveragePrecision = 0.0;


							long OKAPIEnd = System.nanoTime();
							elaplsedTime = OKAPIEnd - OKAPIStart ;
							double OKAPIAccurateTime = (double)elaplsedTime / 1_000_000_000;
							OKAPIMRT += OKAPIAccurateTime;

							if(testQueryCounter == 0){
								double OKAPIThroughputAT30 = (double)30 / OKAPIAccurateTime ;
								System.out.println("OKAPI THROUGHPUT AT 30 " + OKAPIThroughputAT30);
							}

							//Counter to tell us how many docs we have retrieved so far
							int OKAPIRetrievalCounter = 1;

							//Counter to tell us how many relevant docs we have found so far
							int OKAPIRelevantFound = 0;

							while(!OKAPIRetrievals.isEmpty()){
								Map.Entry<Integer,Double> retrieval = OKAPIRetrievals.poll() ;
								String currentDoc =  corpus.getDocument(retrieval.getKey()).getDocumentName();
								currentDoc = currentDoc.substring(0, currentDoc.lastIndexOf('.'));

								if (currentDoc.charAt(0) ==  '0'  ) {

									int whereItStarts = 0;
									while (currentDoc.charAt(whereItStarts) == '0') {
										whereItStarts++;
									}
									currentDoc = currentDoc.substring(whereItStarts);
								}

								System.out.println(OKAPIRetrievalCounter + " Current doc: " + currentDoc);
								if(relevantResults.get(testQueryCounter).contains(currentDoc)){

									OKAPIRelevantFound++;
									System.out.println("relevant doc found " + currentDoc + ".json at index" + OKAPIRetrievalCounter);
									OKAPIAveragePrecision += (double)OKAPIRelevantFound / (double)OKAPIRetrievalCounter;

								}

								if(testQueryCounter == chosenQueryToGraph){
									OKAPIPrecisions.add((double)OKAPIRelevantFound / (double)OKAPIRetrievalCounter);
									OKAPIRecalls.add((double)OKAPIRelevantFound / (double)relevantResults.get(testQueryCounter).size());
								}

								if(OKAPIRetrievalCounter==50){
									break;
								}

								OKAPIRetrievalCounter++;

							}

							//Notice how the here we are only dividing by the number of relevant docs for the query we are running.
							//This is because the summantion of the average precission gets calulated inside the loop
							//This part is just the division part of the AP formula.
							OKAPIAveragePrecision/= relevantResults.get(testQueryCounter).size();
							System.out.println("AP: " + OKAPIAveragePrecision);
							System.out.println("relevant size " + relevantResults.get(testQueryCounter).size());
							OKAPIMeanAveragePrecision += OKAPIAveragePrecision;



							System.out.println("-------testing Wacky retrieval model-------");

							//TEME HERE. We have to time right before we get the results.
							long wackyStart = System.nanoTime();

							PriorityQueue<Map.Entry<Integer, Double>> wackyRetrievals = wackyRetriever.getTopIdsWithScores(index, corpus.getCorpusSize(), processedQuery);

							//Helps us calculate the average precission for the query we are running in the BM25 retrieval model.
							//This one changes for each query. Thus, we have it in the loop.
							Double wackyAveragePrecision = 0.0;

							long wackyEnd = System.nanoTime();
							elaplsedTime = wackyEnd - wackyStart ;
							double wackyAccurateTime = (double)elaplsedTime / 1_000_000_000;
							wackyMRT += wackyAccurateTime;

							//Counter to tell us how many docs we have retrieved so far
							int wackyRetrievalCounter = 1;
							//Counter to tell us how many relevant docs we have found so far
							int wackyRelevantFound = 0;

							while(!wackyRetrievals.isEmpty()){
								Map.Entry<Integer,Double> retrieval = wackyRetrievals.poll() ;
								String currentDoc =  corpus.getDocument(retrieval.getKey()).getDocumentName();
								currentDoc = currentDoc.substring(0, currentDoc.lastIndexOf('.'));

								if (currentDoc.charAt(0) ==  '0'  ) {

									int whereItStarts = 0;
									while (currentDoc.charAt(whereItStarts) == '0') {
										whereItStarts++;
									}
									currentDoc = currentDoc.substring(whereItStarts);
								}

								System.out.println(wackyRetrievalCounter + " Current doc: " + currentDoc);
								if(relevantResults.get(testQueryCounter).contains(currentDoc)){

									wackyRelevantFound++;
									System.out.println("relevant doc found " + currentDoc + ".json at index" + wackyRetrievalCounter);
									wackyAveragePrecision += (double)wackyRelevantFound / (double)wackyRetrievalCounter;

								}

								if(testQueryCounter == chosenQueryToGraph){
									wackyPrecisions.add((double)wackyRelevantFound / (double)wackyRetrievalCounter);
									wackyRecalls.add((double)wackyRelevantFound / (double)relevantResults.get(testQueryCounter).size());
								}
								


								if(wackyRetrievalCounter==50){
									break;
								}

								wackyRetrievalCounter++;

							}

							//Notice how the here we are only dividing by the number of relevant docs for the query we are running.
							//This is because the summantion of the average precission gets calulated inside the loop
							//This part is just the division part of the AP formula.
							wackyAveragePrecision/= relevantResults.get(testQueryCounter).size();
							System.out.println("AP: " + wackyAveragePrecision);
							System.out.println("relevant size " + relevantResults.get(testQueryCounter).size());
							wackyMeanAveragePrecision += wackyAveragePrecision;








							

							//we now have the sorted retrievals for each method, we can now get the average precision for each method							
							testQueryCounter++;
						}

						System.out.println("-----------------OVERALL RESULTS FOR SEARCH ENGINE-----------------");

						//----------DefaultResults----------------
						defaultMeanAveragePrecision /= testQueries.size();
						defaultThroughput = (double)testQueries.size() / defaultMRT;
						defaultMRT /= (double)testQueries.size();
						System.out.println("default throughput: " + defaultThroughput);
						System.out.println("default MRT: " + defaultMRT);
						System.out.println("default MAP: " + defaultMeanAveragePrecision);
						//----------------------------------------

						//----------tfidfResults------------------
						tfidfMeanAveragePrecision /= testQueries.size();
						tfidfThroughput = (double)testQueries.size() / tfidfMRT;
						tfidfMRT /= (double)testQueries.size();
						System.out.println("tfidf throughput: " + tfidfThroughput);
						System.out.println("tfidf MRT: " + tfidfMRT);
						System.out.println("tfidf MAP: " + tfidfMeanAveragePrecision);
						//----------------------------------------


						//----------OKAPIResults------------------
						OKAPIMeanAveragePrecision /= testQueries.size();
						OKAPIThroughput = (double)testQueries.size() / OKAPIMRT;
						OKAPIMRT /= (double)testQueries.size();
						System.out.println("OKAPI throughput: " + OKAPIThroughput);
						System.out.println("OKAPI MRT: " + OKAPIMRT);
						System.out.println("OKAPI MAP: " + OKAPIMeanAveragePrecision);
						//----------------------------------------

						//----------WackyResults------------------
						wackyMeanAveragePrecision /= testQueries.size();
						wackyThroughput = (double)testQueries.size() / wackyMRT;
						wackyMRT /= (double)testQueries.size();
						System.out.println("wacky throughput: " + wackyThroughput);
						System.out.println("wacky MRT: " + wackyMRT);
						System.out.println("wacky MAP: " + wackyMeanAveragePrecision);
						//----------------------------------------







						//datapointCollecter pointCollecter = pointCollecter.getInstance(FILL IN THE ARRAYS CONTAINING ALL THE PRECISION AND RECALL VALUES);
						//datapointCollecter pointCollecter = pointCollecter.getInstance(d )

//						for(double d : defaultPrecisions){
//							System.out.println("default precision: " + d);
//						}
//
//						for(double d : defaultRecalls){
//							System.out.println("default recall: " + d);
//						}
//
//						for(double d : tfidfPrecisions){
//							System.out.println("tfidf precision: " + d);
//						}
//
//						for(double d : tfidfRecalls){
//							System.out.println("tfidf recall: " + d);
//						}
//
//						for(double d : OKAPIPrecisions){
//							System.out.println("OKAPI precision: " + d);
//						}
//
//						for(double d : OKAPIRecalls){
//							System.out.println("OKAPI recall: " + d);
//						}
//
//						for(double d : wackyPrecisions){
//							System.out.println("wacky precision: " + d);
//						}
//
//						for(double d : wackyRecalls){
//							System.out.println("wacky recall: " + d);
//						}

						


						
						//GraphMaker graphMaker = new GraphMaker();
						datapointCollecter pointCollecter = datapointCollecter.getInstance(defaultPrecisions, defaultRecalls, tfidfPrecisions, tfidfRecalls, OKAPIPrecisions, OKAPIRecalls, wackyPrecisions, wackyRecalls);


						Application.launch(GraphMaker.class, args);

						Platform.exit();

						continue;
						
					}

					System.out.println("What formula would you like to use");
					System.out.println("1. Default");
					System.out.println("2. tf-idf");
					System.out.println("3. Okapi BM25");
					System.out.println("4. Wacky");

					String option = scan.nextLine();



					String[] choppedQuery = query.split(" ");

					ArrayList<String> processedQuery = new ArrayList<String>();

					BasicTokenProcessorV3 newTokenProcessor = new BasicTokenProcessorV3();

					for(String s :choppedQuery){

						processedQuery.add(newTokenProcessor.processToken( s ) );
					
					}


					//processedQuery = newTokenProcessor.processTokenList(query);





					PriorityQueue<Map.Entry<Integer, Double>> top10;
					int N  = corpus.getCorpusSize();
					Retrieval retriever = new defaultRetrieval();


					
					while(true){

						if(option.equals("1")){
							retriever = new defaultRetrieval();
							break;
						}
						else if(option.equals("2")){
							retriever = new tfIdf();
							break;
						}
						else if(option.equals("3")){
							retriever = new OKAPI();
							break;
						}
						else if(option.equals("4")){
							retriever = new wonky();
							break;
						}
						else{
							option = scan.nextLine();
							System.out.println("Please enter a valid option!");
							continue;
						}

					}

					top10 =  retriever.getTopIdsWithScores(index, N, processedQuery);

					if(top10==null){

						System.out.println("No postings were found");
						continue;
					}


					int counter = 1;

					while(!top10.isEmpty()){
			
					    //System.out.println(priorityQueue.poll().getKey());
					    //System.out.println(priorityQueue.poll().getValue());
			
			
						
			
					    Map.Entry<Integer,Double> entry = top10.poll() ;
					    //System.out.println(entry);
					    
						System.out.println("Document ID: " + Integer.toString( entry.getKey() ) + " Title " +  corpus.getDocument( entry.getKey()       ).getTitle()  );
						System.out.println("with a score of " + String.valueOf(  entry.getValue()   )  );
						
			
					    if(counter == 50){
					        break;
					    }
			
						counter++;
			
					}


					// for(Integer docId: top10.keySet() ){

						
					// 	System.out.println("Document ID: " + Integer.toString(docId ) + " Title " +  corpus.getDocument( docId       ).getTitle()  );
					// 	System.out.println("with a score of " + String.valueOf(  top10.get( docId )   )  );

					// }








				}




				
				System.out.println("Would you like to open a document? Y/N");
							
				String openOption = scan.nextLine();
				if(openOption.equals("Y")){
					System.out.println("Enter the document ID that you would like to see.");
					openOption = scan.nextLine();
					corpus.getDocument(Integer.parseInt(openOption));

					System.out.println();
					System.out.println();

					Reader content =corpus.getDocument(Integer.parseInt(openOption)).getContent() ;
					try {
						int data = content.read();
						while(data != -1 ){
							System.out.print((char)data);
							data = content.read();
						}
						System.out.println();
						System.out.println();
						content.close();
						System.out.println();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		

			System.out.println("--------------------\nPlease enter the type of");
			System.out.println("corpus that will be used.");
			System.out.println("\"json\"\nor");
			System.out.println("\"txt\"");
			String option = scan.nextLine();
			while (true){
				if(option.equals("json")) {
					setCorpusToJson(corpusType);
					break;
				}
				else if (option.equals("txt")){ 
					setCorpusToTxt(corpusType);
					break;
				}
				System.out.print("Please enter a valid option.\njson\ntxt");
				option = scan.nextLine();
				
			}
	
			//small national park
			//C:\Users\lapiz\Documents\CSULB\Fall2022\CECS 429\SmallNationalParkFiles
			//BIG national park
			//C:\Users\lapiz\Documents\CSULB\Fall2022\CECS 429\NationalParkFiles
			//MovieDickFiles
			//C:\Users\lapiz\Documents\CSULB\Fall2022\CECS 429\MovieDickTextFiles
	
			System.out.println("Enter the location of the corpus.");
			directory = scan.nextLine();
		}		
	}
	

	//A way for us to know the type of corpus we will use.
	private static void setCorpusToJson(Byte flag){
		corpusType = 1;
	}

	private static void setCorpusToTxt(Byte flag){
		corpusType = 2;
	}

	private static Index indexCorpusSoundex(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		SoundexIndex soundIndex = new SoundexIndex(vocabulary);
		

		for (Document d : corpus.getDocuments()) {
			//tempTokenResults is always reset for each new document
			ArrayList<String> tempTokenResults = new ArrayList<String>();
			EnglishTokenStream stream =  new EnglishTokenStream(d.getAuthor());
			Integer position = 1;

			for(String token: stream.getTokens()){
				//Convert the token into the hashkey
				token = SoundexIndex.termConverter(token);
				tempTokenResults.add(token);
				for(String newTerm: tempTokenResults){
					soundIndex.addTerm(newTerm, d.getId(), position);
		
				}
				position++;
			}
		}
		return soundIndex;
	}
	
	private static Index indexCorpusjson(DocumentCorpus corpus) {

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
				for(String stemmedTerm: tempTokenResults){

					invertedIndex.addTerm(stemmedTerm, d.getId(), position);
				}
				
				position++;
			}
		}
		//and we return our cool Index B)
		return invertedIndex;

	}


	private static Index indexCorpustxt(DocumentCorpus corpus) {

		DiskIndexWriter diskWriter = new DiskIndexWriter();

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

		double totalTokens = 0.0;
		for (Document d : corpus.getDocuments()) {

			HashMap <String, Integer> termAndtftd = new HashMap<String, Integer>();


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


					//It is okay! Checking for a key in a HashMap is 0(1)
					if(!termAndtftd.containsKey(stemmedTerm)){

						//if the term is not in here, we just add it and now we know the term is here at least once
						termAndtftd.put(stemmedTerm, 1);

					}
					else{

						//if we can see the term tho, we add one to the current value
						//.put replaces the current key, we use termAndtftd.get(stemmedTerm) to get
						//the current count of the term and then we just add one (+1)
						termAndtftd.put(stemmedTerm, termAndtftd.get(stemmedTerm) + 1 );


					}

					invertedIndex.addTerm(stemmedTerm, d.getId(), position);

				}
				
				//position itself already tells us how many tokesn are in the amount of tokens!
				position++;
				totalTokens ++;
				
			}



			//Need to calculate the weight for a document
			double wdt = 0.0;

		

			for(Integer value:termAndtftd.values() ){

				wdt = wdt +  Math.pow(  (  1 + Math.log(value)   )  ,   2);


				
			}

			double Ld = Math.sqrt(wdt);
			

			
			//NEED TO CALCULATE THE AVERAGE

			//the total nummber of tokens / unique toquens... right?
			double average = (double)(position - 1.0) /  termAndtftd.size() ;
			





			//We are using position -1 as docLenD because it is able to tell us the number of tokens
			//in tje document we are looking at.
			diskWriter.writeLds(d.getId(), Ld, (double)(position - 1.0),  Math.sqrt( d.getByteSize() )   , average);


			//now we have to write this guy to disk

		}



		//Hewwoo

		diskWriter.writeDocLenA(   (double)(totalTokens / (double)(corpus.getCorpusSize()))   );



		//and we return our cool Index B)
		return invertedIndex;

	}





}
