package minijavac.context;

import minijavac.ast.*;
import minijavac.context.enter.Enter;
import minijavac.context.err.ArgTypeError;
import minijavac.context.err.NoSuitableMethodError;
import minijavac.context.err.SymbolError;
import minijavac.err.CompileError;
import minijavac.listener.Listener;
import minijavac.syntax.Position;

import java.util.*;
import java.util.function.BiConsumer;

import static minijavac.context.Types.incompatibleTypes;
import static minijavac.context.Types.match;

/**
 * <pre>
 * Used by {@link Enter} and {@link Context} to record symbols and their corresponding declarations.
 *
 * Contains a scoped symbol table as well as data structures for class, method, and field lookup.
 *
 * Note: the same instance of this class is shared between all {@link Enter} and {@link Context} invocations - the class
 * and member information remains the same, but the scoped symbol table will change over the course of the traversals.
 *
 * Relies on the given {@link TraversalStateViewer} to access traversal data needed to construct errors.
 * </pre>
 */
public class SymbolTable {

    Listener listener;
    TraversalStateViewer traversalState;

    /*
    * Maintain stack of argument types for method invocations, to be used to resolve their corresponding method
    * declarations.
    * */
    public record ArgType(Position pos, Type type) {}
    public record ArgTypes(List<ArgType> list, String signature) {}
    private final Deque<ArgTypes> argTypesStack = new ArrayDeque<>();

    /**
     * <pre>
     * Contains method-related information for a class. Specifically:
     *   - Set of all method signatures.
     *   - Mapping of method names to {@link MethodDecl} instances.
     *     - Note that a method name can map to multiple declarations, as method overloading is supported.
     * </pre>
     */
    record Methods(Set<String> signatures, Map<String, List<MethodDecl>> idMap){}

    /**
     * <pre>
     * Contains constructor-related information for a class. Specifically:
     *   - Set of all constructor signatures.
     *   - List of all constructor method declarations.
     * </pre>
     */
    record Constructors(Set<String> signatures, List<MethodDecl> decls){}

    /**
     * Contains all relevant member data for a class. Includes a mapping of field
     * names to field declarations, plus method and constructor signatures and mappings.
     */
    record Members(Map<String, FieldDecl> fields, Methods methods, Constructors constructors){}

    /**
     * Maps class names to their corresponding {@link ClassDecl} instance.
     */
    private final Map<String, ClassDecl> classMap = new HashMap<>();

    /**
     * Maps class names to their corresponding {@link SymbolTable.Members} instance.
     */
    private final Map<String, Members> membersByClass = new HashMap<>();

    /**
     * Scoped symbol table mapping names to declarations.
     * <br><br>
     * Last map in list belongs to the current scope.
     */
    private final List<Map<String, Declaration>> table = new ArrayList<>();

    /**
     * {@link SymbolTable.Members} instance corresponding to the class currently being traversed.
     */
    private Members currMembers;

    public SymbolTable(Listener listener) {
        this.listener = listener;
    }

    /**
     * Attempts to add the given class decl and, if successful, readies the {@link SymbolTable} for the traversal of
     * that class.
     * @param classDecl class to be traversed
     * @param state     traversal state viewer
     * @return whether class could be added
     */
    public boolean addAndEnterClass(ClassDecl classDecl, TraversalStateViewer state) {
        boolean added = addClassDecl(classDecl);
        if (added) enterClass(classDecl, state);
        return added;
    }

    /**
     * Readies the {@link SymbolTable} for the traversal of that class.
     * @param classDecl class to be traversed
     * @param state     traversal state viewer
     */
    public void enterClass(ClassDecl classDecl, TraversalStateViewer state) {
        this.traversalState = state;
        this.currMembers = membersByClass.get(classDecl.id.contents);
    }

    /**
     * Removes all state related to a class's traversal.
     */
    public void exitClass() {
        this.traversalState = null;
        this.currMembers = null;
        this.argTypesStack.clear();
    }

    /**
     * Attempts to add entries for the given {@link ClassDecl} to the {@link #classMap} and {@link #membersByClass}
     * maps.
     * <br><br>
     * Logs error if class is a duplicate.
     * @param decl class declaration
     */
    public boolean addClassDecl(ClassDecl decl) {
        if (classMap.containsKey(decl.id.contents)) {
            listener.err(new CompileError(decl.pos, String.format("duplicate class: %s", decl.id.contents)));
            return false;
        }
        classMap.put(decl.id.contents, decl);

        Members members = new Members(new HashMap<>(), new Methods(new HashSet<>(), new HashMap<>()),
                new Constructors(new HashSet<>(), new ArrayList<>()));
        membersByClass.put(decl.id.contents, members);

        if (decl.shortName != null) {
            classMap.put(decl.shortName, decl);
            membersByClass.put(decl.shortName, members);
        }

        return true;
    }

