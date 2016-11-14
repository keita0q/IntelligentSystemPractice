/*
 AIFrameSystem.java
 フレームシステム
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
	void createFrame(AIClassFrame inSuperFrame, String inName,
			boolean inIsInstance) {
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
	public Object readSlotValue(String inFrameName, String inSlotName,
			boolean inDefault) {
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
	public Object readSlotValue(String inFrameName, String inSlotName,
			String inFacetName) {
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
	public void writeSlotValue(String inFrameName, String inSlotName,
			Object inSlotValue) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		frame.writeSlotValue(this, inSlotName, inSlotValue);
	}

	// demon procedure の設定

	/**
	 * setWhenConstructedProc<br>
	 * when-constructed procedure を設定する．
	 */
	public void setWhenConstructedProc(String inFrameName, String inSlotName,
			AIWhenConstructedProc inDemonProc) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		if (frame != null)
			frame.setWhenConstructedProc(inDemonProc);
	}

	public void setWhenConstructedProc(String inFrameName, String inSlotName,
			String inClassName) {
		try {
			AIWhenConstructedProc demonProc = (AIWhenConstructedProc) Class
					.forName(inClassName).newInstance();
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
	public void setWhenRequestedProc(String inFrameName, String inSlotName,
			AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_REQUESTED, inFrameName, inSlotName,
				inDemonProc);
	}

	public void setWhenRequestedProcClass(String inFrameName,
			String inSlotName, String inClassName) {
		setDemonProcClass(AISlot.WHEN_REQUESTED, inFrameName, inSlotName,
				inClassName);
	}

	/**
	 * setWhenReadProc<br>
	 * when-read procedure を設定する．
	 */
	public void setWhenReadProc(String inFrameName, String inSlotName,
			AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_READ, inFrameName, inSlotName, inDemonProc);
	}

	public void setWhenReadProcClass(String inFrameName, String inSlotName,
			String inClassName) {
		setDemonProcClass(AISlot.WHEN_READ, inFrameName, inSlotName,
				inClassName);
	}

	/**
	 * setWhenWrittenProc<br>
	 * when-written procedure を設定する．
	 */
	public void setWhenWrittenProc(String inFrameName, String inSlotName,
			AIDemonProc inDemonProc) {
		setDemonProc(AISlot.WHEN_WRITTEN, inFrameName, inSlotName, inDemonProc);
	}

	public void setWhenWrittenProcClass(String inFrameName, String inSlotName,
			String inClassName) {
		setDemonProcClass(AISlot.WHEN_WRITTEN, inFrameName, inSlotName,
				inClassName);
	}

	/**
	 * setDemonProc<br>
	 * demon procedure を設定する．
	 */
	void setDemonProc(int inType, String inFrameName, String inSlotName,
			AIDemonProc inDemonProc) {
		AIFrame frame = (AIFrame) mFrames.get(inFrameName);
		if (frame != null)
			frame.setDemonProc(inType, inSlotName, inDemonProc);
	}

	/**
	 * setDemonClass<br>
	 * demon procedure を設定する．
	 */
	void setDemonProcClass(int inType, String inFrameName, String inSlotName,
			String inClassName) {
		try {
			AIDemonProc demon = (AIDemonProc) Class.forName(inClassName)
					.newInstance();
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
	 */
	public AIFrame getAIFrame(String name) {
		return mFrames.get(name);
	}

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
						for (String tSuperName : mFrames.get(tFrameName)
								.getSuperNames()) {
							HashMap<String, String> tHashMap = new HashMap<>();
							if (tMather.matching(
									aQuestion.getFrame() + " "
											+ aQuestion.getSlot() + " "
											+ aQuestion.getValue(), tFrameName
											+ " " + tSlotName + " "
											+ tSuperName, tHashMap)) {
								tResuls.add(tHashMap);
							}
						}
					} else {
						HashMap<String, String> tHashMap = new HashMap<>();
						if (tMather.matching(
								aQuestion.getFrame() + " "
										+ aQuestion.getSlot() + " "
										+ aQuestion.getValue(), tFrameName
										+ " " + tSlotName + " "
										+ ((AIFrame) tValue).getName(),
								tHashMap)) {
							tResuls.add(tHashMap);
						}
					}
				} else {
					HashMap<String, String> tHashMap = new HashMap<>();
					if (tMather.matching(
							aQuestion.getFrame() + " " + aQuestion.getSlot()
									+ " " + aQuestion.getValue(), tFrameName
									+ " " + tSlotName + " " + tValue, tHashMap)) {
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
	 */
	public List<Map<String, String>> doQuery(List<Link> tQueries) {
		List<Map<String, String>> tMergeList = new ArrayList<>();
		boolean tFirstFlag = true;
		for (Link tQuery : tQueries) {
			List<Map<String, String>> tBindings = query(tQuery);
			if (tBindings.size() != 0) {
				if (tFirstFlag) {
					tMergeList = tBindings;
					tFirstFlag = false;
				} else {
					tMergeList = join(tMergeList, tBindings);
				}
			} else {
				// 失敗したとき
				return (new ArrayList<>());
			}
		}
		return tMergeList;
	}

	/**
	 * 関係データベースのJOIN演算に相当する(2つの変数束縛情報のリストを結合する)
	 * 
	 * @param array1
	 *            変数束縛情報のリスト
	 * @param array2
	 *            変数束縛情報のリスト
	 * @return 結合された変数束縛情報のリスト
	 */
	public static List<Map<String, String>> join(
			List<Map<String, String>> array1, List<Map<String, String>> array2) {
		// 空のリストを用意
		List<Map<String, String>> merge = new ArrayList<>();
		// それぞれのタプルに対する2重ループ
		for (Map<String, String> hash1 : array1) {
			for (Map<String, String> hash2 : array2) {
				Map<String, String> hash = new HashMap<>(hash1);// hash1をコピー
				boolean add = true;// hashを追加すべきかどうか
				for (String key : hash2.keySet()) {// hash2のキーに対して
					if (hash1.containsKey(key)) {// hash1にそのキーが存在する時
						if (!hash2.get(key).equals(hash1.get(key))) {// 値が同じでないなら
							add = false;// 追加しない
							break;
						}
					} else {// hash1に存在しないキーのとき
						hash.put(key, hash2.get(key));// hashに追加
					}
				}
				if (add) {// 追加すべき時
					merge.add(hash);// 結合されたタプルを追加
				}
			}
		}
		return merge;
	}
} // end of class definition
