import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 文節のリストから成る1文．文節間の係り受け構造を保持するクラス．
 */
public class Sentence extends ArrayList<Chunk> {

	Chunk head;

	static Process caboChaPrc;
	static PrintWriter caboChaOut;
	static BufferedReader caboChaIn;
	static String cabochaCmd = "/opt/cse/bin/cabocha -f1";
	static String encoding = "EUC-JP";

	public Sentence() {
		super();
	}

	public Sentence(List<Chunk> chunks) {
		super(chunks);
		initDependency();
	}

	public void initDependency() {
		Iterator<Chunk> i = this.iterator();
		while (i.hasNext()) {
			Chunk chunk = i.next();
			int dependency = chunk.getDependency();
			if (dependency == -1) {
				head = chunk;
			} else {
				Chunk depChunk = this.get(dependency);
				depChunk.addDependentChunk(chunk);
				chunk.setDependencyChunk(depChunk);
			}
		}
	}

	/**
	 * 主辞の文節を返す
	 *
	 * @return 主辞の文節
	 */
	public Chunk getHeadChunk() {
		return head;
	}

	/**
	 * 指定した品詞・原型の形態素を主辞に持つ文節を探して返す
	 *
	 * @param pos
	 *            探しててる文節の主辞形態素の品詞
	 * @param baseform
	 *            探している文節の主辞形態素の原型
	 * @return 見つかった文節のリスト
	 */
	public List<Chunk> findChunkByHeads(String pos, String baseform) {
		List<Chunk> matches = new ArrayList<Chunk>();
		for (Iterator<Chunk> i = this.iterator(); i.hasNext();) {
			Chunk chunk = i.next();
			Morpheme head = chunk.getHeadMorpheme();
			if (pos.equals(head.getPos()) && baseform.equals(head.getBaseform())) {
				matches.add(chunk);
			}
		}
		return matches;
	}

	/**
	 * 動作主格の文節を返す
	 *
	 * @return 動作主格の文節
	 */
	public Chunk getAgentCaseChunk() {
		// 主辞に係る文節の中からガ格の文節を探す
		Chunk cand = findChunkByHead(head.getDependents(), "助詞", "が");
		if (cand != null)
			return cand;
		// 主辞に係る文節の中からハ格の文節を探す
		cand = findChunkByHead(head.getDependents(), "助詞", "は");
		if (cand != null)
			return cand;
		// 全ての文節の中からガ格の文節を探す
		cand = findChunkByHead(this, "助詞", "が");
		if (cand != null)
			return cand;
		// 全ての文節の中からハ格の文節を探す
		cand = findChunkByHead(this, "助詞", "は");
		return cand;
	}

	/**
	 * 指定した品詞・原型の形態素を主辞に持つ文節を文節リストchunksから探して返す
	 *
	 * @param chunks
	 *            文節リスト
	 * @param pos
	 *            探しててる文節の主辞形態素の品詞
	 * @param baseform
	 *            探している文節の主辞形態素の原型
	 * @return 見つかった文節
	 */
	public Chunk findChunkByHead(List<Chunk> chunks, String pos, String baseform) {
		for (Iterator<Chunk> i = chunks.iterator(); i.hasNext();) {
			Chunk chunk = i.next();
			Morpheme head = chunk.getHeadMorpheme();
			if (pos.equals(head.getPos()) && baseform.equals(head.getBaseform())) {
				return chunk;
			}
		}
		return null;
	}

