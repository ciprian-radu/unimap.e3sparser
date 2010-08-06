package ro.ulbsibiu.acaps.e3s.parser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import ro.ulbsibiu.acaps.e3s.ctg.E3sBenchmarkData;

import de.susebox.jtopas.Flags;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.ReaderSource;

/**
 * File parser for the <a href="http://ziyang.eecs.umich.edu/~dickrp/e3s/">E3S
 * benchmarks</a>. Those benchmarks are written in the <a
 * href="http://ziyang.eecs.umich.edu/~dickrp/tgff/">TGFF</a> file format.
 * However, this file parser is specific to E3S.
 * 
 * @author Ciprian Radu
 * 
 */
public class E3sTgffFileParser {

	// REGEX specific patterns
	
	private static final String REGEX_ANY_CHARACTER = "[A-Za-z0-9_]";
	
	private static final String REGEX_ANY_CHARACTER_MULTIPLE_TIMES = REGEX_ANY_CHARACTER + "*";
	
	private static final String REGEX_ANY_INTEGER_NUMBER = "-?[0-9]*";
	
	private static final String REGEX_AS_MANY_SPACES = "[ ]*";
	
	// TGFF specifc patterns
	
	private static final String TGFF_LINE_COMMENT = "#";
	
	private static final String TGFF_BLOCK_START = "{";
	
	private static final String TGFF_BLOCK_END = "}";
	
	private static final String AT_TASK_GRAPH = "@TASK_GRAPH";
	
	/** regex: @TASK_GRAPH followed by an integer number */
	private static final String TGFF_TASK_GRAPH = "@TASK_GRAPH " + REGEX_ANY_INTEGER_NUMBER;
	
	private static final String TASK = "TASK";
	
	private static final String TYPE = "TYPE";
	
	/** regex: TASK followed by its name and its TYPE (an integer number)*/
	private static final String TGFF_TASK = TASK + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + TYPE + " "
			+ REGEX_ANY_INTEGER_NUMBER;
	
	private static final String ARC = "ARC";
	
	private static final String FROM = "FROM";
	
	private static final String TO = "TO";
	
	/** regex: ARC followed by its name, then FROM task name TO task name*/
	private static final String TGFF_ARC = ARC + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + FROM + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + TO + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + TYPE + " "
			+ REGEX_ANY_INTEGER_NUMBER;
	
	// E3S specific patterns
	
	private static final String AT_COMMUN_QUANT = "@COMMUN_QUANT";
	
	private static final String E3S_COMMUN_QUANTITY = AT_COMMUN_QUANT + " "
			+ REGEX_ANY_INTEGER_NUMBER + " \\{";
	
	/** the file path */
	private String filePath;
	
	/** the E3S Communication Task Graphs */
	private List<E3sBenchmarkData> ctgs;
	
	/**
	 * Default constructor
	 */
	public E3sTgffFileParser(String filePath) {
		this.filePath = filePath;
		ctgs = new ArrayList<E3sBenchmarkData>();
	}
	
	/**
	 * @return the E3S Communication Task Graphs
	 */
	public List<E3sBenchmarkData> getE3sCtgs() {
		return ctgs;
	}

	/**
	 * Searches for the value of a specified attribute, in a String line. It is
	 * required that the value of the attribute is right after its name (only a
	 * space separates the two).
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @param line
	 *            the String line
	 * @return the value of the attribute or <tt>null</tt> if no value was found
	 */
	private static String getAttributeValue(String attributeName, String line) {
		String value = null;
		
		final String SPACE = " ";
		if (line != null && !line.isEmpty() 
				&& attributeName != null && !attributeName.isEmpty()) {
			int index = line.indexOf(attributeName + SPACE);
			if (index >= 0) {
				value = line.substring(index + (attributeName + SPACE).length());
				int spaceIndex = value.indexOf(SPACE);
				if (spaceIndex > 0) {
					value = value.substring(0, spaceIndex);
				}
			}
		}
		
		return value;
	}
	
