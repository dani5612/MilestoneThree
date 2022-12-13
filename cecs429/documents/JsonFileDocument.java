package cecs429.documents;

//Gson!
import com.google.gson.Gson;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;




public class JsonFileDocument implements FileDocument {

    //we create the stuff needed 
    private int mDocumentId;
    private Path mFilePath;
    Gson gson = new Gson();

    public JsonFileDocument(int id, Path absoluteFilePath){
        mDocumentId = id;
        mFilePath = absoluteFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }


    public double getByteSize(){
		return (double)(mFilePath.toFile().length());
	}

    @Override
    public Reader getContent() {

        try{

            Reader rdr = Files.newBufferedReader(mFilePath);

            Map<?, ?> map = gson.fromJson(rdr, Map.class);

            return new StringReader(map.get("body").toString());


        }
        catch (IOException e){

            throw new RuntimeException(e);
            
        }
        


    }


    public String getDocumentName(){



        return new File(mFilePath.toString()).getName();
        
    }

    @Override
    public String getTitle() {

        try{

            Reader rdr = Files.newBufferedReader(mFilePath);

            Map<?, ?> map = gson.fromJson(rdr, Map.class);

            return map.get("title").toString();


        }
        catch (IOException e){

            throw new RuntimeException(e);
            
        }

    }
    @Override
    public Reader getAuthor() {

        try{

            Reader rdr = Files.newBufferedReader(mFilePath);

            Map<?, ?> map = gson.fromJson(rdr, Map.class);

            return new StringReader(map.get("author").toString());


        }
        catch (IOException e){

            throw new RuntimeException(e);
            
        }

    }
    @Override
    public String getAuthorNames() {

        try{

            Reader rdr = Files.newBufferedReader(mFilePath);

            Map<?, ?> map = gson.fromJson(rdr, Map.class);

            return map.get("author").toString();


        }
        catch (IOException e){

            throw new RuntimeException(e);
            
        }

    }
    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
    
}
