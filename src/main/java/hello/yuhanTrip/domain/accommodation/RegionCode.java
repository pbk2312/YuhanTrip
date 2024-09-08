package hello.yuhanTrip.domain.accommodation;

import java.util.HashMap;
import java.util.Map;

public class RegionCode {

    private static final Map<String, Integer> regionCodeMap = new HashMap<>();

    static {
        regionCodeMap.put("Seoul", 1);
        regionCodeMap.put("Incheon", 2);
        regionCodeMap.put("Daejeon", 3);
        regionCodeMap.put("Daegu", 4);
        regionCodeMap.put("Gwangju", 5);
        regionCodeMap.put("Busan", 6);
        regionCodeMap.put("Ulsan", 7);
        regionCodeMap.put("Sejong", 8);
        regionCodeMap.put("Gyeonggi", 31);
        regionCodeMap.put("Gangwon", 32);
        regionCodeMap.put("Chungbuk", 33);
        regionCodeMap.put("Chungnam", 34);
        regionCodeMap.put("Gyeongbuk", 35);
        regionCodeMap.put("Gyeongnam", 36);
        regionCodeMap.put("Jeonbuk", 37);
        regionCodeMap.put("Jeonnam", 38);
        regionCodeMap.put("Jeju", 39);
    }

    public static Integer getCodeByRegion(String region) {
        return regionCodeMap.get(region);
    }

    public static String getRegionByCode(Integer code) {
        return regionCodeMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
