/**
 * Implements FileDocument
 * Meaning that it will need to do all the things a FileDocument can do.
 * Since FileDocument extends Document, we also will need to be able to do all the
 * things Document can do. (They can't DO anything, but we will do it.)
 * 
 * It loads all the full contents of a simple text fila as the contents of the document
 * 
 * Notice this IS NOT and Interface. This is a class.
 * 
 */

package cecs429.documents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple text file in the local file system.
 */
public class TextFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;
	
	/**
	 * Constructs a TextFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public TextFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
	}
	
	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	@Override
	public int getId() {
		return mDocumentId;
	}
	
	public double getByteSize(){
		return (double)(mFilePath.toFile().length());
	}



	@Override
	public Reader getContent() {
		try {
			return Files.newBufferedReader(mFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getTitle() {
		return mFilePath.getFileName().toString();
	}
	@Override
	public Reader getAuthor() {
		try {
			return Files.newBufferedReader(mFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public String getAuthorNames() {
		return mFilePath.getFileName().toString();
	}
	public static FileDocument loadTextFileDocument(Path absolutePath, int documentId) {
		return new TextFileDocument(documentId, absolutePath);
	}

	@Override
	public String getDocumentName() {

		return new File(mFilePath.toString()).getName();
	}
}
