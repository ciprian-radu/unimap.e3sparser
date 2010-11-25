package ro.ulbsibiu.acaps.e3s.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import ro.ulbsibiu.acaps.e3s.ctg.E3sBenchmarkData;
import ro.ulbsibiu.acaps.e3s.ctg.E3sCore;
import ro.ulbsibiu.acaps.e3s.ctg.E3sCore.E3sCoreParams;
import ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType;
import ro.ulbsibiu.acaps.e3s.ctg.E3sTaskCore;
import ro.ulbsibiu.acaps.e3s.ctg.E3sTaskCore.E3sTaskCoreParams;
import de.susebox.jtopas.Flags;
import de.susebox.jtopas.ReaderSource;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.Tokenizer;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;

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
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(E3sTgffFileParser.class);

	// REGEX specific patterns
	
	private static final String REGEX_ANY_CHARACTER = "[A-Za-z0-9_\\-]";
	
	private static final String REGEX_ANY_CHARACTER_MULTIPLE_TIMES = REGEX_ANY_CHARACTER + "*";
	
	private static final String REGEX_ANY_INTEGER_NUMBER = "-?[0-9]*";
	
//	private static final String REGEX_ANY_DOUBLE_NUMBER = "[-+]?\\b\\d+(\\.\\d+)?\\b";
	
	private static final String REGEX_ANY_POSITIVE_DOUBLE_NUMBER = "[+]?\\b\\d+(\\.\\d+)?\\b";
	
	private static final String REGEX_AS_MANY_SPACES = "[ ]*";
	
	// TGFF specifc patterns
	
	private static final String TGFF_LINE_COMMENT = "#";
	
