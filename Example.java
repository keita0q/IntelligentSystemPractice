import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
/*
 Example.java

*/

public class Example {

 public static void main(String args[]) {
  System.out.println( "Frame" );

  // フレームシステムの初期化
  AIFrameSystem fs = new AIFrameSystem();

  /*課題用に変更*/

  // クラスフレーム 人間 の生成
  fs.createClassFrame( "人間" );

  // クラスフレーム 学生 の生成
  fs.createClassFrame("人間", "学生");
  //クラスフレーム 課題 の生成
  fs.createClassFrame( "課題5" );

  // クラス 人間 インスタンスフレーム  の生成
  fs.createInstanceFrame( "学生","淺野" );
  fs.createInstanceFrame( "学生", "中村" );
  fs.createInstanceFrame( "学生", "西" );
  fs.createInstanceFrame( "学生", "水谷" );
  fs.createInstanceFrame( "学生", "吉田" );


   //クラス 課題 インスタントフレーム の生成

  fs.createInstanceFrame( "課題5", "課題5-1" );
  fs.createInstanceFrame( "課題5", "課題5-2" );
  fs.createInstanceFrame( "課題5", "課題5-3" );
  fs.createInstanceFrame( "課題5", "課題5-4" );
  fs.createInstanceFrame( "課題5", "発展課題" );



  fs.writeSlotValue( "淺野", "担当", fs.getAIFrame("課題5-2"));
  fs.writeSlotValue( "中村", "担当", fs.getAIFrame("課題5-4"));
  fs.writeSlotValue( "西", "担当", fs.getAIFrame("課題5-3"));
  fs.writeSlotValue( "水谷", "担当", fs.getAIFrame("課題5-1" ));
  fs.writeSlotValue( "吉田", "担当", fs.getAIFrame("発展課題"));



  fs.writeSlotValue( "課題5-1", "担当者", fs.getAIFrame("水谷"));
  fs.writeSlotValue( "課題5-2", "担当者", fs.getAIFrame("淺野"));
  fs.writeSlotValue( "課題5-3", "担当者", fs.getAIFrame("西"));
  fs.writeSlotValue( "課題5-4", "担当者", fs.getAIFrame("中村"));
  fs.writeSlotValue( "発展課題", "担当者", fs.getAIFrame("吉田"));


	// FrameGUIの使い方
	Map<String, Point> pointTable = new HashMap<>();
	//クラス 人間 と インスタンスクラス群 のマッピング
	pointTable.put("人間", new Point(20, 50));
	pointTable.put("学生", new Point(20, 130));
	pointTable.put("水谷", new Point(130, 90));
	pointTable.put("淺野", new Point(130, 190));
	pointTable.put("西", new Point(130, 290));
	pointTable.put("中村", new Point(130, 390));
	pointTable.put("吉田", new Point(130, 490));


	//クラス 課題 と インスタンスクラス群のマッピング
	pointTable.put("課題5", new Point(450, 270));
	pointTable.put("課題5-1", new Point(300, 50));
	pointTable.put("課題5-2", new Point(300, 150));
	pointTable.put("課題5-3", new Point(300, 250));
	pointTable.put("課題5-4", new Point(300, 350));
	pointTable.put("発展課題", new Point(300, 450));

	new FrameGUI(fs, pointTable).setVisible(true);
 }
}
