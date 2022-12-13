package cecs429.text;

import java.util.ArrayList;

import org.tartarus.snowball.ext.englishStemmer;
/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessorV2 implements TokenProcessor {

	public ArrayList<String> processTokenList(String token) {
		
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
		ArrayList<String> tokensList = new ArrayList<String>();
        if(newToken.contains("-")){
            //First Mod Token
            String firstModToken = newToken.replaceAll("-", "").toLowerCase();
            tokensList.add(firstModToken);
            //Second+ Mod Token
            String tokenSplit[] = newToken.split("-");   
            for(String s : tokenSplit) {
                tokensList.add(s.toLowerCase());
            }
        }
        else{
            tokensList.add(newToken.toLowerCase());
        }
		//Stem the tokens
        englishStemmer stemmer = new englishStemmer();
        for(int i=0; i < tokensList.size(); i++){
            stemmer.setCurrent(tokensList.get(i));
            stemmer.stem();
            String stemmedToken = stemmer.getCurrent();
            tokensList.set(i, stemmedToken);
        }
		return tokensList;
	}

	@Override
	public String processToken(String token) {
		// TODO Auto-generated method stub
		return null;
	}
}