    /**
     * Adds given {@link FieldDecl} to current {@link SymbolTable.Members} instance.
     * <br><br>
     * Logs error if field name is a duplicate.
     * @param decl field decl
     */
    public void addFieldDecl(FieldDecl decl) {
        if (currMembers.fields().containsKey(decl.id.contents)) {
            listener.err(new CompileError(decl.id.pos,
                    String.format("variable %s is already defined in class %s", decl.id.contents, traversalState.getCurrClass().id.contents)));
            return;
        }
        currMembers.fields().put(decl.id.contents, decl);
    }

    /**
     * Adds given {@link MethodDecl} to current {@link SymbolTable.Members} instance.
     * <br><br>
     * Logs error if method signature is a duplicate.
     * @param decl method decl
     */
    public void addMethodOrConstructorDecl(MethodDecl decl) {
        if (decl.isConstructor()) addConstructorDecl(decl);
        else addMethodDecl(decl);
    }

    /**
     * Adds given {@link MethodDecl} to current {@link SymbolTable.Members} instance.
     * <br><br>
     * Logs error if method signature is a duplicate.
     * @param decl method decl
     */
    public void addMethodDecl(MethodDecl decl) {
        Methods methods = currMembers.methods();
        if (methods.signatures().contains(decl.signature)) {
            listener.err(new CompileError(decl.id.pos,
                    String.format("method %s is already defined in class %s", decl.signature, traversalState.getCurrClass().id.contents)));
            return;
        }

        methods.signatures().add(decl.signature);

        List<MethodDecl> methodsByName = methods.idMap().get(decl.id.contents);
        if (methodsByName != null) {
            methodsByName.add(decl);
        } else {
            List<MethodDecl> methodList = new ArrayList<>();
            methodList.add(decl);
            methods.idMap().put(decl.id.contents, methodList);
        }
    }

    /**
     * Adds given constructor {@link MethodDecl} to current {@link SymbolTable.Members} instance.
     * <br><br>
     * Logs error if constructor signature is a duplicate.
     * @param decl constructor decl
     */
    public void addConstructorDecl(MethodDecl decl) {
        if (currMembers.constructors().signatures().contains(decl.signature)) {
            listener.err(new CompileError(decl.id.pos,
                    String.format("constructor %s is already defined in class %s", decl.signature, traversalState.getCurrClass().id.contents)));
            return;
        }

        currMembers.constructors().signatures().add(decl.signature);
        currMembers.constructors().decls().add(decl);
    }

    /**
     * If the current class has no constructors defined, creates a {@link MethodDecl} node for a default constructor and
     * adds it to the {@link SymbolTable}.
     * @return default constructor, if created
     */
    public MethodDecl addDefaultConstructorIfNecessary() {
        if (currMembers.constructors().signatures().isEmpty()) {
            MethodDecl defaultConstructor = MethodDecl.defaultConstructor(traversalState.getCurrClass().id.contents);
            addConstructorDecl(defaultConstructor);
            return defaultConstructor;
        }
        return null;
    }

    /**
     * Adds a new level to the table, to contain the identifier-to-declaration mappings for a new scope.
     * <br><br>
     * Called when entering into a method, conditional statement, or loop.
     */
    public void pushScope() {
        table.add(new HashMap<>());
    }

    /**
     * Removes the most recently added level from the table.
     * <br><br>
     * Called when exiting out of a method, conditional statement, or loop.
     */
    public void popScope() {
        table.remove(table.size() - 1);
    }

    /**
     * Pushes the argument types for a method invocation onto the stack.
     * @param argTypes argument types
     */
    public void pushArgTypes(ArgTypes argTypes) {
        argTypesStack.push(argTypes);
    }

    /**
     * Adds a local variable declaration ({@link ParameterDecl} or {@link VarDecl}) to the table in the current
     * scope - if variable is not already declared in the current scope or any of its enclosing scopes.
     * <br><br>
     * Logs error if variable is a duplicate.
     * @param decl Local variable declaration
     */
    public void addLocalDecl(LocalDecl decl) {
        for (int i = table.size()-1; i >= 0; i--) {
            if (table.get(i).containsKey(decl.id.contents)) {
                String err = String.format("variable %s is already defined in method %s", decl.id.contents,
                        decl instanceof ParameterDecl ? traversalState.getCurrMethod().id.contents : traversalState.getCurrMethod().signature);
                listener.err(new CompileError(decl.id.pos, err));
                return;
            }
        }
        table.get(table.size()-1).put(decl.id.contents, decl);
    }

