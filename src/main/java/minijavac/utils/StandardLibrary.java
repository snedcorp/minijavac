package minijavac.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import minijavac.ast.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StandardLibrary {

    private record Method(String name, String type, List<String> params, @JsonProperty("static") boolean isStatic) {}
    private record Field(String name, String type, @JsonProperty("static") boolean isStatic) {}
    private record Class(String name, List<Method> methods, List<Field> fields) {}
    private record Package(String name, List<Class> classes) {}
    private record Stdlib(List<Package> packages) {}

    /**
     * Constructs {@link ClassDecl} nodes for standard library classes, as defined in the {@code stdlib.yaml} file.
     * @return std lib classes
     */
    public static List<ClassDecl> getClasses() {
        List<ClassDecl> classes = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try (InputStream yamlStream = StandardLibrary.class.getResourceAsStream("/stdlib.yaml")) {

            Stdlib stdlib = mapper.readValue(yamlStream, Stdlib.class);
            for (Package pkg : stdlib.packages()) {
                for (Class cls : pkg.classes()) {
                    classes.add(getClass(cls, pkg.name));
                }
            }
        } catch (IOException ignore) {}
        return classes;
    }

    private static ClassDecl getClass(Class cls, String pkg) {
        List<FieldDecl> fields = new ArrayList<>();
        if (cls.fields() != null) {
            for (Field field : cls.fields()) {
                FieldDecl fieldDecl = FieldDecl.stdLib(field.isStatic(), false, getType(field.type),
                        Identifier.of(field.name));
                fields.add(fieldDecl);
            }
        }

        List<MethodDecl> methods = new ArrayList<>();
        if (cls.methods() != null) {
            for (Method method : cls.methods()) {
                FieldDecl fieldDecl = FieldDecl.stdLib(method.isStatic(), false, getType(method.type),
                        Identifier.of(method.name));
                ParameterDeclList parameterDeclList = new ParameterDeclList();
                for (String param : method.params) {
                    parameterDeclList.add(ParameterDecl.stdLib(getType(param)));
                }
                MethodDecl methodDecl = MethodDecl.stdLib(fieldDecl, parameterDeclList);
                methods.add(methodDecl);
            }
        }

        return ClassDecl.stdLib(String.format("java/%s/%s", pkg, cls.name), fields, methods, cls.name);
    }

    private static Type getType(String str) {
        return switch (str) {
            case "int" -> BaseType.stdLib(TypeKind.INT);
            case "float" -> BaseType.stdLib(TypeKind.FLOAT);
            case "boolean" -> BaseType.stdLib(TypeKind.BOOLEAN);
            case "void" -> BaseType.stdLib(TypeKind.VOID);
            default -> ClassType.stdLib(str);
        };
    }
}
