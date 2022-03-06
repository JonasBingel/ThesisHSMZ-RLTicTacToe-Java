package pack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Logger {

	private static final String BASE_DIRECTORY = "C:/Bachelorarbeit/95_Output/";
	private static final String FILE_PATH_EXPERIENCE = BASE_DIRECTORY + "01_Experience/";
	private static final String LOG_PATH = BASE_DIRECTORY + "02_Log/";

	private static String filenameLogGameEval = "";
	private static String filenameLogGameTrain = "";
	private static String filenameLogPlyEval = "";
	private static String filenameLogPlyTrain = "";
	private static String filenameLogMeta = "";
	private static String filenameLogState = "";

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
			.withZone(ZoneId.systemDefault());
	private static String definitiveLogTime = "";

	private static final Object[] HEADER_PLY_CSV = new String[] { "episode", "batch", "symbol", "ply", "state_before",
			"action", "state_after", "was_exploring", "was_action_optimal", "optimal_actions", "reward_previous_SA" };

	private static final Object[] HEADER_GAME_CSV = new String[] { "episode", "batch", "symbol", "number_of_plies",
			"number_of_actions", "number_of_exploratory_actions", "number_of_optimal_actions",
			"number_of_optimal_actions_wo_exploration", "current_epsilon", "current_alpha", "episode_won",
			"episode_lost", "episode_draw", "reward", "total_number_of_plies", "total_number_of_actions",
			"total_number_of_exploratory_actions", "total_number_of_optimal_actons",
			"total_number_of_optimal_actions_wo_exploration", "total_games_won", "total_games_lost",
			"total_games_draw" };

	/**
	 * Sets the definitiveLogTime variable of the current datetime in the format
	 * yyyMMddHHmm to differentiate different runs
	 */
	private static void setDefinitiveTime() {
		LocalDateTime currentTime = LocalDateTime.now();
		definitiveLogTime = currentTime.format(dateTimeFormatter);
	}

	/**
	 * Generates the filenames for all files that data is logged to during an
	 * execution
	 * 
	 * @param baseFilename basename that is used for all files; should contain the
	 *                     algorithm and if afterstates are used
	 */
	public static void generateFilenames(String baseFilename) {
		Logger.setDefinitiveTime();
		filenameLogGameEval = Logger.constructCSVFilename(baseFilename, Stage.EVAL, "GAME");
		filenameLogGameTrain = Logger.constructCSVFilename(baseFilename, Stage.TRAIN, "GAME");
		filenameLogPlyEval = Logger.constructCSVFilename(baseFilename, Stage.EVAL, "PLY");
		filenameLogPlyTrain = Logger.constructCSVFilename(baseFilename, Stage.TRAIN, "PLY");

		filenameLogMeta = definitiveLogTime + "_" + baseFilename + "META.txt";

	}

	public static void setFilenameLogState(String baseFilename) {
		filenameLogState = baseFilename + "_STATE.txt";
	}

	/**
	 * serialise/Export the passed Experience object as a file to the experience
	 * directory. The passed filename should only contain the filename and no
	 * directory paths as the path to the directory is prepended. However it should
	 * contain the extension - which is .ser by convention
	 * 
	 * @param experienceToserialise experience object to be serialised
	 * @param filename              that the serialised object will be saved to,
	 *                              extension of this filename should be .ser
	 */
	public static void serialiseExperience(Experience experienceToserialise, String filename) {
		try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH_EXPERIENCE + definitiveLogTime + "_" + filename);
				ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
			objectOut.writeObject(experienceToserialise);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	/**
	 * Returns the deserialised experience object corresponding to the passed
	 * filename. A cast to the specific type of experience object, i.e. qTable or
	 * wTable, is not done by this function
	 * 
	 * @param filename file which should be deserialised and returned as an
	 *                 experience object
	 * @return deserialised file as experience object
	 */
	public static Experience deserialiseExperience(String filename) {

		Experience deserialisedExperience = null;
		try (FileInputStream fileIn = new FileInputStream(FILE_PATH_EXPERIENCE + filename);
				ObjectInputStream objectIn = new ObjectInputStream(fileIn);) {
			deserialisedExperience = (Experience) objectIn.readObject();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("QTableTest class not found");
			c.printStackTrace();
		}
		return deserialisedExperience;
	}

	/**
	 * Builds the filename for the CSV file according to the passed parameters The
	 * start time of the execution is prepended in the format yyyyMMddHHmm to
	 * differentiate between different runs
	 * 
	 * @param baseFilename     basename of the cSV file that contains the used
	 *                         algorithm and whether afterstates was used
	 * @param stage            stage in the execution process the CSV is used for;
	 *                         either TRAIN or EVAL
	 * @param aggregationLevel either used on GAME or Ply level
	 * @return filename for the CSV that satisfies set conventions
	 */
	private static String constructCSVFilename(String baseFilename, Stage stage, String aggregationLevel) {
		return definitiveLogTime + "_" + baseFilename + stage.toString() + "_" + aggregationLevel + ".csv";
	}

	/**
	 * Derives the filename of the csvFile to use based on the passed parameters
	 * 
	 * @param isLogAggregated true if the CSV file to be written to is aggregated on
	 *                        a GameLevel, false if it contains just a ply
	 * @param stage           stage in the execution process the CSV is used for;
	 *                        either TRAIN or EVAL
	 * @return string containing the CSV filename to use
	 */
	private static String deriveCSVFilename(boolean isLogAggregated, Stage stage) {
		String csvFileToAppend = "";

		if (isLogAggregated) {
			if (stage == Stage.TRAIN) {
				csvFileToAppend = filenameLogGameTrain;
			} else {
				csvFileToAppend = filenameLogGameEval;
			}

		} else {
			if (stage == Stage.TRAIN) {
				csvFileToAppend = filenameLogPlyTrain;
			} else {
				csvFileToAppend = filenameLogPlyEval;
			}

		}
		return csvFileToAppend;
	}

	/**
	 * Logs the passed values to the Game CSV file corresponding to the passed
	 * stage, optionally write the header beforehand
	 * 
	 * @param values      record of one episode to be logged to the CSV file
	 * @param stage       stage in the execution process the csv is used for; either
	 *                    TRAIN or EVAL
	 * @param printHeader true if the header is to be printed
	 */
	public static void logToGameCSV(Object[] values, Stage stage, boolean printHeader) {
		String csvFileToAppend = Logger.deriveCSVFilename(true, stage);

		try (FileWriter writer = new FileWriter(LOG_PATH + csvFileToAppend, true);
				CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL);) {
			if (printHeader) {
				printer.printRecord(HEADER_GAME_CSV);
			}
			printer.printRecord(values);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Logs the passed values to the Ply CSV file corresponding to the passed stage,
	 * optionally write the header beforehand
	 * 
	 * @param values      record of multiple plies to be logged to the CSV file
	 * @param stage       stage in the execution process the csv is used for; either
	 *                    TRAIN or EVAL
	 * @param printHeader true if the header is to be printed
	 */
	public static void logToPlyCSV(LinkedList<Object[]> values, Stage stage, boolean printHeader) {
		String csvFileToAppend = deriveCSVFilename(false, stage);

		try (FileWriter writer = new FileWriter(LOG_PATH + csvFileToAppend, true);
				CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL);) {
			if (printHeader) {
				printer.printRecord(HEADER_PLY_CSV);
			}
			printer.printRecords(values);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method logs the passed string to one of the two txt-files used to evaluate
	 * the runs
	 * 
	 * @param textToLog String to write to the file
	 * @param isMetaLog if true string is written to the metaLog, else it is written
	 *                  to the StateLog
	 */
	public static void logToTxtFile(String textToLog, boolean isMetaLog) {
		String logFile;
		if (isMetaLog) {
			logFile = filenameLogMeta;
		} else {
			logFile = filenameLogState;
		}

		try (FileWriter writer = new FileWriter(LOG_PATH + logFile, true)) {
			writer.write(textToLog);
			writer.write(System.lineSeparator());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logMetaData(ExperimentParameters experimentparameters) {
		String metaDataString = Utility.generateMetaDataString(experimentparameters);
		Logger.logToTxtFile(metaDataString, true);
	}

}
