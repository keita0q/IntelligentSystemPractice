/**
 * 単体の質問の構造を表すクラス
 *
 * @author Yoshida
 */
public class Link {
	String frame, slot, value;

	public Link(String frame, String slot, String value) {
		this.frame = frame;
		this.slot = slot;
		this.value = value;
	}

	public String getFrame() {
		return frame;
	}

	public String getSlot() {
		return slot;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "(" + frame + ", " + slot + ", " + value + ")";
	}
}
