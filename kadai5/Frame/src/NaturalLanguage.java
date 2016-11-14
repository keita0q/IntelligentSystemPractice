import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 自然言語処理に関連するクラス
 *
 * @author Yoshida
 *
 */
public class NaturalLanguage {
	public static final String NAME = "?name";
	public static final String VALUE = "?value";

	/**
	 * 自然言語の質問文から聞いていることを取り出す<br>
	 * 例:<br>
	 * "nameのslotは何ですか?" -> return [(name, slot, ?value)]<br>
	 * "slotがvalueなものは何?" -> return [(?name, slot, value)] <br>
	 * "slot1がvalue1で、slot2がvalue2なclassは何?" -> return <br>
	 * [(?name, slot1, value1), (?name, slot2, value2), (?name, is-a, class)]
	 *
	 * @param question
	 *            自然言語の質問文
	 * @return 質問のリスト
	 */
	public static List<Link> questionAnalysis(String question) {
		List<Link> query = new ArrayList<>();
		// 形態素解析
		List<Morpheme> morphemes = Morpheme.analyzeMorpheme(question);
		for (Iterator<Morpheme> it = morphemes.iterator(); it.hasNext();) {
			// 形態素
			Morpheme morph = it.next();
			// 形態素が名詞で代名詞でない
			if (morph.isNoun() && !morph.isPronoun()) {
				// 連続した名詞を繋げる
				String noun1 = morph.getSurface();
				for (; it.hasNext();) {
					morph = it.next();
					if (morph.isNoun() && !morph.isPronoun()) {
						noun1 += morph.getSurface();
					} else {
						break;
					}
				}
				// 形態素が助詞
				if (morph.isParticle()) {
					String particle = morph.getSurface();
					// 次の要素があれば
					if (it.hasNext()) {
						morph = it.next();
						// 形態素が名詞
						if (morph.isNoun()) {
							// 形態素が代名詞でない
							if (!morph.isPronoun()) {
								// 連続した名詞を繋げる
								String noun2 = morph.getSurface();
								for (; it.hasNext();) {
									morph = it.next();
									if (morph.isNoun() && !morph.isPronoun()) {
										noun2 += morph.getSurface();
									} else {
										// 助詞が「の」のとき
										if (particle.equals("の")) {
											query.add(new Link(noun1, noun2, VALUE));
											return query;
										}
										query.add(new Link(NAME, noun1, noun2));
										break;
									}
								}
							} else if (particle.equals("は")) {
								// 助詞が「は」のとき
								query.add(new Link(NAME, "is-a", noun1));
							}
						} else if (morph.isMark() && particle.equals("は")) {
							// 助詞が「は」のとき
							query.add(new Link(NAME, "is-a", noun1));
						}
					} else if (particle.equals("は")) {
						// 次の要素がなく、助詞が「は」のとき
						query.add(new Link(NAME, "is-a", noun1));
					}
				}
			}
		}
		return query;
	}

	/**
	 * 求めた結果を自然言語へ変換(questionAnalysisからの結果を想定)
	 *
	 * @param bindings
	 *            変数束縛情報のリスト
	 * @return 自然言語の応答文
	 */
	public static String toNL(List<Link> query, List<Map<String, String>> bindings) {
		if (bindings.isEmpty())
			return "そのようなものは存在しません。";
		String answer = "";
		if (query.get(0).getValue().equals(VALUE)) {
			// VALUEが聞かれているとき
			answer = query.get(0).getFrame() + "の" + query.get(0).getSlot() + "は";
			for (Map<String, String> binding : bindings)
				answer += binding.get(VALUE) + "と";
			answer = answer.substring(0, answer.length() - 1) + "です。";
		} else {
			// NAMEが聞かれているとき
			String isa = "もの";
			for (Link link : query) {
				if (link.getSlot().equals("is-a")) {
					// is-aを記録
					isa = link.getValue();
					continue;
				}
				answer += link.getSlot() + "が" + link.getValue() + "で、";
			}
			if (!answer.isEmpty())
				answer = (answer.substring(0, answer.length() - 1) + "ある");
			answer += isa + "は";
			for (Map<String, String> binding : bindings)
				answer += binding.get(NAME) + "と";
			answer = answer.substring(0, answer.length() - 1) + "です。";
		}
		return answer;
	}
}
