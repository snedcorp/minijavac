package minijavac.gen.file;

import minijavac.ast.*;
import minijavac.gen.constant.ConstantPool;
import minijavac.gen._byte.U2;
import minijavac.gen._byte.U4;
import minijavac.gen.attribute.Attribute;
import minijavac.gen.attribute.CodeAttribute;
import minijavac.gen.attribute.SourceFileAttribute;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a Java .class file
 */
public class ClassFile implements Writable {

    private final U4 magic;
    private final U2 minorVersion;
    private final U2 majorVersion;
    private final ConstantPool constantPool;
    private final String className;
    private U2 accessFlags;
    private final U2 thisClass;
    private final U2 superClass;
    // interfaces
    private final List<FieldEntry> fields;
    private final List<MethodEntry> methods;
    private final List<Attribute> attributes;
    private Path sourceFilePath;


    public ClassFile(String className) {
        magic = U4.of(0xCAFEBABE);
        majorVersion = U2.of(61); // Java 17
        minorVersion = U2.of(0);
        constantPool = new ConstantPool();
        this.className = className;
        /*
        * "In Java SE 8 and above, the Java Virtual Machine considers the ACC_SUPER flag to be set in every class file,
        * regardless of the actual value of the flag in the class file and the version of the class file."
        * */
        accessFlags = AccessFlag.mask(List.of(AccessFlag.ACC_SUPER));
        thisClass = constantPool.addClassConstant(className);
        superClass = constantPool.addClassConstant("java/lang/Object");
        fields = new ArrayList<>();
        methods = new ArrayList<>();
        attributes = new ArrayList<>();
    }

    @Override
    public void writeTo(DataOutputStream stream) throws IOException {
        magic.writeTo(stream);                    // write magic
        minorVersion.writeTo(stream);             // write minor_version
        majorVersion.writeTo(stream);             // write major_version
        constantPool.writeTo(stream);             // write constant_pool_count, constant_pool[]
        accessFlags.writeTo(stream);              // write access_flags
        thisClass.writeTo(stream);                // write this_class
        superClass.writeTo(stream);               // write super_class
        U2.of(0).writeTo(stream);             // write interfaces_count
                                                  // skip interfaces[]
        U2.of(fields.size()).writeTo(stream);     // write fields_count
        for (FieldEntry fieldEntry : fields) {    // write fields[]
            fieldEntry.writeTo(stream);
        }

        U2.of(methods.size()).writeTo(stream);    // write methods_count
        for (MethodEntry methodEntry : methods) { // write methods[]
            methodEntry.writeTo(stream);
        }

        U2.of(attributes.size()).writeTo(stream); // write attributes_count
        for (Attribute attribute : attributes) {  // write attributes[]
            attribute.writeTo(stream);
        }
    }

    /**
     * Adds a {@link SourceFileAttribute} to the class file.
     * @param file source file name
     */
    public void addSourceFileAttribute(Path file) {
        sourceFilePath = file;
        U2 nameIndex = constantPool.addUTFConstant("SourceFile");
        U2 fileIndex = constantPool.addUTFConstant(file.getFileName().toString());
        SourceFileAttribute sourceFileAttribute = new SourceFileAttribute(nameIndex, fileIndex);
        attributes.add(sourceFileAttribute);
    }

    /**
     * Adds {@link MethodEntry} for the given method declaration to the class file.
     * @param methodDecl
     * @return entry
     */
    public MethodEntry addMethod(MethodDecl methodDecl) {
        MethodEntry methodEntry = new MethodEntry();
        methodEntry.setAccessFlags(AccessFlag.mask(methodDecl));

        String methodName = methodDecl.isConstructor() ? "<init>" : methodDecl.id.contents;
        methodEntry.setNameIndex(constantPool.addUTFConstant(methodName));
        methodEntry.setDescriptorIndex(constantPool.addUTFConstant(methodDecl.descriptor()));
        methodEntry.setCodeAttribute(CodeAttribute.create(constantPool, methodDecl));

        methods.add(methodEntry);
        return methodEntry;
    }

    public CodeAttribute getCode() {
        return methods.get(methods.size()-1).getCodeAttribute();
    }

    public U4 getMagic() {
        return magic;
    }

    public U2 getMinorVersion() {
        return minorVersion;
    }

    public U2 getMajorVersion() {
        return majorVersion;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public U2 getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(U2 accessFlags) {
        this.accessFlags = accessFlags;
    }

    public U2 getThisClass() {
        return thisClass;
    }

    public U2 getSuperClass() {
        return superClass;
    }

    public List<FieldEntry> getFields() {
        return fields;
    }

    public List<MethodEntry> getMethods() {
        return methods;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public String getClassName() {
        return className;
    }

    public Path getSourceFilePath() {
        return sourceFilePath;
    }
}
