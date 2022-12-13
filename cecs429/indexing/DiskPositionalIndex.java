package cecs429.indexing;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DiskPositionalIndex implements Index {

    String path;


    public DiskPositionalIndex(String path){

        this.path = path;
        

    }


    @Override
    public List<Posting> getPostings(String term) {

        File indexFile =  new File(path);
        Connection conn = null;


        List<Posting> postings = new ArrayList<Posting>();
        //ArrayList<Posting> newPostingsList = new ArrayList<Posting>();

        try {

            conn = DriverManager.getConnection("jdbc:sqlite:termLocations.db");
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFile, "rw");

            int termPosInIndex = getPosition(conn, term);

            if(termPosInIndex < 0) {
                randomAccessFile.close();
                return null;
            }

            randomAccessFile.seek(termPosInIndex);

            //number of docs that have the term
            int dft = randomAccessFile.readInt();

            int prevDocID = 0;

            for(int i =0; i < dft; i++){

                int docID = randomAccessFile.readInt() + prevDocID;
                Posting newPosting = new Posting(docID);
                prevDocID = docID;


                //number of positions in this doc
                //how many times this term shows up in the doc!
                int tftd = randomAccessFile.readInt();

                newPosting.addtftd(tftd);

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
            return null;
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

    @Override
    public List<String> getVocabulary() {
        // TODO Auto-generated method stub
        return null;
    }


    public Double getLdForDocId(int docId, int LdType){


        File docWeightsFile =  new File("storedIndex\\docWeights.bin");


        try {

            RandomAccessFile randomAccessFile= new RandomAccessFile(docWeightsFile, "rw");
            

            if(LdType == 0){

                randomAccessFile.seek(docId * 32);
                //gotta close the RAF somehow, so I have to store the return before closing the file.
                Double returnValue = randomAccessFile.readDouble();

            
                randomAccessFile.close();


                return returnValue;

            }
            else if(LdType == 1){
                //Returns DocLenD

                randomAccessFile.seek(docId * 32 + 8);
                //gotta close the RAF somehow, so I have to store the return before closing the file.
                Double returnValue = randomAccessFile.readDouble();

            
                randomAccessFile.close();


                return returnValue;



            }
            else if(LdType == 2){
                //Returns ByteSize

                randomAccessFile.seek(docId * 32 + 16);
                //gotta close the RAF somehow, so I have to store the return before closing the file.
                Double returnValue = randomAccessFile.readDouble();

            
                randomAccessFile.close();


                return returnValue;



            }
            else if(LdType == 3){
                //Returns AveSize

                randomAccessFile.seek(docId * 32 + 24);
                //gotta close the RAF somehow, so I have to store the return before closing the file.
                Double returnValue = randomAccessFile.readDouble();

            
                randomAccessFile.close();


                return returnValue;

            }
            else if(LdType == 4){
                //Returns docLenA

                //it is the last thing we write because it is a constant!
                randomAccessFile.seek(randomAccessFile.length() - 8);
                //gotta close the RAF somehow, so I have to store the return before closing the file.
                Double returnValue = randomAccessFile.readDouble();

            
                randomAccessFile.close();


                return returnValue;

            }
            else{

                

            }
            
            

            


        } catch (Exception e) {
            // TODO: handle exception
        }


        return 0.0;



    }

    public int getPosition(Connection conn, String term)throws SQLException{

        //System.out.println("checking term " + term);
        //Professor, forgive me as I have forgotten how to sanitize my input
        String selectSQL = "SELECT position from TermPositions WHERE term = \""+ term +"\"";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(selectSQL);

        

        while(rs.next()){
            //System.err.println("position is: " + rs.getInt("position"));
            int r = rs.getInt("position");
            //System.out.println("RESULT: " + r);
            return r;
            // return rs.getInt("position");
        }
        
        return -1;


    }





















    
}
