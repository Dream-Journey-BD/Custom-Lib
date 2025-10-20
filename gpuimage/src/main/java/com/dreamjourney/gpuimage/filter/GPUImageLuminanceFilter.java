package com.dreamjourney.gpuimage.filter;

public class GPUImageLuminanceFilter extends GPUImageFilter {

    public static final String LUMINANCE_FRAGMENT_SHADER =
            "precision highp float;\n" +
                    "\n" +
                    "varying vec2 textureCoordinate;\n" +
                    "\n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    "\n" +
                    "// Values from \"Graphics Shaders: Theory and Practice\" by Bailey and Cunningham\n" +
                    "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "    float luminance = dot(textureColor.rgb, W);\n" +
                    "    \n" +
                    "    gl_FragColor = vec4(vec3(luminance), textureColor.a);\n" +
                    "}";

    public GPUImageLuminanceFilter() {
        super(NO_FILTER_VERTEX_SHADER, LUMINANCE_FRAGMENT_SHADER);
    }

}
