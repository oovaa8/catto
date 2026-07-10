package com.oovaa8.catto.opencl.doubleprecission;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.density_functions.Steepness;
import com.oovaa8.catto.opencl.SteepnessNode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SteepnessEmitterDouble implements OpenCLCEmitter<SteepnessNode> {
    public static final SteepnessEmitterDouble INSTANCE = new SteepnessEmitterDouble();

    private final Set<OpenCLCGenContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());



    public static double[] buildPrefixHashBuffer(int N) {
        double[] buf = new double[2 * N + 1]; // buf[N] = P[0] = 0.0 by default
        for (int n = 1; n <= N; n++) {
            buf[n + N] = buf[(n - 1) + N] + Steepness.hash(n - 1);
        }
        for (int n = -1; n >= -N; n--) {
            buf[n + N] = buf[(n + 1) + N] - Steepness.hash(n);
        }
        return buf;
    }

    private static byte[] doublesToBytes(double[] arr) {
        ByteBuffer buf = ByteBuffer.allocate(arr.length * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (double v : arr) buf.putDouble(v);
        return buf.array();
    }

    private WeakHashMap<OpenCLCGenContext, Integer> const_offset = new WeakHashMap<>();


    public static void EmitHelpers(OpenCLCGenContext context){
        if(SteepnessEmitterDouble.INSTANCE.helpersEmitted.add(context)){
            int N = 200;
            double[] prefixHash = buildPrefixHashBuffer(N);
            byte[] prefixBytes = doublesToBytes(prefixHash);
            INSTANCE.const_offset.put(context, context.allocGlobalConstData(prefixBytes, 8));
            context.appendRaw("""
static double hash_steepness(int n){
    uint n_unsigned = (n < 0) ? (uint)(~((long)n << 1)) : (uint)(n << 1);
    n_unsigned = ((n_unsigned >> 16) ^ n_unsigned) * 0x45d9f3bu;
    n_unsigned = ((n_unsigned >> 16) ^ n_unsigned) * 0x45d9f3bu;
    n_unsigned = (n_unsigned >> 16) ^ n_unsigned;
    return (double)(n_unsigned & 0x7fffffffu) / 0x7fffffffu;
}

static double heightOffset(int j, double w) { return 1.0 + ((double)(hash_steepness(j)) * 2.0 - 1.0) * w; }


static double accumulatedHeight(int index, double d, double w,
                                global const double * restrict prefix_hash,
                                int N) {
    double p = prefix_hash[index + N];
    return d * ((double)index * (1.0 - w) + 2.0 * w * p);
}

static double steepness_mod(double a, double b) { double r = fmod(a, b); return (r < 0) ? r + b : r; }

static double steepness_smooth(double d, double r, double s, double hro){
    double ds = d*s/2.0;
    return smoothstep(r - ds, r + ds, steepness_mod(hro,d));
}""");
        }
    }
    public SteepnessEmitterDouble() {
    }

    /*
    AstNode height;
    AstNode steepness;
    AstNode flatness;
    AstNode length;
    AstNode noise;
    int worldHeight;
    double ocean;
    */

    //context.callDelegate(height) on nodes
    //double.toHexString on doubles
    //just + on ints
    @Override
    public String doCLGen(SteepnessNode node, OpenCLCGenContext context) {
        EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD steepness = context.newMethod(node.steepness);
        ValuesMethodDefD flatness = context.newMethod(node.flatness);
        ValuesMethodDefD length = context.newMethod(node.length);
        ValuesMethodDefD noise = context.newMethod(node.noise);
        return "double h = " + context.callDelegate(height) + ";\n" +
                "double f = " + context.callDelegate(flatness) + ";\n" +
                "if(f<=0) return h;\n" +
                "double s = max((double)(" + context.callDelegate(steepness) + "), 1e-10);\n" +
                "double w = (" + context.callDelegate(noise) + " + 1) * 0.25 + 0.5;\n" +
                "double d = " + context.callDelegate(length) + "/" + node.worldHeight + ";\n" +
                "double r = d / 2.0;\n" +
                "double hro = h+r - " + Double.toHexString(node.ocean) + ";\n" +
                """
                int i = (int)(floor(hro/d));
                if (i < -199 || i > 199)
                    return h;
                double gap = d * heightOffset(i, w);"""+
                "global const double * const prefix_hash = ptr_shift_global(ctx.const_data, " + const_offset.get(context) + ");\n" +
                "double height = accumulatedHeight(i,d,w, prefix_hash, 200);\n" +
                """
                double sm = steepness_smooth(d, r, s, hro);
                return h + f * (height + sm * gap - hro);
                """;
    }
}
