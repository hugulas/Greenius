package org.greenius;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.svenson.JSONParser;

import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;

public class CloudantConstant {

	private static final int TIME_OUT = 10000;
	private static LuceneAwareCouchDbConnector sdfaDB;
	private static HttpClient authenticatedHttpClient;
	private static CouchDbInstance dbInstance;
	private static LuceneAwareCouchDbConnector stanSdfaDB;
	private static LuceneAwareCouchDbConnector greenDB;


	public static LuceneAwareCouchDbConnector getSdfaDB() {
		return sdfaDB;
	}

	public static LuceneAwareCouchDbConnector getStanSdfaDB() {
		return stanSdfaDB;
	}

	static {
		 init();
//		 initAWS();
	}

	private static void initAWS() {
		try {
			authenticatedHttpClient = new StdHttpClient.Builder().caching(false).url("http://54.169.173.208:5984/database")
					.connectionTimeout(TIME_OUT).socketTimeout(TIME_OUT).username("greenius").password("secret")
					.maxConnections(20).build();
			dbInstance = new StdCouchDbInstance(authenticatedHttpClient);
			sdfaDB = new LuceneAwareCouchDbConnector("database", dbInstance);
			stanSdfaDB = new LuceneAwareCouchDbConnector("sdfa", dbInstance);
			greenDB = new LuceneAwareCouchDbConnector("greendb", dbInstance);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		System.setProperty("org.ektorp.support.AutoUpdateViewOnChange", "true");

		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
		if (VCAP_SERVICES == null) {
			VCAP_SERVICES = "{\"cloudantNoSQLDB\": [{\"name\": \"greenius\",\"label\": \"cloudantNoSQLDB\",\"plan\": \"Shared\",\"credentials\": {\"username\": \"d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix\",\"password\": \"424adfcafc338e69c3b3e06ba96d5cba5ba0ea46cf2e9750351899d302547d1d\",\"host\": \"d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix.cloudant.com\",\"port\": 443, \"url\": \"https://d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix:424adfcafc338e69c3b3e06ba96d5cba5ba0ea46cf2e9750351899d302547d1d@d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix.cloudant.com\"}}]}";
		}
		Map json = JSONParser.defaultJSONParser().parse(Map.class,
				VCAP_SERVICES);
		List list = (List) json.get("cloudantNoSQLDB");
		Map map = (Map) list.get(0);
		Map credentials = (Map) map.get("credentials");
		String url = (String) credentials.get("url");

		try {

			authenticatedHttpClient = new StdHttpClient.Builder()
					.enableSSL(true).caching(false).relaxedSSLSettings(true)
					.url(url).port(443).connectionTimeout(TIME_OUT)
					.socketTimeout(TIME_OUT).maxConnections(20)
					.username((String) credentials.get("username"))
					.password((String) credentials.get("password")).build();
			dbInstance = new StdCouchDbInstance(authenticatedHttpClient);
			sdfaDB = new LuceneAwareCouchDbConnector("database", dbInstance);
			stanSdfaDB = new LuceneAwareCouchDbConnector("sdfa", dbInstance);
			greenDB = new LuceneAwareCouchDbConnector("greendb", dbInstance);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static LuceneAwareCouchDbConnector getGreenDB() {
		return greenDB;
	}

	public static void putNewMap(Map map, Map newMap, String key) {
		newMap.put(key, map.get(key));
	}

	public static Map convertJsonMap(Map map) {
		Map newMap = new HashMap();
		putNewMap(map, newMap, "name");
		putNewMap(map, newMap, "mtype");
		putNewMap(map, newMap, "MId");
		putNewMap(map, newMap, "producer");
		return newMap;
	}

}