    /**
     * <pre>
     * Retrieves the {@link Declaration} corresponding to an {@link Identifier}.
     *
     * Steps:
     *   1) If looking for a method, retrieve method candidates for the given identifier. If none exist, log error
     *      and return. Otherwise, return result from {@link #getMethodDecl(List, Identifier, boolean) getMethodDecl}
     *      call with those candidates.
     *   2) Otherwise, iterate upwards through the table starting from the current scope and return if there's a
     *      match for the given identifier.
     *   3) If no match found, then see if it matches a field declaration in the current class, and return if found.
     *   4) If no match found, then see if it matches any class declaration, and return if found.
     *   5) If still no match found, then log {@link SymbolError}.
     * </pre>
     * @param id       identifier
     * @param isMethod whether identifier is part of a call reference
     * @return declaration
     */
    public Declaration getDecl(Identifier id, boolean isMethod) {
        if (isMethod) {
            List<MethodDecl> methodCandidates = currMembers.methods().idMap().get(id.contents);
            if (methodCandidates == null) {
                SymbolError err = SymbolError.builder()
                        .position(id.pos)
                        .methodSymbol(id.contents, argTypesStack.pop().signature())
                        .classLocation(traversalState.getCurrClass().id.contents)
                        .build();
                listener.err(err);
                return null;
            }
            return getMethodDecl(methodCandidates, id, false);
        }

        for (int i = table.size()-1; i >= 0; i--) {
            if (table.get(i).containsKey(id.contents)) {
                return table.get(i).get(id.contents);
            }
        }

        if (currMembers.fields().containsKey(id.contents)) {
            return currMembers.fields().get(id.contents);
        }

        if (classMap.containsKey(id.contents)) {
            return classMap.get(id.contents);
        }

        SymbolError err = SymbolError.builder()
                .position(id.pos)
                .variableSymbol(id.contents)
                .classLocation(traversalState.getCurrClass().id.contents)
                .build();

        listener.err(err);
        return null;
    }

    /**
     * Retrieves constructor {@link MethodDecl} for a given class name.
     * @param id class name
     * @return method declaration
     */
    public MethodDecl getConstructorDecl(Identifier id) {
        List<MethodDecl> candidates = membersByClass.get(id.contents).constructors().decls();
        return getMethodDecl(candidates, id, true);
    }

    /**
     * Receives a list of method declaration candidates for a given call reference, and using the {@link ArgTypes}
     * list at the top of the {@link #argTypesStack}, searches within those candidates for a method whose signature
     * matches those argument types exactly.
     * @param methodCandidates method declaration candidates selected by the caller
     * @param id               method name
     * @param isConstructor    {@code true} if candidates are constructors
     * @return method declaration
     */
    public MethodDecl getMethodDecl(List<MethodDecl> methodCandidates, Identifier id, boolean isConstructor) {
        // pop argument types from most recently visited call
        ArgTypes argTypes = argTypesStack.pop();

        /*
         * If only one candidate:
         *   - Log arg type error if arguments are the wrong size, otherwise:
         *   - Attempt type matching between arguments and parameters:
         *     - If errors occur -> log them and return null
         *     - If no errors -> return candidate
         * */
        if (methodCandidates.size() == 1) {
            MethodDecl decl = methodCandidates.get(0);
            if (argTypes.list().size() != decl.parameterDeclList.size()) {
                listener.err(new ArgTypeError(id.pos, decl, argTypes.signature()));
                return null;
            }

            if (argTypesMatch(argTypes.list(), decl, publishMismatchConsumer())) return decl;
            return null;
        }

        // filter candidates down to only those with parameter lists the same size as the argument list
        List<MethodDecl> sameSizeMethodCandidates = methodCandidates.stream()
                .filter(md -> md.parameterDeclList.size() == argTypes.list().size())
                .toList();

        // if no candidates remain after filtering, log generic no suitable method found error
        if (sameSizeMethodCandidates.isEmpty()) {
            listener.err(new NoSuitableMethodError(id, argTypes.signature(), methodCandidates, isConstructor));
            return null;
        }

        /*
         * If only one candidate remains after filtering, attempt type matching between arguments and parameters:
         *   - If errors occur -> log them and return null
         *   - If no errors -> return candidate
         * */
        if (sameSizeMethodCandidates.size() == 1) {
            MethodDecl decl = sameSizeMethodCandidates.get(0);
            if (argTypesMatch(argTypes.list(), decl, publishMismatchConsumer())) return decl;
            return null;
        }

        /*
         * If multiple candidates remain after filtering, iteratively attempt type matching between arguments and
         * each candidate's parameters:
         *   - If a candidate matches, exit loop by returning it
         *   - If a candidate doesn't match, append mismatch details to list
         * If no candidates end up matching, then a no suitable method error is logged, with each candidate's
         * mismatch details passed in for printing purposes.
         * */
        List<String> mismatches = new ArrayList<>();
        for (MethodDecl decl : sameSizeMethodCandidates) {
            if (argTypesMatch(argTypes.list(), decl, storeMismatchConsumer(mismatches))) return decl;
        }

        listener.err(new NoSuitableMethodError(id, argTypes.signature(), sameSizeMethodCandidates, isConstructor,
                mismatches));
        return null;
    }

