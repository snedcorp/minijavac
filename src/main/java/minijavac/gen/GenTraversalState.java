package minijavac.gen;

import minijavac.gen.attribute.CodeAttribute;
import minijavac.gen.constant.ConstantPool;
import minijavac.gen.file.ClassFile;
import minijavac.utils.TraversalState;

/**
 * Maintains the state of the current {@link minijavac.ast.ClassDecl} traversal being undertaken by the
 * {@link Generator} visitor implementation.
 */
public class GenTraversalState extends TraversalState {

    /*
    * Class file instance being constructed by the current traversal
    * */
    private ClassFile classFile;

    public ClassFile getClassFile() {
        return classFile;
    }

    public void setClassFile(ClassFile classFile) {
        this.classFile = classFile;
    }

    public CodeAttribute getCode() {
        return classFile.getCode();
    }

    public ConstantPool getConstantPool() {
        return classFile.getConstantPool();
    }
}
