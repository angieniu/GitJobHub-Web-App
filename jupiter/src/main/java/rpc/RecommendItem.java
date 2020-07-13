package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import recommendation.Recommendation;

/**
 * Servlet implementation class RecommendItem
 */
public class RecommendItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	// constructor, call parent class constructor
    public RecommendItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response as parameter, void method, reason: 要定义返回的object. 
		// TODO Auto-generated method stub
//		response.setContentType("application/json"); // notifies the client side, response to the client side is json.
//		PrintWriter writer = response.getWriter(); // writer的作用: 把下面想要放得response (line 42-44)放到response里面，write to response for the client side, writer change passed json object (java class) to string (as json format).
		
//		JSONArray array = new JSONArray(); // jsonArray
//		array.put(new JSONObject().put("name", "abcd").put("address", "San Francisco").put("time", "01/01/2017"));
//		array.put(new JSONObject().put("name", "1234").put("address", "San Jose").put("time", "01/01/2017"));
//		writer.print(array); 
		
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		String userId = request.getParameter("user_id");

		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		Recommendation recommendation = new Recommendation();
		// favorite items, relevant items, based on recommendation.
		List<Item> items = recommendation.recommendItems(userId, lat, lon);
		JSONArray array = new JSONArray();
		for (Item item : items) {
			array.put(item.toJSONObject());
		}
		RpcHelper.writeJsonArray(response, array);	

	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
