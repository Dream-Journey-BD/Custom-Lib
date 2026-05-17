package com.dreamjourney.gpuimage.filter;

import android.opengl.GLES20;

/**
 * Adjusts the white balance (Temperature & Tint) of incoming image. <br>
 * <br>
 * 🔸 Simplified version — Only supports temperature range -1 ➡ +1 <br>
 * 🔸 Temperature Convert (-1 ➡ +1) <br><br>
 * Slider value -1 ➡ +1 → mapped internally (5000 ± 3000 scale) <br>
 * Default: 0.0f
 */
public class GPUImageWhiteBalanceFilter extends GPUImageFilter {
    public static final String WHITE_BALANCE_FRAGMENT_SHADER =
            "uniform sampler2D inputImageTexture;\n" +
                    "varying highp vec2 textureCoordinate;\n" +
                    "\n" +
                    "uniform lowp float temperature;\n" +
                    "uniform lowp float tint;\n" +
                    "\n" +
                    "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);\n" +
                    "\n" +
                    "const mediump mat3 RGBtoYIQ = mat3(\n" +
                    "    0.299, 0.587, 0.114,\n" +
                    "    0.596, -0.274, -0.322,\n" +
                    "    0.212, -0.523, 0.311);\n" +
                    "const mediump mat3 YIQtoRGB = mat3(\n" +
                    "    1.0, 0.956, 0.621,\n" +
                    "    1.0, -0.272, -0.647,\n" +
                    "    1.0, -1.105, 1.702);\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    lowp vec4 source = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "\n" +
                    "    mediump vec3 yiq = RGBtoYIQ * source.rgb; // adjusting tint\n" +
                    "    yiq.b = clamp(yiq.b + tint * 0.5226 * 0.1, -0.5226, 0.5226);\n" +
                    "    lowp vec3 rgb = YIQtoRGB * yiq;\n" +
                    "\n" +
                    "    lowp vec3 processed = vec3(\n" +
                    "        (rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))),\n" +
                    "        (rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))),\n" +
                    "        (rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));\n" +
                    "\n" +
                    "    gl_FragColor = vec4(mix(rgb, processed, temperature), source.a);\n" +
                    "}";

    private int temperatureLocation;
    private float temperature;
    private int tintLocation;
    private float tint;

    public GPUImageWhiteBalanceFilter() {
        this(0.0f, 0.0f);
    }

    public GPUImageWhiteBalanceFilter(final float temperature, final float tint) {
        super(NO_FILTER_VERTEX_SHADER, WHITE_BALANCE_FRAGMENT_SHADER);
        this.temperature = temperature;
        this.tint = tint;
    }

    @Override
    public void onInit() {
        super.onInit();
        temperatureLocation = GLES20.glGetUniformLocation(getProgram(), "temperature");
        tintLocation = GLES20.glGetUniformLocation(getProgram(), "tint");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setTemperature(temperature);
        setTint(tint);
    }

    public void setTemperature(final float temperature) {
        this.temperature = temperature;

        // slider value -1 ➡ +1 → mapped to 2000 ➡ 8000
        float mappedTemp = 5000.0f + (this.temperature * 3000.0f);
        // example: -1 → 2000, 0 → 5000, +1 → 8000

        setFloat(temperatureLocation, mappedTemp < 5000 ?
                0.0004f * (mappedTemp - 5000.0f) :
                0.00006f * (mappedTemp - 5000.0f)
        );
    }

    public void setTint(final float tint) {
        this.tint = tint;
        setFloat(tintLocation, this.tint / 100.0f);
    }

    public float getTemperature() {
        return temperature;
    }

    public float getTint() {
        return tint;
    }

}
