package com.dermacare.clinic.doctor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dermacare.clinic.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamineSkinLesionFragment extends Fragment implements ExamineStep {

    private TextInputEditText edtMainComplaint, edtSymptomDuration, edtProgression;
    private TextInputEditText edtLesionLocation, edtLesionSize, edtSpecialSigns;
    private TextInputEditText edtTriggers, edtPreviousTreatment, edtExtraNotes;
    private MaterialAutoCompleteTextView actItchingLevel, actAssociatedSensation;
    private MaterialAutoCompleteTextView actLesionType, actLesionColor, actLesionSurface;
    private LinearLayout photoGallery;
    private final List<Bitmap> photoBitmaps = new ArrayList<>();

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    addPhotoToGallery(bitmap);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    loadBitmapFromUri(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examine_skin_lesion, container, false);

        edtMainComplaint = view.findViewById(R.id.edtMainComplaint);
        edtSymptomDuration = view.findViewById(R.id.edtSymptomDuration);
        edtProgression = view.findViewById(R.id.edtProgression);
        edtLesionLocation = view.findViewById(R.id.edtLesionLocation);
        edtLesionSize = view.findViewById(R.id.edtLesionSize);
        edtSpecialSigns = view.findViewById(R.id.edtSpecialSigns);
        edtTriggers = view.findViewById(R.id.edtTriggers);
        edtPreviousTreatment = view.findViewById(R.id.edtPreviousTreatment);
        edtExtraNotes = view.findViewById(R.id.edtExtraNotes);

        actItchingLevel = view.findViewById(R.id.actItchingLevel);
        actAssociatedSensation = view.findViewById(R.id.actAssociatedSensation);
        actLesionType = view.findViewById(R.id.actLesionType);
        actLesionColor = view.findViewById(R.id.actLesionColor);
        actLesionSurface = view.findViewById(R.id.actLesionSurface);

        photoGallery = view.findViewById(R.id.photoGallery);
        MaterialButton btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        MaterialButton btnPickGallery = view.findViewById(R.id.btnPickGallery);

        setupDropdown(actItchingLevel, R.array.itching_levels);
        setupDropdown(actAssociatedSensation, R.array.associated_sensations);
        setupDropdown(actLesionType, R.array.lesion_types);
        setupDropdown(actLesionColor, R.array.lesion_colors);
        setupDropdown(actLesionSurface, R.array.lesion_surfaces);

        btnTakePhoto.setOnClickListener(v -> dispatchTakePicture());
        btnPickGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        return view;
    }

    private void dispatchTakePicture() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void addPhotoToGallery(Bitmap bitmap) {
        photoBitmaps.add(bitmap);

        ImageView imageView = new ImageView(requireContext());
        int size = (int) (80 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundResource(R.drawable.bg_card);
        imageView.setClipToOutline(true);

        imageView.setOnLongClickListener(v -> {
            photoBitmaps.remove(bitmap);
            photoGallery.removeView(imageView);
            return true;
        });

        photoGallery.addView(imageView);
    }

    private void loadBitmapFromUri(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap != null) {
                addPhotoToGallery(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDropdown(MaterialAutoCompleteTextView view, int arrayRes) {
        String[] items = getResources().getStringArray(arrayRes);
        view.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, items));
    }

    @Override
    public boolean isValid() {
        return edtMainComplaint != null && edtMainComplaint.getText() != null
                && edtMainComplaint.getText().toString().trim().length() > 0
                && actLesionType != null && actLesionType.getText() != null
                && actLesionType.getText().toString().trim().length() > 0;
    }

    public List<Bitmap> getPhotoBitmaps() {
        return photoBitmaps;
    }

    public String getCombinedExaminationData() {
        List<String> parts = new ArrayList<>();

        parts.add("=== 1. TRIỆU CHỨNG CƠ NĂNG ===");
        addPart(parts, "Triệu chứng chính", textOf(edtMainComplaint));
        addPart(parts, "Mức độ ngứa", textOf(actItchingLevel));
        addPart(parts, "Cảm giác kèm theo", textOf(actAssociatedSensation));
        addPart(parts, "Thời gian tiến triển", textOf(edtSymptomDuration));
        addPart(parts, "Diễn tiến", textOf(edtProgression));

        parts.add("");
        parts.add("=== 2. KHÁM TỔN THƯƠNG DA ===");
        addPart(parts, "Loại tổn thương", textOf(actLesionType));
        addPart(parts, "Vị trí", textOf(edtLesionLocation));
        addPart(parts, "Kích thước", textOf(edtLesionSize));
        addPart(parts, "Màu sắc", textOf(actLesionColor));
        addPart(parts, "Bề mặt", textOf(actLesionSurface));
        addPart(parts, "Dấu hiệu đặc biệt", textOf(edtSpecialSigns));

        parts.add("");
        parts.add("=== 3. YẾU TỐ LIÊN QUAN ===");
        addPart(parts, "Yếu tố liên quan", textOf(edtTriggers));
        addPart(parts, "Điều trị trước đây", textOf(edtPreviousTreatment));
        addPart(parts, "Nhận xét thêm", textOf(edtExtraNotes));

        parts.add("");
        parts.add("=== 4. ẢNH TỔN THƯƠNG ===");
        addPart(parts, "Số ảnh đã chụp", String.valueOf(photoBitmaps.size()));

        return TextUtils.join("\n", parts);
    }

    private static void addPart(List<String> parts, String label, String value) {
        if (!TextUtils.isEmpty(value)) {
            parts.add(String.format(Locale.ROOT, "%s: %s", label, value));
        }
    }

    private static String textOf(android.widget.EditText field) {
        if (field == null || field.getText() == null) return "";
        return field.getText().toString().trim();
    }
}
