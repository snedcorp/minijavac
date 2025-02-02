package minijavac.cli;

import picocli.CommandLine.ITypeConverter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathConverter implements ITypeConverter<Path> {

    @Override
    public Path convert(String s) throws Exception {
        if (s.equals(".")) return Paths.get(".");
        return Paths.get(s);
    }
}
