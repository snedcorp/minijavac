package minijavac.gen.file;

import minijavac.ast.MemberDecl;
import minijavac.gen._byte.U2;

import java.util.List;

public enum AccessFlag {

    ACC_PACKAGE_PRIVATE(0x0000),
    ACC_PUBLIC(0x0001),
    ACC_PRIVATE(0x0002),
    ACC_STATIC(0x0008),
    ACC_FINAL(0x0010),
    ACC_SUPER(0x0020);

    AccessFlag(int flag) {
       this.flag = flag;
    }

    private final int flag;

    public int getFlag() {
        return flag;
    }

    /**
     * Returns the access mask for the given flags
     * @param flags list of flags
     * @return mask
     */
    public static U2 mask(List<AccessFlag> flags) {
        int mask = flags.stream().reduce(0, (acc, f) -> acc + f.getFlag(), Integer::sum);
        return U2.of(mask);
    }

    /**
     * Returns the access mask for the given {@link MemberDecl}.
     * @param decl member declaration
     * @return mask
     */
    public static U2 mask(MemberDecl decl) {
        int mask = switch (decl.access) {
            case PACKAGE_PRIVATE -> ACC_PACKAGE_PRIVATE.getFlag();
            case PRIVATE -> ACC_PRIVATE.getFlag();
            case PUBLIC -> ACC_PUBLIC.getFlag();
        };
        if (decl.isStatic) {
            mask += AccessFlag.ACC_STATIC.getFlag();
        }
        if (decl.isFinal) {
            mask += AccessFlag.ACC_FINAL.getFlag();
        }
        return U2.of(mask);
    }
}
