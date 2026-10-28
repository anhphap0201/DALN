package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phân tích cấu trúc câu hỏi người dùng thành Intent, ràng buộc (constraints)
 * và phát hiện thông tin còn thiếu (missing information) trước khi gọi Tool hoặc phản hồi.
 */
@Component
public class ChatIntentAnalyzer {

    public enum Intent {
        BUILD_PROJECT,
        PRODUCT_SEARCH,
        PRODUCT_COMPARE,
        PRODUCT_RECOMMEND,
        ORDER_TRACKING,
        ORDER_ANALYTICS,
        INVENTORY_ANALYTICS,
        SALES_ANALYTICS,
        CUSTOMER_ANALYTICS,
        INQUIRY_ANALYTICS,
        WARRANTY_INQUIRY,
        CHIT_CHAT,
        GENERAL_KNOWLEDGE
    }

    public record AnalysisResult(
            Intent intent,
            String projectTopic,
            Map<String, Object> constraints,
            List<String> missingInformation,
            boolean needsClarification
    ) {}

    public AnalysisResult analyze(String message) {
        if (message == null || message.isBlank()) {
            return new AnalysisResult(Intent.CHIT_CHAT, null, Map.of(), List.of(), false);
        }

        String norm = normalizeText(message);

        // 1. Phân tích intent xây dựng / lắp ráp dự án kỹ thuật (BUILD_PROJECT)
        if (isBuildProjectIntent(norm)) {
            return analyzeBuildProject(message, norm);
        }

        // 2. Tra cứu đơn hàng (ORDER_TRACKING)
        if (norm.contains("don hang") || norm.contains("tra cuu don") || norm.contains("ma don")
                || norm.contains("ord-") || norm.contains("#ord")
                || norm.matches(".*\\b(van chuyen|giao hang|tinh trang don)\\b.*")) {
            Map<String, Object> constraints = new HashMap<>();
            Matcher mOrder = Pattern.compile("(?i)(?:#|ord-)[a-z0-9-]+").matcher(message);
            if (mOrder.find()) {
                constraints.put("orderCode", mOrder.group());
            }
            Matcher mPhone = Pattern.compile("(?:0|\\+84)[0-9]{8,10}\\b").matcher(message);
            if (mPhone.find()) {
                constraints.put("phoneNumber", mPhone.group());
            }
            return new AnalysisResult(Intent.ORDER_TRACKING, null, constraints, List.of(), false);
        }

        // 2.5. Tra cứu bảo hành (WARRANTY_INQUIRY)
        if (norm.contains("bao hanh") || norm.contains("tra cuu bao hanh") || norm.contains("kiem tra bao hanh")
                || norm.contains("tem bao hanh") || norm.contains("sn-") || norm.contains("#sn")
                || norm.matches(".*\\b(con bao hanh|het bao hanh|chinh sach bao hanh|dieu kien bao hanh)\\b.*")) {
            Map<String, Object> constraints = new HashMap<>();
            Matcher mSerial = Pattern.compile("(?i)(?:#|sn-)[a-z0-9-]+").matcher(message);
            if (mSerial.find()) {
                constraints.put("serialNumber", mSerial.group().toUpperCase());
            }
            Matcher mPhone = Pattern.compile("(?:0|\\+84)[0-9]{8,10}\\b").matcher(message);
            if (mPhone.find()) {
                constraints.put("phoneNumber", mPhone.group());
            }
            return new AnalysisResult(Intent.WARRANTY_INQUIRY, null, constraints, List.of(), false);
        }

        // 3. So sánh sản phẩm (PRODUCT_COMPARE)
        if (norm.contains("so sanh") || norm.matches(".*\\b(vs|versus)\\b.*")) {
            return new AnalysisResult(Intent.PRODUCT_COMPARE, null, Map.of(), List.of(), false);
        }

        // 4. Báo cáo & Cảnh báo tồn kho nội bộ (INVENTORY_ANALYTICS)
        if (norm.contains("sap het hang") || norm.contains("canh bao ton kho") || norm.contains("hang sap het") || norm.contains("ton kho thap")
                || norm.contains("gia tri ton kho") || norm.contains("von luu kho") || norm.contains("bao cao ton kho") || norm.contains("hang ton lau")
                || norm.contains("cham luan chuyen") || norm.contains("nhap them")) {
            return new AnalysisResult(Intent.INVENTORY_ANALYTICS, null, Map.of(), List.of(), false);
        }

        // 5. Phân tích đơn hàng (ORDER_ANALYTICS)
        if (norm.contains("ty le huy") || norm.contains("ty le hoan") || norm.contains("aov") || norm.contains("gia tri don trung binh")
                || norm.contains("phan tich don hang")) {
            return new AnalysisResult(Intent.ORDER_ANALYTICS, null, Map.of(), List.of(), false);
        }

        // 6. Phân tích khách hàng (CUSTOMER_ANALYTICS)
        if (norm.contains("khach hang moi") || norm.contains("khach quay lai") || norm.contains("ty le giu chan") || norm.contains("phan tich khach hang")
                || norm.contains("ti le mua lai")) {
            return new AnalysisResult(Intent.CUSTOMER_ANALYTICS, null, Map.of(), List.of(), false);
        }

        // 6.5. Phân tích thắc mắc & chủ đề khách thường hỏi (INQUIRY_ANALYTICS)
        if (norm.contains("nguoi dung thuong hoi") || norm.contains("hay hoi") || norm.contains("khach hang quan tam")
                || norm.contains("chu de hoi") || norm.contains("nhu cau khach") || norm.contains("thong ke tim kiem")
                || norm.contains("quan tam den gi") || norm.contains("hoi ve van de gi")) {
            return new AnalysisResult(Intent.INQUIRY_ANALYTICS, null, Map.of(), List.of(), false);
        }

        // 7. Báo cáo doanh thu & Hiệu suất bán hàng nội bộ (SALES_ANALYTICS - Dành cho Quản trị viên)
        if (norm.contains("doanh thu") || norm.contains("bao cao doanh thu") || norm.contains("tien ban")
                || norm.contains("doanh so") || norm.contains("loi nhuan") || norm.contains("tai chinh")
                || norm.contains("hieu suat san pham") || norm.contains("danh muc nao ban chay") || norm.contains("thong ke doanh thu")) {
            return new AnalysisResult(Intent.SALES_ANALYTICS, null, Map.of(), List.of(), false);
        }

        // 7.5. Tra cứu sản phẩm bán chạy / phổ biến (PRODUCT_RECOMMEND - Dành cho khách hàng)
        if (norm.contains("ban chay") || norm.contains("top ban") || norm.contains("mua nhieu") || norm.contains("pho bien nhat")) {
            return new AnalysisResult(Intent.PRODUCT_RECOMMEND, null, Map.of("topSelling", true), List.of(), false);
        }

        // 6. Gợi ý theo nhu cầu / ngân sách (PRODUCT_RECOMMEND)
        if (norm.contains("goi y") || norm.contains("tu van") || norm.contains("ngan sach")
                || norm.contains("duoi ") || norm.contains("toi da ") || norm.contains("tam gia")) {
            return new AnalysisResult(Intent.PRODUCT_RECOMMEND, null, Map.of(), List.of(), false);
        }

        // 7. Chào hỏi & Xã giao (CHIT_CHAT)
        if (norm.matches("^(xin chao|chao|hello|hi|hey|alo|cam on|thanks|thank you|tam biet|bye)\\b.*")
                || norm.contains("hom nay the nao") || norm.contains("khoe khong")
                || norm.contains("ban la ai") || norm.contains("ban co the lam gi") || norm.contains("ban giup duoc gi")) {
            return new AnalysisResult(Intent.CHIT_CHAT, null, Map.of(), List.of(), false);
        }

        // 8. Tra cứu sản phẩm cụ thể (PRODUCT_SEARCH)
        if (norm.matches(".*\\b(co ban|con hang|co san|co khong|con khong|tim mua|can mua)\\b.*")
                || norm.matches(".*\\b(arduino|esp32|esp8266|raspberry|raspi|ic|linh kien|cam bien|module|dien tro|tu dien|oled|lcd|servo|motor|dong co|nguon|relay|led|uno|mega|mach|gia|con hang|pin|lipo|esc|canh quat|khung|frame|driver|tay dieu khien|tx|rx|breadboard|day cam|day noi)\\b.*")) {
            return new AnalysisResult(Intent.PRODUCT_SEARCH, null, Map.of(), List.of(), false);
        }

        return new AnalysisResult(Intent.GENERAL_KNOWLEDGE, null, Map.of(), List.of(), false);
    }

