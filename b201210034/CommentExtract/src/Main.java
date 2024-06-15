import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
*
* @author Huseyin Yaman huseyin.yaman2@ogr.sakarya.edu.tr
* @since 15.04.2023 - 23.04.2023
* <p>
* Bir Java dosyas�ndaki yorumlar� analiz etmek i�in olu�turulan program�n Main class'�
* </p>
*/
public class Main {
	static String text = "";
	static ArrayList<Comment> javadocComments,  multiLineComments, oneLineComments;
	static ArrayList<Function> functions;
	public static void main(String[] args) throws Exception {
		// �nput kontrol edilir
		try {
			text = getText(args[0]);
		} catch (Exception e) {
			throw new Exception("Ge�erli bir dosya girin.");
		}
		
		// REGEX PATTERNLER�
		// /** + herhangi bir karakter + */ 
		Pattern javadocPattern = Pattern.compile("/\\*\\*(?s)(.*?)\\*/");
		// /* + herhangi bir karakter + */
		Pattern multiLineCommentPattern = Pattern.compile("/\\*(?s)(.*?)\\*/");
		// // + herhangi bir karakter
		Pattern oneLineCommentPattern = Pattern.compile("//.*");
		// kelime + bo�luk + ( + herhangi bir karakter + ) + bo�luk + { + herhangi bir karakter + }
		Pattern funcPattern = Pattern.compile("(\\w+)\\s*\\((.*?)\\)\\s\\{(?s)(.*?)\\}");
		
		/*
		 * Burada fonksiyonlar�n bu s�rada �al��t�r�lmas� �nemlidir.
		 * �lk �nce Javadoc, sonra �ok sat�rl�, en sonunda da tek sat�rl� yorumlar
		 * tespit edilir ve dosyadan ��kart�l�r.
		 * �rne�in Javadoclar da �ok sat�rl� yorum olarak de�erlendirilebilir,
		 * bu y�zden bu s�ra izlenmelidir.
		 */
		javadocComments = extractComments(javadocPattern);
		multiLineComments = extractComments(multiLineCommentPattern);
		oneLineComments = extractComments(oneLineCommentPattern);
		/*
		 * Yukar�daki i�lemler sonucunda hi� yorum sat�r� kalmayan bir dosya elde edilir.
		 * Bu �ekilde kalan di�er i�lemler hem daha kolay, hem de daha g�venililr
		 * bir �ekilde halledilebilir.
		 */
		
		functions = extractFunctions(funcPattern);
		saveComments();
		consoleOutput();
	}
	
	// Konsol ��kt�s�n� yazd�r�r
	static void consoleOutput() {
		System.out.println("S�n�f: " + getClassName());
		for (Function f : functions) {
			System.out.println("\tFonksiyon: " + f.name);
			System.out.println("\t\tTek Sat�r Yorum Say�s�:   " + f.oneLineComments.size());
			System.out.println("\t\t�ok Sat�rl� Yorum Say�s�: " + f.multiLineComments.size());
			System.out.println("\t\tJavadoc Yorum Say�s�:     " + f.javadocs.size());
			System.out.println("-------------------------------------------");
		}
	}
	
	/*
	 * Yorumlar� dosyaya kaydeder
	 * 
	 * Tek sat�rl� yorumlar -> teksatir.txt
	 * �ok sat�rl� yorumlar -> coksatir.txt
	 * Javadoc yorumlar� -> javadoc.txt
	 */
	static void saveComments() throws IOException {
		String tekStr = "";
		String cokStr = "";
		String javadocStr = "";
		for (Function function : functions) {
			tekStr += "Fonksiyon: " + function.name + "\n\n";
			cokStr += "Fonksiyon: " + function.name + "\n\n";
			javadocStr += "Fonksiyon: " + function.name + "\n\n";
			ArrayList<Comment> oneLine = function.oneLineComments;
			ArrayList<Comment> multiLine = function.multiLineComments;
			ArrayList<Comment> javadoc = function.javadocs;
			if (oneLine.size() == 0) {
				tekStr += "-\n\n";
			} else {
				for (int i = 0; i < oneLine.size(); i++) {
					tekStr += (i+1) + ".\n" + oneLine.get(i).text + "\n\n";
				}
			}
			if (multiLine.size() == 0) {
				cokStr += "-\n\n";
			} else {
				for (int i = 0; i < multiLine.size(); i++) {
					cokStr += (i+1) + ".\n" + multiLine.get(i).text + "\n\n";
				}
			}
			if (javadoc.size() == 0) {
				javadocStr += "-\n\n";
			} else {
				for (int i = 0; i < javadoc.size(); i++) {
					javadocStr += (i+1) + ".\n" + javadoc.get(i).text + "\n\n";
				}
			}
			tekStr += "-----------------------------------------\n\n";
			cokStr += "-----------------------------------------\n\n";
			javadocStr += "-----------------------------------------\n\n";
		}
		File tekSatir = new File("teksatir.txt");
		FileWriter tekSatirFw = new FileWriter(tekSatir);
		tekSatirFw.write(tekStr);
		tekSatirFw.close();
		
		File cokSatir = new File("coksatir.txt");
		FileWriter cokSatirFw = new FileWriter(cokSatir);
		cokSatirFw.write(cokStr);
		cokSatirFw.close();
		
		File javadocFile = new File("javadoc.txt");
		FileWriter javadocFw = new FileWriter(javadocFile);
		javadocFw.write(javadocStr);
		javadocFw.close();
	}
	
