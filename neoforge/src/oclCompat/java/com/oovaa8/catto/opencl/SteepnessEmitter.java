package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenFunctionContext;
import com.oovaa8.catto.density_functions.Steepness;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen.literal;

public class SteepnessEmitter implements OpenCLCEmitter<SteepnessNode> {
    public static final SteepnessEmitter INSTANCE = new SteepnessEmitter();

    private final Set<OpenCLCGenFunctionContext> helpersEmitted =
            Collections.newSetFromMap(new WeakHashMap<>());



    public static float[] buildPrefixHashBuffer(int N) {
        float[] buf = new float[2 * N + 2]; //+2 here so it is divisible by 8 cause apparently otherwise it won't flush
        for (int n = 1; n <= N; n++) {
            buf[n + N] = buf[(n - 1) + N] + (float) Steepness.hash(n - 1);
        }
        for (int n = -1; n >= -N; n--) {
            buf[n + N] = buf[(n + 1) + N] - (float) Steepness.hash(n);
        }
        return buf;
    }

    private static byte[] floatsToBytes(float[] arr) {
        ByteBuffer buf = ByteBuffer.allocate(arr.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float v : arr) buf.putFloat(v);
        return buf.array();
    }

    private final WeakHashMap<OpenCLCGenFunctionContext, Integer> const_offset = new WeakHashMap<>();


    public static void EmitHelpers(OpenCLCGenFunctionContext context){
        if(SteepnessEmitter.INSTANCE.helpersEmitted.add(context)){
            int N = 200;
            float[] prefixHash = buildPrefixHashBuffer(N);
            byte[] prefixBytes = floatsToBytes(prefixHash);
            INSTANCE.const_offset.put(context, context.getGlobalContext().allocGlobalConstData(prefixBytes, 4));
            context.appendRaw(
//GOD I HOPE THE HASH WORKS
"""
static double hash_steepness(int n){
    if (n==0) return 0.5;
    uint u = as_uint(n);
    u = ((u >> 16) ^ u) * 0x45d9f3bu;
    u = ((u >> 16) ^ u) * 0x45d9f3bu;
    u = (u >> 16) ^ u;
    return (double)(u & 0x7fffffffu) / (double)0x7fffffffu;
}

static float heightOffset(int j, float w) { return 1.0f + ((float)(hash_steepness(j)) * 2.0f - 1.0f) * w; }


static float accumulatedHeight(int index, float d, float w,
                                global const float * restrict prefix_hash,
                                int N) {
    float p = prefix_hash[index + N];
    return d * ((float)index * (1.0f - w) + 2.0f * w * p);
}

static float steepness_mod(float a, float b) { float r = fmod(a, b); return (r < 0) ? r + b : r; }

static float steepness_smooth(float d, float r, float s, float hro){
    float ds = d*s/2.0f;
    return smoothstep(r - ds, r + ds, steepness_mod(hro,d));
}""");
        }
    }
    public SteepnessEmitter() {
    }

    /*
    AstNode height;
    AstNode steepness;
    AstNode flatness;
    AstNode length;
    AstNode noise;
    int worldHeight;
    float ocean;
    */

    //context.callDelegate(height) on nodes
    //float.toHexString on floats
    //just + on ints
    @Override
    public String doCLGen(SteepnessNode node, OpenCLCGenFunctionContext context, String s) {
        EmitHelpers(context);
        ValuesMethodDefF64 height = context.newVarF64(node.height);
        ValuesMethodDefF64 steepness = context.newVarF64(node.steepness);
        ValuesMethodDefF64 flatness = context.newVarF64(node.flatness);
        ValuesMethodDefF64 length = context.newVarF64(node.length);
        ValuesMethodDefF64 noise = context.newVarF64(node.noise);
        return "float h = (float)(" + context.getDelegateVar(height) + ");\n" +
                "float f = (float)(" + context.getDelegateVar(flatness) + ");\n" +
                "if(f>0) {" +
                    "float s = max((float)(" + context.getDelegateVar(steepness) + "), 1e-10f);\n" +
                    "float w = ((float)(" + context.getDelegateVar(noise) + ") + 1) * 0.25f + 0.5f;\n" +
                    "float d = ((float)(" + context.getDelegateVar(length) + ")) / " + node.worldHeight + ".0f;\n" +
                    "float r = d / 2.0f;\n" +
                    "float hro = h+r - " + literal(node.ocean) + ";\n" +
                    """
                    int i = (int)(floor(hro/d));
                    if (i >= -199 && i <= 199){
                        float gap = d * heightOffset(i, w);""" +
                        "global const float * const prefix_hash = ptr_shift_global(ctx.const_data, " + const_offset.get(context) + ");\n" +
                        "float height = accumulatedHeight(i,d,w, prefix_hash, 200);\n" +
                        "float sm = steepness_smooth(d, r, s, hro);\n" +
                        s + " = h + f * (height + sm * gap - hro);\n" +
                    "} else {" + s + " = h;}\n" +
                "} else {" + s + " = h;}\n";
    }
}
