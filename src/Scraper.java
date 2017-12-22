import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;

public class Scraper {
	static String[] serviceTypes = { "Basic Service", "Digital Basic Service",
			"Expanded Basic Service", "Pay Service 1", "Pay-Per-View",
			"Pay Service 2", "Pay Service 3", "Pay Service 4", "Pay Service 5",
			"Pay Service 6", "Pay Service 7", "Pay Service 8", "Pay Service 9",
			"Pay Service 10", "Internet Service" };
	static String[] serviceAttribute = { "Subscribers", "Pay units",
			"Programming (received off-air)", "Programming (via satellite)",
			"Miles of plant", "State manager", "Manager", "Ownership", "Fee",
			"Current originations", "Local advertising", "City fee",
			"Tv Market Ranking", "Equipment", "Addressable homes",
			"Program Guide", "Chief technician" };

	public static void main(String[] args) {

		FileInputStream fis;
		try {
			fis = new FileInputStream("factbook_sample.pdf");

			// BufferedWriter writer = new BufferedWriter(new FileWriter(
			// "pdf_change.txt"));
			PDFParser p = new PDFParser(fis);
			p.parse();
			PDFTextStripper ts = new PDFTextStripper();
			// ts.setStartPage(1);
			// ts.setEndPage(101);
			// ts.getText(p.getPDDocument());
			// ts.writeText(p.getPDDocument(), writer);
			String s = ts.getText(p.getPDDocument());
			// writer.write(s);
			analyse(s);
			fis.close();
			// writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void analyse(String s) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"factbook_out.csv"));
			writer.write("town_name,");
			writer.write("company_info,");
			for (int i = 0; i < serviceTypes.length; i++) {
				for (int j = 0; j < serviceAttribute.length; j++) {
					if (isColumnEnable(serviceTypes[i], serviceAttribute[j])) {
						writer.write(serviceTypes[i] + "."
								+ serviceAttribute[j] + ",");
					}
				}
			}

			writer.write("\n");

			int townIndex = s.indexOf("—");
			boolean isEnd = false;
			while (!isEnd) {
				int nextTownIndex = s.indexOf("—", townIndex + 1);

				int nextTownIndex2 = s.lastIndexOf("\n", nextTownIndex);
				if (nextTownIndex == -1) {
					nextTownIndex2 = s.length();
					isEnd = true;
				}
				String townStr = s.substring(
						s.lastIndexOf("\n", townIndex) + 1, nextTownIndex2);
				// test name
				String name = townStr.substring(0, townStr.indexOf("—"));
				if (name.indexOf(")") != -1 && !name.startsWith("LIMESTONE")) {
					int modIndex = s.lastIndexOf("\n", townIndex);
					modIndex = s.lastIndexOf("\n", modIndex - 1);
					townStr = s.substring(modIndex + 1, nextTownIndex2);
				}
				analyseTown(townStr, writer);

				townIndex = nextTownIndex + 1;
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean isColumnEnable(String type, String attribute) {
		if (type.equals("Basic Service") && attribute.equals("Pay units")) // Basic
																			// Service
																			// cannot
		// have a column called
		// 'pay units'
		{
			return false;
		}
		return true;
	}

	private static void analyseTown(String townStr, BufferedWriter writer)
			throws IOException {

		townStr=townStr.replaceAll("Senice", "Service");
		townStr=townStr.replaceAll("Intemet", "Internet");
		townStr=townStr.replaceAll("Maricet", "Market");
		townStr=townStr.replaceAll("Tumer", "Turner");
		townStr=townStr.replaceAll("Networic", "Network");
		townStr=townStr.replaceAll("Leaming", "Learning");
		townStr=townStr.replaceAll("Outskie", "Outside");
		
		townStr=townStr.replaceAll("nlcat", "nicat");
		townStr=townStr.replaceAll("vllle", "ville");
		townStr=townStr.replaceAll("lnboro", "inboro");
		townStr=townStr.replaceAll("vlsion", "vision");
		townStr=townStr.replaceAll("vislon", "vision");
		
		townStr=townStr.replaceAll("Digitai", "Digital");
		
		// name
		String name = townStr.substring(0, townStr.indexOf("—"));

		if (name.equalsIgnoreCase("Alabama")
				|| name.equalsIgnoreCase("Cable Systems")) {
			return;
		}
		name = correctString(name);
		writer.write(name + ",");
		System.out.println("************************town: " + name
				+ "*******************************");
		// company
		int bsIndex = indexOfLevenshtein("Basic Service", townStr, 0);
		if (bsIndex == -1) {
			bsIndex = townStr.length();
		}
		String companyInfo = townStr.substring(townStr.indexOf("—") + 1,
				bsIndex);
		writer.write(correctString(companyInfo) + ",");
		// service

		for (int i = 0; i < serviceTypes.length; i++) {
			int tempIndex = townStr.indexOf(serviceTypes[i], 0);
			if (tempIndex == -1) {
				for (int j = 0; j < serviceAttribute.length; j++) {
					if (isColumnEnable(serviceTypes[i], serviceAttribute[j])) {
						writer.write(",");
					}

				}
				continue;
			}
			int nextIndex = -1;
			nextIndex = findNextService(tempIndex + serviceTypes[i].length(),
					townStr);
			System.out.println("---------------------------service: "
					+ serviceTypes[i] + "----------------------------");
			String serviceStr = townStr.substring(
					tempIndex + serviceTypes[i].length() + 1, nextIndex);
			// get service attribute
			for (int j = 0; j < serviceAttribute.length; j++) {
				int indexStart = indexOfLevenshtein(serviceAttribute[j],
						serviceStr, 0);
				if (indexStart == -1) {
					if (isColumnEnable(serviceTypes[i], serviceAttribute[j])) {
						writer.write(",");
					}
					continue;
				}
				serviceStr=serviceStr.replaceAll(";", ":");
				int tempAttrIndex = serviceStr.indexOf(":", indexStart) + 1;
				String tempAttr = serviceStr.substring(tempAttrIndex,
						findNextAttrIndex(name,serviceAttribute[j],tempAttrIndex, serviceStr));
				System.out.println(serviceAttribute[j] + ": "
						+ correctString(tempAttr));
				writer.write(correctString(tempAttr, serviceAttribute[j]) + ",");
			}
			System.out.println("---------------------------service: "
					+ serviceTypes[i] + " end ----------------------------");

		}
		writer.write("\n");
		System.out.println("************************town: " + name
				+ " end *******************************");
	}

	private static int indexOfLevenshtein(String matchStr, String townStr,
			int startIndex) {
		double matchNo = 0.3;

		int matchLength = matchStr.length();
		for (int i = startIndex; i < townStr.length(); i++) {
			
			if (i + matchLength > townStr.length()) {
				return -1;
			}
			String tempStr = townStr.substring(i, i + matchLength);
			if ((double) StringUtils.getLevenshteinDistance(tempStr, matchStr)
					/ matchStr.length() < matchNo) {
				return i;
			}
		}

		return -1;
	}

	private static String correctString(String str) {
		str = str.trim();
		str = str.replaceAll("(\r\n|\n|\r)", "");
		str = str.replaceAll(",", ".");
		str = str.replaceAll("­", "");
		str = str.replaceAll("http://\\s+www", "http://www");
		str = str.replaceAll("httpy.+?www", "http://www");
		str = str.replaceAll("ht1lp.+?www", "http://www");
		str = str.replaceAll("http;.+?www", "http://www");
		return str;
	}

	private static String correctString(String str, String attr) {
		str = correctString(str);
		if (attr.equals("Subscribers")) {
			str = str.replaceAll(",", "");
			str = str.replaceAll("\\.", "");
			if (str.trim().endsWith(".")) {
				str = str.substring(0, str.length() - 1);
			}
		}

		return str;
	}

	private static int findNextService(int tempIndex, String townStr) {
		int retIndex = townStr.length();
		for (int i = 0; i < serviceTypes.length; i++) {
			int tempTypeIndex=indexOfLevenshtein(serviceTypes[i], townStr, tempIndex);
			if (tempTypeIndex != -1) {
				if (tempTypeIndex < retIndex) {
					retIndex = tempTypeIndex;
				}

			}
		}
		return retIndex;
	}

	private static int findNextAttrIndex(String name,String attribute,int tempAttrIndex, String serviceStr) {
		int retIndex = serviceStr.length();
		
		for (int i = 0; i < serviceAttribute.length; i++) {
			int tempIndex = indexOfLevenshtein(serviceAttribute[i], serviceStr,
					tempAttrIndex);
			if (tempIndex != -1) {
				if (tempIndex < retIndex) {
					retIndex = tempIndex;
				}

			}
		}
		if(name.startsWith("BALDWIN") && (attribute.equals("Ownership") || attribute.equals("Manager")))
		{
			return serviceStr.indexOf(".",tempAttrIndex);
		}
		return retIndex;
	}
}