//	private static final String TGFF_BLOCK_START = "{";
	
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
	
	private static final String PERIOD = "PERIOD";
	
	/** regex: PERIOD followed by its value (a positive floating point number)*/
	private static final String TGFF_PERIOD = PERIOD + " " + REGEX_ANY_POSITIVE_DOUBLE_NUMBER;
	
	private static final String ON = "ON";
	
	private static final String AT = "AT";
	
	private static final String HARD_DEADLINE = "HARD_DEADLINE";
	
	/** regex: HARD_DEADLINE followed its name ON task name AT time */
	private static final String TGFF_HARD_DEADLINE = HARD_DEADLINE + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + ON + " "
			+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + AT + " "
			+ REGEX_ANY_POSITIVE_DOUBLE_NUMBER;
	
	private static final String SOFT_DEADLINE = "SOFT_DEADLINE";
	
	/** regex: SOFT_DEADLINE followed its name ON task name AT time */
	private static final String TGFF_SOFT_DEADLINE = SOFT_DEADLINE + " "
	+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + ON + " "
	+ REGEX_ANY_CHARACTER_MULTIPLE_TIMES + REGEX_AS_MANY_SPACES + AT + " "
	+ REGEX_ANY_POSITIVE_DOUBLE_NUMBER;
	
	// E3S specific patterns
	
	// constants for communication quantities
	
	private static final String AT_COMMUN_QUANT = "@COMMUN_QUANT";
	
	private static final String E3S_COMMUN_QUANTITY = AT_COMMUN_QUANT + " "
			+ REGEX_ANY_INTEGER_NUMBER + " \\{";
	
	// constants for core parameters
	
	private static final String AT_CORE = "@CORE";
	
	private static final String E3S_CORE = AT_CORE + " "
			+ REGEX_ANY_INTEGER_NUMBER + " \\{";
	
	// we use the last comment for obtaining the name of each core
	private String lastComment;
	
	private int e3sCoreParamIndex = 0;
	
	private int e3sCoreTaskParamIndex = 0;
	
	private E3sCore e3sCore;
	
	private E3sTaskCore e3sTaskCore;
	
	// constants for wire bandwidth
	
	private static final String AT_WIRE_BIT_WIDTH = "@WIRE_BIT_WIDTH";
	
	private static final String E3S_WIRE_BIT_WIDTH = "@WIRE_BIT_WIDTH" + " "
			+ REGEX_ANY_INTEGER_NUMBER;
	
	// ---
	
	/** the file path */
	private String filePath;
	
	/** the E3S Communication Task Graphs */
	private List<E3sBenchmarkData> e3sCtgs;
	
	/**
	 * Default constructor
	 */
	public E3sTgffFileParser(String filePath) {
		this.filePath = filePath;
		logger.info("Parsing the E3S " + filePath + " benchmark");
		e3sCtgs = new ArrayList<E3sBenchmarkData>();
	}
	
	/**
	 * @return the E3S Communication Task Graphs
	 */
	public List<E3sBenchmarkData> getE3sCtgs() {
		return e3sCtgs;
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
		if (logger.isInfoEnabled()) {
			logger.debug("Parsing the E3S .tgff file");
		}

		FileInputStream stream = new FileInputStream(filePath);
		InputStreamReader reader = new InputStreamReader(stream);
		TokenizerProperties props = new StandardTokenizerProperties();
		Tokenizer tokenizer = new StandardTokenizer();
		Token token;
		
		// setup the tokenizer
		props.setParseFlags(Flags.F_NO_CASE | Flags.F_TOKEN_POS_ONLY
				| Flags.F_RETURN_WHITESPACES);
//		int caseFlags = props.getParseFlags() & ~Flags.F_NO_CASE;
		props.setSeparators(null);
		
		props.addLineComment(TGFF_LINE_COMMENT);
		props.addSpecialSequence(TGFF_BLOCK_END);
		
		props.addPattern(E3S_COMMUN_QUANTITY);
		
		props.addPattern(E3S_CORE);
		
		props.addPattern(TGFF_TASK_GRAPH);
		props.addPattern(TGFF_TASK);
		props.addPattern(TGFF_ARC);
		props.addPattern(TGFF_PERIOD);
		props.addPattern(TGFF_HARD_DEADLINE);
		props.addPattern(TGFF_SOFT_DEADLINE);
		
		props.addPattern(E3S_WIRE_BIT_WIDTH);

		tokenizer.setTokenizerProperties(props);
		tokenizer.setSource(new ReaderSource(reader));

		String currentAttribute = null;
		String communType = null;
		String communValue = null;
		int taskGraphCounter = -1;
		E3sBenchmarkData e3sCtg = new E3sBenchmarkData(filePath, taskGraphCounter + 1);
		e3sCtgs.add(e3sCtg);
		
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
//							System.out.println(communType + " " + communValue);
							e3sCtg.addCommunicationVolume(communType, new Double(communValue));
							communType = tokenizer.currentImage();
							communValue = null;
						}
					}
				} else {
					if (currentAttribute == AT_CORE) {
						if (e3sCoreParamIndex < E3sCoreParams.values().length) {
//							System.out.println(E3sCoreParams.values()[e3sCoreParamIndex] + " " + tokenizer.currentImage());
							e3sCore.setCoreParameter(E3sCoreParams.values()[e3sCoreParamIndex], new Double(tokenizer.currentImage()));
							e3sCoreParamIndex++;
						} else {
							if (e3sCoreTaskParamIndex >= E3sTaskCoreParams.values().length) {
								e3sCoreTaskParamIndex = 0;
								e3sTaskCore = new E3sTaskCore();
								e3sCore.addE3sTaskCore(e3sTaskCore);
							}
//							System.out.println(E3sTaskCoreParams.values()[e3sCoreTaskParamIndex] + " " + tokenizer.currentImage());
							e3sTaskCore.setTaskCoreParameter(E3sTaskCoreParams.values()[e3sCoreTaskParamIndex], new Double(tokenizer.currentImage()));
							e3sCoreTaskParamIndex++;
						}
					} else {
						if (currentAttribute == AT_WIRE_BIT_WIDTH) {
							e3sCoreParamIndex = E3sCoreParams.values().length;
							e3sCoreTaskParamIndex = E3sTaskCoreParams.values().length;
						}
					}
				}
				break;
			case Token.LINE_COMMENT:
//				System.out.println(tokenizer.currentImage());
				lastComment = tokenizer.currentImage();
				break;
			case Token.SPECIAL_SEQUENCE:
