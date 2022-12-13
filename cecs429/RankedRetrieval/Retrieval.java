
package cecs429.RankedRetrieval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import cecs429.indexing.*;

public interface Retrieval {


    PriorityQueue<Map.Entry<Integer, Double>>  getTopIdsWithScores(Index index, int corpusSize, ArrayList<String> stemmedQuery );


    
}
