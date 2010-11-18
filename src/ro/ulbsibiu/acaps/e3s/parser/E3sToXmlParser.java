package ro.ulbsibiu.acaps.e3s.parser;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ro.ulbsibiu.acaps.ctg.xml.core.CoreType;
import ro.ulbsibiu.acaps.ctg.xml.ctg.CommunicatingTaskType;
import ro.ulbsibiu.acaps.ctg.xml.ctg.CommunicationType;
import ro.ulbsibiu.acaps.ctg.xml.ctg.CtgType;
import ro.ulbsibiu.acaps.ctg.xml.ctg.DeadlineType;
import ro.ulbsibiu.acaps.ctg.xml.task.ObjectFactory;
import ro.ulbsibiu.acaps.ctg.xml.task.TaskType;
import ro.ulbsibiu.acaps.e3s.ctg.E3sBenchmarkData;
import ro.ulbsibiu.acaps.e3s.ctg.E3sCore;
import ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline;
import ro.ulbsibiu.acaps.e3s.ctg.E3sEdge;
import ro.ulbsibiu.acaps.e3s.ctg.E3sTaskCore;
import ro.ulbsibiu.acaps.e3s.ctg.E3sVertex;

/**
 * @author cipi
 * 
 */
public class E3sToXmlParser {
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(E3sToXmlParser.class);

	private static final String E3S_XML_FILE_PATH = "../CTG-XML"
			+ File.separator + "xml" + File.separator;
	
	private static final String XML = "xml";

	private static final String CTG = "ctg";

	private static final String TASK = "task";

	private static final String TASKS = "tasks";

	private static final String CORE = "core";

	private static final String CORES = "cores";

	private E3sBenchmarkData e3sBenchmarkData;

	/**
	 * Each {@link E3sBenchmarkData} corresponds to a CTG from a benchmark.
	 * Multiple CTGs may exist in a benchmark set, each CTG will have the same
	 * cores associated. Thus, parsing cores must really be made only for the
	 * first CTG of each benchmark.
	 */
	private static boolean coresParsed;

	/** keeps the name of the last parsed E3S benchmark */
	private static String oldName = "";

	/**
	 * maps tasks, by their names, to their IDs, given when the tasks XML files
	 * are created (note that the task type is not an ID; it is just a reference
	 * in the all-tasks file from E3S)
	 */
	private static Map<String, String> taskNameToIdMap;

	/**
	 * Constructor
	 * 
	 * @param e3sBenchmarkData
	 *            the E3S benchmark (cannot be <tt>null</tt>)
	 */
	public E3sToXmlParser(E3sBenchmarkData e3sBenchmarkData) {
		logger.assertLog(e3sBenchmarkData != null, "An E3sBenchmarkData must be specified");

		this.e3sBenchmarkData = e3sBenchmarkData;
		coresParsed = false;
		taskNameToIdMap = null;
	}

