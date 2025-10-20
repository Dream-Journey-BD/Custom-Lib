package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

/**
 * gamma value ranges from 0.0 to 3.0, with 1.0 as the normal level
 */
public class GPUImageGammaFilter extends GPUImageFilter {
    public static final String GAMMA_FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;\n" +
                    " \n" +
                    " uniform sampler2D inputImageTexture;\n" +
                    " uniform lowp float gamma;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "     \n" +
                    "     gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);\n" +
                    " }";

    private int gammaLocation;
    private float gamma;

    public GPUImageGammaFilter() {
        this(1.2f);
    }

    public GPUImageGammaFilter(final float gamma) {
        super(NO_FILTER_VERTEX_SHADER, GAMMA_FRAGMENT_SHADER);
        this.gamma = gamma;
    }

    @Override
    public void onInit() {
        super.onInit();
        gammaLocation = GLES20.glGetUniformLocation(getProgram(), "gamma");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setGamma(gamma);
    }

    public void setGamma(final float gamma) {
        this.gamma = gamma;
        setFloat(gammaLocation, this.gamma);
    }

}
