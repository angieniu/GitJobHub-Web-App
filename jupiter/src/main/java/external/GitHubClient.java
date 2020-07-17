package external;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class GitHubClient {
	// replace %s 占位符
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	// 默认search position description as developer
	private static final String DEFAULT_KEYWORD = "developer";
	
	public List<Item> search(double lat, double lon, String keyword) {
		// edge case
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
		//keyword in url 特殊字符 encode + & ?+endpoint %
			//UTF-8 unicode 编码方法 英语汉字加密
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//拼接 %s占位符 加输入argument
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		// apache http methods, client 发请求to github api, 收请求from github api
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		//auto
		try {
			//HttpGet requent sent to url // call github api,并且拿到response
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
			if (response.getStatusLine().getStatusCode() != 200) {
				// return null 可拓展 400， 500
				return new ArrayList<>();
			}
			//response里面的entity, 含有content metadata
			HttpEntity entity = response.getEntity();
			//edge case
			if (entity == null) {
				return new ArrayList<>();
			}
			//entity.getContent() return input stream 请求文件太大的话 保护memory, content 用 stream
			// read stream java类 InputStreamReader reader = new InputStreamReader(entity.getContent());
			// 但只能读1个字符，或声明读certain length字符，则1个1个读会读得很慢，或者1次读500个总共1001个，剩1个，不智能，不得不需要判断。引入java 新reader BufferedReader,可以把按行读, stream line by line.
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			// build response body
			StringBuilder responseBody = new StringBuilder();
			//读的每一行的数据
			String line = null;
			// 一直有数据一直读
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			// 把json格式的字符串变成java JSONArray object
			JSONArray array = new JSONArray(responseBody.toString());
			return getItemList(array);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	// clean data 		// batch processing, not read limit 
	private List<Item> getItemList(JSONArray array){
		List<Item> itemList = new ArrayList<>();
	
		// List: dynamically add. list<string> could dynamically add. string[] could not.
	List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title")); // when contents put in title
			} else {
				descriptionList.add(description);
			}	
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitations on request per minute.

		List<List<String>> keywords = MonkeyLearnClient.extractKeywords(descriptionList.toArray(new String[descriptionList.size()]));
		// change from list<String> to String[], as above 
        
		// array refers to batch data.

		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			//List<String> list = keywords.get(i);
			builder.setKeywords(new HashSet<String>(keywords.get(i))); // hashset dedup. list: has order. but for recommendation , not care order, so hashset.


			/* nested
			 * JSONArray temp = object. 
			 */
			
			Item item = builder.build();
			itemList.add(item);
		}
		
		return itemList;
	}
	
//			builder.setAddress(object.getString("address"));
	
	// null to "", avoid npe
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}

}
