package com.oovaa8.catto.density_functions;

/**
 * Java port of the OpenCL continent/terrain generator.
 *
 * The simplex noise section (mod289, permute, contrib, snoise, snoise01) is a Java
 * translation (via an OpenCL translation of the original GLSL) of:
 *
 *   Description : Array and textureless GLSL 2D simplex noise function.
 *        Author : Ian McEwan, Ashima Arts.
 *    Maintainer : stegu
 *       Lastmod : 20110822 (ijm)
 *       License : Copyright (C) 2011 Ashima Arts. All rights reserved.
 *                 Distributed under the MIT License.
 *                 https://github.com/ashima/webgl-noise
 *                 https://github.com/stegu/webgl-noise
 *
 * MIT License (applies to the simplex noise portion):
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 *
 */

public class ContinentNoise {
    private static final float INV_289 = 1.0f / 289.0f;

    private static float mod289(float x) {
        return x - Continent.floor(x * INV_289) * 289.0f;
    }

    private static float permute(float x) {
        return mod289(((x * 34.0f) + 10.0f) * x);
    }

    // C = (0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439)
    private static final float CX = 0.211324865405187f;  // (3.0 - sqrt(3.0)) / 6.0
    private static final float CY = 0.366025403784439f;  // 0.5 * (sqrt(3.0) - 1.0)
    private static final float CZ = -0.577350269189626f; // -1.0 + 2.0 * C.x
    private static final float CW = 0.024390243902439f;  // 1.0 / 41.0

    /** Per-corner gradient contribution: m * dot(grad, (gx, gy)). */
    private static float contrib(float m, float p, float gx, float gy) {
        // Gradients: 41 points uniformly over a line, mapped onto a diamond.
        float scaled = p * CW;
        float x = 2.0f * (scaled - Continent.floor(scaled)) - 1.0f;
        float h = Math.abs(x) - 0.5f;
        float ox = Continent.floor(x + 0.5f);
        float a0 = x - ox;

        // Normalise gradients implicitly by scaling m
        // Approximation of: m *= inversesqrt(a0*a0 + h*h);
        float mm = m * (1.79284291400159f - 0.85373472095314f * (a0 * a0 + h * h));

        return mm * (a0 * gx + h * gy);
    }

    public static float snoise(float vx, float vy) {
        // First corner
        float ix = Continent.floor(vx + (vx * CY + vy * CY));
        float iy = Continent.floor(vy + (vx * CY + vy * CY));
        float dI = ix * CX + iy * CX;
        float x0x = vx - ix + dI;
        float x0y = vy - iy + dI;

        // Other corners
        float i1x, i1y;
        if (x0x > x0y) {
            i1x = 1.0f;
            i1y = 0.0f;
        } else {
            i1x = 0.0f;
            i1y = 1.0f;
        }

        // x12 = x0.xyxy + C.xxzz; x12.xy -= i1;
        float x12x = (x0x + CX) - i1x;
        float x12y = (x0y + CX) - i1y;
        float x12z = x0x + CZ;
        float x12w = x0y + CZ;

        // Permutations
        ix = mod289(ix); // Avoid truncation effects in permutation
        iy = mod289(iy);
        float p0 = permute(permute(iy + 0.0f) + ix + 0.0f);
        float p1 = permute(permute(iy + i1y) + ix + i1x);
        float p2 = permute(permute(iy + 1.0f) + ix + 1.0f);

        float m0 = Math.max(0.5f - (x0x * x0x + x0y * x0y), 0.0f);
        float m1 = Math.max(0.5f - (x12x * x12x + x12y * x12y), 0.0f);
        float m2 = Math.max(0.5f - (x12z * x12z + x12w * x12w), 0.0f);
        m0 *= m0; m0 *= m0;
        m1 *= m1; m1 *= m1;
        m2 *= m2; m2 *= m2;

        float c0 = contrib(m0, p0, x0x, x0y);
        float c1 = contrib(m1, p1, x12x, x12y);
        float c2 = contrib(m2, p2, x12z, x12w);

        return 130.0f * (c0 + c1 + c2);
    }

    public static float snoise01(float x, float y) {
        return (snoise(x, y) + 1.0f) / 2.0f;
    }
}
