package com.dermacare.clinic.patient;

import java.util.HashMap;
import java.util.Map;

public class DiseaseTranslator {
    private static final Map<String, String> DICTIONARY = new HashMap<>();

    static {
        DICTIONARY.put("acanthosis nigricans", "Bệnh gai đen");
        DICTIONARY.put("acne", "Mụn trứng cá");
        DICTIONARY.put("acne vulgaris", "Mụn trứng cá thông thường");
        DICTIONARY.put("acquired autoimmune bullous diseaseherpes gestationis", "Bệnh da bọng nước tự miễn");
        DICTIONARY.put("acrodermatitis enteropathica", "Viêm da đầu chi ruột");
        DICTIONARY.put("actinic keratosis", "Dày sừng quang hóa");
        DICTIONARY.put("allergic contact dermatitis", "Viêm da tiếp xúc dị ứng");
        DICTIONARY.put("aplasia cutis", "Bất sản da");
        DICTIONARY.put("basal cell carcinoma", "Ung thư biểu mô tế bào đáy (BCC)");
        DICTIONARY.put("basal cell carcinoma morpheiform", "Ung thư tế bào đáy thể xơ cứng");
        DICTIONARY.put("becker nevus", "Bớt Becker");
        DICTIONARY.put("behcets disease", "Bệnh Behcet");
        DICTIONARY.put("calcinosis cutis", "Chứng vôi hóa da");
        DICTIONARY.put("cheilitis", "Viêm môi");
        DICTIONARY.put("congenital nevus", "Nốt ruồi bẩm sinh");
        DICTIONARY.put("dariers disease", "Bệnh Darier");
        DICTIONARY.put("dermatofibroma", "U xơ da");
        DICTIONARY.put("dermatomyositis", "Viêm cơ địa");
        DICTIONARY.put("disseminated actinic porokeratosis", "Porokeratosis do quang hóa lan tỏa");
        DICTIONARY.put("drug eruption", "Phát ban do thuốc");
        DICTIONARY.put("drug induced pigmentary changes", "Thay đổi sắc tố do thuốc");
        DICTIONARY.put("dyshidrotic eczema", "Chàm tổ đỉa");
        DICTIONARY.put("eczema", "Bệnh chàm / Viêm da cơ địa");
        DICTIONARY.put("ehlers danlos syndrome", "Hội chứng Ehlers-Danlos");
        DICTIONARY.put("epidermal nevus", "Bớt thượng bì");
        DICTIONARY.put("epidermolysis bullosa", "Ly thượng bì bọng nước");
        DICTIONARY.put("erythema annulare centrifigum", "Hồng ban vòng li tâm");
        DICTIONARY.put("erythema elevatum diutinum", "Hồng ban nổi sẩn kéo dài");
        DICTIONARY.put("erythema multiforme", "Hồng ban đa dạng");
        DICTIONARY.put("erythema nodosum", "Hồng ban nút");
        DICTIONARY.put("factitial dermatitis", "Viêm da giả tạo");
        DICTIONARY.put("fixed eruptions", "Hồng ban nhiễm sắc cố định");
        DICTIONARY.put("folliculitis", "Viêm nang lông");
        DICTIONARY.put("fordyce spots", "Hạt Fordyce");
        DICTIONARY.put("granuloma annulare", "U hạt vòng");
        DICTIONARY.put("granuloma pyogenic", "U hạt sinh mủ");
        DICTIONARY.put("hailey hailey disease", "Bệnh Hailey-Hailey");
        DICTIONARY.put("halo nevus", "Nốt ruồi viền sáng");
        DICTIONARY.put("hidradenitis", "Viêm tuyến mồ hôi mủ");
        DICTIONARY.put("ichthyosis vulgaris", "Bệnh vảy cá");
        DICTIONARY.put("incontinentia pigmenti", "Sắc tố dầm dề");
        DICTIONARY.put("juvenile xanthogranuloma", "U hạt vàng ở trẻ em");
        DICTIONARY.put("kaposi sarcoma", "Sarcoma Kaposi");
        DICTIONARY.put("keloid", "Sẹo lồi");
        DICTIONARY.put("keratosis pilaris", "Dày sừng nang lông");
        DICTIONARY.put("langerhans cell histiocytosis", "Bệnh mô bào Langerhans");
        DICTIONARY.put("lentigo maligna", "Đốm ác tính");
        DICTIONARY.put("lichen amyloidosis", "Lichen amyloidosis");
        DICTIONARY.put("lichen planus", "Lichen phẳng");
        DICTIONARY.put("lichen simplex", "Lichen đơn dạng");
        DICTIONARY.put("livedo reticularis", "Mạng xanh tím");
        DICTIONARY.put("lupus erythematosus", "Lupus ban đỏ");
        DICTIONARY.put("lupus subacute", "Lupus ban đỏ bán cấp");
        DICTIONARY.put("lyme disease", "Bệnh Lyme");
        DICTIONARY.put("lymphangioma", "U bạch mạch");
        DICTIONARY.put("malignant melanoma", "Ung thư hắc tố ác tính");
        DICTIONARY.put("melanoma", "Ung thư hắc tố (Melanoma)");
        DICTIONARY.put("milia", "Nang hạt kê");
        DICTIONARY.put("mucinosis", "Bệnh nhiễm nhầy");
        DICTIONARY.put("mucous cyst", "Nang nhầy");
        DICTIONARY.put("mycosis fungoides", "U lympho tế bào T (Mycosis Fungoides)");
        DICTIONARY.put("myiasis", "Bệnh giòi ròi");
        DICTIONARY.put("naevus comedonicus", "Bớt mụn trứng cá");
        DICTIONARY.put("necrobiosis lipoidica", "Hoại tử mỡ");
        DICTIONARY.put("nematode infection", "Nhiễm giun sán");
        DICTIONARY.put("neurodermatitis", "Viêm da thần kinh");
        DICTIONARY.put("neurofibromatosis", "U xơ thần kinh");
        DICTIONARY.put("neurotic excoriations", "Vết xước do loạn thần kinh");
        DICTIONARY.put("neutrophilic dermatoses", "Bệnh da do bạch cầu trung tính");
        DICTIONARY.put("nevocytic nevus", "Nốt ruồi hắc tố");
        DICTIONARY.put("nevus sebaceous of jadassohn", "Bớt bã tuyến Jadassohn");
        DICTIONARY.put("papilomatosis confluentes and reticulate", "U nhú hội tụ và mạng lưới");
        DICTIONARY.put("paronychia", "Viêm quanh móng");
        DICTIONARY.put("pediculosis lids", "Rận mí mắt");
        DICTIONARY.put("perioral dermatitis", "Viêm da quanh miệng");
        DICTIONARY.put("photodermatoses", "Viêm da do ánh sáng");
        DICTIONARY.put("pilar cyst", "Nang bã");
        DICTIONARY.put("pilomatricoma", "U nang lông");
        DICTIONARY.put("pityriasis lichenoides chronica", "Vảy phấn dạng lichen mạn tính");
        DICTIONARY.put("pityriasis rosea", "Vảy phấn hồng");
        DICTIONARY.put("pityriasis rubra pilaris", "Vảy phấn đỏ nang lông");
        DICTIONARY.put("porokeratosis actinic", "Porokeratosis quang hóa");
        DICTIONARY.put("porokeratosis of mibelli", "Porokeratosis Mibelli");
        DICTIONARY.put("porphyria", "Bệnh Porphyria");
        DICTIONARY.put("port wine stain", "Bớt rượu vang đỏ");
        DICTIONARY.put("prurigo nodularis", "Sẩn ngứa cục");
        DICTIONARY.put("psoriasis", "Vảy nến");
        DICTIONARY.put("pustular psoriasis", "Vảy nến mụn mủ");
        DICTIONARY.put("pyogenic granuloma", "U hạt sinh mủ");
        DICTIONARY.put("rhinophyma", "Mũi sư tử");
        DICTIONARY.put("rosacea", "Chứng đỏ mặt");
        DICTIONARY.put("sarcoidosis", "Bệnh Sarcoidosis");
        DICTIONARY.put("scabies", "Bệnh ghẻ");
        DICTIONARY.put("scleroderma", "Xơ cứng bì");
        DICTIONARY.put("scleromyxedema", "Scleromyxedema");
        DICTIONARY.put("seborrheic dermatitis", "Viêm da tiết bã");
        DICTIONARY.put("seborrheic keratosis", "Dày sừng tiết bã");
        DICTIONARY.put("solid cystic basal cell carcinoma", "Ung thư tế bào đáy dạng nang đặc");
        DICTIONARY.put("squamous cell carcinoma", "Ung thư biểu mô tế bào vảy (SCC)");
        DICTIONARY.put("stasis edema", "Phù ứ trệ");
        DICTIONARY.put("stevens johnson syndrome", "Hội chứng Stevens-Johnson");
        DICTIONARY.put("striae", "Rạn da");
        DICTIONARY.put("sun damaged skin", "Da tổn thương do nắng");
        DICTIONARY.put("superficial spreading melanoma ssm", "Ung thư hắc tố lan tỏa nông");
        DICTIONARY.put("syringoma", "U ống tuyến mồ hôi");
        DICTIONARY.put("telangiectases", "Giãn mao mạch");
        DICTIONARY.put("tick bite", "Vết cắn của ve");
        DICTIONARY.put("tuberous sclerosis", "Xơ cứng củ");
        DICTIONARY.put("tungiasis", "Bệnh rận cát");
        DICTIONARY.put("urticaria", "Mề đay");
        DICTIONARY.put("urticaria pigmentosa", "Mề đay sắc tố");
        DICTIONARY.put("vitiligo", "Bạch biến");
        DICTIONARY.put("xanthomas", "U vàng");
        DICTIONARY.put("xeroderma pigmentosum", "Khô da sắc tố");
    }

    public static String translate(String englishName) {
        if (englishName == null) return "Không xác định";
        String lower = englishName.toLowerCase().replace("_", " ").trim();
        if (DICTIONARY.containsKey(lower)) {
            return DICTIONARY.get(lower);
        }
        
        // Fallback: capitalize first letters
        String[] words = lower.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
