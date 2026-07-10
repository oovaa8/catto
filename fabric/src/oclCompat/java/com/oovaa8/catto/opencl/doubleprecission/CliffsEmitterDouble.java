package com.oovaa8.catto.opencl.doubleprecission;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.opencl.CliffsNode;

public class CliffsEmitterDouble implements OpenCLCEmitter<CliffsNode> {
    public static final CliffsEmitterDouble INSTANCE = new CliffsEmitterDouble();
    public CliffsEmitterDouble() {
    }

    //context.callDelegate(height) on nodes
    //Double.toHexString on Doubles
    //just + on ints
    @Override
    public String doCLGen(CliffsNode node, OpenCLCGenContext context) {
        SteepnessEmitterDouble.EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD length = context.newMethod(node.length);
        return  "double h = " + context.callDelegate(height) + ";\n" +
                "double d = " + context.callDelegate(length) + "/" + node.worldHeight + ";\n" +
                "double r = d / 2.0;\n" +
                "return steepness_mod(h - " + Double.toHexString(node.ocean) + "+r,d) / r - 1.0;\n";
    }
}
