import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class SFDAScaner {

	@View(name = "all", map = "function(doc) { if (doc.type == 'sdfa' ) emit( null, doc._id )}")
	private class SDFARepository extends CouchDbRepositorySupport<Map> {

		protected SDFARepository(Class<Map> type, CouchDbConnector db) {
			super(type, db);
			initStandardDesignDocument();
		}

		@View(name = "by_URL", map = "function(doc) { if (doc.type == 'sdfa'&&doc.URL ) emit(doc.URL,null)}")
		public boolean containsSDFA(String url) {
			List<Map> r = queryView("by_URL", url);
			return !r.isEmpty();
		}

	}

	public WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);

	public final static String DOMESTIC_URL = "http://app1.sfda.gov.cn/datasearch/face3/base.jsp?tableId=25&tableName=TABLE25&title=%B9%FA%B2%FA%D2%A9%C6%B7&bcId=124356560303886909015737447882";

	public final static String ABORAD_URL = "http://app1.sfda.gov.cn/datasearch/face3/base.jsp?tableId=36&tableName=TABLE36&title=%BD%F8%BF%DA%D2%A9%C6%B7&bcId=124356651564146415214424405468";

	private CouchDbConnector db;

	private static boolean isAbroad = false;

	public static void main(String[] args) throws MalformedURLException {

		SFDAScaner sfdaScaner = new SFDAScaner();
		try {
			sfdaScaner.scanList(7850, 150);
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public SFDAScaner() throws MalformedURLException {
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

		HttpClient authenticatedHttpClient;
		try {

			authenticatedHttpClient = new StdHttpClient.Builder()
					.enableSSL(true)
					.caching(false)
					.relaxedSSLSettings(true)
					.url("https://d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix.cloudant.com")
					.port(443)
					.username("d077c084-3f69-4a86-bd69-b27af2b2fb97-bluemix")
					.password(
							"424adfcafc338e69c3b3e06ba96d5cba5ba0ea46cf2e9750351899d302547d1d")
					.build();
			CouchDbInstance dbInstance = new StdCouchDbInstance(
					authenticatedHttpClient);
			db = new StdCouchDbConnector("database", dbInstance);

			db.createDatabaseIfNotExists();
			sdfaRepository = new SDFARepository(Map.class, db);
		} catch (MalformedURLException e) {
			throw e;
		}

	}

	private int currentPage = 1;

	private int totalPage = -1;

	private SDFARepository sdfaRepository;

	private int startPage;

	public void scanList(int pageNumber, int pagecount) {

		HtmlPage page = null;

		boolean needJump = true;
		currentPage = pageNumber;
		startPage = pageNumber;
		do {
			try {
				if (needJump) {
					page = jumpToPage(currentPage, page);
				}
				if (page != null) {
					handlePage(page);
					needJump = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				needJump = true;
			}
		} while (currentPage != totalPage
				&& currentPage < startPage + pagecount);

	}

	private HtmlPage jumpToPage(int pageNumber, HtmlPage page)
			throws IOException {
		String url = isAbroad ? ABORAD_URL : DOMESTIC_URL;
		if (page == null) {
			page = webClient.getPage(url);
		}
		if (pageNumber > 1) {
			
			((HtmlTextInput) page.getElementById("goInt")).setText(Integer
					.toString(pageNumber));
			DomNodeList<DomElement> elementsByTagName = page
					.getElementsByTagName("input");
			for (DomElement domElement : elementsByTagName) {
				if (domElement.getAttribute("src") != null
						&& domElement.getAttribute("src").indexOf(
								"dataanniu_11.gif") != -1) {
					page = ((HtmlInput) domElement).click();
				}
			}
		}
		return page;
	}

	private void handlePage(HtmlPage page) throws IOException {

		DomNodeList<DomElement> elementsByTagName = page
				.getElementsByTagName("td");
		for (DomElement domElement : elementsByTagName) {
			String asText = domElement.asText();

			if (asText.startsWith("第") && asText.endsWith("条")) {
				int start = asText.indexOf("第");
				int end = asText.indexOf("页");
				currentPage = Integer.parseInt(asText.substring(start + 1, end)
						.trim());
				int start2 = asText.indexOf("共");
				int end2 = asText.indexOf("页", start2);
				totalPage = Integer.parseInt(asText.substring(start2 + 1, end2)
						.trim());
				System.out.println(asText);
				visitGoodList(page);
				FileWriter writer = new FileWriter(startPage + ".txt");
				writer.write("" + currentPage);
				writer.close();
				if (currentPage == totalPage) {
					return;
				} else {
					DomNodeList<DomElement> elementsByTagName2 = page
							.getElementsByTagName("img");
					boolean isNext = false;
					for (DomElement domElement2 : elementsByTagName2) {
						HtmlImage htmlImage = (HtmlImage) domElement2;

						if (htmlImage.getSrcAttribute().indexOf(
								"dataanniu_07.gif") != -1) {
							isNext = true;

							HtmlPage nextPage = (HtmlPage) htmlImage.click();
						}
					}
					if (!isNext) {
						System.out.println(page.asXml());
					}
				}
				break;
			}

		}

	}

	public void visitGoodList(HtmlPage page)
			throws FailingHttpStatusCodeException, IOException {
		List<HtmlAnchor> anchors = page.getAnchors();

		for (HtmlAnchor htmlAnchor : anchors) {
			String hrefAttribute = htmlAnchor.getHrefAttribute();
			int urlIndex = hrefAttribute.indexOf("content.jsp");
			if (urlIndex != -1) {
				int endUrl1 = hrefAttribute.indexOf("\"", urlIndex);
				int endUrl2 = hrefAttribute.indexOf("'", urlIndex);
				int endUrl = -1;
				if (endUrl1 == -1) {
					endUrl = endUrl2;
				} else if (endUrl2 == -1) {
					endUrl = endUrl1;
				} else {
					endUrl = Math.min(endUrl1, endUrl2);
				}
				String url = hrefAttribute.substring(urlIndex, endUrl);
				url = "http://app1.sfda.gov.cn/datasearch/face3/" + url;

				if (!sdfaRepository.containsSDFA(url)) {
					HtmlPage enclosedPage = (HtmlPage) webClient.openWindow(
							new URL(url), "none").getEnclosedPage();
					DomNodeList<DomElement> elementsByTagName = enclosedPage
							.getElementsByTagName("tr");
					Map map = new TreeMap();
					for (DomElement domElement : elementsByTagName) {
						DomNodeList<HtmlElement> childNodes = domElement
								.getElementsByTagName("td");
						if (childNodes.size() == 2) {

							String title = childNodes.get(0).asText();
							if (title != null && !"".equals(title.trim())
									&& !title.trim().equals("注")
									&& !title.trim().equals("相关数据库查询")) {
								String value = childNodes.get(1).asText()
										.trim();
								map.put(title, value);
							}

						}
					}
					map.put("URL", url);
					map.put("ABOARD", new Boolean(isAbroad));
					map.put("type", "sdfa");
					System.out.println("Create document:" + url);
					db.create(map);
				}

			}
		}
		webClient.closeAllWindows();

	}

}
