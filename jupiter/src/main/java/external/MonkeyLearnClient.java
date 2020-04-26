
package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;

public class MonkeyLearnClient {
	private static final String API_KEY = "5c6e28a0941a83797090d0c87da6c70ec4e4969f";// make sure change it to your api key.
       public static void main(String[] args) { // test, no need to be called externally, but could be used as test.

           String[] data = {
           "Amazon has a new openning in game development and distributed systems", // jd 1
           "Google has many openning in software development", // jd 2
           "Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look."// jd 3
           };
           List<List<String>> words = extractKeywords(data);
           for (List<String> ws : words) {
        	   for (String w : ws) {
        		   System.out.println(w);
        	   }
        	   System.out.println();
           }
	}

    //This function is used to connected to other codes in this project, coz main function could not be used called in this project. should form a static function and could be used in other places. but if private, then could not be called. 
       // Also, the reason we use static is because we do not need to record things related to the object, we can just process when data by batch come, no need to initiated an  instance. When calling, I can just use MonkeyLearnClient.extractKeywords, no need to new.
       // utility function, call external api, we would usually use public static. 
       // List<List<String>>, List -- String[]  20 jd, external list size = 20; List<String> each description's keyword, size = 0 1 2 3.
	//jsonarray pass data, but internal process data, better to use list<list<string>>: jsonarray --> parse--> keyword--> list<list<String>>. conversion. 
       public static List<List<String>> extractKeywords(String[] text) {
		// corner case
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}

		// Use the API key from your account
		MonkeyLearn ml = new MonkeyLearn(API_KEY);

        String modelId = "ex_YCya9nrn";
		// Use the keyword extractor, and parameters in api documentation + original source code. 
		ExtraParam[] extraParams = { new ExtraParam ("max_keywords", "3") };
		MonkeyLearnResponse response;
		// possible exception: web connection broken, model problem  
		try {
			// click extractors to check function definition
			response = ml.extractors.extract(modelId, text, extraParams);
			// Why return JSONArray? coz data could be batch processing, coz String[] i.e., each jd as one string, string, string ... as array, several array together could be batch processed . each result has three keywords.  
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {// it’s likely to have an exception
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
   
	// change result JSONarray to list<List<String>>, helper function, only in this class, so private is ok. 
	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		List<List<String>> topKeywords = new ArrayList<>();
		// Iterate the result array and convert it to our format.
		// i represents 20 job descriptions, batch processing
		for (int i = 0; i < mlResultArray.size(); ++i) { // external iteration, 20 jd, iterate each
			// inside each jd, kw
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i); // cast 
			// every job description has j keywords
			for (int j = 0; j < keywordsArray.size(); j++) {
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j); // cast from object to jsonobject   // check as jsonobject format. also to use debug mode or print mode to know it's jsonarray.
				// We just need the keyword, excluding other fields.
				String keyword = (String) keywordObject.get("keyword"); //jasonarray api. // cast //also "keyword", when we print keywordsArray it;s name is called "keyword". so java library, changes the n to "keyword". but restAPI is called parsevalue.
				keywords.add(keyword);

			}
			topKeywords.add(keywords);
		}
		return topKeywords;
	}
}
