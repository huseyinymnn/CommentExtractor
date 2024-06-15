import java.util.ArrayList;
/**
*
* @author Huseyin Yaman huseyin.yaman2@ogr.sakarya.edu.tr
* @since 15.04.2023 - 23.04.2023
* <p>
* Fonksiyonun ismini ve o fonksiyona ait yorumlar�n ArrayList'lerini tutar.
* </p>
*/
public class Function {

	public ArrayList<Comment> javadocs, multiLineComments, oneLineComments;
	public String name;
	public Function(String name) {
		this.name = name;
		javadocs = new ArrayList<>();
		multiLineComments = new ArrayList<>();
		oneLineComments = new ArrayList<>();
	}
	
	public ArrayList<Comment> getAllComments(){
		ArrayList<Comment> all = new ArrayList<>();
		all.addAll(oneLineComments);
		all.addAll(multiLineComments);
		all.addAll(javadocs);
		return all;
	}
	
}
