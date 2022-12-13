package cecs429.text;

import java.util.ArrayList;

import org.tartarus.snowball.ext.englishStemmer;
/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessorV3 implements TokenProcessor {

	public String processToken(String token) {
		
		String newToken = "";
		String firstChar = "" + token.charAt(0);
		int tokenLength = token.length();
		String lastChar = "" + token.charAt(tokenLength -1);

		//If 1st or last char is non-alpha, loops until all beginning & ending chars are gone
		if (firstChar.matches("\\W") || lastChar.matches("\\W")){

			int firstIndex = 0;
            int lastIndex = 0;
			for (int i=0; i < tokenLength-1; i++){
				String tempChar = "" + token.charAt(i);
				if (tempChar.matches("\\w")){
					firstIndex = i;
                    break;
				}
			}
			for (int i=(tokenLength -1); i >= 0; i--){
                String tempChar = "" + token.charAt(i);
				if (tempChar.matches("\\w")){
					lastIndex = i+1;
                    break;
                }
			}
            newToken = token.substring(firstIndex, lastIndex);
		}
        else {newToken = token;}

		//Remove all apostrophe's & quotation marks
		newToken = newToken.replaceAll("\"", "");
        newToken = newToken.replaceAll("\'", "");

		//Remove hyphens, create multiple tokens if needed
		//Lowercase the token(s)
        englishStemmer stemmer = new englishStemmer();

        stemmer.setCurrent(newToken);
        stemmer.stem();
		return stemmer.getCurrent().toLowerCase();
	}

}
