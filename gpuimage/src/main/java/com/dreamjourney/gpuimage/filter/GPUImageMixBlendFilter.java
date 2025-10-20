package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

public class GPUImageMixBlendFilter extends GPUImageTwoInputFilter {

    private int mixLocation;
    private float mix;

    public GPUImageMixBlendFilter(String fragmentShader) {
        this(fragmentShader, 0.5f);
    }

    public GPUImageMixBlendFilter(String fragmentShader, float mix) {
        super(fragmentShader);
        this.mix = mix;
    }

    @Override
    public void onInit() {
        super.onInit();
        mixLocation = GLES20.glGetUniformLocation(
                getProgram(), "mixturePercent"
        );
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setMix(mix);
    }

    /**
     * @param mix ranges from 0.0 (only image 1) to 1.0
     *            (only image 2), with 0.5 (half of either)
     *            as the normal level
     */
    public void setMix(final float mix) {
        this.mix = mix;
        setFloat(mixLocation, this.mix);
    }

}
