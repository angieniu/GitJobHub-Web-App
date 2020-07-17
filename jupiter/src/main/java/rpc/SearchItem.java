package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;
import external.GitHubClient;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// protect endpoint
		// if no session, reject request, so (false). but (true), no session create session. 403 Forbidden error, no authenticated, no login data.
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		String userId = request.getParameter("user_id");
		//http request lat, lon
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		GitHubClient client = new GitHubClient();
		List<Item> items = client.search(lat, lon, null);
		
		MySQLConnection connection = new MySQLConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		connection.close();
		
		JSONArray array = new JSONArray();
		for(Item item: items) {
			JSONObject obj = item.toJSONObject();
			obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array); // no need to create an instance to use RpcHelper function, coz static function. so util class and helper class usually use static.
		}
// line 69, 70, 80 每个servlet都用，decouple通用复用的逻辑，直接调用某method. rpcHelper
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//static
		// JSONObject obj = new JSONObject();
//		obj.put("username", "abcd");
		// writer.print(obj);
		// dynamic
//		if (request.getParameter("username") != null) {
//			JSONObject obj = new JSONObject();
//			String username = request.getParameter("username");
//			obj.put("username", username);
//			writer.print(obj);
//		}}
//		
//		JSONArray array = new JSONArray();
//		array.put(new JSONObject().put("username", "abcd"));
//		array.put(new JSONObject().put("username", "1234"));
//		writer.print(array);}
//		RpcHelper.writeJsonArray(response, array);

		// command + left button--> source code
//		response.getWriter().append("Served at: ").append(request.getContextPath());
	/*
	 * shown as [{"username": "abcd"}, {"username": "1234"}]
	 */
//	RpcHelper.writeJsonArray(response, array);
	/* PrintWriter writer = response.getWriter();
	 * if (request.getParameter("username" != null){
	 * 		JSONObject obj = new JSONObject();
	 * 		String username = request.getParameter("username"); // 动态获取 get input from url localhost:8080/jupiter/search?username=abc, so 从url读出来user name, put into response get abc
	 * 		obj.put("username", username); //放到response
	 * 		writer.print(obj); // so 从url读出来user name, put into response get abc
	 * }
	 */

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
