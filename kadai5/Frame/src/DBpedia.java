import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * DBpediaを利用して質問に答える
 *
 * @author Yoshida
 *
 */
public class DBpedia {
	public static void main(String[] args) {
		HashMap<String, String> hash = new HashMap<>();
		hash.put("郵便番号", "?x");
		hash.put("都道府県名", "?y");
		System.out.println(query("東京都", hash));

		HashMap<String, String> map = new HashMap<>();
		String name = questionAnalysis("身長が160で、体重が52な人は誰。", map);
		System.out.println(name);
		System.out.println(map);
	}

	public static String toSparqlQuery(String name, HashMap<String, String> propertys) {
		String sparql = "SELECT * WHERE { ";
		if (name.startsWith("?")) {
			sparql += name + " ";
		} else {
			sparql += "<" + pageRedirects(name) + "> ";
		}
		for (String property : propertys.keySet()) {
			String value = propertys.get(property);
			if (property.startsWith("?")) {
				sparql += property + " ";
			} else {
				sparql += "<http://ja.dbpedia.org/property/" + property + "> ";
			}
			if (value.startsWith("?") || isNumber(value)) {
				sparql += value;
			} else {
				sparql += "\"" + value + "\"";
				if (isJapanese(value))
					sparql += "@ja";
			}
			sparql += " ;";
		}
		sparql = sparql.substring(0, sparql.length() - 1) + ". }LIMIT 100";
		return sparql;
	}

	public static boolean isJapanese(String str) {
		return (str.getBytes().length != str.length());
	}

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
	 * @param name
	 *            フレーム名(変数でもよい)
	 * @param propertys
	 *            スロット名とその値(変数を含んでよい)
	 * @return 変数束縛情報のリスト
	 */
	public static List<HashMap<String, String>> query(String name, HashMap<String, String> propertys) {
		String sparqlQuery = toSparqlQuery(name, propertys); // name、propertysからSPARQLクエリへ変換
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

		return csvToListOfHash(result);
	}

	public static List<HashMap<String, String>> csvToListOfHash(String csv) {
		String[] lines = csv.split("\n");
		String[] vars = lines[0].split(",");
		List<HashMap<String, String>> list = new ArrayList<>();
		for (int i = 1; i < lines.length; ++i) {
			HashMap<String, String> hash = new HashMap<>();
			String[] values = lines[i].split(",");
			for (int j = 0; j < values.length; ++j) {
				hash.put("?" + vars[j], values[j]);
			}
			list.add(hash);
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
			e.printStackTrace();
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
		String sparqlQuery = "SELECT * WHERE {<http://ja.dbpedia.org/resource/" + name
				+ "> <http://dbpedia.org/ontology/wikiPageRedirects> ?x. } LIMIT 10";
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
			String address = result.split("\n")[1].replace("\"", "");
			return (address.isEmpty()) ? "http://ja.dbpedia.org/resource/" + name : address;
		} catch (ArrayIndexOutOfBoundsException e) {
			return "http://ja.dbpedia.org/resource/" + name;
		}
	}

	/**
	 * 自然言語の質問文から聞いていることを取り出す<br>
	 * 例:<br>
	 * "nameのpropertyは何ですか?" ---> return "name", propertys={property=?value}<br>
	 * "propertyがvalueなものは何?" ---> return "?name", propertys={property=value}
	 * <br>
	 * "property1がvalue1で、property2がvalue2なものは何?"---> return "?name",
	 * propertys={property1=value1, property2=value2}<br>
	 *
	 * @param question
	 *            自然言語の質問文
	 * @param propertys
	 *            質問内容(property, value)の組
	 * @return name フレーム名
	 */
	public static String questionAnalysis(String question, HashMap<String, String> propertys) {
		List<Morpheme> morphemes = Morpheme.analyzeMorpheme(question);
		for (Iterator<Morpheme> it = morphemes.iterator(); it.hasNext();) {
			Morpheme morph = it.next();
			if (morph.isNoun() && !morph.isPronoun()) {
				String noun1 = morph.getSurface();
				for (; it.hasNext();) {
					morph = it.next();
					if (morph.isNoun() && !morph.isPronoun()) {
						noun1 += morph.getSurface();
					} else {
						break;
					}
				}
				if (morph.isParticle() && it.hasNext()) {
					String particle = morph.getSurface();
					morph = it.next();
					if (morph.isNoun() && !morph.isPronoun()) {
						String noun2 = morph.getSurface();
						for (; it.hasNext();) {
							morph = it.next();
							if (morph.isNoun() && !morph.isPronoun()) {
								noun2 += morph.getSurface();
							} else {
								if (particle.equals("の")) {
									propertys.put(noun2, "?value");
									return noun1;
								}
								propertys.put(noun1, noun2);
								break;
							}
						}
					}
				}
			}
		}
		return "?name";
	}
}
