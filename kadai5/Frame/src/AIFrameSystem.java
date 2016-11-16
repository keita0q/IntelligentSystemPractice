
/*
 AIFrameSystem.java
 フレームシステム
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AIFrameSystem {

	final static String sTopFrameName = "top_level_frame";

	// すべてのフレームを格納するための辞書．
	// フレーム名をインデックスとして利用．
	private HashMap<String, AIFrame> mFrames = new HashMap<String, AIFrame>();

	/**
	 * AIFrameSystem <br>
	 * コンストラクタ
	 */
	public AIFrameSystem() {
		mFrames.put(sTopFrameName, new AIClassFrame(this, null, sTopFrameName));
	}

	/**
	 * createClassFrame<br>
	 * クラスフレーム inName を作成する．
	 *
	 * @param inName
	 *            クラスフレーム名
	 */
	public void createClassFrame(String inName) {
		createFrame(sTopFrameName, inName, false);
	}

	/**
	 * createClassFrame<br>
	 * スーパーフレームとして inSuperName を持つクラスフレーム inName を作成する．
	 *
	 * @param inSuperName
	 *            スーパーフレームのフレーム名
	 * @param inName
	 *            フレーム名
	 */
	public void createClassFrame(String inSuperName, String inName) {
		createFrame(inSuperName, inName, false);
	}

	/**
	 * createInstanceFrame <br>
	 * スーパーフレームとして inSuperName を持つインスタンスフレーム inName を作成する．
	 *
	 * @param inSuperName
	 *            スーパーフレームのフレーム名
	 * @param inName
	 *            フレーム名
	 */
	public void createInstanceFrame(String inSuperName, String inName) {
		createFrame(inSuperName, inName, true);
	}

	/**
	 * createFrame<br>
	 * フレームを作成する
	 *
	 * @param inSuperName
	 *            スーパーフレームのフレーム名
	 *
	 * @param inName
	 *            フレーム名
	 *
	 * @param inIsInstance
	 *            インスタンスフレームなら true
	 */
	void createFrame(String inSuperName, String inName, boolean inIsInstance) {
		AIClassFrame frame;
		try {
			frame = (AIClassFrame) mFrames.get(inSuperName);
			createFrame(frame, inName, inIsInstance);
		} catch (Throwable err) {
		}
	}

	/**
	 * createFrame <br>
	 * フレームを作成する
	 *
	 * @param inSuperName
	 *            スーパーフレーム
	 *
	 * @param inName
	 *            フレーム名
	 *
	 * @param inIsInstance
	 *            インスタンスフレームなら true
	 */
	void createFrame(AIClassFrame inSuperFrame, String inName, boolean inIsInstance) {
		AIFrame frame;
		if (inIsInstance == true) {
			frame = new AIInstanceFrame(this, inSuperFrame, inName);
		} else {
			frame = new AIClassFrame(this, inSuperFrame, inName);
		}
		mFrames.put(inName, frame);
	}

	/**
	 * readSlotValue <br>
	 * スロット値を返す
	 *
	 * @param inFrameName
	 *            フレーム名
	 * @param inSlotName
	 *            スロット名
	 * @param inDefault
	 *            デフォルト値を優先したいなら true
	 */
	public Object readSlotValue(String inFrameName, String inSlotName, boolean inDefault) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		return frame.readSlotValue(this, inSlotName, inDefault);
	}

	/**
	 * readSlotValue<br>
	 * スロット値を返す
	 *
	 * @param inFrameName
	 *            フレーム名
	 * @param inSlotName
	 *            スロット名
	 */
	public Object readSlotValue(String inFrameName, String inSlotName) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		return frame.readSlotValue(this, inSlotName, false);
	}

	/**
	 * readSlotValue<br>
	 * スロット値を返す
	 *
	 * @param inFrameName
	 *            フレーム名
	 * @param inSlotName
	 *            スロット名
	 * @param inFacetName
	 *            ファセット名
	 */
	public Object readSlotValue(String inFrameName, String inSlotName, String inFacetName) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		return frame.readSlotValue(this, inSlotName, false);
	}

	/**
	 * writeSlotValue<br>
	 * スロット値を設定する．
	 *
	 * @param inFrameName
	 *            フレーム名
	 * @param inSlotName
	 *            スロット名
	 * @param inSlotValue
	 *            スロット値
	 */
	public void writeSlotValue(String inFrameName, String inSlotName, Object inSlotValue) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		frame.writeSlotValue(this, inSlotName, inSlotValue);
	}

	// demon procedure の設定

	/**
	 * setWhenConstructedProc<br>
	 * when-constructed procedure を設定する．
	 */
	public void setWhenConstructedProc(String inFrameName, String inSlotName, AIWhenConstructedProc inDemonProc) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		if (frame != null)
			frame.setWhenConstructedProc(inDemonProc);
	}

	public void setWhenConstructedProc(String inFrameName, String inSlotName, String inClassName) {
		try {
			AIWhenConstructedProc demonProc = (AIWhenConstructedProc) Class.forName(inClassName).newInstance();
			AIFrame frame = (AIFrame) mFrames.get(inFrameName);
			if (frame != null)
				frame.setWhenConstructedProc(demonProc);
		} catch (Exception err) {
			System.out.println(err);
		}
	}

	/**
	 * setWhenRequestedProc<br>
	 * when-requested procedure を設定する．
	 */
	public void setWhenRequestedProc(String inFrameName, String inSlotName, AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_REQUESTED, inFrameName, inSlotName, inDemonProc);
	}

	public void setWhenRequestedProcClass(String inFrameName, String inSlotName, String inClassName) {
		setDemonProcClass(AISlot.WHEN_REQUESTED, inFrameName, inSlotName, inClassName);
	}

	/**
	 * setWhenReadProc<br>
	 * when-read procedure を設定する．
	 */
	public void setWhenReadProc(String inFrameName, String inSlotName, AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_READ, inFrameName, inSlotName, inDemonProc);
	}

	public void setWhenReadProcClass(String inFrameName, String inSlotName, String inClassName) {
		setDemonProcClass(AISlot.WHEN_READ, inFrameName, inSlotName, inClassName);
	}

	/**
	 * setWhenWrittenProc<br>
	 * when-written procedure を設定する．
	 */
	public void setWhenWrittenProc(String inFrameName, String inSlotName, AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_WRITTEN, inFrameName, inSlotName, inDemonProc);
	}

	public void setWhenWrittenProcClass(String inFrameName, String inSlotName, String inClassName) {
		setDemonProcClass(AISlot.WHEN_WRITTEN, inFrameName, inSlotName, inClassName);
	}

	/**
	 * setDemonProc<br>
	 * demon procedure を設定する．
	 */
	void setDemonProc(int inType, String inFrameName, String inSlotName, AIDemonProc inDemonProc) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		if (frame != null)
			frame.setDemonProc(inType, inSlotName, inDemonProc);
	}

	/**
	 * setDemonClass<br>
	 * demon procedure を設定する．
	 */
	void setDemonProcClass(int inType, String inFrameName, String inSlotName, String inClassName) {
		try {
			AIDemonProc demon = (AIDemonProc) Class.forName(inClassName).newInstance();
			setDemonProc(inType, inFrameName, inSlotName, demon);
		} catch (Exception err) {
			System.out.println(err);
		}
	}

	/**
	 * 名前に対するフレームを取得
	 *
	 * @param name
	 *            フレーム名
	 * @return フレーム
	 * @author Yoshida
	 */
	public AIFrame getAIFrame(String name) {
		return mFrames.get(name);
	}

	/**
	 * 単体の質問をする
	 *
	 * @param aQuestion
	 *            単体の質問
	 * @return 変数束縛情報のリスト
	 * @author Nishi
	 */
	public List<Map<String, String>> query(Link aQuestion) {
		List<Map<String, String>> tResuls = new ArrayList<>();
		Matcher tMather = new Matcher();
		// フレームで回す
		for (String tFrameName : mFrames.keySet()) {
			// スロットで回す
			for (String tSlotName : mFrames.get(tFrameName).getSlotNames(true)) {
				Object tValue = readSlotValue(tFrameName, tSlotName);
				if (tValue instanceof AIFrame) {
					if (tSlotName.equals("is-a") || tSlotName.equals("ako")) {
						for (String tSuperName : mFrames.get(tFrameName).getSuperNames()) {
							HashMap<String, String> tHashMap = new HashMap<>();
							if (tMather.matching(
									aQuestion.getFrame() + " " + aQuestion.getSlot() + " " + aQuestion.getValue(),
									tFrameName + " " + tSlotName + " " + tSuperName, tHashMap)) {
								tResuls.add(tHashMap);
							}
						}
					} else {
						HashMap<String, String> tHashMap = new HashMap<>();
						if (tMather.matching(
								aQuestion.getFrame() + " " + aQuestion.getSlot() + " " + aQuestion.getValue(),
								tFrameName + " " + tSlotName + " " + ((AIFrame) tValue).getName(), tHashMap)) {
							tResuls.add(tHashMap);
						}
					}
				} else {
					HashMap<String, String> tHashMap = new HashMap<>();
					if (tMather.matching(aQuestion.getFrame() + " " + aQuestion.getSlot() + " " + aQuestion.getValue(),
							tFrameName + " " + tSlotName + " " + tValue, tHashMap)) {
						tResuls.add(tHashMap);
					}
				}
			}
		}
		return tResuls;
	}

	/**
	 * 質問をする
	 *
	 * @param tQueries
	 *            質問のリスト
	 * @return 変数束縛情報のリスト(解のリスト)
	 * @author Nishi
	 */
	public List<Map<String, String>> doQuery(List<Link> tQueries) {
		List<List<Map<String, String>>> bindingsList = new ArrayList<>();
		for (Link tQuery : tQueries) {
			List<Map<String, String>> bindings = query(tQuery);
			if (bindings.size() != 0) {
				bindingsList.add(bindings);
			} else {
				// 失敗したとき
				return (new ArrayList<>());
			}
		}
		return join(bindingsList);
	}

	/**
	 * 変数束縛情報のリストを全て結合する
	 *
	 * @param theBindingsList
	 *            変数束縛情報のリストのリスト
	 * @return 結合後の変数束縛情報のリスト
	 */
	public List<Map<String, String>> join(List<List<Map<String, String>>> theBindingsList) {
		int size = theBindingsList.size();
		switch (size) {
		case 0:
			// 失敗している時？
			break;
		case 1:
			return theBindingsList.get(0);
		case 2:
			List<Map<String, String>> bindings1 = theBindingsList.get(0);
			List<Map<String, String>> bindings2 = theBindingsList.get(1);
			return joinBindings(bindings1, bindings2);
		default:
			bindings1 = theBindingsList.get(0);
			theBindingsList.remove(bindings1);
			bindings2 = join(theBindingsList);
			return joinBindings(bindings1, bindings2);
		}
		// ダミー
		return null;
	}

	/**
	 * 変数束縛情報のリストを結合する
	 *
	 * @param theBindings1
	 *            変数束縛情報のリスト1
	 * @param theBindings2
	 *            変数束縛情報のリスト2
	 * @return 結合後の変数束縛情報のリスト
	 */
	public List<Map<String, String>> joinBindings(List<Map<String, String>> theBindings1,
			List<Map<String, String>> theBindings2) {
		List<Map<String, String>> resultBindings = new ArrayList<>();
		for (int i = 0; i < theBindings1.size(); i++) {
			Map<String, String> theBinding1 = theBindings1.get(i);
			for (int j = 0; j < theBindings2.size(); j++) {
				Map<String, String> theBinding2 = theBindings2.get(j);
				Map<String, String> resultBinding = joinBinding(theBinding1, theBinding2);
				if (resultBinding.size() != 0) {
					resultBindings.add(resultBinding);
				}
			}
		}
		return resultBindings;
	}

	/**
	 * 変数束縛情報を結合する
	 *
	 * @param theBinding1
	 *            変数束縛情報1
	 * @param theBinding2
	 *            変数束縛情報2
	 * @return 結合後の変数束縛情報
	 */
	public Map<String, String> joinBinding(Map<String, String> theBinding1, Map<String, String> theBinding2) {
		Map<String, String> resultBinding = new HashMap<>();
		// System.out.println(theBinding1.toString() + "<->" +
		// theBinding2.toString());
		// theBinding1 の key & value をすべてコピー
		for (Iterator<String> e = theBinding1.keySet().iterator(); e.hasNext();) {
			String key = (String) e.next();
			String value = (String) theBinding1.get(key);
			resultBinding.put(key, value);
		}
		// theBinding2 の key & value を入れて行く，競合があったら失敗
		for (Iterator<String> e = theBinding2.keySet().iterator(); e.hasNext();) {
			String key = (String) e.next();
			String value2 = (String) theBinding2.get(key);
			if (resultBinding.containsKey(key)) {
				String value1 = (String) resultBinding.get(key);
				// System.out.println("=>"+value1 + "<->" + value2);
				if (!value2.equals(value1)) {
					resultBinding.clear();
					break;
				}
			}
			resultBinding.put(key, value2);
		}
		return resultBinding;
	}

} // end of class definition
