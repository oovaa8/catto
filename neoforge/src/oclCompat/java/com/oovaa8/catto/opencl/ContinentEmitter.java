package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class ContinentEmitter implements OpenCLCEmitter<ContinentNode> {
    public static final ContinentEmitter INSTANCE = new ContinentEmitter();

    private final Set<OpenCLCGenContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());
    @Override
    public String doCLGen(ContinentNode continentNode, OpenCLCGenFunctionContext context, String s) {
        if(INSTANCE.helpersEmitted.add(context.getGlobalContext())){
            context.getGlobalContext().appendRaw(ContinentHelpers.random_hash);
            context.getGlobalContext().appendRaw(ContinentHelpers.simplex);
            context.getGlobalContext().appendRaw(ContinentHelpers.main);
        }

        ValuesMethodDefF64 noise = context.newVarF64(continentNode.noise);

        return s  + " = get_continent_height(ctx.x, ctx.z, " + context.getDelegateVar(noise) + ", "+ (int)(continentNode.seed) + ");\n";
    }
}