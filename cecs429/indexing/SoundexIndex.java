package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SoundexIndex implements Index {
     //This index maps from soundex hash keys to document IDs
     //NOTE: the hashkey conversions are done in the TermDocumentIndexer class, NOT in here
     private HashMap <String, ArrayList<Posting> > SoundexIndex;
     private List <String> vocabulary;
 
     public SoundexIndex (Collection<String> inputVocabulary){
         
        SoundexIndex = new HashMap< String, ArrayList<Posting> > ();
         this.vocabulary = new ArrayList<String>();
         this.vocabulary.addAll(inputVocabulary);
         Collections.sort(vocabulary);

     }
     
    public HashMap <String, ArrayList<Posting> > getSoundexIndex(){

        return this.SoundexIndex;
    }
    public void addTerm(String term, int docID, Integer Position){

        if(!SoundexIndex.containsKey(term)){
            vocabulary.add(term);
            ArrayList<Posting> newPostingsList = new ArrayList<Posting>();  
            newPostingsList.add(new Posting(docID, Position));
            SoundexIndex.put(term, newPostingsList );

        } 
        else if (SoundexIndex.containsKey(term)) {
            if (SoundexIndex.get(term).get(SoundexIndex.get(term).size() - 1).getDocumentId() == docID){
                
                SoundexIndex.get(term).get(SoundexIndex.get(term).size() - 1).addPosition(Position);
            } 
            else if (SoundexIndex.get(term).get(SoundexIndex.get(term).size() - 1).getDocumentId() != docID){
                
                SoundexIndex.get(term).add(new Posting(docID, Position));
            } 

        }


    }
     
     //****************************** Original getPostings ******************************
     
     @Override
     public List<Posting> getPostings(String term) {
         if(SoundexIndex.containsKey(term)){
             return SoundexIndex.get(term);
         }
         else {
             return null;
         }
     }

     //****************************** Disk Soundex Writer ******************************
     /*
     //NOTE: Copied over from DiskIndexWriter
     //NOTE: Original comments erased, new comements note changes or new lines for this
     // particular index for the soundex
     int  lastWeightPositionWritten;
     boolean firstIteration = true;
     public void DiskIndexWriter(){
        //Empty
    }
    public void writeLd(int docId, double Ld){
        Connection conn = null;
        //NOTE: Changed bin name to docSoundexWeights.bin
        //storedIndex\docWeights.bin
        File docWeightsFile =  new File("storedIndex\\docSoundexWeights.bin");
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:idWeights.db");
            if(firstIteration){
                firstIteration = false;
                lastWeightPositionWritten = 0;
                try {
                    deleteWeightTable(conn);
                } catch (Exception ignored) {
                }
                createWeightTable(conn);
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(docWeightsFile, "rw");
            insertWeightPosition(conn, docId, lastWeightPositionWritten);
            randomAccessFile.seek(lastWeightPositionWritten);
            randomAccessFile.writeDouble(Ld);
            lastWeightPositionWritten += 8; 
            randomAccessFile.seek(0);
            System.out.println(randomAccessFile.readDouble()); 
            displayWeightDatabase(conn);
            randomAccessFile.close();
        } catch (Exception e) {
            System.out.println(e);
            // TODO: handle exception
        }
        finally{
            if(conn != null){
                try {
                    conn.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }
    public void writeIndex(Index positionalInvertedIndex, String path) {
        if(positionalInvertedIndex == null){
            System.out.println("Could not write index to disk. Null inverted index passed.");
            return;
        }
        Connection conn = null;
        File indexFile =  new File(path);
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:termLocations.db");
            try {
                deleteTable(conn);
            } catch (Exception ignored) { 
            }
            createTable(conn);
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "rw");
            List<String> vocab = positionalInvertedIndex.getVocabulary();
            int wordStart = 0;
            for(String term : vocab){
                insertPosition(conn, term, wordStart);
                int dft =  positionalInvertedIndex.getPostings(term).size();
                randomAccessFile.writeInt(dft);
                wordStart += 4;
                int prevID = 0;
                for(Posting posting : positionalInvertedIndex.getPostings(term)){
                    randomAccessFile.writeInt(posting.getDocumentId() - prevID);
                    wordStart += 4;
                    prevID = posting.getDocumentId();
                    randomAccessFile.writeInt(posting.getPositions().size());
                    wordStart += 4;
                    int prevPosition = 0;
                    for(int position : posting.getPositions()){
                        randomAccessFile.writeInt(position - prevPosition);
                        wordStart += 4;
                        prevPosition = position;
                    }
                }
            }
            randomAccessFile.close();         
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(conn != null){
                try {
                    conn.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }
    public int displayDatabase(Connection conn)throws SQLException{
        String selectSQL = "SELECT position from TermPositions WHERE term = \"bunni\" ";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);
        while(rs.next()){
            return rs.getInt("position");
        }
        return 0;
    }
    public int displayWeightDatabase(Connection conn)throws SQLException{
        String selectSQL = "SELECT * from WeightPositions where docid = 1" ;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);
        while(rs.next()){
            System.out.println("hello from inside the loop");
            System.out.println("doc ID" + rs.getInt("docid") ) ;
            System.out.println("at position " + rs.getInt("position") );
            return rs.getInt("position");
        }
        System.out.println("hello from down here");
        return 0;
    }
    public void createTable(Connection conn) throws SQLException{
        String createTableSQL = "CREATE TABLE TermPositions ( term varchar(50), position integer )";
        Statement stmt = conn.createStatement();
        stmt.execute(createTableSQL);
    }
    public void createWeightTable(Connection conn) throws SQLException{
        String createTableSQL = "CREATE TABLE WeightPositions ( docid integer, position integer )";
        Statement stmt = conn.createStatement();
        stmt.execute(createTableSQL);
    }
    private void insertPosition(Connection conn, String term, int position)throws SQLException{
        String insertSQL = "INSERT INTO TermPositions(term, position) VALUES(?,?)";
        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setString(1, term);
        pstmt.setInt(2, position);
        pstmt.executeUpdate();
    }
    private void insertWeightPosition(Connection conn, int documentId, int position)throws SQLException{
        String insertSQL = "INSERT INTO WeightPositions(docid, position) VALUES(?,?)";
        PreparedStatement pstmt = conn.prepareStatement(insertSQL);
        pstmt.setInt(1, documentId);
        pstmt.setInt(2, position);
        pstmt.executeUpdate();
    }
    private void deleteTable(Connection conn) throws SQLException{
        String delteTableSQL = "DROP TABLE TermPositions";
        Statement stmt = conn.createStatement();
        stmt.execute(delteTableSQL);
    }
    private void deleteWeightTable(Connection conn) throws SQLException{
        String delteTableSQL2 = "DROP TABLE WeightPositions";
        Statement stmt = conn.createStatement();
        stmt.execute(delteTableSQL2);
    }
    
     //****************************** DiskPositionalIndex ******************************
     String path;
     //Changed the name to match this class
     public SoundexIndex(String path){
        this.path = path;
    }
    @Override
    public List<Posting> getPostings(String term) {
        File indexFile =  new File(path);
        Connection conn = null;
        List<Posting> postings = new ArrayList<Posting>();
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:termLocations.db");
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "rw");
            int termPosInIndex = getPosition(conn, term);
            randomAccessFile.seek(termPosInIndex);
            int dft = randomAccessFile.readInt();
            int prevDocID = 0;
            for(int i =0; i < dft; i++){
                int docID = randomAccessFile.readInt() + prevDocID;
                Posting newPosting = new Posting(docID);
                prevDocID = docID;
                int tftd = randomAccessFile.readInt();
                int prevPosition = 0;
                for(int y = 0; y < tftd ; y++){
                    int position = randomAccessFile.readInt() + prevPosition;
                    newPosting.addPosition(position);
                    prevPosition = position;
                }
                postings.add(newPosting);
            }
            randomAccessFile.close();
        } catch (Exception e) { 

        }
        finally{
            if(conn != null){
                try {
                    conn.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        return postings;
    }
    public int getPosition(Connection conn, String term)throws SQLException{
        String selectSQL = "SELECT position from TermPositions WHERE term = \""+ term +"\"";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);
        while(rs.next()){
            return rs.getInt("position");
        }
        return 0;
    }
*/
     //****************************** TermConverter ******************************
     public static String termConverter(String term){
        //1.Turn every term to be indexed into a 4-character reduced form. Build an
        //inverted index from these reduced forms to the original terms; call this
        //the soundex index.
        //2. Do the same with query terms.
        //3. When the query calls for a soundex match, search this soundex index.
        //Lowercase & hold on to original term
        String originalTerm = term.toUpperCase();

        //Create an array to convert the changes, this will not be the returned result
        char[] charHolder = originalTerm.toCharArray();
        
        //For converting the term...
        //1. Retain the first letter of the term
        // newTerm will be the returned
        String newTerm = "" + originalTerm.charAt(0);
        charHolder[0] = originalTerm.charAt(0);
        //2.Change all occurrences of the following letters to ’0’ (zero): 
        //’A’, E’, ’I’, ’O’,’U’, ’H’, ’W’, ’Y’
        for(int i=1; i < originalTerm.length(); i++){
            //String idk = "" + charHolder[i];
            //System.out.println("charHolder[" + i + "] = " + idk);
            if (charHolder[i] == 'A' || charHolder[i] == 'E' || charHolder[i] == 'I' || charHolder[i] == 'O' || charHolder[i] == 'U' ||
            charHolder[i] == 'H' || charHolder[i] == 'W' || charHolder[i] == 'Y'){
                charHolder[i] = '0';
            }
        }
        //3. (1) = B, F, P, V
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'B' || charHolder[i] == 'F' || charHolder[i] == 'P' || charHolder[i] == 'V' ){
                charHolder[i] = '1';
            }
        }
        //4. (2) = C, G, J, K, Q, S, X, Z
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'C' || charHolder[i] == 'G' || charHolder[i] == 'J' || charHolder[i] == 'K' || charHolder[i] == 'Q' ||
            charHolder[i] == 'S' || charHolder[i] == 'X' || charHolder[i] == 'Z'){
                charHolder[i] = '2';
            }
        }
        //5. (3) = D,T
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'D' || charHolder[i] == 'T'){
                charHolder[i] = '3';
            }
        }
        //6. (4) = L
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'L'){
                charHolder[i] = '4';
            }
        }
        //7. (5) = M, N
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'M' || charHolder[i] == 'N' ){
                charHolder[i] = '5';
            }
        }
        //8. (6) = R
        for(int i=1; i<originalTerm.length(); i++){
            if (charHolder[i] == 'R'){
                charHolder[i] = '6';
            }
        }
        //Example: Hermann --> H065055
        //9. Remove one out of each pair of consecutive identical digits
        //10. Remove all 0s
        for(int i=1; i<charHolder.length; i++){
            //Example: 55 --> 5
            //If it is not a '0' and does not match the previus Char, it gets added.
            if(charHolder[i] != charHolder[i-1] && charHolder[i] != '0'){
                newTerm += charHolder[i];
            }
        }
        //11. Pad resulting string with trailing 0s, this is in case the reduced term does not have enough numbers.
        newTerm += "0000";
        //12. ONLY take the first 4 chars now for the new term string.
        //Remember, the first letter was kept originally from the original term.
        newTerm = newTerm.substring(0,4);
        return (newTerm);
    }
    //****************************** getVocabulary ******************************
    @Override
    public List<String> getVocabulary() {
        // Not needed
        return Collections.unmodifiableList(vocabulary);
    }
    //****************************** getLdForDocId ******************************
    @Override
    public Double getLdForDocId(int docId, int LdType) {
        // TODO Auto-generated method stub
        return null;
    }
 }
 