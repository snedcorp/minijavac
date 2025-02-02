package minijavac.cli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mixin for command-line parameters and options received by the {@link minijavac.Compiler}.
 */
public class Args {

    private static final Pattern JAVA_FILE_REGEX = Pattern.compile(".+\\.java$");

    @Spec
    CommandSpec commandSpec;

    public List<Path> files;

    @Parameters(arity = "1..*", converter = PathConverter.class, description = "Specify source files to compile.")
    public void setFiles(List<Path> files) {
        for (Path file : files) {
            String fileStr = file.toString();
            Matcher matcher = JAVA_FILE_REGEX.matcher(fileStr);
            if (!matcher.matches()) {
                throw new ParameterException(commandSpec.commandLine(),
                        String.format("Invalid source file: %s", fileStr));
            }
        }
        this.files = files;
    }

    @Option(names = {"--source-path", "-sourcepath"}, converter = PathConverter.class, defaultValue = ".",
            description = "Specify where to find input source files")
    public Path sourcePath;

    @Option(names = "-d", converter = PathConverter.class, description = "Specify where to place generated class files")
    public Path destinationDir;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelp;
}
