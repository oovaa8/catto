package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;

public class CliffsEmitter implements OpenCLCEmitter<CliffsNode> {
    public static final CliffsEmitter INSTANCE = new CliffsEmitter();
    public CliffsEmitter() {
    }

    //context.callDelegate(height) on nodes
    //Float.toHexString on Floats
    //just + on ints
    @Override
    public String doCLGen(CliffsNode node, OpenCLCGenContext context) {
        SteepnessEmitter.EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD length = context.newMethod(node.length);
        return  "float h = " + context.callDelegate(height) + ";\n" +
                "float d = " + context.callDelegate(length) + "/" + node.worldHeight + ";\n" +
                "float r = d / 2.0f;\n" +
                "return steepness_mod(h - " + Float.toHexString((float) node.ocean) + "+r,d) / r - 1.0f;\n";
    }
}
