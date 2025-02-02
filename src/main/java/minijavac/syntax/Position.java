package minijavac.syntax;

import java.nio.file.Path;

/**
 * Immutable object containing the specific file, location, and position of a {@link Token}.
 */
public record Position(Path file, int line, int offset) {}
