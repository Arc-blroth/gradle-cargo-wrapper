package ai.arcblroth.cargo;

import java.io.*;
import java.util.function.Consumer;

/**
 * Provides a simplistic stream-based method of parsing a <code>toml</code> file.
 * <p>
 * This implementation is not at all feature-complete and only designed to handle tables and strings
 * for the purposes of reading a Cargo configuration.
 *
 * @since 1.1.0
 * @author Maow
 */
final class TomlParser implements Closeable {
	private final BufferedReader reader;

	private String table = null;

	TomlParser(File file) throws IOException {
		this.reader = new BufferedReader(new FileReader(file));
	}

	TomlParser(String s) {
		this.reader = new BufferedReader(new StringReader(s));
	}

	public void forEach(Consumer<TomlEntry> consumer) throws IOException {
		TomlEntry entry = read();
		while (entry != null && !entry.isEnd()) {
			consumer.accept(entry);
			entry = read();
		}
	}

	public TomlEntry read() throws IOException {
		if (reader.ready()) {
			String line = reader.readLine();
			// This usually means an error occurred in the reader.
			if (line == null) return null;
			// Keep reading until a non-empty line is found, then trim to remove additional whitespace.
			while (line.isEmpty())
				line = reader.readLine();
			line = line.trim();
			// If the line starts with '[', assume it is a table.
			if (line.startsWith("[")) {
				table = line.substring(1, line.indexOf(']'));
				return read();
			}
			// If there is a '=' in the string, assume it is a pair.
			// Then, locate the beginning of a string, if it exists, assume the pair is a string.
			final int index = line.indexOf('=');
			if (index != 0) {
				// This should hopefully avoid edge-cases like inline tables.
				String value = line.substring(index + 1).trim();
				if (value.startsWith("\""))      return readString('"',  index, line, value);
				else if (value.startsWith("'"))  return readString('\'', index, line, value);
			}
		}
		table = null;
		return new TomlEnd();
	}

	private TomlEntry readString(char quote, int index, String line, String value) {
		final String prefix = (table != null) ? table + "." : "";
		final String name = line.substring(0, index - 1).trim();
		value = value.substring(value.indexOf(quote) + 1, value.lastIndexOf(quote));
		return new TomlEntry(prefix + name, value);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	static class TomlEntry {
		final String name;
		final String value;

		private TomlEntry(String name, String value) {
			this.name = name;
			this.value = value;
		}

		boolean isEnd() {
			return false;
		}
	}

	static final class TomlEnd extends TomlEntry {
		private TomlEnd() {
			super("END", "END");
		}

		@Override
		boolean isEnd() {
			return true;
		}
	}
}