    /**
     * @return {@link BiConsumer} that consumes mismatched types and logs the resulting errors.
     */
    private BiConsumer<ArgType, Type> publishMismatchConsumer() {
        return (ArgType argType, Type paramType) ->
                listener.err(incompatibleTypes(argType.pos, argType.type(), paramType));
    }

    /**
     * @param mismatches running list of mismatches
     * @return {@link BiConsumer} that consumes mismatched types and appends them to given list
     */
    private BiConsumer<ArgType, Type> storeMismatchConsumer(List<String> mismatches) {
        return (ArgType argType, Type paramType) ->
                mismatches.add(String.format("%s cannot be converted to %s", argType.type().print(), paramType.print()));
    }

    /**
     * Evaluates whether the given argument types are valid for the given method.
     * <br><br>
     * If valid, return {@code true}.
     * If invalid, pass mismatched types to given consumer, and return {@code false}.
     * @param argTypes         list of argument types
     * @param decl             target method declaration
     * @param mismatchConsumer consumes mismatched types
     * @return argument validity
     */
    private boolean argTypesMatch(List<ArgType> argTypes, MethodDecl decl, BiConsumer<ArgType, Type> mismatchConsumer) {
        for (int i=0; i<argTypes.size(); i++) {
            ArgType argType = argTypes.get(i);
            Type paramType = decl.parameterDeclList.get(i).type;
            if (!match(argType.type(), paramType)) {
                mismatchConsumer.accept(argType, paramType);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@link ClassDecl} for given class name.
     * <br><br>
     * Logs error if no entry for given class name is found.
     * @param id class name
     * @return class declaration
     */
    public Declaration getClassDecl(Identifier id) {
        Declaration decl = classMap.get(id.contents);
        if (decl == null) {
            SymbolError err = SymbolError.builder()
                    .position(id.pos)
                    .classSymbol(id.contents)
                    .classLocation(traversalState.getCurrClass().id.contents)
                    .build();
            listener.err(err);
        }
        return decl;
    }

    /**
     * Retrieves declaration for a member on a specific class.
     * @param className class name
     * @param id        member identifier
     * @param isMethod  if member should be a method
     * @param prevId    previous non-method identifier in reference chain, if applicable (used for error printing)
     * @return member declaration
     */
    public MemberDecl getMemberDecl(String className, Identifier id, boolean isMethod, Identifier prevId) {
        // retrieve members for relevant class
        Members members = membersByClass.get(className);
        // short circuit if invalid class or no members
        if (members == null) return null;

        // find method candidates - if none found, log error, otherwise search for target
        if (isMethod) {
            List<MethodDecl> methodDecls = members.methods().idMap().get(id.contents);
            if (methodDecls == null) {
                listener.err(SymbolError.builder()
                        .position(id.pos)
                        .methodSymbol(id.contents, argTypesStack.pop().signature())
                        .location(className, prevId)
                        .build());
                return null;
            }
            // not a constructor, inside qualified ref
            return getMethodDecl(methodDecls, id, false);
        }

        // if not a method, must be a field
        FieldDecl fieldDecl = members.fields().get(id.contents);
        if (fieldDecl == null) {
            listener.err(SymbolError.builder()
                    .position(id.pos)
                    .variableSymbol(id.contents)
                    .location(className, prevId)
                    .build());
            return null;
        }
        return fieldDecl;
    }

}
