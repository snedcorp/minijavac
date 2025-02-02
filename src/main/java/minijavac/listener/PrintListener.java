package minijavac.listener;

import minijavac.err.CompileError;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.util.*;

/**
 * {@link Listener} implementation that writes to stderr.
 */
public class PrintListener extends AbstractListener {
    /*
    * Use LinkedHashMap so files are iterated over in insertion order.
    * Use TreeMap for a file's errors, so they are iterated over in ascending line number order.
    * */
    private final LinkedHashMap<Path, TreeMap<Integer, List<CompileError>>> errors;
    private int errCount = 0;

    public PrintListener() {
        this.errors = new LinkedHashMap<>();
    }

    /**
     * Stores errors on a per-file, per-line basis, if {@link #ignore} field is not true.
     * Each file's errors are stored in ascending order by line number, for printing purposes.
     */
    @Override
    public void err(CompileError err) {
        if (ignore) return;

        Path file = err.getPos().file();
        int line = err.getPos().line();
        if (!errors.containsKey(file)) {
            errors.put(file, new TreeMap<>());
        }
        if (!errors.get(file).containsKey(line)) {
            errors.get(file).put(line, new ArrayList<>());
        }
        errors.get(file).get(line).add(err);
        errCount++;
    }

    @Override
    public boolean hasErrors() {
        return errCount > 0;
    }

    @Override
    public int getErrCnt() {
        return errCount;
    }

    /**
     * Prints recorded errors, if any, to stderr.
     */
    public void printErrors() {
        if (!hasErrors()) return;

        errors.keySet().forEach(this::printErrors);

        System.err.printf("%d %s%n", errCount, errCount > 1 ? "errors" : "error");
    }

    private void printErrors(Path file) {
        SortedMap<Integer, List<CompileError>> lineMap = errors.get(file);
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file.toString()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Integer lineNumber = reader.getLineNumber();
                List<CompileError> lineErrors = lineMap.get(lineNumber);

                if (lineErrors == null) continue;

                for (CompileError err : lineErrors) {
                    err.print(line);
                }
                if (lineMap.lastKey().equals(lineNumber)) break;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