	/**
	 * `text` de�i�kenindeki class'�n ismini d�nd�r�r
	 */
	static String getClassName() {
		Pattern pattern = Pattern.compile("class\\s*(\\w+)\\s*\\{");
		Matcher matcher = pattern.matcher(text);
		matcher.find();
		return matcher.group(1);
	}
	
	// Fonksiyonlar� tespit eder, daha �nceden bulunan yorumlarla fonksiyonlar� e�le�tirir.
	static ArrayList<Function> extractFunctions(Pattern pattern){
		Matcher matcher = pattern.matcher(text);
		ArrayList<Function> functions = new ArrayList<Function>();
		
		int prevEnd = -1;
		while (matcher.find()) {
			String functionName = matcher.group(1);
			int end = matcher.end();
			Function function = new Function(functionName);
			for (Comment comment : javadocComments) {
				int commentEnd = comment.end;
				/*
				 * E�le�tirme yap�l�rken bir �nceki fonksiyonun son indexi ile
				 * �imdiki fonksiyonun son indexi aras�nda kalan yorumlar 
				 * �imdiki fonksiyona ait kabul edilir.
				 */
				if (commentEnd > prevEnd && commentEnd < end) {
					function.javadocs.add(comment);
				}
			}
			for (Comment comment : multiLineComments) {
				int commentEnd = comment.end;
				if (commentEnd > prevEnd && commentEnd < end) {
					function.multiLineComments.add(comment);
				}
			}
			for (Comment comment : oneLineComments) {
				int commentEnd = comment.end;
				if (commentEnd > prevEnd && commentEnd < end) {
					function.oneLineComments.add(comment);
				}
			}
			functions.add(function);
			prevEnd = end;
		}
		return functions;
	}
	
	/**
	 * Belirtilen patternde yorumlar� ArrayList olarak geri d�nd�r�r. 
	 */
	static ArrayList<Comment> extractComments(Pattern pattern) {
		
		Matcher matcher = pattern.matcher(text);
		ArrayList<Comment> comments = new ArrayList<Comment>();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			String comment = text.substring(start, end);
			removeComment(start, end);
			comments.add(new Comment(comment, start, end));
		}
		return comments;
	}

	/**
	 * �stenilen uzunlukta bo�luk ile dolu bir String d�nd�r�r.
	 */
	static String space(int count) {
		String s = "";
		for (int i = 0; i < count; i++) {
			s+=" ";
		}
		return s;
	}
	
	/**
	 * Yorumlar� kald�r�r ve dosyadaki indexlerin bozulmamas� i�in
	 * kald�r�lan yorum yerine bo�luk karakterleri eklenir
	 */
	static void removeComment(int start, int end) {
		text = text.substring(0,start) + space(end - start) + text.substring(end);
	}
	
	/**
	 * Bir dosyay� String halinde d�nd�r�r,
	 * yeni sat�r karakterlerini dahil eder.
	 */
	static String getText(String path) throws IOException, FileNotFoundException {
		FileReader fileReader = new FileReader(path);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String[] lines = bufferedReader.lines().toArray(String[]::new);

		bufferedReader.close();

		String text = "";
		for (int i = 0; i < lines.length; i++) {
			text += lines[i] + "\n";
		}
		return text;
	}
}
