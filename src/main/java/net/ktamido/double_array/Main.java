package net.ktamido.double_array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import net.ktamido.double_array.prefixtree.*;

public class Main {
	public static void addFromDictionaryFile(Path path, Charset charset, PrefixTree pt) {
		try (BufferedReader br = Files.newBufferedReader(path, charset)) {
			br.lines()
				.filter(str -> str.split(",").length == 13)
				.map(str -> {
					String[] s = str.split(",");
					return new Morpheme(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]), s[4], s[11]);
				})
				.sorted((m1, m2) -> {
					String a = m1.getSurface();
					String b = m2.getSurface();
					if (a.length() != b.length()) {
						return a.length() - b.length();
					} else {
						return a.compareTo(b);
					}
				})
				.forEach(m -> { if (pt.add(m) == false) System.out.print(m); });
		} catch (IOException e) {
			System.out.println(String.format("%sの読み込みに失敗しました。処理を終了します。", path.toString()));
		}
	}

	public static void addOld(Path path, Charset charset, PrefixTree pt) {
		try (BufferedReader br = Files.newBufferedReader(path, charset)) {
			String line = null;
			while((line = br.readLine()) != null) {
				String[] s = line.split(",");
				if (s.length == 13) {
					pt.add(new Morpheme(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]), s[4], s[11]));
				}
			}
		} catch (IOException e) {
			System.out.println(String.format("%sの読み込みに失敗しました。処理を終了します。", path.toString()));
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		PrefixTree<Morpheme> pt = new PrefixTree<Morpheme>();
		while (true) {
			System.out.print("command : ");
			String mode = sc.next();
			if (!Arrays.asList(new String[] {"get_all", "import", "get", "add", "serialize", "deserialize"}).contains(mode)) {
				StringBuilder help = new StringBuilder();
				help.append("import <csv_file>\n");
				help.append("get_all <csv_file>\n");
				help.append("get <word>\n");
				help.append("add <word>\n");
				help.append("serialize <output_file>\n");
				help.append("deserialize <input_file>\n");
				System.out.println(help);
			} else {
				String param = sc.next();
				System.out.println(LocalDateTime.now());
				if (mode.equals("get_all")) {
					Path inputFile = Paths.get(param);
					int cnt = 0;
					try (BufferedReader br = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
						String line = null;
						while ((line = br.readLine()) != null) {
							String word = line.split(",")[0];
							if (pt.get(word) == null) {
								cnt++;
								System.out.println(line);
							}
						}
					} catch (IOException e) {
						System.out.println(String.format("%sの読み込みに失敗しました。処理を終了します。", inputFile.toString()));
					}
					System.out.println("add error words : " + cnt);
				} else if (mode.equals("get")) {
					System.out.println(Arrays.toString(pt.get(param)));
				} else if (mode.equals("add")) {
					System.out.println(pt.add(new Morpheme(param, 12345, 12345, 12345, "手入力", "")));
				} else if (mode.equals("serialize")) {
					pt.serialize(param);
				} else if (mode.equals("deserialize")) {
					PrefixTree<Morpheme> tmp = pt.deserialize(param);
					if (tmp != null) pt = tmp;
				} else if (mode.equals("import")) {
					Path path = Paths.get(param);
					Main.addFromDictionaryFile(path, StandardCharsets.UTF_8, pt);
				}
				System.out.println(LocalDateTime.now());
			}
		}
	}
}
