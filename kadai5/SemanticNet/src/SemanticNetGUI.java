import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * セマンティックネットGUIクラス
 *
 * @author Yoshida
 */
public class SemanticNetGUI extends JFrame {
	PaintPanel paintPanel;// 描画パネル

	/**
	 * セマンティックネットGUIコンストラクタ
	 *
	 * @param sn
	 *            描画したいセマンティックネット
	 * @param pointTable
	 *            ノード名に対する座標テーブル
	 */
	public SemanticNetGUI(SemanticNet sn, Map<String, Point> pointTable) {
		super("SemanticNetGUI");
		paintPanel = new PaintPanel(sn, pointTable);
		add(paintPanel);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

/**
 * 描画用パネルクラス
 */
class PaintPanel extends JPanel {
	// 矢印の先端のヒゲと線の角度ANGLE、矢印の先端のヒゲのサイズHEAD_SIZE
	final static double ANGLE = Math.PI / 9, HEAD_SIZE = 12;
	// 破線の空白のサイズ
	final static double BLANK_SIZE = 6;
	// ノード名に対する座標テーブル
	private Map<String, Point> pointTable;
	private SemanticNet sn;
	// 最大座標からの余白の長さ
	final static int MARGIN = 100;
	// ウィンドウサイズwidth x height
	private int width, height;

	/**
	 * セマンティックネット用描画パネル コンストラクタ
	 *
	 * @param sn
	 *            描画したいセマンティックネット
	 * @param pointTable
	 *            ノード名に対する座標テーブル
	 */
	public PaintPanel(SemanticNet sn, Map<String, Point> pointTable) {
		this.sn = sn;
		this.pointTable = pointTable;

		// 描画サイズの決定
		width = height = 0;
		for (Point point : pointTable.values()) {
			if (width < point.x)
				width = point.x;
			if (height < point.y)
				height = point.y;
		}
		width += MARGIN;
		height += MARGIN;
		// サイズ指定
		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * 矢印を描く
	 *
	 * @param g
	 *            対象のGraphicsオブジェクト
	 * @param x1
	 *            始点のx座標
	 * @param y1
	 *            始点のy座標
	 * @param x2
	 *            終点のx座標
	 * @param y2
	 *            終点のy座標
	 * @param dot
	 *            破線矢印かどうか
	 */
	public static void drawArrow(Graphics g, int x1, int y1, int x2, int y2, boolean dot) {
		// 2点間のベクトルを求める
		double dx = x2 - x1, dy = y2 - y1;
		double length = Math.sqrt(dx * dx + dy * dy);
		// 大きさをsizeにする
		dx = (dx / length) * HEAD_SIZE;
		dy = (dy / length) * HEAD_SIZE;
		// 回転変換R(angle)
		double vx1 = Math.cos(ANGLE) * dx - Math.sin(ANGLE) * dy;
		double vy1 = Math.sin(ANGLE) * dx + Math.cos(ANGLE) * dy;
		// 回転変換R(-angle)
		double vx2 = Math.cos(-ANGLE) * dx - Math.sin(-ANGLE) * dy;
		double vy2 = Math.sin(-ANGLE) * dx + Math.cos(-ANGLE) * dy;
		// 直線を描く
		if (dot) {
			drawDottedLine(g, x1, y1, x2, y2);
		} else {
			g.drawLine(x1, y1, x2, y2);
		}
		// 矢印にする
		g.drawLine((int) (x2 - vx1), (int) (y2 - vy1), x2, y2);
		g.drawLine((int) (x2 - vx2), (int) (y2 - vy2), x2, y2);
	}

	/**
	 * 破線を描く
	 *
	 * @param g
	 *            対象のGraphicsオブジェクト
	 * @param x1
	 *            始点のx座標
	 * @param y1
	 *            始点のy座標
	 * @param x2
	 *            終点のx座標
	 * @param y2
	 *            終点のy座標
	 *
	 */
	public static void drawDottedLine(Graphics g, int x1, int y1, int x2, int y2) {
		double dx = (x2 - x1), dy = (y2 - y1);
		double length = Math.sqrt(dx * dx + dy * dy);
		dx = (dx / length) * BLANK_SIZE;
		dy = (dy / length) * BLANK_SIZE;

		for (double x = x1, y = y1; ((x - x2) * (x - x2) + (y - y2) * (y - y2)) > BLANK_SIZE * BLANK_SIZE; x += 2
				* dx, y += 2 * dy) {
			g.drawLine((int) x, (int) y, (int) (x + dx), (int) (y + dy));
		}
	}

	/**
	 * 描画メソッド
	 */
	@Override
	public void paintComponent(Graphics g) {
		// 白で覆う
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		for (Node node : sn.getNodes()) {
			Point point = pointTable.get(node.getName());
			if (point != null) {
				for (Link link : node.getDepartFromMeLinks()) {
					Point pointTail = pointTable.get(link.getHead().getName());
					g.setColor(Color.MAGENTA);
					drawArrow(g, point.x, point.y, pointTail.x, pointTail.y, link.inheritance);
					g.setColor(Color.BLUE);
					g.drawString(link.getLabel(), (point.x + pointTail.x) / 2, (point.y + pointTail.y) / 2);
				}
				g.setColor(Color.BLACK);
				g.drawString(node.getName(), point.x + 10, point.y + 10);
				g.fillRect(point.x, point.y, 5, 5);
			}
		}
	}
}
