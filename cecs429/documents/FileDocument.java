/**
 * This class extends Document
 * It represents a document built from a single file
 * 
 * Notice this is ALSO an interface
 * 
 */

package cecs429.documents;

import java.nio.file.Path;

/**
 * Represents a document saved as a file on the local file system.
 */
public interface FileDocument extends Document {
	/**
	 * The absolute path to the document's file.
	 */
	Path getFilePath();
}
