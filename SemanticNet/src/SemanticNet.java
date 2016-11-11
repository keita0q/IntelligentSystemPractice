import java.util.*;

/***
 * 意味ネットワーク (Semantic Net)
 * 
 */
public class SemanticNet {
	ArrayList<Link> links;
	ArrayList<Node> nodes;
	HashMap<String, Node> nodesNameTable;

	SemanticNet() {
		links = new ArrayList<Link>();
		nodes = new ArrayList<Node>();
		nodesNameTable = new HashMap<String, Node>();
	}

	/**
	 * 質問の表示と解の表示
	 * 
	 * @param theQueries
	 *            質問のリスト
	 */
	public void query(ArrayList<Link> theQueries) {
		System.out.println("*** Query ***");
		for (int i = 0; i < theQueries.size(); i++) {
			System.out.println(((Link) theQueries.get(i)).toString());
		}
		System.out.println((doQuery(theQueries)).toString());
	}

	/**
	 * 質問をする
	 * 
	 * @param theQueries
	 *            質問のリスト
	 * @return 変数束縛情報のリスト(解のリスト)
	 */
	public ArrayList doQuery(ArrayList theQueries) {
		ArrayList bindingsList = new ArrayList();
		for (int i = 0; i < theQueries.size(); i++) {
			Link theQuery = (Link) theQueries.get(i);
			ArrayList bindings = queryLink(theQuery);
			if (bindings.size() != 0) {
				bindingsList.add(bindings);
			} else {
				// 失敗したとき
				return (new ArrayList());
			}
		}
		return join(bindingsList);
	}

	/**
	 * 単体の質問をする
	 * 
	 * @param theQuery
	 *            質問
	 * @return 変数束縛情報
	 */
	public ArrayList queryLink(Link theQuery) {
		ArrayList bindings = new ArrayList();
		for (int i = 0; i < links.size(); i++) {
			Link theLink = (Link) links.get(i);
			HashMap<String, String> binding = new HashMap<String, String>();
			String theQueryString = theQuery.getFullName();
			String theLinkString = theLink.getFullName();
			if ((new Matcher())
					.matching(theQueryString, theLinkString, binding)) {
				bindings.add(binding);
			}
		}
		return bindings;
	}