    private boolean isBuildProjectIntent(String norm) {
        return norm.matches(".*\\b(build|lap rap|che tao|tu lam|lam|thiet ke|du an)\\b.*\\b(drone|quadcopter|robot|xe do line|xe tranh vat can|xe dieu khien|may ap trung|nha thong minh|smart home|tram thoi tiet|tuoi tu dong|canh tay robot|may in 3d|mach|he thong)\\b.*")
                || norm.matches(".*\\b(drone|quadcopter|xe do line|xe tranh vat can|may ap trung|tram thoi tiet|canh tay robot)\\b.*\\b(can linh kien gi|can nhung gi|linh kien can thiet|huong dan|tu van)\\b.*")
                || norm.contains("build mini drone")
                || norm.contains("build drone");
    }

    private AnalysisResult analyzeBuildProject(String originalMessage, String norm) {
        String project = "dự án kỹ thuật";
        if (norm.contains("drone") || norm.contains("quadcopter")) {
            project = "mini_drone";
        } else if (norm.contains("xe do line") || norm.contains("xe do duong")) {
            project = "line_following_robot";
        } else if (norm.contains("xe tranh vat can") || norm.contains("xe tu hanh")) {
            project = "obstacle_avoiding_robot";
        } else if (norm.contains("smart home") || norm.contains("nha thong minh")) {
            project = "smart_home_iot";
        } else if (norm.contains("tram thoi tiet") || norm.contains("quan trac")) {
            project = "weather_station";
        } else if (norm.contains("canh tay robot")) {
            project = "robotic_arm";
        }

        Map<String, Object> constraints = new HashMap<>();
        List<String> missing = new ArrayList<>();

        // 1. Kiểm tra ngân sách
        BigDecimal budget = extractBudget(norm);
        if (budget != null) {
            constraints.put("budget", budget);
        } else {
            missing.add("Mức ngân sách dự kiến của bạn (ví dụ: khoảng 1.5 triệu, 3 triệu...)?");
        }

        // 2. Kiểm tra thông số theo từng loại dự án
        if ("mini_drone".equals(project)) {
            if (norm.contains("2 inch") || norm.contains("2inch") || norm.contains("3 inch") || norm.contains("3inch") || norm.contains("5 inch") || norm.contains("5inch")) {
                constraints.put("frame_size", extractFrameSize(norm));
            } else {
                missing.add("Kích thước khung mong muốn (ví dụ: mini Whoop 2 inch, 3 inch nhỏ gọn, hay 5 inch FPV tiêu chuẩn)?");
            }

            if (norm.contains("trong nha") || norm.contains("indoor") || norm.contains("ngoai troi") || norm.contains("outdoor")) {
                constraints.put("environment", norm.contains("trong nha") || norm.contains("indoor") ? "indoor" : "outdoor");
            } else {
                missing.add("Môi trường bay chủ yếu (trong nhà hay ngoài trời)?");
            }

            if (norm.contains("camera") || norm.contains("fpv") || norm.contains("gps")) {
                constraints.put("has_fpv_or_gps", true);
            } else {
                missing.add("Bạn có cần gắn thêm Camera FPV / Kính FPV hoặc GPS tự giữ vị trí không?");
            }

            if (!norm.contains("da co") && !norm.contains("co san")) {
                missing.add("Bạn đã có sẵn tay điều khiển (Transmitter), pin LiPo và bộ sạc chưa hay cần mua mới trọn gói?");
            }
        } else if ("line_following_robot".equals(project) || "obstacle_avoiding_robot".equals(project)) {
            if (!norm.contains("arduino") && !norm.contains("esp32") && !norm.contains("stm32")) {
                missing.add("Bạn dự định dùng vi điều khiển nào làm mạch não (Arduino Uno, ESP32 hay STM32)?");
            }
            if (!norm.contains("2 banh") && !norm.contains("4 banh") && !norm.contains("2wd") && !norm.contains("4wd")) {
                missing.add("Khung xe bạn muốn loại 2 bánh tự lựa (2WD) hay 4 bánh (4WD)?");
            }
        }

        boolean needsClarification = !missing.isEmpty();
        return new AnalysisResult(Intent.BUILD_PROJECT, project, constraints, missing, needsClarification);
    }

