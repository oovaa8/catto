package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class CliffsInvertedEmitter implements OpenCLCEmitter<CliffsInvertedNode> {
    public static final CliffsInvertedEmitter INSTANCE = new CliffsInvertedEmitter();
    public CliffsInvertedEmitter() {
    }
    /*
    AstNode height;
    AstNode cliffs;
    AstNode raw_height;
    AstNode length;
    AstNode flatness;
    AstNode steepness;
    AstNode noise;
    int worldHeight;
    float ocean;
    int min_y;*/
    //context.callDelegate(height) on nodes
    //Float.toHexString on Floats
    //just + on ints
    @Override
    public String doCLGen(CliffsInvertedNode node, OpenCLCGenFunctionContext context, String s) {
        SteepnessEmitter.EmitHelpers(context.getGlobalContext());
        ValuesMethodDefF64 height = context.newVarF64(node.height);
        ValuesMethodDefF64 cliffs = context.newVarF64(node.cliffs);
        ValuesMethodDefF64 raw_height = context.newVarF64(node.raw_height);
        ValuesMethodDefF64 length = context.newVarF64(node.length);
        ValuesMethodDefF64 flatness = context.newVarF64(node.flatness);
        ValuesMethodDefF64 steepness = context.newVarF64(node.steepness);
        ValuesMethodDefF64 noise = context.newVarF64(node.noise);
        return  "float height_map = (float)(" + context.getDelegateVar(height) + ");\n" +
                "float h = height_map - (2.0f*(ctx.y - " + node.min_y + ") / " + literal(node.worldHeight) + " - 1.0f);\n" +
                "float s = (float)(" + context.getDelegateVar(steepness) + ");\n"+
                "if(s<0.0f) {\n" +
                    "float f = (float)(" + context.getDelegateVar(flatness) + ");\n" +
                    "float c = (float)(" + context.getDelegateVar(cliffs) + ");\n" +
                    "float t = 0.15f * -s * min(f * 20.0f, 1.0f);\n" +
                    "if(c>=0.0f && c<t) {\n" +
                        "float d = (float)(" + context.getDelegateVar(length) + ")/" + node.worldHeight + ";\n" +
                        "float hro = d / 2.0f - " + literal(node.ocean) + " + (float)(" + context.getDelegateVar(raw_height) + ");\n" +
                        "int i = (int)(floor(hro/d));\n" +
                        "float w = ((float)(" + context.getDelegateVar(noise) + ") + 1.0f) * 0.25f + 0.5f;\n" +
                        "float gap = heightOffset(i,w);\n" +
                        "float l = 2.0f*(h/(d*f*gap)) - 1.0f - 0.05f;\n" +
                        "if(l>=-1.0 && l<=1.0) {\n" +
                            "c /= t;\n" +
                            s + " =  (c*c + l*l) >= 0.9f ? h : -h;\n" +
                        "} else {" + s + " = h;} \n" +
                    "} else {" + s + " = h;} \n" +
                "} else {" + s + " = h;}\n";
    }
}
