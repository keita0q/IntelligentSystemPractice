import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 文節を表すクラス
 */
public class Chunk extends ArrayList<Morpheme> {

	/**
	 * 文節番号
	 */
	int id;

	/**
	 * 係り先の文節
	 */
	Chunk dependencyChunk;

	/**
	 * 係り先の文節番号
	 */
	int dependency;

	/**
	 * この文節に係る文節のリスト
	 */
	List<Chunk> dependents;

	public Chunk() {
		super();
		dependents = new LinkedList<Chunk>();
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDependency(int dependency) {
		this.dependency = dependency;
	}

	public void setMorphemes(List<Morpheme> morphs) {
		for (Iterator<Morpheme> i = iterator(); i.hasNext();) {
			remove(i.next());
		}
		addAll(morphs);
	}

	public void addMorpheme(Morpheme morph) {
		add(morph);
	}

	public void addDependentChunk(Chunk chunk) {
		dependents.add(chunk);
	}

	public void setDependencyChunk(Chunk chunk) {
		dependencyChunk = chunk;
	}

	/**
	 * この文節の主辞の形態素を入れ替える
	 *
	 * @param morph
	 *            セットする形態素
	 */
	public void replaceHeadMorpheme(Morpheme morph) {
		set(size() - 1, morph);
	}

	/**
	 * この文節の文節番号を返す
	 *
	 * @return 文節番号
	 */
	public int getId() {
		return id;
	}

	/**
	 * この文節が係る文節のidを返す
	 *
	 * @return この文節が係る文節のid
	 */
	public int getDependency() {
		return dependency;
	}

	/**
	 * この文節が係る文節を返す
	 *
	 * @return この文節が係る文節
	 */
	public Chunk getDependencyChunk() {
		return dependencyChunk;
	}

	/**
	 * この文節に係る文節のリストを返す
	 *
	 * @return この文節に係る文節のリスト
	 */
	public List<Chunk> getDependents() {
		return dependents;
	}

	/**
	 * 主辞である最後の形態素を返す
	 *
	 * @return 主辞である最後の形態素
	 */
	public Morpheme getHeadMorpheme() {
		return get(size() - 1);
	}

	/**
	 * この文節の表層文字列を返す
	 *
	 * @return この文節の表層文字列
	 */
	public String getSurface() {
		StringBuffer sb = new StringBuffer();
		for (Iterator<Morpheme> i = iterator(); i.hasNext();) {
			sb.append(i.next().getSurface());
		}
		return sb.toString();
	}

	/**
	 * この文節に含まれる形態素のリストを表すXML風の文字列を返す
	 *
	 * @return XML風の文字列
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" <文節 id=\"" + id);
		sb.append("\" 係り先=\"" + dependency);
		if (!dependents.isEmpty()) {
			sb.append("\" 係り元=\"");
			for (Iterator<Chunk> i = dependents.iterator(); i.hasNext();) {
				sb.append(i.next().getId());
				if (i.hasNext()) {
					sb.append(",");
				}
			}
		}
		sb.append("\">\n");
		for (Iterator<Morpheme> i = iterator(); i.hasNext();) {
			sb.append("  ");
			sb.append(i.next().toString());
			sb.append("\n");
		}
		sb.append(" </文節>");
		return sb.toString();
	}
}