    private String extractFrameSize(String norm) {
        if (norm.contains("2 inch") || norm.contains("2inch")) return "2 inch";
        if (norm.contains("3 inch") || norm.contains("3inch")) return "3 inch";
        if (norm.contains("5 inch") || norm.contains("5inch")) return "5 inch";
        return "tùy chọn";
    }

    private BigDecimal extractBudget(String norm) {
        Matcher matcher = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|k|nghin|ngan|dong|d)?\\b").matcher(norm);
        while (matcher.find()) {
            String number = matcher.group(1);
            String unit = matcher.group(2);
            String context = norm.substring(Math.max(0, matcher.start() - 24), matcher.start());
            boolean hasBudgetContext = context.matches("(?s).*\\b(duoi|toi da|ngan sach|tam|khoang)\\s*$");
            if (unit == null && !number.contains(".") && !number.contains(",") && !hasBudgetContext) {
                continue;
            }
            try {
                if (unit == null || unit.equals("d") || unit.equals("dong")) {
                    return new BigDecimal(number.replaceAll("[.,]", ""));
                } else {
                    BigDecimal amount = new BigDecimal(number.replace(',', '.'));
                    return amount.multiply(unit.equals("tr") || unit.equals("trieu")
                            ? BigDecimal.valueOf(1_000_000)
                            : BigDecimal.valueOf(1_000));
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static String normalizeText(String value) {
        return Normalizer.normalize(Objects.toString(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replace('đ', 'd');
    }
}
