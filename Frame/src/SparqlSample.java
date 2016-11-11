import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SparqlSample {

	public static void main(String args[]) {
		String dbpediaBaseUrl = "http://ja.dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fja.dbpedia.org&timeout=0&debug=on";
		String format = "text/csv";
		// formatには，"text/csv" の他にも以下のような形式を指定可能
		// "text/tsv"
		// "application/rdf+xml"
		// "application/sparql-results+json"
		// "text/html"

		// 人物（foaf:Personクラス）のインスタンスを取得するSPARQLの例
		String sparql = "SELECT * WHERE { " + "  ?person rdf:type foaf:Person;" + " ?p ?o."
				+ "} LIMIT 1000";

		String url = dbpediaBaseUrl + "&format=" + URLEncoder.encode(format) + "&query=" + URLEncoder.encode(sparql);

		String result = getWebContent(url, "UTF-8");
		System.out.println(result);
	}

	/**
	 * 与えられたURLからHTML等のコンテンツを取得し，返す．
	 *
	 * @param url
	 *            取得するコンテンツのURL
	 * @param enc
	 *            コンテンツの文字コード（UTF-8やEUC-JP, Shift_JISなど）
	 * @return コンテンツ
	 */
	public static String getWebContent(String url, String enc) {
		StringBuffer sb = new StringBuffer();
		try {
			URLConnection conn = new URL(url).openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), enc));
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
