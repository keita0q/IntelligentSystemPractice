/*
 * http://matarillo.com/layout/part1.php を参考にしました。
 */

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SemanticNetMapAdder {
	int iterateMax = 1000;			// ノードの移動を1000手と設定。
	double mapCenter = 100.0d;		// マップのおおよその中心。たぶんだいぶずれる。
	double dt = 0.1d;				// 一回の試行の時間(単位時間、微少時間)
	double k = 0.5d;				// バネ定数
	double l = 130.0;				// バネの自然長
	double g = 1000.0d;			// 静電気の反発係数
	double m = 0.3d;				// 空気抵抗の係数
	SemanticNetMapAdder(){}

	Map<String, Point> semanticNetMapAdd(SemanticNet sn){
		//pointTable.put("study", new Point(400, 200));
		ArrayList<Node> nodes = sn.getNodes();
		Map<String,Point> pointTable = new HashMap<String,Point>();
		HashMap<String,RadiusVelocityOfNode> nodeSurrounds = new HashMap<String,RadiusVelocityOfNode>();
		for(int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			nodeSurrounds.put(node.getName(), new RadiusVelocityOfNode(node));
		}
		// 円形に並べる
		circularize(nodeSurrounds);
		// 移動
		for(int i=0; i<iterateMax;i++){
			springModelNodeMover(nodeSurrounds);
		} // 移動完了

		for(Map.Entry<String,RadiusVelocityOfNode> entry : nodeSurrounds.entrySet()){
			if (entry != null){
				Point ponta = new Point();
				ponta.setLocation((int)entry.getValue().r.getX() +250,(int)entry.getValue().r.getY() +250);
				String strrr = entry.getKey();
				pointTable.put(strrr, ponta);
			}
		}

		return pointTable;
	}

	void circularize(HashMap<String,RadiusVelocityOfNode> nodeS){
		int count = nodeS.size();
		double dtheta = 2.0d * Math.PI / (double)count;
		double theta = 0.0d;
		Random rand = new Random();
		for(Map.Entry<String,RadiusVelocityOfNode> entry : nodeS.entrySet()){
			RadiusVelocityOfNode n = entry.getValue();
			n.r.setLocation(mapCenter * Math.cos(theta) + mapCenter+rand.nextDouble()*20, mapCenter * Math.sin(theta) + mapCenter+rand.nextDouble()*20);
			nodeS.put(entry.getKey(), n);
			theta += dtheta;
		}

	}

	void springModelNodeMover(HashMap<String,RadiusVelocityOfNode> nodeSurrounds){
		for(Map.Entry<String,RadiusVelocityOfNode> entry : nodeSurrounds.entrySet()){
			int i=0;
			RadiusVelocityOfNode nodeS = entry.getValue();
			Point2D.Double force = new Point2D.Double();
			Point2D.Double forceTemp = new Point2D.Double();	// あらゆるノードからかかるバネの力と反発力、摩擦力を加算してゆく。

			// 隣接ノードからのバネの力
			for(i=0; i<nodeS.neighborNames.size(); i++){
				RadiusVelocityOfNode nsTemp = nodeSurrounds.get(nodeS.neighborNames.get(i));
				forceTemp.setLocation(nodeS.getSpringForce(nsTemp, l, k));
				force.setLocation(force.getX()+forceTemp.getX(), force.getY()+forceTemp.getY());
			}

			// 全ノードからの反発力
			i=0;
			for(Map.Entry<String,RadiusVelocityOfNode> entry2 : nodeSurrounds.entrySet()){
				if(!entry.getKey().equals(entry2.getKey())){
					forceTemp.setLocation(nodeS.getReplusiveForce(entry2.getValue(), g));
					force.setLocation(force.getX()+forceTemp.getX(), force.getY()+forceTemp.getY());
				}
				i++;
			}

			// 空気抵抗力
			forceTemp.setLocation(nodeS.getFrictionalForce(m));
			force.setLocation(force.getX()+forceTemp.getX(), force.getY()+forceTemp.getY());

			nodeS.moveEular(force, dt);
			nodeSurrounds.put(entry.getKey(), nodeS);
		}
	}


}

