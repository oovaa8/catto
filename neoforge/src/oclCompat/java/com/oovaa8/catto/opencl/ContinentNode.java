package com.oovaa8.catto.opencl;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Objects;

public class ContinentNode extends DelegateNode {
    DensityFunction df;
    long seed;
    public AstNode noise;
    public ContinentNode(DensityFunction densityFunction, AstNode noise, long seed) {
        this.df = densityFunction;
        this.seed = seed;
        this.noise = noise;
        super(densityFunction);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.noise};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode noise = this.noise.transform(transformer);
        if (this.noise == noise) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new ContinentNode(this.df,noise,this.seed));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContinentNode that = (ContinentNode) o;
        return noise == that.noise && Long.compare(seed, that.seed) == 0;
    }


    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.noise.hashCode();
        result = 31 * result + Long.hashCode(this.seed);

        return result;
    }

    @Override
    public boolean relaxedEquals(AstNode o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContinentNode that = (ContinentNode) o;
        return noise == that.noise && Long.compare(seed, that.seed) == 0;
    }

    @Override
    public int relaxedHashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.noise.relaxedHashCode();
        result = 31 * result + Long.hashCode(this.seed);

        return result;
    }
}
