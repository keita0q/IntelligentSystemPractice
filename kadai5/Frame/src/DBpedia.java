import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DBpediaを利用して質問に答える
 *
 * @author Yoshida
 */
public class DBpedia {
	public static final int LIMIT = 100;

	/**
	 * sparqlのクエリへ変換
	 *
	 * @param query
	 *            質問のリスト
	 * @return sparqlのクエリ
	 */
	public static String toSparqlQuery(List<Link> query) {
		String sparql = "SELECT DISTINCT * WHERE { ";
		// それぞれのクエリに対して
		for (Link link : query) {
			String frame = link.getFrame(), slot = link.getSlot(), value = link.getValue();
			// DBpediaにis-aやakoは無いのでパス
			if (slot.equals("is-a") || slot.equals("ako"))
				continue;
			String row = "{";
			// フレームが変数なら、そのまま。そうでないとき、URL。
			row += (frame.startsWith("?")) ? frame + " " : "<" + pageRedirects(frame) + "> ";
			// スロットが変数なら、そのまま。そうでないとき、URL。
			row += (slot.startsWith("?")) ? slot + " " : "<http://ja.dbpedia.org/property/" + slot + "> ";
			if (value.startsWith("?") || isNumber(value)) {// スロット値が変数か数字
				row += value + " }";
			} else {// そうでないとき、"value"@jaかURL。
				row = row + "\"" + value + "\"@ja } UNION " + row + "<http://ja.dbpedia.org/resource/" + value + "> }";
			}
			sparql += row;
		}
		sparql += " }LIMIT " + LIMIT;// 上限を設定
		return sparql;
	}

	/**
	 * 文字列が数値かどうか
	 *
	 * @param str
	 *            対象文字列
	 * @return 数値の場合true
	 */
	public static boolean isNumber(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * DBpediaへ問い合わせる
	 *
	 * @param query
	 *            質問のリスト
	 * @return 変数束縛情報のリスト
	 */
	public static List<Map<String, String>> query(List<Link> query) {
		String sparqlQuery = toSparqlQuery(query); // queryからSPARQLクエリへ変換
		String format = "text/csv"; // 受け取る検索結果のフォーマットにcsvを指定
		// SPARQL検索のURL
		String searchURL = "";
		try {
			searchURL = "http://ja.dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fja.dbpedia.org&timeout=0&debug=on"
					+ "&format=" + URLEncoder.encode(format, "UTF-8") + "&query="
					+ URLEncoder.encode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		String result = getWebContent(searchURL, "UTF-8"); // SPARQL検索結果を得る
		result = result.replace("\"", "")// 「"」を削除
				.replace("http://ja.dbpedia.org/resource/", "");// URLがあるとき、それを削除
		return csvToListOfMap(result);// csvをマップのリストに変換
	}

	/**
	 * csv形式の文字列からマップのリストへ変換
	 *
	 * @param csv
	 *            csv形式の文字列
	 * @return マップのリストへ変換
	 */
	public static List<Map<String, String>> csvToListOfMap(String csv) {
		String[] lines = csv.split("\n");// 改行で分割
		String[] vars = lines[0].split(",");// 変数をカンマで分割
		List<Map<String, String>> list = new ArrayList<>();
		for (int i = 1; i < lines.length; ++i) {
			Map<String, String> map = new HashMap<>();
			String[] values = lines[i].split(",");
			for (int j = 0; j < values.length; ++j)
				map.put("?" + vars[j], values[j]);// 変数束縛情報を追加
			list.add(map);
		}
		return list;
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
			// e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * nameからリダイレクトするアドレスを求める<br>
	 * リダイレクト先が無いときは、そのままのアドレスを返す
	 *
	 * @param name
	 *            調べたい名称
	 * @return リダイレクト先のアドレス
	 */
	public static String pageRedirects(String name) {
		// nameからリダイレクトするページのURL
		String sparqlQuery = "SELECT DISTINCT * WHERE {<http://ja.dbpedia.org/resource/" + name
				+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?x. } LIMIT " + LIMIT;
		String format = "text/csv"; // 受け取る検索結果のフォーマットにcsvを指定
		// SPARQL検索のURL
		String searchURL = "";
		try {
			searchURL = "http://ja.dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fja.dbpedia.org&timeout=0&debug=on"
					+ "&format=" + URLEncoder.encode(format, "UTF-8") + "&query="
					+ URLEncoder.encode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		String result = getWebContent(searchURL, "UTF-8");// SPARQL検索結果を得る
		try {
			// 2行目を取得
			String address = result.split("\n")[1].replace("\"", "");
			return (address.isEmpty()) ? "http://ja.dbpedia.org/resource/" + name : address;
		} catch (ArrayIndexOutOfBoundsException e) {
			// 2行目が無い時(リダイレクト先がない時)
			return "http://ja.dbpedia.org/resource/" + name;
		}
	}
}
