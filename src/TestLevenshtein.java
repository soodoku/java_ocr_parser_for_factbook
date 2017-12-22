import org.apache.commons.lang3.StringUtils;


public class TestLevenshtein {
	public static void main(String[] args) {
		String test="Internet Service";
		String test1="Intemet Senice";
		int result=StringUtils.getLevenshteinDistance(test, test1);
		System.out.println(test.length());
		System.out.println(result);
		System.out.println((double)result/test.length());
	}
}
