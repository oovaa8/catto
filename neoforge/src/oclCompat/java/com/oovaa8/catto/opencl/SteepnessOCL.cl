double hash_steepness(int n){
    uint n_unsigned;
    if (n < 0) {
        n_unsigned = (uint)(~((long)n << 1));
    } else {
        n_unsigned = (uint)(n << 1);
    }
    n_unsigned = ((n_unsigned >> 16) ^ n_unsigned) * 0x45d9f3bu;
    n_unsigned = ((n_unsigned >> 16) ^ n_unsigned) * 0x45d9f3bu;
    n_unsigned = (n_unsigned >> 16) ^ n_unsigned;
    return (double)(n_unsigned & 0x7fffffffu) / 0x7fffffffu;
}

double heightOffset(int j, double w) { return 1.0 + (hash_steepness(j) * 2.0 - 1.0) * w; }


double accumulatedHeight(int index, double d, double w){
    double h = 0;
    if(index > 0){
        for(int k = 0; k < index; k++) h += d * heightOffset(k, w);
    }else{
        for(int k = index; k < 0; k++) h -= d * heightOffset(k, w);
    }
    return h;
}

double steepness_mod(double a, double b) { double r = fmod(a, b); return (r < 0) ? r + b : r; }

double steepness_smooth(double d, double r, double s, double hro){
    double ds = d*s/2.0;
    return smoothstep(r - ds, r + ds, steepness_mod(hro,d));
}

double s(double h, double d, double r, double w, double s){
    double hro = h+r - -0x1.58d0fac687d64p-1;
    int i = (int)(floor(hro/d));
    double gap = d * heightOffset(i, w);
    double height = accumulatedHeight(i,d,w);
    double sm = steepness_smooth(d, r, s, hro)
    return h + f * (height + sm * gap - hro);
}