class RadiusVelocityOfNode{
	public ArrayList<String> neighborNames;
	public Point2D.Double r = new Point2D.Double();	// 座標	座標のrはradiusのrであると調べたら出てきた
	public Point2D.Double v = new Point2D.Double();	// 速さ

	RadiusVelocityOfNode(Node node){
		ArrayList<Node> neighbors = new ArrayList<Node>();
		ArrayList<Link> intoLinks = node.getArriveAtMeLinks();		// ここから主語側のノード(tail)を取り出したい
		ArrayList<Link> outLinks = node.getDepartFromMeLinks();	// ここから述語側のノード(head)を取り出したい
		ArrayList<Node> intoNodes = new ArrayList<Node>();
		ArrayList<Node> outNodes = new ArrayList<Node>();
		ArrayList<String> tempNeighborNames = new ArrayList<String>();
		for(int j=0; j<intoLinks.size(); j++){
			intoNodes.add(intoLinks.get(j).getTail());
		}
		for(int j=0; j<outLinks.size(); j++){
			outNodes.add(outLinks.get(j).getTail());
		}
		neighbors.addAll(intoNodes);
		neighbors.addAll(outNodes);									// neighbors完成

		// 重複無しで、隣接ノードの名前を抽出。
		for(int i=0; i<neighbors.size(); i++){
			Node neighbor = neighbors.get(i);
			tempNeighborNames.add(neighbor.getName());
		}
		Set<String> hoge1 = new HashSet<>(tempNeighborNames);
		neighborNames = new ArrayList<>(hoge1);

		v.setLocation(0, 0);  // 座標rについては、全てのノードの速さと隣接ノードが求められてからまとめて初期化する。
	}


	/*
	 * 隣接ノード同士にかかるバネ的な力の計算。引っ張る力
	 * */
	Point2D.Double getSpringForce(RadiusVelocityOfNode nn, double l, double k){
		double dx = r.getX() - nn.r.getX();
		double dy = r.getY() - nn.r.getY();
		double d2 = dx*dx + dy*dy;

		if(d2 < Double.MIN_VALUE){	// ノードに面積はなく点として表現されているが、もしそれらの座標が重なってしまった場合。
			Random rand = new Random();
			return new Point2D.Double(rand.nextDouble() - 0.5d, rand.nextDouble() - 0.5d);
		}

		double d = Math.sqrt(d2);
		double cos = dx/d;
		double sin = dy/d;
		double dl = d-l;		// 自然長との差
		return new Point2D.Double(-k*dl*cos, -k*dl*sin);
	}


	/*
	 * 前ノード同士にかかる静電気的な力の計算。反発力
	 * */
	Point2D.Double getReplusiveForce(RadiusVelocityOfNode nn, double g){
		double dx = r.getX() - nn.r.getX();
		double dy = r.getY() - nn.r.getY();
		double d2 = dx*dx + dy*dy;

		if(d2 < Double.MIN_VALUE){	// ノードに面積はなく点として表現されているが、もしそれらの座標が重なってしまった場合。
			Random rand = new Random();
			return new Point2D.Double(rand.nextDouble() - 0.5d, rand.nextDouble() - 0.5d);
		}

		double d = Math.sqrt(d2);
		double cos = dx/d;
		double sin = dy/d;
		return new Point2D.Double((g/d2) * cos, (g/d2) * sin);		// ノード間の距離の二乗に反比例
	}


	/*
	 *  当ノードにかかる空気抵抗力
	 * */
	Point2D.Double getFrictionalForce(double m){
		return new Point2D.Double(-1*m*v.getX(), -1*m*v.getY());
	}


	/*
	 *
	 * */
	void moveEular(Point2D.Double force, double dt){
		r.setLocation(r.getX()+dt*v.getX(), r.getY()+dt*v.getY());
		v.setLocation(v.getX()+dt*force.getX(), v.getY()+dt+force.getY());
	}
}