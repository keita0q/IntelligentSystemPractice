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

	/**
	 * 自然言語の質問文から聞いていることを取り出す<br>
	 * 例:<br>
	 * "nameのslotは何ですか?" -> return "name", slots={slot=?value}<br>
	 * "slotがvalueなものは何?" -> return "?name", slots={slot=value} <br>
	 * "slot1がvalue1で、slot2がvalue2なclassは何?"-> return "?name",
	 * slots={slot1=value1, slot2=value2, is-a=class}
	 *
	 * @param question
	 *            自然言語の質問文
	 * @param slots
	 *            質問内容(slot, value)の組
	 * @return name フレーム名
	 */
	public static String questionAnalysis(String question, Map<String, String> slots) {
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
											slots.put(noun2, "?value");
											return noun1;
										}
										slots.put(noun1, noun2);
										break;
									}
								}
							} else if (particle.equals("は")) {
								// 助詞が「は」のとき
								slots.put("is-a", noun1);
							}
						}
					} else if (particle.equals("は")) {
						// 次の要素がなく、助詞が「は」のとき
						slots.put("is-a", noun1);
					}
				}
			}
		}
		return "?name";
	}

	/**
	 * 求めた結果を自然言語へ変換
	 *
	 * @param name
	 *            questionAnalysisで求めたname
	 * @param slots
	 *            questionAnalysisで求めたslots
	 * @param bindings
	 *            変数束縛情報のリスト
	 * @return
	 */
	public static String toNL(String name, Map<String, String> slots, List<Map<String, String>> bindings) {
		String answer;
		if (name.startsWith("?")) {
			answer = "該当する" + (slots.containsKey("is-a") ? slots.get("is-a") : "もの") + "は";
			if (bindings.isEmpty()) {
				answer += "存在しません。";
				return answer;
			}
			for (Map<String, String> binding : bindings)
				answer += binding.get(name) + "と";
			answer = answer.substring(0, answer.length() - 1) + "です。";
		} else {
			String slot = slots.keySet().iterator().next();
			answer = name + "の" + slot + "は";
			if (bindings.isEmpty()) {
				answer += "存在しません。";
				return answer;
			}
			String value = slots.get(slot);
			answer += bindings.get(0).get(value) + "です。";
		}
		return answer;
	}

}
