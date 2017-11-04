package net.ktamido.double_array.prefixtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class PrefixTree implements Serializable {

	/**
	 * 単語追加時に遷移先ノード情報を保存しておくためのクラス
	 */
	static class NextNode {
		private int index;
		private int srcCharPoint;
		NextNode(int index, int srcCharPoint) {
			this.index = index;
			this.srcCharPoint = srcCharPoint;
		}
	}

	private int size;
	private int[] base;
	private int[] check;
	private MorphemeData[][] data;

	public PrefixTree() {
		this(65535);
	}

	public PrefixTree(int size) {
		this.size = size;
		base = new int[size];
		base[1] = 1;
		check = new int[size];
		data = new MorphemeData[size][0];
		Arrays.fill(data, null);
	}

	/**
	 * PrefixTreeオブジェクトをシリアライズして指定されたファイルに保存する
	 */
	public void serialize(String file) {
		Path path = Paths.get(file);
		// ファイルが存在しない場合は作成、存在する場合は既存の内容を削除して上書き
		try (OutputStream os = Files.newOutputStream(path);
		     ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(this);
			oos.flush();
			oos.reset();
		} catch (IOException e) {
			System.out.println("シリアライズに失敗 : ファイルへの書き込みに失敗しました。");
			e.printStackTrace();
		}
	}

	/**
	 * 指定されたファイルからPrefixTreeオブジェクトをデシリアライズして新しいPrefixTreeオブジェクトを返す
	 */
	public static PrefixTree deserialize(String file) {
		PrefixTree newObj = null;
		Path path = Paths.get(file);
		try (InputStream is = Files.newInputStream(path);
		     ObjectInputStream ois = new ObjectInputStream(is)) {
			newObj = (PrefixTree) ois.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("デシリアライズに失敗 : 対象ファイルが存在しません。");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("デシリアライズに失敗 : ファイルの読み込みに失敗しました。");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("デシリアライズに失敗 : ファイルの読み込みに失敗しました。");
			e.printStackTrace();
		}
		return newObj;
	}

	/**
	 * wordを探索し、登録されている形態素データを取得する
	 * 形態素データが見つからなかった場合はnullを返す
	 */
	public MorphemeData[] get(String word) {
		SimpleEntry<Integer,Integer>  entry = this._get(word);
		if (entry.getValue().intValue() == -1) {
			int index = entry.getKey().intValue();
			MorphemeData[] ret = (data[index] != null) ? Arrays.copyOf(data[index], data[index].length) : null;
			if (ret != null) {
				for (MorphemeData i : ret) {
					if (i != null) i = i.clone();
				}
			}
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * word 探索の本体
	 * ノードのインデックス(nodeIndex)と探索文字列におけるインデックス(wordIndex)を返す
	 * 探索に成功した場合
	 *   - nodeIndex : 形態素データが格納されているノードインデックス
	 *   - wordIndex : -1
	 * 探索に失敗した場合
	 *   - nodeIndex : 探索に失敗した時点のインデックス
	 *   - wordIndex : 探索に失敗した時点の探索文字列におけるインデックス
	 */
	private SimpleEntry<Integer,Integer> _get(String word) {
		int currIndex = 1;
		int currBase = base[currIndex];
		int currCheck = check[currIndex];
		char[] chars = word.toCharArray();
		int i;
		for (i = 0; i < chars.length; i++) {
			int charPoint = (int) chars[i];
			int nextIndex = currBase + charPoint;
			if (nextIndex >= size || check[nextIndex] != currIndex) {
				// nextIndexが配列のサイズを超えてしまう場合、
				// 遷移先のcheck値が現在のcurrIndexではない場合は探索失敗
				return new SimpleEntry<Integer,Integer>(Integer.valueOf(currIndex), Integer.valueOf(i));
			}
			currIndex = nextIndex;
			currBase  = base[nextIndex];
			currCheck = check[nextIndex];
		}
		// 文字列の最後まで遷移完了
		return new SimpleEntry<Integer,Integer>(Integer.valueOf(currIndex), Integer.valueOf(-1));
	}

	/**
	 * 形態素データを登録する
	 * 登録に成功した場合はtrue, すでに登録されていて登録しなかった場合はfalseを返す
	 */
	public boolean add (MorphemeData m) {
		// 形態素データを格納するためのインデックスを取得
		int index = _add(m.getSurface());
		// dataに形態素を登録
		if (data[index] == null) {
			// 形態素データの配列そのものが存在しない場合は生成
			data[index] = new MorphemeData[] {m};
		} else {
			// すでに登録済みの形態素データの場合は登録せずにfalseを返す
			for (MorphemeData i : data[index]) {
				if (m.equals(i)) {
					return false;
				}
			}
			// 配列内に形態素データが登録されていなければ配列を拡張して登録する
			data[index] = Arrays.copyOf(data[index], data[index].length + 1);
			data[index][data[index].length -1] = m;
		}
		return true;
	}

	/**
	 * 単語をDoubleArrayに登録し、形態素データを格納するためのインデックス(1以上)を返す。
	 */
	private int  _add(String word) {
		SimpleEntry<Integer,Integer> entry = this._get(word);
		int wordIndex = entry.getValue().intValue();
		int currIndex = entry.getKey().intValue();
		// ダブル配列上に単語が登録済みの場合はそのまま現在のインデックスを返す
		if (wordIndex == -1) return currIndex;

		int currBase  = base[currIndex];
		int currCheck = check[currIndex];
		char[] chars = word.toCharArray();
		for (int i = wordIndex; i < chars.length; i++) {
			int charPoint = (int) chars[i];
			int nextIndex = currBase + charPoint;
			// nextIndexが配列の大きさを超えている場合は配列を拡張
			extendArray(nextIndex);
			if (check[nextIndex] == 0) {
				// 非衝突時
				// 挿入したノードのbase値に1を、check値に遷移元インデックスを設定
				base[nextIndex] = 1;
				check[nextIndex] = currIndex;
				// 遷移先にcurrを移動する
				currIndex = nextIndex;
				currBase  = base[nextIndex];
				currCheck = check[nextIndex];
			} else {
				// 衝突時
				// 1. 現在ノードの遷移先ノードをすべて取得
				ArrayList<NextNode> nodes = new ArrayList<>(50);
				for (int j = currBase; j < size && j <= currBase + 65535; j++) {
					if (currIndex == check[j]) {
						nodes.add(new NextNode(j, j - currBase));
					}
				}
				// 2. 遷移先ノードすべてが遷移可能かつ、追加対象ノードも配置可能なbase値を求める
				int newBase = -1;
				for (int j = 1; j < size; j++) {
					// 遷移先ノードが配置可能か
					int cnt = 0;
					for (NextNode n : nodes) {
						// 配列の大きさが足りない場合は拡張
						extendArray(j + n.srcCharPoint);
						if (check[j + n.srcCharPoint] != 0) {
							break;
						}
						cnt++;
					}
					// 追加対象ノードが配置可能か
					if (cnt == nodes.size() && check[j + charPoint] == 0) {
						newBase = j;
						break;
					}
				}
				base[currIndex] = newBase;
				// 3. 追加対象の文字を新しいbase値で計算した遷移先に登録
				int newIndex = newBase + charPoint;
				base[newIndex]  = 1;
				check[newIndex] = currIndex;
				data[newIndex]  = null;
				// 4. 遷移先ノードを新しいbase値で計算した遷移先に移動
				for (NextNode n : nodes) {
					newIndex = newBase + n.srcCharPoint;
					// 遷移先ノードを移動
					base[newIndex]  = base[n.index];
					check[newIndex] = check[n.index];
					data[newIndex]  = data[n.index];
					// 旧遷移先ノードを削除
					base[n.index]  = 0;
					check[n.index] = 0;
					data[n.index]  = null;
					// 5. 旧遷移先ノードからさらに遷移しているノードのcheck値を移動後の遷移先ノードのインデックスで上書きする
					// 遷移先ノードの探索範囲はbase値からbase値 + 65535まで
					for (int j = base[newIndex]; j < size && j <= base[newIndex] + 65535; j++) {
						if (check[j] == n.index) {
							check[j] = newIndex;
						}
					}
				}
				// 5. 遷移先を現在ノードに置き換える
				currIndex = newBase + charPoint;
				currBase = base[currIndex];
				currCheck = check[currIndex];
			}
		}
		return currIndex;
	}

	/**
	 * 配列(base, check, data)が引数のindex以上の大きさになるまで拡張する
	 * ただし配列の大きさがintの最大値の場合は何もしない
	 */
	private void extendArray(int index) {
		while (index >= size && size != Integer.MAX_VALUE) {
			size  = (int) Math.floor(size * 1.5);
			base  = Arrays.copyOf(base, size);
			check = Arrays.copyOf(check, size);
			data  = Arrays.copyOf(data, size);
		}
	}

	/**
	 * 現在の配列の状態を表示する
	 */
	public void dumpArray() {
		StringBuilder indexStr = new StringBuilder("index [");
		StringBuilder baseStr  = new StringBuilder("base  [");
		StringBuilder checkStr = new StringBuilder("check [");
		StringBuilder dataStr  = new StringBuilder("data  [");
		for (int i = 0; i < size; i++) {
			if (base[i] != 0 || check[i] != 0 || data[i] != null) {
				String obj = (data[i] != null) ? "E" : " ";
				indexStr.append(String.format("%9s", i) + ", ");
				baseStr.append(String.format("%9s", base[i]) + ", ");
				checkStr.append(String.format("%9s", check[i]) + ", ");
				dataStr.append(String.format("%9s", obj) + ", ");
				//if (data[i] != null) System.out.println(String.format("data[%s] = %s", i, word));
			}
		}
		System.out.println(indexStr.delete(indexStr.length() - 2, indexStr.length()).append("]"));
		System.out.println(baseStr.delete(baseStr.length() - 2, baseStr.length()).append("]"));
		System.out.println(checkStr.delete(checkStr.length() - 2, checkStr.length()).append("]"));
		System.out.println(dataStr.delete(dataStr.length() - 2, dataStr.length()).append("]"));
		System.out.println("size = " + size);
	}
}
