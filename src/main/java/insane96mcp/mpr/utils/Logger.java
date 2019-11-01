/*
 * 
 * Copy-pasted "some" code from https://github.com/CraftTweaker/CraftTweaker/blob/1.12/CraftTweaker2-MC1120-Main/src/main/java/crafttweaker/mc1120/logger/MCLogger.java
 *
 */

package insane96mcp.mpr.utils;

import insane96mcp.mpr.init.ModConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class Logger {
	public static File logFile;
	public static Writer writer;
	public static PrintWriter printWriter;
	
	public static void init(String filePath) {
		logFile = new File(filePath);
		try {
			writer = new OutputStreamWriter(new FileOutputStream(logFile), "utf-8");
			printWriter = new PrintWriter(writer);
		}  catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("How?");
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("Could not open log file " + logFile);
        }
	}

	public static void info(String message) {
		if (ModConfig.General.debug.get()) {
			try {
				writer.write("[INFO] " + message + "\n");
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void debug(String message) {
		if (ModConfig.General.debug.get()) {
			try {
				writer.write("[DEBUG] " + message + "\n");
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void warning(String message) {
		try {
			writer.write("[WARNING] " + message + "\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void error(String message) {
		try {
			writer.write("[ERROR] " + message + "\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}