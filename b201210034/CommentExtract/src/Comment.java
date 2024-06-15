/**
*
* @author Huseyin Yaman huseyin.yaman2@ogr.sakarya.edu.tr
* @since 15.04.2023 - 23.04.2023
* <p>
* Yorumlar�n i�eri�ini, ba�lang�� ve biti� indexlerini tutar.
* </p>
*/
public class Comment {
	public String text;
	public int start, end;
	
	public Comment(String text, int start, int end) {
		this.text = text;
		this.start = start;
		this.end = end;
	}
	
}
