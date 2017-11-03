package net.ktamido.double_array.prefixtree;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.io.Serializable;

/**
 * 複数の形態素データを保存するためのクラス
 * 同じ字面でも複数の形態素があることを考慮してデータは複数登録できるようにする
 */
abstract class MorphemeData implements Serializable, Cloneable {
	protected String surface;        // 表層系
	MorphemeData(String surface) {
		this.surface = surface;
	}
	public String getSurface() {return surface;}
	abstract public int hashCode();
	abstract public boolean equals(Object obj);
	abstract public String toString();
	@Override
	public MorphemeData clone() {
		MorphemeData ret = null;
		try {
			ret = (MorphemeData) super.clone();
			ret.surface = new String(this.surface);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}

/**
 * 形態素を格納するためのクラス
 * 現状はコストのみ
 */
public class Morpheme extends MorphemeData implements Cloneable {
	private int    leftId;         // 左文脈ID
	private int    rightId;        // 右文脈ID
	private int    cost;           // コスト
	private String pos;            // 品詞
	private String yomi;           // 読み

	public Morpheme(String surface, int leftId, int rightId, int cost, String pos, String yomi) {
		super(surface);
		this.leftId  = leftId;
		this.rightId = rightId;
		this.cost    = cost;
		this.pos     = pos;
		this.yomi    = yomi;
	}
	@Override
		public int hashCode() {
			return Objects.hash(surface, leftId, rightId, cost, pos, yomi);
		}
	@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Morpheme other = (Morpheme) obj;
			if (!surface.equals(other.surface)) return false;
			if (leftId != other.leftId) return false;
			if (rightId != other.rightId) return false;
			if (cost != other.cost) return false;
			if (!pos.equals(other.pos)) return false;
			if (!yomi.equals(other.yomi)) return false;
			return true;
		}
	@Override
	public String toString() {
		return "[" + surface + ", " + cost + ", " + pos + "]";
	}
	@Override
	public Morpheme clone() {
		Morpheme ret = null;
		try {
			ret = (Morpheme) super.clone();
			ret.surface = new String(this.surface);
			ret.leftId = this.leftId;
			ret.rightId = this.rightId;
			ret.cost = this.cost;
			ret.pos = new String(this.pos);
			ret.yomi = new String(this.yomi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