	public void parseTgffFile () throws FileNotFoundException, TokenizerException {
		FileInputStream stream = new FileInputStream(filePath);
		InputStreamReader reader = new InputStreamReader(stream);
		TokenizerProperties props = new StandardTokenizerProperties();
		Tokenizer tokenizer = new StandardTokenizer();
		Token token;
		int caseFlags;
		
		// setup the tokenizer
		props.setParseFlags(Flags.F_NO_CASE | Flags.F_TOKEN_POS_ONLY
				| Flags.F_RETURN_WHITESPACES);
		caseFlags = props.getParseFlags() & ~Flags.F_NO_CASE;
		props.setSeparators(null);
		
		props.addLineComment(TGFF_LINE_COMMENT);
		props.addSpecialSequence(TGFF_BLOCK_END);
		
		props.addPattern(E3S_COMMUN_QUANTITY);
		
		props.addPattern(TGFF_TASK_GRAPH);
		props.addPattern(TGFF_TASK);
		props.addPattern(TGFF_ARC);

		tokenizer.setTokenizerProperties(props);
		tokenizer.setSource(new ReaderSource(reader));

		String currentAttribute = null;
		String communType = null;
		String communValue = null;
		int taskGraphCounter = -1;
		E3sBenchmarkData ctg = new E3sBenchmarkData(filePath + "-" + (taskGraphCounter + 1));
		ctgs.add(ctg);
		
		// tokenize the file and print basically
		// formatted context to stdout
		while (tokenizer.hasMoreToken()) {
			token = tokenizer.nextToken();
			switch (token.getType()) {
			case Token.NORMAL:
//				System.out.println(tokenizer.currentImage());
				if (currentAttribute == AT_COMMUN_QUANT) {
					if (communType == null) {
						communType = tokenizer.currentImage();
					} else {
						if (communValue == null) {
							communValue = tokenizer.currentImage();
						} else {
							System.out.println(communType + " " + communValue);
							ctg.addCommunicationVolume(communType, new Double(communValue));
							communType = tokenizer.currentImage();
							communValue = null;
						}
					}
				}
				break;
			case Token.LINE_COMMENT:
//				System.out.println(tokenizer.currentImage());
				break;
			case Token.SPECIAL_SEQUENCE:
//				System.out.println(tokenizer.currentImage());
				break;
			case Token.PATTERN:
//				System.out.println(tokenizer.currentImage());
				if (tokenizer.currentImage().startsWith(AT_TASK_GRAPH)) {
					taskGraphCounter++;
					if (taskGraphCounter > 0) {
						ctg = new E3sBenchmarkData(filePath + "-" + taskGraphCounter);
						ctgs.add(ctg);
					}
				}
				if (tokenizer.currentImage().startsWith(AT_COMMUN_QUANT)) {
					currentAttribute = AT_COMMUN_QUANT;
				} else {
					if (tokenizer.currentImage().startsWith(TASK)) {
						currentAttribute = TASK;
						String taskValue = getAttributeValue(TASK, tokenizer.currentImage());
						if (taskValue == null || taskValue.isEmpty()) {
							taskValue = getAttributeValue(TASK.toLowerCase(),tokenizer.currentImage());
						}
						assert taskValue != null && !taskValue.isEmpty();
						String typeValue = getAttributeValue(TYPE, tokenizer.currentImage());
						if (typeValue == null || typeValue.isEmpty()) {
							typeValue = getAttributeValue(TYPE.toLowerCase(),tokenizer.currentImage());
						}
						assert typeValue != null && !typeValue.isEmpty();
						System.out.println(taskValue + " " + typeValue);
						ctg.addTask(taskValue, typeValue);
					} else {
						if (tokenizer.currentImage().startsWith(ARC)) {
							currentAttribute = ARC;
							String arcValue = getAttributeValue(ARC,tokenizer.currentImage());
							if (arcValue == null || arcValue.isEmpty()) {
								arcValue = getAttributeValue(ARC.toLowerCase(),tokenizer.currentImage());
							}
							assert arcValue != null && !arcValue.isEmpty();
							String fromValue = getAttributeValue(FROM,tokenizer.currentImage());
							if (fromValue == null || fromValue.isEmpty()) {
								fromValue = getAttributeValue(FROM.toLowerCase(),tokenizer.currentImage());
							}
							assert fromValue != null && !fromValue.isEmpty();
							String toValue = getAttributeValue(TO,tokenizer.currentImage());
							if (toValue == null || toValue.isEmpty()) {
								toValue = getAttributeValue(TO.toLowerCase(),tokenizer.currentImage());
							}
							assert toValue != null && !toValue.isEmpty();
							String typeValue = getAttributeValue(TYPE,tokenizer.currentImage());
							if (typeValue == null || typeValue.isEmpty()) {
								typeValue = getAttributeValue(TYPE.toLowerCase(),tokenizer.currentImage());
							}
							assert typeValue != null && !typeValue.isEmpty();
							System.out.println(arcValue + " " + fromValue +  " " + toValue + " " + typeValue);
							ctg.addEdge(arcValue, fromValue, toValue, typeValue);
						} else {
							currentAttribute = null;
						}
					}
				}
				break;
			}
		}
		// process the data which is still "buffered"
		assert communType != null && communValue != null;
		System.out.println(communType + " " + communValue);
		ctgs.get(0).addCommunicationVolume(communType, new Double(communValue));
		
		buildCtgs();
	}
	
	private void buildCtgs() {
		for (int i = 0; i < ctgs.size(); i++) {
			if (i > 0) {
				ctgs.get(i).setCommunicationVolumes(ctgs.get(0).getCommunicationVolumes());
			}
			ctgs.get(i).buildCtg();
		}
	}
	
	// Main method. Supply a TGFF file name as argument
	public static void main(String[] args) throws FileNotFoundException,
			TokenizerException, JAXBException {
		E3sTgffFileParser e3sFileParser = new E3sTgffFileParser(args[0]);
		e3sFileParser.parseTgffFile();
		
		List<E3sBenchmarkData> e3sCtgs = e3sFileParser.getE3sCtgs();
		for (E3sBenchmarkData e3sBenchmarkData : e3sCtgs) {
			E3sToXmlParser.parse(e3sBenchmarkData);
		}
	}
}