//				System.out.println(tokenizer.currentImage());
				break;
			case Token.PATTERN:
//				System.out.println(tokenizer.currentImage());
				if (tokenizer.currentImage().startsWith(AT_TASK_GRAPH)) {
					taskGraphCounter++;
					if (taskGraphCounter > 0) {
						e3sCtg = new E3sBenchmarkData(filePath, taskGraphCounter);
						e3sCtgs.add(e3sCtg);
					}
				}
				if (tokenizer.currentImage().startsWith(AT_COMMUN_QUANT)) {
					currentAttribute = AT_COMMUN_QUANT;
				} else {
					if (tokenizer.currentImage().startsWith(AT_CORE)) {
						currentAttribute = AT_CORE;
						String coreName = lastComment.trim().substring(2);
						e3sCore = new E3sCore(coreName,
								tokenizer.currentImage().substring(AT_CORE.length() + 1, tokenizer.currentImage().length() - 2));
						e3sTaskCore = new E3sTaskCore();
						e3sCore.addE3sTaskCore(e3sTaskCore);
						// all the E3S CTGs were already added to the list e3sCtgs
						for (int i = 0; i < e3sCtgs.size(); i++) {
							e3sCtgs.get(i).addCore(e3sCore);
						}
						e3sCoreParamIndex = 0;
						e3sCoreTaskParamIndex = 0;
					} else {
						if (tokenizer.currentImage().startsWith(AT_WIRE_BIT_WIDTH)) {
							currentAttribute = AT_WIRE_BIT_WIDTH;
						} else {
							if (tokenizer.currentImage().startsWith(TASK)) {
								currentAttribute = TASK;
								String taskValue = getAttributeValue(TASK, tokenizer.currentImage());
								if ("display".equals(taskValue)){
									System.out.println();
								}
								if (taskValue == null || taskValue.isEmpty()) {
									taskValue = getAttributeValue(TASK.toLowerCase(),tokenizer.currentImage());
								}
								assert taskValue != null && !taskValue.isEmpty();
								String typeValue = getAttributeValue(TYPE, tokenizer.currentImage());
								if (typeValue == null || typeValue.isEmpty()) {
									typeValue = getAttributeValue(TYPE.toLowerCase(),tokenizer.currentImage());
								}
								assert typeValue != null && !typeValue.isEmpty();
//								System.out.println(taskValue + " " + typeValue);
								e3sCtg.addTask(taskValue, typeValue);
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
//									System.out.println(arcValue + " " + fromValue +  " " + toValue + " " + typeValue);
									e3sCtg.addEdge(arcValue, fromValue, toValue, typeValue);
								} else {
									if (tokenizer.currentImage().startsWith(PERIOD)) {
										currentAttribute = PERIOD;
										String periodValue = getAttributeValue(PERIOD, tokenizer.currentImage());
										assert periodValue != null && !periodValue.isEmpty();
//										System.out.println(PERIOD + " " + periodValue);
										e3sCtg.setPeriod(new Double(periodValue));
									} else {
										if (tokenizer.currentImage().startsWith(HARD_DEADLINE)) {
											currentAttribute = HARD_DEADLINE;
											String hdValue = getAttributeValue(HARD_DEADLINE,tokenizer.currentImage());
											if (hdValue == null || hdValue.isEmpty()) {
												hdValue = getAttributeValue(HARD_DEADLINE.toLowerCase(),tokenizer.currentImage());
											}
											assert hdValue != null && !hdValue.isEmpty();
											String onValue = getAttributeValue(ON,tokenizer.currentImage());
											if (onValue == null || onValue.isEmpty()) {
												onValue = getAttributeValue(ON.toLowerCase(),tokenizer.currentImage());
											}
											assert onValue != null && !onValue.isEmpty();
											String atValue = getAttributeValue(AT,tokenizer.currentImage());
											if (atValue == null || atValue.isEmpty()) {
												atValue = getAttributeValue(AT.toLowerCase(),tokenizer.currentImage());
											}
											assert atValue != null && !atValue.isEmpty();
//											System.out.println(HARD_DEADLINE + " " + hdValue + " " + ON + " " + onValue +  " " + AT + " " + atValue);
											e3sCtg.addDeadline(DeadlineType.HARD, hdValue, onValue, new Double(atValue));
										} else {
											if (tokenizer.currentImage().startsWith(SOFT_DEADLINE)) {
												currentAttribute = SOFT_DEADLINE;
												String softValue = getAttributeValue(SOFT_DEADLINE,tokenizer.currentImage());
												if (softValue == null || softValue.isEmpty()) {
													softValue = getAttributeValue(SOFT_DEADLINE.toLowerCase(),tokenizer.currentImage());
												}
												assert softValue != null && !softValue.isEmpty();
												String onValue = getAttributeValue(ON,tokenizer.currentImage());
												if (onValue == null || onValue.isEmpty()) {
													onValue = getAttributeValue(ON.toLowerCase(),tokenizer.currentImage());
												}
												assert onValue != null && !onValue.isEmpty();
												String atValue = getAttributeValue(AT,tokenizer.currentImage());
												if (atValue == null || atValue.isEmpty()) {
													atValue = getAttributeValue(AT.toLowerCase(),tokenizer.currentImage());
												}
												assert atValue != null && !atValue.isEmpty();
//												System.out.println(SOFT_DEADLINE + " " + softValue + " " + ON + " " + onValue +  " " + AT + " " + atValue);
												e3sCtg.addDeadline(DeadlineType.SOFT, softValue, onValue, new Double(atValue));
											} else {
												currentAttribute = null;
											}
										}
									}
								}
							}
						}
					}
				}
				break;
			}
		}
		// process the data which is still "buffered"
		assert communType != null && communValue != null;
