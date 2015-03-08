package org.greenius;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.svenson.JSON;

import com.github.ldriscoll.ektorplucene.LuceneQuery;
import com.github.ldriscoll.ektorplucene.LuceneResult;
import com.github.ldriscoll.ektorplucene.LuceneResult.Row;

/**
 * Servlet implementation class MedicineInfoServlet
 */
@WebServlet("/Name")
public class MedicineNameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MedicineNameServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String name = (String) request.getParameter("name");
		PrintWriter writer = response.getWriter();
		LuceneQuery query = new LuceneQuery("_design/Map", "_search/name");
		
		query.setQuery("name:"+name);
		query.setIncludeDocs(true);
		LuceneResult result = CloudantConstant.getStanSdfaDB().queryLucene(query);
		List<Map> docList = new ArrayList<Map>();
		List<Row> rows = result.getRows();
		for (Row row : rows) {
			LinkedHashMap<String, Object> map = row.getDoc();
			Map newMap = CloudantConstant.convertJsonMap(map);
			docList.add(newMap);
			
		}
		String jsonStr = new JSON().forValue(docList);
		writer.write(jsonStr);
		writer.close();
	
	}
}
