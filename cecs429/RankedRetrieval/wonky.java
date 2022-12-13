package cecs429.RankedRetrieval;

import java.security.KeyStore.Entry;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Index;
import cecs429.indexing.Posting;

public class wonky implements   Retrieval {

    @Override
    public PriorityQueue<Map.Entry<Integer, Double>> getTopIdsWithScores(Index index, int corpusSize, ArrayList<String> stemmedQuery) {


        HashMap<Integer, Double> docsScores =  new HashMap<>();

        PriorityQueue<Map.Entry<Integer, Double>> priorityQueue = new PriorityQueue<>(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        //PriorityQueue priorityQueue = new PriorityQueue<>(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for(String term: stemmedQuery){
        


            List<Posting> postings = index.getPostings(term);

            if(postings  == null){

                System.out.println("postings are null");
                continue;

            }
            

            //The weight of the term in the query!
            double wqt = Math.max(   0.0, Math.log(   (corpusSize - postings.size()    )  /  ( postings.size()   )         )           );

            for(Posting p: postings ){

                //The acculator that will help us
                //keep track of each score for each doc
                //Ad
                Double Ad = 0.0;

                //We calculate wdt, which is the weight of
                //the term in the doc

                Double wdt =   (  1 + Math.log(  p.gettftd()    )  )   /  (   1 + Math.log(   index.getLdForDocId(  p.getDocumentId()  , 3)       )         )  ;

                Ad =  wqt * wdt; 

                //we put the documentID and (The old score it has + the new score we just calculated)
                //we use getOrDefault because if we try to get the value of a key that doesn't exist yet
                //we will get null. gotOrDefault allows us to get a 0.0 if the key is not there
                //so it would be like 0.0 + Ad
                docsScores.put( p.getDocumentId() ,  docsScores.getOrDefault(p.getDocumentId(), 0.0) + Ad);

            }

        }

        //At this point we have our hash map with all the docIDs and the values for each doc ID. . .
        //Now we have to go through each value and divide it by Ld

        for(Integer docId: docsScores.keySet() ){



            //we are gonna divide by the weight of the document
            Double ld = index.getLdForDocId(docId, 2);
            

            docsScores.put(docId, docsScores.get(docId) / ld );   

            // Map.Entry<Integer, Double> mapEntry = new AbstractMap.SimpleEntry<>(docId, docsScores.get(docId));

            Map.Entry<Integer, Double> entry =   Map.entry(docId, docsScores.get(docId));

            //System.out.println(docId);

            priorityQueue.add(entry);

        }


        return priorityQueue;

        //priorityQueue.addAll(docsScores);
        


        // HashMap<Integer, Double> top10 = new HashMap<Integer, Double>();


        // int counter = 0;

        // while(!priorityQueue.isEmpty()){

        //     //System.out.println(priorityQueue.poll().getKey());
        //     //System.out.println(priorityQueue.poll().getValue());


            

        //     Map.Entry<Integer,Double> entry = priorityQueue.poll() ;
        //     //System.out.println(entry);
        //     top10.put(entry.getKey(), entry.getValue() ) ;

        //     if(counter == 10){
        //         break;
        //     }


        // }


        // return top10;

        //now all the scores we have are divided by their corresponding lds



    }
    





}
