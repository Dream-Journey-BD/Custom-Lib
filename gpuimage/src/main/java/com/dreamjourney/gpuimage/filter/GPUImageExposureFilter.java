package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

/**
 * exposure: The adjusted exposure (-10.0 - 10.0, with 0.0 as the default)
 */
public class GPUImageExposureFilter extends GPUImageFilter {
    public static final String EXPOSURE_FRAGMENT_SHADER =
            " varying highp vec2 textureCoordinate;\n" +
                    " \n" +
                    " uniform sampler2D inputImageTexture;\n" +
                    " uniform highp float exposure;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     highp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "     \n" +
                    "     gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);\n" +
                    " } ";

    private int exposureLocation;
    private float exposure;

    public GPUImageExposureFilter() {
        this(0f);
    }

    public GPUImageExposureFilter(final float exposure) {
        super(NO_FILTER_VERTEX_SHADER, EXPOSURE_FRAGMENT_SHADER);
        this.exposure = exposure;
    }

    @Override
    public void onInit() {
        super.onInit();
        exposureLocation = GLES20.glGetUniformLocation(getProgram(), "exposure");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setExposure(exposure);
    }

    public void setExposure(final float exposure) {
        this.exposure = exposure;
        setFloat(exposureLocation, this.exposure);
    }

    public float getExposure() {
        return exposure;
    }

}
