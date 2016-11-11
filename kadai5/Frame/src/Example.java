import java.awt.Point;
import java.util.HashMap;

/*
 Example.java
 */

public class Example {

	public static void main(String args[]) {
		System.out.println("Frame");

		// フレームシステムの初期化
		AIFrameSystem fs = new AIFrameSystem();

		// クラスフレーム human の生成
		fs.createClassFrame("human");
		// height スロットを設定
		fs.writeSlotValue("human", "height", new Integer(160));

		// height から weight を計算するための式 weight = 0.9*(height-100) を
		// when-requested demon として weight スロットに割り当てる
		fs.setWhenRequestedProc("human", "weight", new AIDemonProcReadTest());

		// インスタンスフレーム tora の生成
		fs.createInstanceFrame("human", "tora");

		fs.createClassFrame("OS");
		fs.createInstanceFrame("OS", "Mac OS");
		fs.writeSlotValue("tora", "好き", fs.getAIFrame("OS"));

		// height と weight はデフォルト値
		System.out.println(fs.readSlotValue("tora", "height", false));
		System.out.println(fs.readSlotValue("tora", "weight", false));

		// weight はデフォルト値
		fs.writeSlotValue("tora", "height", new Integer(165));
		System.out.println(fs.readSlotValue("tora", "height", false));
		System.out.println(fs.readSlotValue("tora", "weight", false));

		// 再びデフォルト値を表示
		fs.writeSlotValue("tora", "weight", new Integer(50));
		System.out.println(fs.readSlotValue("tora", "height", true));
		System.out.println(fs.readSlotValue("tora", "weight", true));

		HashMap<String, Point> pointTable = new HashMap<>();
		pointTable.put("human", new Point(150,150));
		pointTable.put("tora", new Point(50, 50));
		pointTable.put("OS", new Point(300, 50));
		pointTable.put("Mac OS", new Point(300, 200));

		new FrameGUI(fs, pointTable).setVisible(true);
	}

}