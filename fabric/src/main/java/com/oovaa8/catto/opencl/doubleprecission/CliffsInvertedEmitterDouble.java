package com.oovaa8.catto.opencl.doubleprecission;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.opencl.CliffsInvertedNode;

public class CliffsInvertedEmitterDouble implements OpenCLCEmitter<CliffsInvertedNode> {
    public static final CliffsInvertedEmitterDouble INSTANCE = new CliffsInvertedEmitterDouble();
    public CliffsInvertedEmitterDouble() {
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
    double ocean;
    int min_y;*/
    //context.callDelegate(height) on nodes
    //Double.toHexString on Doubles
    //just + on ints
    @Override
    public String doCLGen(CliffsInvertedNode node, OpenCLCGenContext context) {
        SteepnessEmitterDouble.EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD cliffs = context.newMethod(node.cliffs);
        ValuesMethodDefD raw_height = context.newMethod(node.raw_height);
        ValuesMethodDefD length = context.newMethod(node.length);
        ValuesMethodDefD flatness = context.newMethod(node.flatness);
        ValuesMethodDefD steepness = context.newMethod(node.steepness);
        ValuesMethodDefD noise = context.newMethod(node.noise);
        return  "double height_map = " + context.callDelegate(height) + ";\n" +
                "double h = height_map - (2.0*(ctx.y - " + node.min_y + ") / " + node.worldHeight + " - 1.0);\n" +
                "double s = " + context.callDelegate(steepness) + ";\n"+
                "if(s>=0.0) return h;\n" +
                "double f = " + context.callDelegate(flatness) + ";\n" +
                "if(f < 0.075) return h;\n" +
                "double c = " + context.callDelegate(cliffs) + ";\n" +
                "double t = 0.25 * -s;\n" +
                "if(c>=0.0 && c<t) {\n" +
                    "double d = " + context.callDelegate(length) + "/" + node.worldHeight + ";\n" +
                    "double hro = d / 2.0 - " + Double.toHexString(node.ocean) + " + " + context.callDelegate(raw_height) + ";\n" +
                    "int i = (int)(floor(hro/d));\n" +
                    "double w = (" + context.callDelegate(noise) + " + 1.0) * 0.25 + 0.5;\n" +
                    "double gap = heightOffset(i,w);\n" +
                    "if(gap<0.075) return h;\n" +
                    "double l = 2.0*(h/(d*f*gap)) - 1.0 - 0.025;\n" +
                    "if(l<-1 || l>1) return h;\n" +
                    "c /= t;\n" +
                    "return (c*c + l*l) >= 0.950625 ? h : -h;\n" +
                "}\n" +
                "return h;\n";
    }
}
