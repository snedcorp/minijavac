package minijavac;

import minijavac.ast.ClassDecl;
import minijavac.cli.Args;
import minijavac.gen.Generator;
import minijavac.gen.file.ClassFile;
import minijavac.utils.UniqueQueue;
import minijavac.context.Context;
import minijavac.context.enter.Enter;
import minijavac.context.SymbolTable;
import minijavac.listener.Listener;
import minijavac.listener.PrintListener;
import minijavac.syntax.Parser;
import minijavac.syntax.Scanner;
import minijavac.utils.StandardLibrary;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "minijavac", description = "minimal Java compiler")
public class Compiler implements Callable<Integer> {

    private final Listener listener;
    private final SymbolTable symbolTable;

    @Mixin
    private Args args;

    public Compiler(Listener listener) {
        this.listener = listener;
        this.symbolTable = new SymbolTable(listener);
    }

    public Compiler(Listener listener, Args args) {
        this(listener);
        this.args = args;
    }

    /**
     * Parses a given file into the {@link ClassDecl} nodes it contains.
     * @param stream file input stream
     * @param file   file path
     * @return parsed classes
     * @throws IOException file unable to be parsed
     */
    public List<ClassDecl> parse(InputStream stream, Path file) throws IOException {
        Scanner scanner = new Scanner(stream, file, listener);
        Parser parser = new Parser(scanner, listener);
        return parser.parse();
    }

    /**
     * <pre>
     * Parses the classes in every file specified by the user and enters their members into the {@link SymbolTable}.
     *
     * Additionally, if any of the specified classes appears to reference a unspecified class, then an attempt is made
     * to parse and enter that unspecified class and any other unspecified classes it references, and so on and so
     * forth - this will continue until all referenced classes have been processed.
     * </pre>
     * @return parsed and entered {@link ClassDecl} nodes
     * @throws IOException file is unable to be parsed, or user-specified file cannot be found
     */
    public List<ClassDecl> parseAndEnter() throws IOException {
        UniqueQueue<Path> toParseAndEnterQueue = new UniqueQueue<>();

        Set<Path> userFiles = new HashSet<>(args.files);
        // enqueue user-specified files
        for (Path file : args.files) {
            toParseAndEnterQueue.offer(file);
        }

        List<ClassDecl> enteredClasses = new ArrayList<>();
        Enter enterVisitor = new Enter(symbolTable, listener);

        // enter std lib classes
        List<ClassDecl> stdClasses = StandardLibrary.getClasses();
        for (ClassDecl stdClass : stdClasses) {
            enterVisitor.enterStd(stdClass);
        }

        while (!toParseAndEnterQueue.isEmpty()) {
            Path file = toParseAndEnterQueue.poll(); // dequeue file

            try (InputStream stream = Files.newInputStream(file)) {
                int errCnt = listener.getErrCnt();

                // parse file
                List<ClassDecl> parsedClasses = parse(stream, file);
                // if parse errors occurred, don't enter
                if (listener.getErrCnt() != errCnt) continue;

                // collect all class names within file
                Set<String> classesInFile = parsedClasses.stream()
                        .map(c -> c.id.contents)
                        .collect(Collectors.toSet());

                for (ClassDecl cls : parsedClasses) {
                    // enter class
                    Set<String> referencedClasses = enterVisitor.enter(cls);

                    // enqueue each referenced class that can't be found within the same file
                    referencedClasses.stream()
                            .filter(refClass -> !classesInFile.contains(refClass))
                            .forEach(refClass -> toParseAndEnterQueue.offer(
                                    args.sourcePath.resolve(String.format("%s.java", refClass))
                            ));

                    enteredClasses.add(cls);
                }
            } catch (IOException ex) {
                // only throw error if file was specified by user, otherwise ignore
                if (userFiles.contains(file)) throw ex;
            }
        }

        return enteredClasses;
    }

    /**
     * Prepares for bytecode generation by first parsing and entering every necessary class, and then performing
     * contextual analysis and type checking on those class nodes.
     * @return list of prepared {@link ClassDecl} nodes
     * @throws IOException file unable to be parsed
     */
    public List<ClassDecl> prepare() throws IOException {
        List<ClassDecl> enteredClasses = parseAndEnter();
        Context context = new Context(symbolTable, listener);
        for (ClassDecl classDecl : enteredClasses) {
            context.resolve(classDecl);
        }
        return enteredClasses;
    }

    /**
     * Generates {@link ClassFile} instances for the given {@link ClassDecl} nodes.
     * @param classes list of prepared {@link ClassDecl} nodes
     * @return list of corresponding {@link ClassFile} instances
     */
    public List<ClassFile> generate(List<ClassDecl> classes) {
        Generator generator = new Generator();
        return classes.stream()
                .map(generator::gen)
                .collect(Collectors.toList());
    }

    /**
     * Executes compilation.
     * @return exit code
     */
    @Override
    public Integer call() {
        List<ClassDecl> classes = null;

        // attempt to parse, enter, analyze, & type check all necessary classes
        try {
            classes = prepare();
        } catch (NoSuchFileException ex) {
            System.err.printf("error: file not found: %s%n", ex.getMessage());
        } catch (IOException ex) {
            System.err.printf("error: unable to parse file: %s%n", ex.getMessage());
        }

        // if io or compile errors occurred, return error exit code
        if (classes == null || listener.hasErrors()) return 1;

        // generate class file instances
        List<ClassFile> classFiles = generate(classes);
        // write bytecode to disk
        write(classFiles);

        return 0;
    }

    /**
     * <pre>
     * Writes the given {@link ClassFile} instances to Java class files (bytecode) at the correct locations within the
     * filesystem.
     *
     * If the user specifies a destination directory, all class files will be written there - otherwise, they are each
     * written to the same directory as their corresponding source file.
     * </pre>
     * @param classFiles classes to write
     */
    public void write(List<ClassFile> classFiles) {
        if (args.destinationDir != null) {
            try {
                Files.createDirectories(args.destinationDir);
            } catch (IOException ex) {
                System.out.printf("error: unable to create destination directory: %s%n", args.destinationDir);
            }
        }

        for (ClassFile classFile : classFiles) {
            Path dirPath = args.destinationDir != null ? args.destinationDir :
                    classFile.getSourceFilePath().getParent();
            String classFileName = String.format("%s.class", classFile.getClassName());
            Path filePath = dirPath != null ? dirPath.resolve(classFileName) : Paths.get(classFileName);

            try (DataOutputStream stream = new DataOutputStream(Files.newOutputStream(filePath))) {
                classFile.writeTo(stream);
            } catch (IOException ex) {
                System.err.printf("error: unable to write file: %s%n", ex.getMessage());
            }
        }
    }

    public Listener getListener() {
        return listener;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Args getArgs() {
        return args;
    }

    public static void main(String[] args) throws IOException {
        PrintListener listener = new PrintListener();
        int res = new CommandLine(new Compiler(listener)).execute(args);

        if (res != 0) {
            listener.printErrors();
            System.exit(res);
        }
    }
}