	private static void parseTasks(List<E3sVertex> vertices,
			String e3sBenchmarkName, int ctgId) throws JAXBException,
			FileNotFoundException {
		if (logger.isInfoEnabled()) {
			logger.info("Parsing the tasks");
		}

		ObjectFactory taskFactory = new ObjectFactory();
		File e3sBenchmarkFile = new File(E3S_XML_FILE_PATH + e3sBenchmarkName);
		e3sBenchmarkFile.mkdirs();

		taskNameToIdMap = new HashMap<String, String>(vertices.size());

		for (int i = 0; i < vertices.size(); i++) {
			E3sVertex e3sVertex = vertices.get(i);
			TaskType taskType = new TaskType();
			taskType.setID(Integer.toString(i));
			taskType.setType(e3sVertex.getType());
			taskType.setName(e3sVertex.getName());
			JAXBElement<TaskType> task = taskFactory.createTask(taskType);

			taskNameToIdMap.put(taskType.getName(), taskType.getID());

			JAXBContext jaxbContext = JAXBContext.newInstance(TaskType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			File taskFile = new File(e3sBenchmarkFile.getPath()
					+ File.separator + CTG + "-" + ctgId + File.separator
					+ TASKS);
			taskFile.mkdirs();
			String name = taskFile.getPath() + File.separator + TASK + "-" + i
					+ "." + XML;
			if (logger.isInfoEnabled()) {
				logger.info("Creating XML " + name);
			}
			marshaller.marshal(task, new FileOutputStream(name));
		}
	}

	private static void parseCores(List<E3sCore> cores,
			String e3sBenchmarkName, int ctgId) throws JAXBException,
			FileNotFoundException {
		if (logger.isInfoEnabled()) {
			logger.info("Parsing the cores");
		}

		ro.ulbsibiu.acaps.ctg.xml.core.ObjectFactory coreFactory = new ro.ulbsibiu.acaps.ctg.xml.core.ObjectFactory();
		File file = new File(E3S_XML_FILE_PATH + e3sBenchmarkName);
		file.mkdirs();
		for (int i = 0; i < cores.size(); i++) {
			E3sCore e3sCore = cores.get(i);
			CoreType coreType = new CoreType();
			coreType.setID(e3sCore.getId());
			coreType.setName(e3sCore.getName());
			coreType.setFrequency(e3sCore.getMaxFrequency());
			coreType.setHeight(e3sCore.getHeight());
			coreType.setWidth(e3sCore.getWidth());
			coreType.setIdlePower(e3sCore.getIdlePower());
			List<E3sTaskCore> e3sTaskCores = e3sCore.getE3sTaskCores();
			for (E3sTaskCore e3sTaskCore : e3sTaskCores) {
				ro.ulbsibiu.acaps.ctg.xml.core.TaskType taskType = new ro.ulbsibiu.acaps.ctg.xml.core.TaskType();
				taskType.setType(e3sTaskCore.getType());
				taskType.setExecTime(e3sTaskCore.getTaskTime());
				taskType.setPower(e3sTaskCore.getTaskPower());
				coreType.getTask().add(taskType);
			}
			JAXBElement<CoreType> core = coreFactory.createCore(coreType);

			JAXBContext jaxbContext = JAXBContext.newInstance(CoreType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			File coreFile = new File(file.getPath() + File.separator + CORES);
			coreFile.mkdir();
			String name = coreFile.getPath() + File.separator + CORE + "-" + i
					+ "." + XML;
			if (logger.isInfoEnabled()) {
				logger.info("Creating XML " + name);
			}
			marshaller.marshal(core, new FileOutputStream(name));
		}
	}

	private static E3sDeadline findDeadline(List<E3sDeadline> deadlines,
			String task, ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType type) {
		E3sDeadline deadline = null;

		for (int i = 0; i < deadlines.size(); i++) {
			E3sDeadline e3sDeadline = deadlines.get(i);
			if (task.equals(e3sDeadline.getTaskName())
					&& type.equals(e3sDeadline.getType())) {
				deadline = e3sDeadline;
				break;
			}
		}

		return deadline;
	}

	private static void parseCtgs(List<E3sVertex> vertices,
			List<E3sEdge> edges, List<E3sDeadline> deadlines, double period,
			String e3sBenchmarkName, int ctgId) throws JAXBException,
			FileNotFoundException {
		if (logger.isInfoEnabled()) {
			logger.info("Parsing the CTGs");
		}

		assert taskNameToIdMap != null;

		File e3sBenchmarkFile = new File(E3S_XML_FILE_PATH + e3sBenchmarkName);
		e3sBenchmarkFile.mkdirs();

		ro.ulbsibiu.acaps.ctg.xml.ctg.ObjectFactory ctgFactory = new ro.ulbsibiu.acaps.ctg.xml.ctg.ObjectFactory();
		CtgType ctgType = new CtgType();
		ctgType.setId(Integer.toString(ctgId));
		ctgType.setPeriod(period);

		for (int i = 0; i < edges.size(); i++) {
			E3sEdge e3sEdge = edges.get(i);
			CommunicationType communicationType = new CommunicationType();

			CommunicatingTaskType source = new CommunicatingTaskType();
			String sourceId = taskNameToIdMap.get(e3sEdge.getFrom());
			source.setId(sourceId);
			E3sDeadline sourceE3sDeadline = findDeadline(deadlines,
					e3sEdge.getFrom(),
					ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType.HARD);
			if (sourceE3sDeadline != null) {
				DeadlineType sourceDeadline = new DeadlineType();
				sourceDeadline.setType(sourceE3sDeadline.getType().toString()
						.toLowerCase());
				sourceDeadline.setValue(sourceE3sDeadline.getTime());
				source.getDeadline().add(sourceDeadline);
			}
			sourceE3sDeadline = findDeadline(deadlines, e3sEdge.getFrom(),
					ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType.SOFT);
			if (sourceE3sDeadline != null) {
				DeadlineType sourceDeadline = new DeadlineType();
				sourceDeadline.setType(sourceE3sDeadline.getType().toString()
						.toLowerCase());
				sourceDeadline.setValue(sourceE3sDeadline.getTime());
				source.getDeadline().add(sourceDeadline);
			}
			communicationType.setSource(source);

			CommunicatingTaskType destination = new CommunicatingTaskType();
			String destinationId = taskNameToIdMap.get(e3sEdge.getTo());
			destination.setId(destinationId);
			E3sDeadline destinationE3sDeadline = findDeadline(deadlines,
					e3sEdge.getTo(),
					ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType.HARD);
			if (destinationE3sDeadline != null) {
				DeadlineType destinationDeadline = new DeadlineType();
				destinationDeadline.setType(destinationE3sDeadline.getType()
						.toString().toLowerCase());
				destinationDeadline.setValue(destinationE3sDeadline.getTime());
				destination.getDeadline().add(destinationDeadline);
			}
			destinationE3sDeadline = findDeadline(deadlines, e3sEdge.getTo(),
					ro.ulbsibiu.acaps.e3s.ctg.E3sDeadline.DeadlineType.SOFT);
			if (destinationE3sDeadline != null) {
				DeadlineType destinationDeadline = new DeadlineType();
				destinationDeadline.setType(destinationE3sDeadline.getType()
						.toString().toLowerCase());
				destinationDeadline.setValue(destinationE3sDeadline.getTime());
				destination.getDeadline().add(destinationDeadline);
			}
			communicationType.setDestination(destination);

			communicationType.setVolume(e3sEdge.getWeight());
			ctgType.getCommunication().add(communicationType);
		}

		JAXBElement<CtgType> ctg = ctgFactory.createCtg(ctgType);
		JAXBContext jaxbContext = JAXBContext.newInstance(CtgType.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
		File ctgFile = new File(e3sBenchmarkFile.getPath() + File.separator
				+ CTG + "-" + ctgId);
		ctgFile.mkdirs();
		String name = ctgFile.getPath() + File.separator + CTG + "-" + ctgId
				+ "." + XML;
		if (logger.isInfoEnabled()) {
			logger.info("Creating XML " + name);
		}
		marshaller.marshal(ctg, new FileOutputStream(name));
	}

	/**
	 * Parses the data of E3S benchmarks to XML files.
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	public void parse() throws JAXBException, FileNotFoundException {
		if (logger.isInfoEnabled()) {
			logger.info("Parsing the E3S benchmark data to XML files");
		}

		if (oldName.equals(e3sBenchmarkData.getName())) {
			coresParsed = true;
		} else {
			coresParsed = false;
		}
		parseTasks(e3sBenchmarkData.getVertices(), e3sBenchmarkData.getName(),
				e3sBenchmarkData.getCtgId());
		if (!coresParsed) {
			parseCores(e3sBenchmarkData.getCores(), e3sBenchmarkData.getName(),
					e3sBenchmarkData.getCtgId());
		}
		parseCtgs(e3sBenchmarkData.getVertices(), e3sBenchmarkData.getEdges(),
				e3sBenchmarkData.getDeadlines(), e3sBenchmarkData.getPeriod(),
				e3sBenchmarkData.getName(), e3sBenchmarkData.getCtgId());
		oldName = e3sBenchmarkData.getName();
	}

}
