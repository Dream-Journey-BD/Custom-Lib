package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

/**
 * Adjusts the alpha channel of the incoming image
 * opacity: The value to multiply the incoming alpha channel for each pixel by (0.0 - 1.0, with 1.0 as the default)
 */
public class GPUImageOpacityFilter extends GPUImageFilter {
    public static final String OPACITY_FRAGMENT_SHADER =
            "  varying highp vec2 textureCoordinate;\n" +
                    "  \n" +
                    "  uniform sampler2D inputImageTexture;\n" +
                    "  uniform lowp float opacity;\n" +
                    "  \n" +
                    "  void main()\n" +
                    "  {\n" +
                    "      lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "      \n" +
                    "      gl_FragColor = vec4(textureColor.rgb, textureColor.a * opacity);\n" +
                    "  }\n";

    private int opacityLocation;
    private float opacity;

    public GPUImageOpacityFilter() {
        this(1.0f);
    }

    public GPUImageOpacityFilter(final float opacity) {
        super(NO_FILTER_VERTEX_SHADER, OPACITY_FRAGMENT_SHADER);
        this.opacity = opacity;
    }

    @Override
    public void onInit() {
        super.onInit();
        opacityLocation = GLES20.glGetUniformLocation(
                getProgram(), "opacity"
        );
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setOpacity(opacity);
    }

    public void setOpacity(final float opacity) {
        this.opacity = opacity;
        setFloat(opacityLocation, this.opacity);
    }

}
