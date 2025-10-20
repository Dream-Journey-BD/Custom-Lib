package com.dreamjourney.customlib;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dreamjourney.gpuimage.GPUImage;
import com.dreamjourney.gpuimage.GPUImageView;
import com.dreamjourney.gpuimage.filter.GPUImageBrightnessFilter;
import com.dreamjourney.gpuimage.filter.GPUImageContrastFilter;
import com.dreamjourney.gpuimage.filter.GPUImageExposureFilter;
import com.dreamjourney.gpuimage.filter.GPUImageFilterGroup;
import com.dreamjourney.gpuimage.filter.GPUImageHighlightShadowFilter;
import com.dreamjourney.gpuimage.filter.GPUImageSaturationFilter;
import com.dreamjourney.gpuimage.filter.GPUImageSharpenFilter;
import com.dreamjourney.gpuimage.filter.GPUImageVignetteFilter;
import com.dreamjourney.gpuimage.filter.GPUImageWhiteBalanceFilter;
import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {


    // Gpu Declare Variable
    private GPUImageView gpuImageView;
    private GPUImageFilterGroup filterGroup;

    // All filters
    private GPUImageBrightnessFilter brightnessFilter;
    private GPUImageContrastFilter contrastFilter;
    private GPUImageSaturationFilter saturationFilter;
    private GPUImageExposureFilter exposureFilter;
    private GPUImageWhiteBalanceFilter temperatureFilter;
    private GPUImageHighlightShadowFilter highlightShadowFilter;
    private GPUImageVignetteFilter vignetteFilter;
    private GPUImageSharpenFilter sharpenFilter;
    // Sliders
    private Slider brightnessSlider, contrastSlider, saturationSlider,
            exposureSlider, temperatureSlider, highlightSlider, shadowSlider,
            vignetteSlider, sharpnessSlider, roundSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        gpuImageView = findViewById(R.id.gpuImageView);
        initializeFilters();
        initializeSliders();

        // Call To Chose Image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);

    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri sourceUri = result.getData().getData();
                    if (sourceUri == null) return;
                    gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
                    gpuImageView.setImage(sourceUri);

                }

            }
    );


    private void initializeFilters() {
        // Initialize filter group Here
        filterGroup = new GPUImageFilterGroup();

        // Initialize filters
        brightnessFilter = new GPUImageBrightnessFilter();
        contrastFilter = new GPUImageContrastFilter();
        saturationFilter = new GPUImageSaturationFilter();
        exposureFilter = new GPUImageExposureFilter();
        temperatureFilter = new GPUImageWhiteBalanceFilter();
        highlightShadowFilter = new GPUImageHighlightShadowFilter();
        vignetteFilter = new GPUImageVignetteFilter();
        sharpenFilter = new GPUImageSharpenFilter();

        // -------- Set default values --------
        vignetteFilter.setVignetteStart(0.00000001f);
        vignetteFilter.setVignetteEnd(0f);

        // Add all filters in sequence
        filterGroup.addFilter(brightnessFilter);
        filterGroup.addFilter(contrastFilter);
        filterGroup.addFilter(saturationFilter);
        filterGroup.addFilter(exposureFilter);
        filterGroup.addFilter(temperatureFilter);
        filterGroup.addFilter(highlightShadowFilter);
        filterGroup.addFilter(vignetteFilter);
        filterGroup.addFilter(sharpenFilter);
        gpuImageView.setFilter(filterGroup);

    }

    private void initializeSliders() {
        // Initialize all sliders
        initializeBrightnessSlider();
        initializeContrastSlider();
        initializeSaturationSlider();
        initializeExposureSlider();
        initializeTemperatureSlider();
        initializeHighlightSlider();
        initializeShadowSlider();
        initializeVignetteSlider();
        initializeSharpnessSlider();
        initializeRoundSlider();
    }

    // ----------- SLIDER INITIALIZATION METHODS -----------

    private void initializeBrightnessSlider() {
        brightnessSlider = findViewById(R.id.brightnessSlider);
        brightnessSlider.setValueFrom(-1f);
        brightnessSlider.setValueTo(1f);
        brightnessSlider.setValue(0f);
        brightnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            brightnessFilter.setBrightness(value); // range -1 to +1
            gpuImageView.requestRender();
        });
    }

    private void initializeContrastSlider() {
        contrastSlider = findViewById(R.id.contrastSlider);
        contrastSlider.setValueFrom(0f);
        contrastSlider.setValueTo(2f);
        contrastSlider.setValue(1f);
        contrastSlider.addOnChangeListener((slider, value, fromUser) -> {
            contrastFilter.setContrast(value); // range 0 to 4
            gpuImageView.requestRender();
        });
    }

    private void initializeSaturationSlider() {
        saturationSlider = findViewById(R.id.saturationSlider);
        saturationSlider.setValueFrom(0f);
        saturationSlider.setValueTo(2f);
        saturationSlider.setValue(1f);
        saturationSlider.addOnChangeListener((slider, value, fromUser) -> {
            saturationFilter.setSaturation(value); // range 0 to 2
            gpuImageView.requestRender();
        });
    }

    private void initializeExposureSlider() {
        exposureSlider = findViewById(R.id.exposureSlider);
        exposureSlider.setValueFrom(-1f);
        exposureSlider.setValueTo(1f);
        exposureSlider.setValue(0f);
        exposureSlider.addOnChangeListener((slider, value, fromUser) -> {
            exposureFilter.setExposure(value * 10f); // GPU range -10 to +10
            gpuImageView.requestRender();
        });
    }

    private void initializeTemperatureSlider() {
        temperatureSlider = findViewById(R.id.temperatureSlider);
        temperatureSlider.setValueFrom(-1f);
        temperatureSlider.setValueTo(1f);
        temperatureSlider.setValue(0f);
        temperatureSlider.addOnChangeListener((slider, value, fromUser) -> {
            temperatureFilter.setTemperature(5000f + (value * 2000f)); // 3000K–7000K
            gpuImageView.requestRender();
        });
    }

    private void initializeHighlightSlider() {
        highlightSlider = findViewById(R.id.highlightSlider);
        highlightSlider.setValueFrom(0f);
        highlightSlider.setValueTo(1f);
        highlightSlider.setValue(1f);
        highlightSlider.addOnChangeListener((slider, value, fromUser) -> {
            highlightShadowFilter.setHighlights(value);
            gpuImageView.requestRender();
        });
    }

    private void initializeShadowSlider() {
        shadowSlider = findViewById(R.id.shadowSlider);
        shadowSlider.setValueFrom(0f);
        shadowSlider.setValueTo(1f);
        shadowSlider.setValue(0f);
        shadowSlider.addOnChangeListener((slider, value, fromUser) -> {
            highlightShadowFilter.setShadows(value);
            gpuImageView.requestRender();
        });
    }

    private void initializeVignetteSlider() {
        vignetteSlider = findViewById(R.id.vignetteSlider);
        vignetteSlider.setValueFrom(0f);
        vignetteSlider.setValueTo(5f);
        vignetteSlider.setValue(0f);
        vignetteSlider.addOnChangeListener((slider, value, fromUser) -> {


        });
    }

    private void initializeRoundSlider() {
        roundSlider = findViewById(R.id.roundSlider);
        roundSlider.setValueFrom(0f);
        roundSlider.setValueTo(5f);
        roundSlider.setValue(0f);
        roundSlider.addOnChangeListener((slider, value, fromUser) -> {

        });
    }

    private void initializeSharpnessSlider() {
        sharpnessSlider = findViewById(R.id.sharpnessSlider);
        sharpnessSlider.setValueFrom(0f);
        sharpnessSlider.setValueTo(1f);
        sharpnessSlider.setValue(0f);
        sharpnessSlider.addOnChangeListener((slider, value, fromUser) -> {
            sharpenFilter.setSharpness(value * 4f); // range -4 to +4
            gpuImageView.requestRender();
        });
    }
}