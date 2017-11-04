package net.ktamido.double_array;

import static org.junit.Assert.*;
import org.junit.Test;
import net.ktamido.double_array.prefixtree.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.*;

public class MainTest {

	@Test
	public void run_all() {
		Path path = Paths.get("dict/sample_dict.csv");
		PrefixTree pt = new PrefixTree();
		Main.addFromDictionaryFile(path, StandardCharsets.UTF_8, pt);
		
		int failCount = 0;
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = br.readLine()) != null) {
				String word = line.split(",")[0];
				if (pt.get(word) == null) failCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		pt.serialize("dict/sample_dict.bin");
		PrefixTree tmp = pt.deserialize("dict/sample_dict.bin");
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = br.readLine()) != null) {
				String word = line.split(",")[0];
				if (tmp.get(word) == null) failCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(0, failCount);
	}
}
