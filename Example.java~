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
  // 担当 スロットを設定
  fs.writeSlotValue( "人間", "担当", "課題5" );

  //クラスフレーム 課題 の生成
  fs.createClassFrame( "課題" );
  // 担当者 スロットを設定
  fs.writeSlotValue( "課題", "担当者", "名前" );

  // クラス 人間 インスタンスフレーム  の生成
  fs.createInstanceFrame( "人間", "淺野" );
  fs.createInstanceFrame( "人間", "中村" );
  fs.createInstanceFrame( "人間", "西" );
  fs.createInstanceFrame( "人間", "水谷" );
  fs.createInstanceFrame( "人間", "吉田" );

  fs.writeSlotValue( "淺野", "担当", "課題5-2" );
  fs.writeSlotValue( "中村", "担当", "課題5-2" );
  fs.writeSlotValue( "西", "担当", "課題5-2" );
  fs.writeSlotValue( "水谷", "担当", "課題5-2" );
  fs.writeSlotValue( "吉田", "担当", "課題5-2" );

  //クラス 課題 インスタントフレーム の生成
  fs.createInstanceFrame( "課題", "課題5-1" );
  fs.createInstanceFrame( "課題", "課題5-2" );
  fs.createInstanceFrame( "課題", "課題5-3" );
  fs.createInstanceFrame( "課題", "課題5-4" );
  fs.createInstanceFrame( "課題", "課題5-5" );
  fs.createInstanceFrame( "課題", "課題5-6" );

  fs.writeSlotValue( "課題5-1", "担当者", "水谷" );
  fs.writeSlotValue( "課題5-2", "担当者", "淺野" );
  fs.writeSlotValue( "課題5-3", "担当者", "西" );
  fs.writeSlotValue( "課題5-4", "担当者", "中村" );
  fs.writeSlotValue( "課題5-5", "担当者", "吉田" );
  fs.writeSlotValue( "課題5-6", "担当者", "吉田" );
 }
}
