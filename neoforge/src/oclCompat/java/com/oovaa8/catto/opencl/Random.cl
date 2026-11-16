static uint __attribute__((pure)) hash(uint x)
{
    x ^= x >> 16;
    x *= 0x7feb352dU;
    x ^= x >> 15;
    x *= 0x846ca68bU;
    x ^= x >> 16;
    return x;
}
static float __attribute__((pure)) random(uint seed) {
    const uint mantissaMask = 0x007FFFFFu;
    const uint one          = 0x3F800000u;

    uint h = hash(seed);
    h &= mantissaMask;
    h |= one;

    float r2 = as_float(h);
    return r2 - 1.0;
}

static float __attribute__((pure)) f_random(float f) {
    return random(as_uint(f));
}

static float2 __attribute__((pure)) v_random2(float2 cell) {
    uint x = as_uint(cell.x);
    uint y = as_uint(cell.y);
    uint seed = hash(x ^ hash(y));
    return (float2)(random(seed), random(seed ^ 0x9e3779b9U));
}

static float __attribute__((pure)) f_random2(float2 cell) {
    uint x = as_uint(cell.x);
    uint y = as_uint(cell.y);
    uint seed = hash(x ^ hash(y));
    return random(seed);
}