	/**
	 * この文に含まれる文節間の係り受け構造を表すXML風の文字列を返す
	 *
	 * @return XML風の文字列
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<文 主辞=\"" + head.getId() + "\">\n");
		for (Iterator<Chunk> i = iterator(); i.hasNext();) {
			sb.append(i.next().toString());
			sb.append("\n");
		}
		sb.append("</文>");
		return sb.toString();
	}

	static void startCaboCha() {
		if (caboChaPrc != null) {
			caboChaPrc.destroy();
		}
		try {
			caboChaPrc = Runtime.getRuntime().exec(cabochaCmd);
			caboChaOut = new PrintWriter(new OutputStreamWriter(caboChaPrc.getOutputStream(), encoding));
			caboChaIn = new BufferedReader(new InputStreamReader(caboChaPrc.getInputStream(), encoding));
		} catch (IOException ex) {
			System.err.println("係り受け解析器CaboChaを起動できませんでした");
			System.exit(-1);
		}
	}

	/**
	 * 文に区切るためのセパレータ
	 */
	static List separators = Arrays.asList(new String[] { "。", "！", "!", "？", "?", "．", "\n" });

	/**
	 * 文に区切る
	 *
	 * @param text
	 *            複数の文を含む可能性のあるString
	 * @return 区切られた文（String）のリスト
	 */
	static List<String> splitSentences(String text) {
		List<String> sentences = new ArrayList<String>();
		while (text.length() > 0) {
			int i = -1;
			for (int k = 0; k < separators.size(); k++) {
				String sep = (String) separators.get(k);
				int j = text.indexOf(sep);
				if (j >= 0 && (i < 0 || j < i)) {
					i = j;
				}
			}
			if (i < 0 || i == text.length() - 1) {
				sentences.add(text);
				text = "";
			} else {
				sentences.add(text.substring(0, i + 1));
				text = text.substring(i + 1);
			}
		}
		return sentences;
	}

	/**
	 * 対象テキストを文に分割した上でCaboChaに渡し，解析結果を取得
	 *
	 * @param tweet
	 *            解析対象のテキスト（ツイート）
	 * @return 解析結果（文のリスト）
	 */
	static List<Sentence> parseTweet(String tweet) {
		List<String> sentenceStrs = splitSentences(tweet); // 文に分割
		List<Sentence> sentences = new ArrayList<Sentence>();
		for (String sentenceStr : sentenceStrs) {
			Sentence sentence = parse(sentenceStr); // 1文ずつ解析
			sentences.add(sentence);
		}
		return sentences;
	}

	/**
	 * 文を係り受け解析
	 *
	 * @param sentenceStr
	 *            日本語文の文字列
	 * @return Sentenceオブジェクトのリスト
	 */
	static Sentence parse(String sentenceStr) {
		if (caboChaOut == null) {
			startCaboCha(); // CaboChaが実行されていない場合は実行する
		}
		caboChaOut.println(sentenceStr); // CaboChaに文を渡す
		caboChaOut.flush();

		Sentence sentence = new Sentence(); // 新しいSentenceオブジェクト(中身は空)
		Chunk chunk = null;

		try {
			// CaboChaから解析結果を受け取るfor文．変数lineに1行ずつ代入される
			for (String line = caboChaIn.readLine(); line != null; line = caboChaIn.readLine()) {
				// System.out.println(line); // 解析結果を1行表示
				// forループの中で文節(Chunkオブジェクト)や文(Sentenceオブジェクト)を作っていく
				if (line.equals("EOS")) {
					if (sentence.head == null) {
						sentence.head = chunk;
					}
					break;
				} else if (line.startsWith("*")) {
					chunk = new Chunk();
					sentence.add(chunk);
					String[] tokens = line.split(" ");
					int id = Integer.parseInt(tokens[1]);
					chunk.setId(id);
					if (tokens[2].endsWith("D")) {
						int dep = Integer.parseInt(tokens[2].substring(0, tokens[2].length() - 1));
						chunk.setDependency(dep);
						if (dep == -1) {
							sentence.head = chunk;
						}
					}
				} else {
					Morpheme morph = new Morpheme(line);
					chunk.addMorpheme(morph);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("CaboChaの係り受け解析に失敗しました: 「" + sentenceStr + "」");
		}
		// return null; // この例では解析結果を表示するだけで，nullを返している
		sentence.initDependency(); // 係り受け関係を構築
		return sentence;
	}
}
