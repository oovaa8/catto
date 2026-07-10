package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCEmitter;
import com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext;
import com.oovaa8.catto.density_functions.Steepness;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SteepnessEmitter implements OpenCLCEmitter<SteepnessNode> {
    public static final SteepnessEmitter INSTANCE = new SteepnessEmitter();

    private final Set<OpenCLCGenContext> helpersEmitted =
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

    private WeakHashMap<OpenCLCGenContext, Integer> const_offset = new WeakHashMap<>();


    public static void EmitHelpers(OpenCLCGenContext context){
        if(SteepnessEmitter.INSTANCE.helpersEmitted.add(context)){
            int N = 200;
            float[] prefixHash = buildPrefixHashBuffer(N);
            byte[] prefixBytes = floatsToBytes(prefixHash);
            INSTANCE.const_offset.put(context, context.allocGlobalConstData(prefixBytes, 4));
            context.appendRaw(
//GOD I HOPE THE HASH WORKS
"""
static double hash_steepness(int n){
    if (n==0) return 0.5;
    uint u = as_uint(n);
    u = ((u >> 16) ^ u) * 0x45d9f3bu;
    u = ((u >> 16) ^ u) * 0x45d9f3bu;
    u = (u >> 16) ^ u;
    return (double)((u & 0x7fffffffu) / 0x7fffffffu);
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
    public String doCLGen(SteepnessNode node, OpenCLCGenContext context) {
        EmitHelpers(context);
        ValuesMethodDefD height = context.newMethod(node.height);
        ValuesMethodDefD steepness = context.newMethod(node.steepness);
        ValuesMethodDefD flatness = context.newMethod(node.flatness);
        ValuesMethodDefD length = context.newMethod(node.length);
        ValuesMethodDefD noise = context.newMethod(node.noise);
        return "float h = " + context.callDelegate(height) + ";\n" +
                "float f = " + context.callDelegate(flatness) + ";\n" +
                "if(f<=0) return h;\n" +
                "float s = max((float)(" + context.callDelegate(steepness) + "), 1e-10f);\n" +
                "float w = (" + context.callDelegate(noise) + " + 1) * 0.25f + 0.5f;\n" +
                "float d = ((float)(" + context.callDelegate(length) + ")) / " + node.worldHeight + ".0f;\n" +
                "float r = d / 2.0f;\n" +
                "float hro = h+r - " + Float.toHexString((float) node.ocean) + ";\n" +
                """
                int i = (int)(floor(hro/d));
                if (i < -199 || i > 199)
                    return h;
                float gap = d * heightOffset(i, w);"""+
                "global const float * const prefix_hash = ptr_shift_global(ctx.const_data, " + const_offset.get(context) + ");\n" +
                "float height = accumulatedHeight(i,d,w, prefix_hash, 200);\n" +
                """
                float sm = steepness_smooth(d, r, s, hro);
                return h + f * (height + sm * gap - hro);
                """;
    }
}
