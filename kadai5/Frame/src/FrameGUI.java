import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * フレームGUIクラス<br>
 * AIFrame.javaの変更点 :<br>
 * public String getName()、 public Set <String> getSlotNames()メソッドの追加<br>
 * AIFrameSystem.javaの変更点 :<br>
 * public AIFrame getAIFrame(String name)メソッドの追加
 * 
 * @author Yoshida
 */
public class FrameGUI extends JFrame implements ActionListener {
	PaintPanel paintPanel;// 描画パネル
	JPanel commandPanel;
	JTextField text;
	JButton search, searchNL;
	JTextArea info;
	JScrollPane infoScroll;
	AIFrameSystem fs;

	/**
	 * フレームGUIコンストラクタ
	 * 
	 * @param fs
	 *            描画したいフレームシステム
	 * @param pointTable
	 *            ノード名に対する座標テーブル
	 */
	public FrameGUI(AIFrameSystem fs, Map<String, Point> pointTable) {
		super("AIFrameGUI");
		this.fs = fs;
		paintPanel = new PaintPanel(fs, pointTable);

		text = new JTextField(30);
		text.setToolTipText("質問文を入力してください");
		search = new JButton("検索");
		search.addActionListener(this);
		searchNL = new JButton("自然言語で検索");
		searchNL.addActionListener(this);
		info = new JTextArea(3, 30);
		info.setEditable(false);
		info.setBackground(Color.LIGHT_GRAY);
		info.setToolTipText("情報表示エリア");
		infoScroll = new JScrollPane(info);
		commandPanel = new JPanel(new FlowLayout());
		commandPanel.add(text);
		commandPanel.add(search);
		commandPanel.add(searchNL);

		setLayout(new BorderLayout());
		add(commandPanel, BorderLayout.NORTH);
		add(paintPanel, BorderLayout.CENTER);
		add(infoScroll, BorderLayout.SOUTH);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * 入力されたテキストから、質問を取り出す
	 * 
	 * @return 質問のリスト
	 */
	public List<Link> getQuery() {
		Pattern regex = Pattern.compile("\"(.*?)\""); // ""内を取り出す正規表現
		java.util.regex.Matcher matcher = regex.matcher(text.getText());// 入力されたテキストを解析
		List<Link> query = new ArrayList<>();// ""で囲まれた部分を保存するリスト
		try {
			if (matcher.find()) {// ""で囲まれた部分があったとき
				do {
					String split[] = matcher.group(1).trim().split("\\s+");
					query.add(new Link(split[0], split[1], split[2]));// 追加
				} while (matcher.find());// 次の要素を探す
			} else {// ""で囲まれた部分がない時、全体を1つのパターンとみなす
				String split[] = text.getText().trim().split("\\s+");
				query.add(new Link(split[0], split[1], split[2]));// 全体を追加
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return query;
	}

	/**
	 * 「検索」ボタンの動作
	 */
	public void searchPressed() {
		List<Link> query = getQuery();
		if (query.isEmpty()) {
			info.setText("入力形式が正しくありません。");
			return;
		}
		List<Map<String, String>> bindings = fs.doQuery(query);
		// 課題5-3で答えられない場合、DBpediaを利用する
		if (bindings.isEmpty()) {
			// DBpediaの利用
			bindings = DBpedia.query(query);
		}
		info.setText(bindings.toString());
	}

	/**
	 * 「自然言語で検索」ボタンの動作
	 */
	public void searchNLPressed() {
		String question = text.getText();
		List<Link> query = NaturalLanguage.questionAnalysis(question);
		System.out.println(query);
		if (query.isEmpty()) {
			info.setText("自然言語を解析できませんでした。");
			return;
		}
		List<Map<String, String>> bindings = fs.doQuery(query);
		// 課題5-3で答えられない場合、DBpediaを利用する
		if (bindings.isEmpty()) {
			// DBpediaの利用
			bindings = DBpedia.query(query);
		}
		info.setText(NaturalLanguage.toNL(bindings));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if (obj == search) {
			searchPressed();
		} else if (obj == searchNL) {
			searchNLPressed();
		}
	}
}

/**
 * 描画用パネルクラス
 */
class PaintPanel extends JPanel {
	// 矢印の先端のヒゲと線の角度ANGLE、矢印の先端のヒゲのサイズHEAD_SIZE
	final static double ANGLE = Math.PI / 9, HEAD_SIZE = 12;
	final static int FONT_SIZE = 12;// フォントサイズ
	final static int CHAR_WIDTH = FONT_SIZE / 2 + 1;// 文字幅
	final static int NEXT_LINE = FONT_SIZE + 3;// 次の行までの距離
	// ノード名に対する座標テーブル
	private Map<String, Point> pointTable;
	private AIFrameSystem fs;
	// 最大座標からの余白の長さ
	final static int MARGIN = 10;
	// ウィンドウサイズwidth x height
	private int width, height;

	/**
	 * フレームシステム用描画パネル コンストラクタ
	 * 
	 * @param fs
	 *            描画したいフレームシステム
	 * @param pointTable
	 *            ノード名に対する座標テーブル
	 */
	public PaintPanel(AIFrameSystem fs, Map<String, Point> pointTable) {
		this.fs = fs;
		this.pointTable = pointTable;
		// 描画サイズの決定
		setSize();
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
	 */
	public static void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
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
		g.drawLine(x1, y1, x2, y2);
		// 矢印にする
		g.drawLine((int) (x2 - vx1), (int) (y2 - vy1), x2, y2);
		g.drawLine((int) (x2 - vx2), (int) (y2 - vy2), x2, y2);
	}

	/**
	 * 描画枠のサイズを決める
	 */
	public void setSize() {
		width = height = 0;
		if (pointTable != null) {
			// 全てのフレームに対して
			for (String frameName : pointTable.keySet()) {
				// 座標取得
				Point point1 = pointTable.get(frameName);
				int y = point1.y + NEXT_LINE;
				// 最大文字列幅
				int maxStrWidth = stringWidth(frameName);
				// 全てのスロットに対して
				AIFrame frame = fs.getAIFrame(frameName);
				for (String slotName : frame.getSlotNames(false)) {
					// スロット値を取得
					Object value = fs.readSlotValue(frameName, slotName);
					// 表示文字列
					String str = "";
					// その文字列の幅
					int strWidth = 0;
					// AIFrameのインスタンスならば
					if (value instanceof AIFrame) {
						// スロット値のフレームの座標を取得
						Point point2 = pointTable.get(((AIFrame) value)
								.getName());
						if (point2 != null) {
							// スロット名と空欄
							str = slotName + " [   ]";
							y += NEXT_LINE;
						}
					} else {// AIFrameのインスタンスでないとき
						// スロット名と値
						str = slotName + " [" + value + "]";
						y += NEXT_LINE;
					}
					// 文字列の幅を求める
					strWidth = stringWidth(str);
					// 最大文字列幅を更新
					if (maxStrWidth < strWidth)
						maxStrWidth = strWidth;
				}
				int w = point1.x + CHAR_WIDTH + maxStrWidth, h = y;
				if (width < w)
					width = w;
				if (height < h)
					height = h;
			}
		}
		width += MARGIN;
		height += MARGIN;
		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * フレームを描画
	 * 
	 * @param g
	 *            描画対象
	 * @param frameName
	 *            フレーム名
	 */
	public void drawFrame(Graphics g, String frameName) {
		// フォント設定
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE));

		// 座標取得
		Point point1 = pointTable.get(frameName);
		int x = point1.x, y = point1.y;

		// フレーム名描画
		g.setColor(Color.BLACK);
		g.drawString(frameName, x, y);
		y += NEXT_LINE;

		// 最大文字列幅
		int maxStrWidth = stringWidth(frameName);
		// 全てのスロットに対して
		AIFrame frame = fs.getAIFrame(frameName);
		for (String slotName : frame.getSlotNames(false)) {
			// スロット値を取得
			Object value = fs.readSlotValue(frameName, slotName);
			g.setColor(Color.BLUE);
			// 表示文字列
			String str = "";
			// その文字列の幅
			int strWidth = 0;
			// AIFrameのインスタンスならば
			if (value instanceof AIFrame) {
				// スロット値のフレームの座標を取得
				Point point2 = pointTable.get(((AIFrame) value).getName());
				if (point2 != null) {
					String blank = " [   ]";
					// スロット名と空欄表示
					str = slotName + blank;
					g.drawString(str, x, y);
					// 矢印を描画
					g.setColor(Color.MAGENTA);
					drawArrow(g, x + stringWidth(slotName) + stringWidth(blank)
							/ 2, y - FONT_SIZE / 2, point2.x, point2.y);
					// 描画位置更新
					y += NEXT_LINE;
				}
			} else {// AIFrameのインスタンスでないとき
				// スロット名と値を表示
				str = slotName + " [" + value + "]";
				g.drawString(str, x, y);
				// 描画位置更新
				y += NEXT_LINE;
			}
			// 文字列の幅を求める
			strWidth = stringWidth(str);
			// 最大文字列幅を更新
			if (maxStrWidth < strWidth)
				maxStrWidth = strWidth;
		}
		// フレームの枠を描画
		g.setColor(Color.GRAY);
		g.drawRect(point1.x - CHAR_WIDTH, point1.y - NEXT_LINE, maxStrWidth
				+ CHAR_WIDTH * 2, y - point1.y + NEXT_LINE);
	}

	/**
	 * 描画メソッド
	 */
	@Override
	public void paintComponent(Graphics g) {
		// 白で覆う
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (pointTable != null) {
			// 全てのフレームに対して
			for (String frameName : pointTable.keySet()) {
				drawFrame(g, frameName);
			}
		}
	}

	/**
	 * 文字列の幅を求める
	 * 
	 * @param str
	 *            文字列
	 * @return 文字列の幅
	 */
	private static int stringWidth(String str) {
		int count = 0;
		for (char ch : str.toCharArray()) {
			String temp = ch + "";
			// 1バイト文字なら1文字分、そうでないときは2文字分
			count += (temp.getBytes().length == 1) ? 1 : 2;
		}
		return count * CHAR_WIDTH;
	}
}