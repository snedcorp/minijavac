package minijavac.gen.constant;

import minijavac.ast.AST;
import minijavac.ast.FieldDecl;
import minijavac.ast.MethodDecl;
import minijavac.gen._byte.U2;
import minijavac.gen.file.Writable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object representation of the run-time constant pool for a Java class.
 * <br><br>
 * The constant pool is a table of entries containing different kinds of symbolic references or static constants needed
 * for execution. Each entry starts with a 1-byte tag indicating the constant type, and is then followed by two or more
 * bytes detailing the constant's actual data, formatted in a type-dependent manner.
 * <br><br>
 * Entries are referred to by their index in the constant pool, which is stored as a {@link U2} and starts at 1 (i.e.
 * NOT zero-indexed). Not only can JVM instructions use these indices to access an entry's data, but so can other
 * entries themselves, as many of the constant types store their data across several different, lower-level entry types.
 * <br><br>
 * Because many constants tend to be referenced multiple times at different locations throughout a class, caches for
 * each constant type are maintained here in order to prevent duplicate entries and reduce bytecode size.
 * </pre>
 */
public class ConstantPool implements Writable {
    private final List<ConstantEntry> constantPool;

    private final Map<String, U2> utfConstantMap;
    private final Map<Integer, U2> intConstantMap;
    private final Map<Integer, U2> floatConstantMap;
    private final Map<String, U2> classConstantMap;
    private final Map<String, U2> refConstantMap;
    private final Map<String, U2> nameAndTypeConstantMap;

    public ConstantPool() {
        this.constantPool = new ArrayList<>();
        this.utfConstantMap = new HashMap<>();
        this.intConstantMap = new HashMap<>();
        this.floatConstantMap = new HashMap<>();
        this.classConstantMap = new HashMap<>();
        this.refConstantMap = new HashMap<>();
        this.nameAndTypeConstantMap = new HashMap<>();
    }

    /**
     * Adds constant entry to the constant pool and returns its index within the pool.
     * <br><br>
     * Note: the constant pool is NOT zero-indexed, so the index is equal to the pool's size after adding.
     * @param constantEntry
     * @return index of entry
     */
    private U2 addConstant(ConstantEntry constantEntry) {
        constantPool.add(constantEntry);
        return U2.of(constantPool.size());
    }

    /**
     * Retrieves the {@link String} value for the {@link ClassConstant} entry at a given index in the constant pool.
     * @param index constant pool index
     * @return class name
     */
    public String getClassConstant(int index) {
        ClassConstant classConstant = (ClassConstant) constantPool.get(index-1);
        return getUTFConstant(classConstant.getNameIndex().getVal());
    }

    /**
     * Retrieves the {@link String} value for the {@link UTF8Constant} entry at a given index in the constant pool.
     * @param index constant pool index
     * @return constant value
     */
    public String getUTFConstant(int index) {
        UTF8Constant utf8Constant = (UTF8Constant) constantPool.get(index-1);
        return utf8Constant.getStr();
    }

    /**
     * Retrieves the descriptor {@link String} for the {@link RefConstant} entry at a given index in the constant pool.
     * @param index constant pool index
     * @return descriptor
     */
    public String getRefDescriptor(int index) {
        RefConstant fieldRef = (RefConstant) constantPool.get(index-1);
        NameAndTypeConstant nameAndType = (NameAndTypeConstant)
                constantPool.get(fieldRef.getNameAndTypeIndex().getVal()-1);
        return getUTFConstant(nameAndType.getDescriptorIndex().getVal());
    }

    /**
     * Adds a {@link ClassConstant} entry for the given class name to the constant pool.
     * <br><br>
     * Note: if entry for class name already exists in the constant pool, index of existing entry is returned.
     * @param constant class name
     * @return entry index
     */
    public U2 addClassConstant(String constant) {
        if (classConstantMap.containsKey(constant)) return classConstantMap.get(constant);
        ClassConstant classConstant = new ClassConstant();
        U2 classIndex = addConstant(classConstant);
        classConstant.setNameIndex(addUTFConstant(constant));
        classConstantMap.put(constant, classIndex);
        return classIndex;
    }

    /**
     * Adds a {@link RefConstant} entry for the given {@link FieldDecl} to the constant pool.
     * <br><br>
     * Note: if entry for field already exists in the constant pool, index of existing entry is returned.
     * @param fieldDecl field declaration
     * @return entry index
     */
    public U2 addFieldRefConstant(FieldDecl fieldDecl) {
        return addRefConstant(RefConstant.field(), fieldDecl.classDecl.id.contents,
                fieldDecl.id.contents, fieldDecl.type.descriptor());
    }

    /**
     * Adds a {@link RefConstant} entry for the given {@link MethodDecl} to the constant pool.
     * <br><br>
     * Note: if entry for method already exists in the constant pool, index of existing entry is returned.
     * @param methodDecl method declaration
     * @return entry index
     */
    public U2 addMethodRefConstant(MethodDecl methodDecl) {
        return addRefConstant(RefConstant.method(), methodDecl.classDecl.id.contents,
                methodDecl.id.contents, methodDecl.descriptor());
    }

