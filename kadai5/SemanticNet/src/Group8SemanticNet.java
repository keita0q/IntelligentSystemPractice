import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/***
 * Semantic Net
 * @author Mizutani
 */
public class Group8SemanticNet {
	public static void main(String args[]) {
		// 課題5-1追加分、SemanticNetLoadFiles.java
		SemanticNetLoadFiles hoge = new SemanticNetLoadFiles();
		SemanticNet semnet = hoge.SNFilesLoader("SemanticNet");
		semnet.printLinks();
		semnet.printNodes();
		Map<String, Point> pointTable_hoge = new HashMap<>();
		pointTable_hoge = (new SemanticNetMapAdder()).semanticNetMapAdd(semnet);
		new SemanticNetGUI(semnet, pointTable_hoge).setVisible(true);
	}
}
