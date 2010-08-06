package ro.ulbsibiu.acaps.e3s.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ro.ulbsibiu.acaps.ctg.xml.task.ObjectFactory;
import ro.ulbsibiu.acaps.ctg.xml.task.TaskType;
import ro.ulbsibiu.acaps.e3s.ctg.E3sBenchmarkData;
import ro.ulbsibiu.acaps.e3s.ctg.E3sVertex;

/**
 * @author cipi
 * 
 */
public class E3sToXmlParser {

	private static final String XML = "xml";

	private E3sToXmlParser() {
		;
	}

	public static void parse(E3sBenchmarkData e3sBenchmarkData)
			throws JAXBException, FileNotFoundException {
		assert e3sBenchmarkData != null;

		List<E3sVertex> vertices = e3sBenchmarkData.getVertices();
		ObjectFactory taskFactory = new ObjectFactory();
		File file = new File(XML + File.separator + e3sBenchmarkData.getName());
		file.mkdirs();
		for (int i = 0; i < vertices.size(); i++) {
			E3sVertex e3sVertex = vertices.get(i);
			TaskType taskType = new TaskType();
			taskType.setID(e3sVertex.getType());
			taskType.setName(e3sVertex.getName());
			JAXBElement<TaskType> task = taskFactory.createTask(taskType);

			JAXBContext jaxbContext = JAXBContext.newInstance(TaskType.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			File taskFile = new File(file.getPath() + File.separator + "tasks");
			taskFile.mkdir();
			marshaller.marshal(task, new FileOutputStream(taskFile.getPath()
					+ File.separator + "task-" + i + ".xml"));
		}

	}

}
