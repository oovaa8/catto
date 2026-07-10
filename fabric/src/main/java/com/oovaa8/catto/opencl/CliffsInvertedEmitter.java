package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;

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
    public String doCLGen(CliffsInvertedNode node, OpenCLCGenContext context) {
        SteepnessEmitter.EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD cliffs = context.newMethod(node.cliffs);
        ValuesMethodDefD raw_height = context.newMethod(node.raw_height);
        ValuesMethodDefD length = context.newMethod(node.length);
        ValuesMethodDefD flatness = context.newMethod(node.flatness);
        ValuesMethodDefD steepness = context.newMethod(node.steepness);
        ValuesMethodDefD noise = context.newMethod(node.noise);
        return  "float height_map = " + context.callDelegate(height) + ";\n" +
                "float h = height_map - (2.0f*(ctx.y - " + node.min_y + ") / " + node.worldHeight + " - 1.0f);\n" +
                "float s = " + context.callDelegate(steepness) + ";\n"+
                "if(s>=0.0f) return h;\n" +
                "float f = " + context.callDelegate(flatness) + ";\n" +
                "float c = " + context.callDelegate(cliffs) + ";\n" +
                "float t = 0.15f * -s * min(f * 20.0f, 1.0f);\n" +
                "if(c>=0.0f && c<t) {\n" +
                    "float d = " + context.callDelegate(length) + "/" + node.worldHeight + ";\n" +
                    "float hro = d / 2.0f - " + Float.toHexString((float) node.ocean) + " + " + context.callDelegate(raw_height) + ";\n" +
                    "int i = (int)(floor(hro/d));\n" +
                    "float w = (" + context.callDelegate(noise) + " + 1.0f) * 0.25f + 0.5f;\n" +
                    "float gap = heightOffset(i,w);\n" +
                    "float l = 2.0f*(h/(d*f*gap)) - 1.0f - 0.05f;\n" +
                    "if(l<-1 || l>1) return h;\n" +
                    "c /= t;\n" +
                    "return (c*c + l*l) >= 0.9f ? h : -h;\n" +
                "}\n" +
                "return h;\n";
    }
}