//		System.out.println(communType + " " + communValue);
		e3sCtgs.get(0).addCommunicationVolume(communType, new Double(communValue));
		
		buildE3sCtgs();
	}
	
	private void buildE3sCtgs() {
		if (logger.isInfoEnabled()) {
			logger.info("Building the E3S CTGs");
		}

		for (int i = 0; i < e3sCtgs.size(); i++) {
			if (i > 0) {
				e3sCtgs.get(i).setCommunicationVolumes(e3sCtgs.get(0).getCommunicationVolumes());
			}
			e3sCtgs.get(i).buildCtg();
		}
	}
	
	// Main method. Supply a TGFF file name as argument
	public static void main(String[] args) throws FileNotFoundException,
			TokenizerException, JAXBException {
		System.err.println("usage:   java E3sCtgViewer.class [.tgff file]");
		System.err.println("example 1 (specify the tgff file): java E3sCtgViewer.class e3s/telecom-mocsyn.tgff");
		System.err.println("example 2 (parse the entire E3S benchmark suite): java E3sCtgViewer.class");
		final String E3S = "e3s";
		File[] tgffFiles = null;
		if (args == null || args.length == 0) {
			File e3sDir = new File(E3S);
			logger.assertLog(e3sDir.isDirectory(),
					"Could not find the E3S benchmarks directory!");
			tgffFiles = e3sDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".tgff");
				}
			});
		} else {
			tgffFiles = new File[args.length];
			for (int i = 0; i < args.length; i++) {
				tgffFiles[i] = new File(args[i]);
			}
		}
		for (int i = 0; i < tgffFiles.length; i++) {
			E3sTgffFileParser e3sFileParser = new E3sTgffFileParser(E3S
					+ File.separator + tgffFiles[i].getName());
			e3sFileParser.parseTgffFile();

			List<E3sBenchmarkData> e3sCtgs = e3sFileParser.getE3sCtgs();
			for (E3sBenchmarkData e3sBenchmarkData : e3sCtgs) {
				E3sToXmlParser e3sToXmlParser = new E3sToXmlParser(
						e3sBenchmarkData);
				e3sToXmlParser.parse();
			}
			logger.info("Finished with " + E3S + File.separator
					+ tgffFiles[i].getName());
		}
		logger.info("Done.");
	}
}