    /**
     * Adds a {@link RefConstant} entry for the given constructor {@link MethodDecl} to the constant pool, using the
     * special name {@code <init>} as its name.
     * <br><br>
     * Note: if entry for constructor already exists in the constant pool, index of existing entry is returned.
     * @param methodDecl constructor method declaration
     * @return entry index
     */
    public U2 addConstructorMethodRefConstant(MethodDecl methodDecl) {
        return addRefConstant(RefConstant.method(), methodDecl.classDecl.id.contents,
                "<init>", methodDecl.descriptor());
    }

    /**
     * Adds a {@link RefConstant} entry to the constant pool for a method that exists outside of the {@link AST} for the
     * current {@link Package}.
     * @param className  class name
     * @param refName    method name
     * @param descriptor method descriptor
     * <br><br>
     * Note: if entry for method already exists in the constant pool, index of existing entry is returned.
     * @return entry index
     */
    public U2 addMethodRefConstant(String className, String refName, String descriptor) {
        return addRefConstant(RefConstant.method(), className, refName, descriptor);
    }

    /**
     * Base method for adding a {@link RefConstant} for a given member to the constant pool.
     * <br><br>
     * Entry is added first, then its component entries are added, then entry is updated with component indices.
     * <br><br>
     * Note: if entry for member already exists in the constant pool, index of existing entry is returned.
     * @param refConstant ref constant object (only tag is set)
     * @param className   class name
     * @param refName     member name
     * @param descriptor  member descriptor
     * @return entry index
     */
    private U2 addRefConstant(RefConstant refConstant, String className, String refName, String descriptor) {
        String key = className + refName + descriptor;
        U2 refIndex = refConstantMap.get(key);
        if (refIndex != null) return refIndex;

        refIndex = addConstant(refConstant);
        refConstant.setClassIndex(addClassConstant(className));
        refConstant.setNameAndTypeIndex(addNameAndTypeConstant(refName, descriptor));
        refConstantMap.put(key, refIndex);
        return refIndex;
    }

    /**
     * Adds a {@link NameAndTypeConstant} entry for the given member to the constant pool.
     * <br><br>
     * Note: if entry for member already exists in the constant pool, index of existing entry is returned.
     * @param name       member name
     * @param descriptor member descriptor
     * @return entry index
     */
    public U2 addNameAndTypeConstant(String name, String descriptor) {
        String key = name + descriptor;
        U2 nameAndTypeIndex = nameAndTypeConstantMap.get(key);
        if (nameAndTypeIndex != null) return nameAndTypeIndex;

        NameAndTypeConstant nameAndTypeConstant = new NameAndTypeConstant();
        nameAndTypeIndex = addConstant(nameAndTypeConstant);
        nameAndTypeConstant.setNameIndex(addUTFConstant(name));
        nameAndTypeConstant.setDescriptorIndex(addUTFConstant(descriptor));
        nameAndTypeConstantMap.put(key, nameAndTypeIndex);
        return nameAndTypeIndex;
    }

    /**
     * Adds a {@link UTF8Constant} entry for the given string to the constant pool.
     * <br><br>
     * Note: if entry for string already exists in the constant pool, index of existing entry is returned.
     * @param constant string
     * @return entry index
     */
    public U2 addUTFConstant(String constant) {
        U2 index = utfConstantMap.get(constant);
        if (index != null) return index;
        index = addConstant(new UTF8Constant(constant));
        utfConstantMap.put(constant, index);
        return index;
    }

    /**
     * Adds a {@link IntConstant} entry for the given integer to the constant pool.
     * <br><br>
     * Note: if entry for integer already exists in the constant pool, index of existing entry is returned.
     * @param constant integer
     * @return entry index
     */
    public U2 addIntConstant(int constant) {
        U2 index = intConstantMap.get(constant);
        if (index != null) return index;
        index = addConstant(new IntConstant(constant));
        intConstantMap.put(constant, index);
        return index;
    }

    /**
     * Adds a {@link FloatConstant} entry for the integer representation of the given float to the constant pool.
     * <br><br>
     * Note: if entry for float already exists in the constant pool, index of existing entry is returned.
     * @param constant float
     * @return entry index
     */
    public U2 addFloatConstant(float constant) {
        int intRep = Float.floatToIntBits(constant);
        U2 index = floatConstantMap.get(intRep);
        if (index != null) return index;
        index = addConstant(new FloatConstant(intRep));
        floatConstantMap.put(intRep, index);
        return index;
    }

    /**
     * Writes constant pool to the given byte stream.
     * @param stream byte stream
     * @throws IOException
     */
    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        U2.of(constantPool.size()+1).writeTo(stream);  // write constant_pool_count (num entries + 1)
        for (ConstantEntry constantEntry : constantPool) { // write constant_pool[]
            constantEntry.writeTo(stream);
        }
    }
}
