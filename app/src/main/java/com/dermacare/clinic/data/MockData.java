package com.dermacare.clinic.data;

import com.dermacare.clinic.R;
import com.dermacare.clinic.model.Doctor;
import com.dermacare.clinic.model.Patient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MockData {
    private MockData() {}


    private static final List<Patient> PATIENTS = new ArrayList<>();


    private static final List<Patient> PATIENT_LIST = new ArrayList<>(Arrays.asList(
            new Patient("Nguyễn Văn A", "32 tuổi — Mụn viêm", "19/05/2026"),
            new Patient("Trần Thị B", "45 tuổi — Nám da", "15/05/2026"),
            new Patient("Lê Văn C", "28 tuổi — Dị ứng", "12/05/2026")
    ));



    public static List<String> specialties() {
        return Arrays.asList(
                "Mụn & da dầu", "Nám & tàn nhang", "Dị ứng da", "Vảy nến",
                "Nấm da", "Ung thư da", "Trẻ em", "Thẩm mỹ da"
        );
    }

    public static List<Integer> specialtyIconResIds() {
        return Arrays.asList(
                R.drawable.ic_specialty_skin,
                R.drawable.ic_specialty_sun,
                R.drawable.ic_specialty_allergy,
                R.drawable.ic_specialty_skin,
                R.drawable.ic_specialty_allergy,
                R.drawable.ic_specialty_sparkle,
                R.drawable.ic_nav_profile,
                R.drawable.ic_specialty_sparkle
        );
    }

    public static List<Doctor> doctors() {
        return Arrays.asList(
                new Doctor(1L, "BS. Nguyễn Trí Đức", "Chuyên khoa Da liễu", "4.8", true, "https://ui-avatars.com/api/?name=ND&background=0D8ABC&color=fff", 150000),
                new Doctor(2L, "BS. Trần Lê Quyên", "Da liễu Thẩm mỹ", "4.9", true, "https://ui-avatars.com/api/?name=TQ&background=FFB6C1&color=fff", 200000),
                new Doctor(3L, "BS. Lê Quang Minh", "Bệnh lý Da liễu", "4.7", false, "https://ui-avatars.com/api/?name=LM&background=2E8B57&color=fff", 150000),
                new Doctor(4L, "BS. Phạm Thu Thủy", "Laser & Phục hồi", "5.0", true, "https://ui-avatars.com/api/?name=PT&background=9370DB&color=fff", 250000)
        );
    }

    public static List<String[]> appointments() {
        return Arrays.asList(
                new String[]{"09:30", "BS. Nguyễn Minh Anh", "22/05/2026 — Sắp tới"},
                new String[]{"14:00", "BS. Trần Hoàng Long", "10/05/2026 — Đã khám"}
        );
    }

    public static List<String[]> medicalRecords() {
        return Arrays.asList(
                new String[]{"Melasma độ I", "10/05/2026 — BS. Trần Hoàng Long", "Có ảnh tổn thương"},
                new String[]{"Viêm da tiếp xúc", "15/03/2026 — BS. Nguyễn Minh Anh", "Có ảnh tổn thương"},
                new String[]{"Mụn trứng cá bọc", "20/04/2026 — BS. Lê Thu Hà", "Có ảnh tổn thương"}
        );
    }

    public static List<String[]> doctorSchedule() {
        return Arrays.asList(
                new String[]{"08:00", "Nguyễn Văn A", "Khám mới"},
                new String[]{"09:00", "Trần Thị B", "Tái khám"},
                new String[]{"10:30", "Lê Văn C", "Khám mới"}
        );
    }


    public static List<String[]> doctorPatientsList() {
        return Arrays.asList(
                new String[]{"Nguyễn Văn A", "32 tuổi — Mụn viêm", "19/05/2026"},
                new String[]{"Trần Thị B", "45 tuổi — Nám da", "15/05/2026"},
                new String[]{"Lê Văn C", "28 tuổi — Dị ứng", "12/05/2026"}
        );
    }

    public static List<Patient> doctorPatients() {
        return PATIENT_LIST;
    }

    public static void addPatient(Patient patient) {
        PATIENT_LIST.add(0, patient);
        PATIENTS.add(patient);
    }

    public static List<Patient> getPatients() {
        return new ArrayList<>(PATIENTS);
    }
}
