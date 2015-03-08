package org.greenius;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.svenson.JSON;
import org.svenson.JSONParser;

import com.github.ldriscoll.ektorplucene.LuceneQuery;
import com.github.ldriscoll.ektorplucene.LuceneResult;
import com.github.ldriscoll.ektorplucene.LuceneResult.Row;

/**
 * Servlet implementation class DrugId
 */
@WebServlet("/DrugId")
public class DrugId extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DrugId() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String parameter = request.getParameter("mid");
		PrintWriter writer = response.getWriter();
		LuceneQuery query = new LuceneQuery("_design/Map", "_search/mid");
		
		query.setQuery("mid:"+parameter);
		query.setIncludeDocs(true);
		LuceneResult result = CloudantConstant.getStanSdfaDB().queryLucene(query);
		List<Row> rows = result.getRows();
		
		if (rows.isEmpty()) {
			writer.close();
			return;
		}
		Map map = rows.get(0).getDoc();
		Map newMap = CloudantConstant.convertJsonMap(map);
		String jsonStr = new JSON().forValue(newMap);
		writer.write(jsonStr);
		writer.close();
	}


}