	/**
	 * 変数束縛情報のリストを全て結合する
	 * 
	 * @param theBindingsList
	 *            変数束縛情報のリスト
	 * @return 結合後の変数束縛情報のリスト
	 */
	public ArrayList join(ArrayList theBindingsList) {
		int size = theBindingsList.size();
		switch (size) {
		case 0:
			// 失敗している時？
			break;
		case 1:
			return (ArrayList) theBindingsList.get(0);
		case 2:
			ArrayList bindings1 = (ArrayList) theBindingsList.get(0);
			ArrayList bindings2 = (ArrayList) theBindingsList.get(1);
			return joinBindings(bindings1, bindings2);
		default:
			bindings1 = (ArrayList) theBindingsList.get(0);
			theBindingsList.remove(bindings1);
			bindings2 = join(theBindingsList);
			return joinBindings(bindings1, bindings2);
		}
		// ダミー
		return (ArrayList) null;
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
	public ArrayList joinBindings(ArrayList theBindings1, ArrayList theBindings2) {
		ArrayList resultBindings = new ArrayList();
		for (int i = 0; i < theBindings1.size(); i++) {
			HashMap<String, String> theBinding1 = (HashMap) theBindings1.get(i);
			for (int j = 0; j < theBindings2.size(); j++) {
				HashMap<String, String> theBinding2 = (HashMap) theBindings2
						.get(j);
				HashMap<String, String> resultBinding = joinBinding(
						theBinding1, theBinding2);
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
	public HashMap<String, String> joinBinding(
			HashMap<String, String> theBinding1,
			HashMap<String, String> theBinding2) {
		HashMap<String, String> resultBinding = new HashMap<String, String>();
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

	/**
	 * セマンティックネットにリンクを加える<br>
	 * 例: Ito =is-a=> NIT-student:<br>
	 * tail : Ito, head : NIT-student, label: is-a.
	 * 
	 * @param theLink
	 *            加えるリンク
	 */
	public void addLink(Link theLink) {
		Node tail = theLink.getTail();
		Node head = theLink.getHead();
		links.add(theLink);

		// 性質の継承
		if ("is-a".equals(theLink.getLabel())) {
			// head のすべてのリンクを is-a をたどってすべてのノードに継承．
			ArrayList<Node> tmp = new ArrayList<Node>();
			tmp.add(tail);
			recursiveInheritance(head.getDepartFromMeLinks(), tmp);
		}
		// theLink を is-a をたどってすべてのノードに継承させる
		ArrayList<Link> tmp = new ArrayList<Link>();
		tmp.add(theLink);
		recursiveInheritance(tmp, tail.getISATails());

		// 関係を head と tail に登録．
		head.addArriveAtMeLinks(theLink);
		tail.addDepartFromMeLinks(theLink);
	}

	/**
	 * 再帰的に継承を行う
	 * 
	 * @param theInheritLinks
	 *            継承すべきリンク
	 * @param theInheritNodes
	 *            継承すべきリンクを継承するノード
	 */
	public void recursiveInheritance(ArrayList<Link> theInheritLinks,
			ArrayList<Node> theInheritNodes) {
		for (int i = 0; i < theInheritNodes.size(); i++) {
			Node theNode = (Node) theInheritNodes.get(i);
			// theNode 自体にリンクを継承．
			for (int j = 0; j < theInheritLinks.size(); j++) {
				// theNode を tail にしたリンクを生成
				Link theLink = (Link) theInheritLinks.get(j);
				Link newLink = new Link(theLink.getLabel(), theNode.getName(),
						(theLink.getHead()).getName(), this);
				newLink.setInheritance(true);
				links.add(newLink);
				theNode.addDepartFromMeLinks(newLink);
			}
			// theNode から is-a でたどれるノードにリンクを継承
			ArrayList<Node> isaTails = theNode.getISATails();
			if (isaTails.size() != 0) {
				recursiveInheritance(theInheritLinks, isaTails);
			}
		}
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public HashMap<String, Node> getNodesNameTable() {
		return nodesNameTable;
	}

	public void printLinks() {
		System.out.println("*** Links ***");
		for (int i = 0; i < links.size(); i++) {
			System.out.println(((Link) links.get(i)).toString());
		}
	}

	public void printNodes() {
		System.out.println("*** Nodes ***");
		for (int i = 0; i < nodes.size(); i++) {
			System.out.println(((Node) nodes.get(i)).toString());
		}
	}
}

/**
 * マッチングを行うクラス
 */
class Matcher {
	StringTokenizer st1;
	StringTokenizer st2;
	HashMap<String, String> vars;

	Matcher() {
		vars = new HashMap<String, String>();
	}

	public boolean matching(String string1, String string2,
			HashMap<String, String> bindings) {
		this.vars = bindings;
		if (matching(string1, string2)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean matching(String string1, String string2) {
		// System.out.println(string1);
		// System.out.println(string2);

		// 同じなら成功
		if (string1.equals(string2))
			return true;

		// 各々トークンに分ける
		st1 = new StringTokenizer(string1);
		st2 = new StringTokenizer(string2);

		// 数が異なったら失敗
		if (st1.countTokens() != st2.countTokens())
			return false;

		// 定数同士
		for (int i = 0; i < st1.countTokens();) {
			if (!tokenMatching(st1.nextToken(), st2.nextToken())) {
				// トークンが一つでもマッチングに失敗したら失敗
				return false;
			}
		}

		// 最後まで O.K. なら成功
		return true;
	}

	boolean tokenMatching(String token1, String token2) {
		// System.out.println(token1+"<->"+token2);
		if (token1.equals(token2))
			return true;
		if (var(token1) && !var(token2))
			return varMatching(token1, token2);
		if (!var(token1) && var(token2))
			return varMatching(token2, token1);
		return false;
	}

	boolean varMatching(String vartoken, String token) {
		if (vars.containsKey(vartoken)) {
			if (token.equals(vars.get(vartoken))) {
				return true;
			} else {
				return false;
			}
		} else {
			vars.put(vartoken, token);
		}
		return true;
	}

	boolean var(String str1) {
		// 先頭が ? なら変数
		return str1.startsWith("?");
	}

}
