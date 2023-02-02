package com.oracle.coherence.gradle;

import org.gradle.api.file.RegularFileProperty;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class PluginUtils {
	private PluginUtils() {
		throw new AssertionError("This is a static utility class.");
	}

	public static void appendString(RegularFileProperty file, String stringToAppend) {
		try {
			final FileWriter fw = new FileWriter(file.get().getAsFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(stringToAppend);
			bw.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
