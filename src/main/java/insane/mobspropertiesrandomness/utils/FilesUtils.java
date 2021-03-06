package insane.mobspropertiesrandomness.utils;

import java.io.File;
import java.util.ArrayList;

public class FilesUtils {
	public static ArrayList<File> ListFilesForFolder(final File folder) {
		ArrayList<File> list = new ArrayList<File>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				ListFilesForFolder(fileEntry);
			} else {
				list.add(fileEntry);
			}
		}
		return list;
	}
}
