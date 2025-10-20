package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

/**
 * Changes the contrast of the image.<br>
 * <br>
 * contrast value ranges from 0.0 to 4.0, with 1.0 as the normal level
 */
public class GPUImageContrastFilter extends GPUImageFilter {
    public static final String CONTRAST_FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;\n" +
                    " \n" +
                    " uniform sampler2D inputImageTexture;\n" +
                    " uniform lowp float contrast;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "     \n" +
                    "     gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);\n" +
                    " }";

    private int contrastLocation;
    private float contrast;

    public GPUImageContrastFilter() {
        this(1f);
    }

    public GPUImageContrastFilter(float contrast) {
        super(NO_FILTER_VERTEX_SHADER, CONTRAST_FRAGMENT_SHADER);
        this.contrast = contrast;
    }

    @Override
    public void onInit() {
        super.onInit();
        contrastLocation = GLES20.glGetUniformLocation(getProgram(), "contrast");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setContrast(contrast);
    }

    public void setContrast(final float contrast) {
        this.contrast = contrast;
        setFloat(contrastLocation, this.contrast);
    }

}
