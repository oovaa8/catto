package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class CliffsEmitter implements OpenCLCEmitter<CliffsNode> {
    public static final CliffsEmitter INSTANCE = new CliffsEmitter();
    public CliffsEmitter() {
    }

    //context.callDelegate(height) on nodes
    //Float.toHexString on Floats
    //just + on ints
    @Override
    public String doCLGen(CliffsNode node, OpenCLCGenFunctionContext context, String s) {
        SteepnessEmitter.EmitHelpers(context.getGlobalContext());
        ValuesMethodDefF64 height = context.newVarF64(node.height);
        ValuesMethodDefF64 length = context.newVarF64(node.length);
        return  "float h = (float)(" + context.getDelegateVar(height) + ");\n" +
                "float d = (float)(" + context.getDelegateVar(length) + ")/" + node.worldHeight + ";\n" +
                "float r = d / 2.0f;\n" +
                s + "= steepness_mod(h - " + literal(node.ocean) + "+r,d) / r - 1.0f;\n";
    }
}
