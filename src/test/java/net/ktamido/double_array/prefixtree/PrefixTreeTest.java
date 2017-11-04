package net.ktamido.double_array.prefixtree;

import static org.junit.Assert.*;
import org.junit.Test;

public class PrefixTreeTest {
	@Test
	public void add_1() {
		PrefixTree pt = new PrefixTree();
		boolean ret = pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		assertEquals(true, ret);
	}
	@Test
	public void add_2() {
		PrefixTree pt = new PrefixTree();
		pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		boolean ret = pt.add(new Morpheme("ac", 2, 2, 2, "名詞", "エーシー"));
		assertEquals(true, ret);
	}
	@Test
	public void add_3() {
		PrefixTree pt = new PrefixTree();
		pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		boolean ret = pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		assertEquals(false, ret);
	}

	@Test
	public void get_1() {
		PrefixTree pt = new PrefixTree();
		pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		assertNotNull(pt.get("abc"));
	}
	@Test
	public void get_2() {
		PrefixTree pt = new PrefixTree();
		pt.add(new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー"));
		assertNull(pt.get("ac"));
	}


	@Test
	public void serialize_1() {
		PrefixTree pt = new PrefixTree();
		Morpheme data1 = new Morpheme("abc", 1, 1, 1, "名詞", "エービーシー");
		Morpheme data2 = new Morpheme("ac", 2, 2, 2, "名詞", "エーシー");
		pt.add(data1);
		pt.add(data2);
		pt.serialize("dict/test.bin");
		PrefixTree tmp = pt.deserialize("dict/test.bin");
		MorphemeData[] ret1 = tmp.get("abc");
		MorphemeData[] ret2 = tmp.get("ac");
		assertEquals(data1, ret1[0]);
		assertEquals(data2, ret2[0]);
	}
}
