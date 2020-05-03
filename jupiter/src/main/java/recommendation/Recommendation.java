package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.MySQLConnection;
import entity.Item;
import external.GitHubClient;

public class Recommendation {
	
	public List<Item> recommendItems(String userId, double lat, double lon){
		List<Item> recommendedItems = new ArrayList<>();
		
		// Step 1, get all favorited itemids
		MySQLConnection connection = new MySQLConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

		// Step 2, get all keywords, sort by count
		// {"software engineer": 6, "backend": 4, "san francisco": 3, "remote": 1}
		Map<String, Integer> allKeywords = new HashMap<>();
		for (String itemId : favoritedItemIds) {
			Set<String> keywords = connection.getKeywords(itemId);
			for (String keyword : keywords) {
				// keyword in the map, get it out, not, get 0 + 1.
				allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
			}
		}
		connection.close();
		
		// collection sort, list is ordered
		// hashmap: we could not directly sort map. we want to get keywords out. number should be sorted. so number needs to be changed to list first. List of Map entry. set each item is entry. hashmap could not be sorted. treemap could be sorted. treemap is ordered. lamda is comparator, comparing e1 and e2. 
		// map: keyword: key; count: value. frequency sort, hashmap => list to be sorted.
		List<Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
		Collections.sort(keywordList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});

		// Cut down search list only top 3
		if (keywordList.size() > 3) {
			keywordList = keywordList.subList(0, 3);
		}
		
		// Step 3, search based on keywords, filter out favorite items
		Set<String> visitedItemIds = new HashSet<>();
		GitHubClient client = new GitHubClient();
		
		for (Entry<String, Integer> keyword : keywordList) {
			// own api
			List<Item> items = client.search(lat, lon, keyword.getKey());

			for (Item item : items) {
				// unvisited add, visited (history table), skip
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}
			}
		}
		return recommendedItems;

		
	}
	

}
