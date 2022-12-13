package cecs429.indexing;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DiskIndexWriter {
    

/**
 * Remember the pattern
 * 
 * dft, id_d1, tft_d, pt0, pt1, pt2 . . . id_d2, tft_d 
 *  
 * where
 * 
 * dft is the number of documents containing term t
 * id is the id of the document containing that term
 * tft_d is the number of times the term occurs in the document
 * pt is the position of the term in d
 * 
 */


    int  lastWeightPositionWritten;
    boolean firstIteration = true;



    public void DiskIndexWriter(){

        
    }

    /** 
     * Takes an existing, initialized in-memory Positional Inverted Index and writes it to disk given a path.
     * 
     * @param invertedIndex
     * @param path
     * @throws IOException
     */


    public void writeDocLenA(double docLenA ){

        File docWeightsFile =  new File("storedIndex\\docWeights.bin");

        try {

            //conn = DriverManager.getConnection("jdbc:sqlite:idWeights.db");
        

            

            RandomAccessFile randomAccessFile = new RandomAccessFile(docWeightsFile, "rw");


            //insertWeightPosition(conn, docId, lastWeightPositionWritten);

            randomAccessFile.seek(lastWeightPositionWritten);



            randomAccessFile.writeDouble(docLenA);

            lastWeightPositionWritten += 8; 

           
            //randomAccessFile.seek(0);

            //System.out.println(randomAccessFile.readDouble());
            
            //displayWeightDatabase(conn);


            
            randomAccessFile.close();

            
        }
        catch(Exception e){


        }




    }

    
    public void writeLds(int docId, double Ld, double docLenD, double byteSize, double aveSize){


        //Connection conn = null;

        //storedIndex\docWeights.bin
        File docWeightsFile =  new File("storedIndex\\docWeights.bin");

        try {

            //conn = DriverManager.getConnection("jdbc:sqlite:idWeights.db");
            

            if(firstIteration){

                firstIteration = false;
                lastWeightPositionWritten = 0;
                
                try {
                    //deleteWeightTable(conn);
                } catch (Exception ignored) {
                    
                }

                //createWeightTable(conn);

            }

            

            RandomAccessFile randomAccessFile = new RandomAccessFile(docWeightsFile, "rw");


            //insertWeightPosition(conn, docId, lastWeightPositionWritten);

            randomAccessFile.seek(lastWeightPositionWritten);



            randomAccessFile.writeDouble(Ld);
            lastWeightPositionWritten += 8; 

            randomAccessFile.writeDouble(docLenD);
            lastWeightPositionWritten += 8; 

            randomAccessFile.writeDouble(byteSize);
            lastWeightPositionWritten += 8; 

            randomAccessFile.writeDouble(aveSize);
            lastWeightPositionWritten += 8;

            


           
            //randomAccessFile.seek(0);

            //System.out.println(randomAccessFile.readDouble());
            
            //displayWeightDatabase(conn);


            
            randomAccessFile.close();




        } catch (Exception e) {

            System.out.println(e);
            // TODO: handle exception


        }
        finally{

            
            // if(conn != null){

            //     try {

            //         conn.close();
                    
            //     } catch (Exception e) {
            //         // TODO: handle exception
            //     }


            // }

        }



    }

    public void writeIndex(Index positionalInvertedIndex, String path) {

        //in case we are passed a null index
        if(positionalInvertedIndex == null){
            System.out.println("Could not write index to disk. Null inverted index passed.");
            return;
        }


        Connection conn = null;

        //needed to create a random access file. It contains the location of postings.bin
        File indexFile =  new File(path);

        try {

            //Creating the connection to store where the beginning of each word starts
            conn = DriverManager.getConnection("jdbc:sqlite:termLocations.db");

            
            try {
                deleteTable(conn);
            } catch (Exception ignored) {
                
            }
            
            createTable(conn);
        
            //Creates the random access file. The two parameters are the indexFile  (postings.bin)
            //and "rw", the mode of the file which is se to read write
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "rw");

            //In order to get the postings, we need the vocabulary.
            List<String> vocab = positionalInvertedIndex.getVocabulary();

            int wordStart = 0;
            
            //We will iterate through every terem in the vocabulary
            for(String term : vocab){

                insertPosition(conn, term, wordStart);

                //System.out.println(term);
                //get the number of documents containting the term
                int dft =  positionalInvertedIndex.getPostings(term).size();
                //write the number of documents containing the term to disk
                randomAccessFile.writeInt(dft);
                //System.out.println(wordStart);
                wordStart += 4;

                //and start writing the doc ID with gaps to disk!
                //for the first Doc ID number, we are gonna write the actual ID
                //so prev gap (which is the thing that will subtract to create the gaps)
                // is seet to 0
                int prevID = 0;

                //we start to iterate through the postings that contain the IDs.
                for(Posting posting : positionalInvertedIndex.getPostings(term)){

                    //we write down the gap that will take us to the actual ID of the Doc
                    randomAccessFile.writeInt(posting.getDocumentId() - prevID);
                    wordStart += 4;
                    //we set the previous gap to whatever we just wrote on the disk
                    prevID = posting.getDocumentId();

                    //We just wrote down the "Document ID"(but in gap form). Now we have to
                    //write down the positions!
                    //now we write down the number of positions that this document has
                    //(The size of the positions array for this posting)
                    randomAccessFile.writeInt(posting.getPositions().size());
                    wordStart += 4;


                    int prevPosition = 0;

                    //we iterate through the positions
                    for(int position : posting.getPositions()){

                        //write them down as gaps
                        randomAccessFile.writeInt(position - prevPosition);
                        wordStart += 4;
                        //and update the previous position
                        prevPosition = position;

                    }

                    //so there we go! After this loop is done, we wil move on to the next posting! until
                    //we got all the postings done!
                   
                }

            }
            //AAAAND we just wrote down our index to disk ;)

            

            //System.out.println(displayDatabase(conn));

            //randomAccessFile.seek(0);
            //System.out.println(randomAccessFile.readInt());

            

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
            //System.out.println("Term: " + rs.getString("term") +", @"  );
            //System.out.println(rs.getInt("position") );

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
            //System.out.println("Term: " + rs.getString("term") +", @"  );
            //System.out.println(rs.getInt("position") );
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


}
