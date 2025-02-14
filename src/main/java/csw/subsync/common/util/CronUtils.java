package csw.subsync.common.util;

public class CronUtils {

    /**
     * 매 n초마다 실행 (예: "0/5 * * * * ?" -> 5초마다 실행)
     *
     * @param seconds 반복 간격(초)
     * @return Cron 표현식
     */
    public static String everyXSeconds(int seconds) {
        // 초 분 시 일 월 요일
        // 0/seconds -> "n초 마다"
        return "0/" + seconds + " * * * * ?";
    }

    /**
     * 매 n분마다 실행 (예: "0 0/10 * * * ?" -> 10분마다 실행)
     *
     * @param minutes 반복 간격(분)
     * @return Cron 표현식
     */
    public static String everyXMinutes(int minutes) {
        // 초 분 시 일 월 요일
        // 0 0/minutes -> "n분 마다"
        return "0 0/" + minutes + " * * * ?";
    }

    /**
     * 매 n시간마다 실행 (예: "0 0 0/2 * * ?" -> 2시간마다 실행)
     *
     * @param hours 반복 간격(시간)
     * @return Cron 표현식
     */
    public static String everyXHours(int hours) {
        // 초 분 시 일 월 요일
        // 0 0 0/hours -> "n시간 마다"
        return "0 0 0/" + hours + " * * ?";
    }

    /**
     * 매 n일마다 실행 (예: "0 0 0 1/3 * ?" -> 3일마다 실행 (매달 1일 기준으로 +3일))
     *
     * @param days 반복 간격(일)
     * @return Cron 표현식
     */
    public static String everyXDays(int days) {
        // 초 분 시 일 월 요일
        // 0 0 0 1/days -> "n일 마다"
        return "0 0 0 1/" + days + " * ?";
    }

    /**
     * 특정 '시:분' 에 매일 실행 (예: "0 30 5 * * ?" -> 매일 05:30:00)
     *
     * @param hour   실행 시 (0~23)
     * @param minute 실행 분 (0~59)
     * @return Cron 표현식
     */
    public static String dailyAtHourAndMinute(int hour, int minute) {
        // 초 분 시 일 월 요일
        return String.format("0 %d %d * * ?", minute, hour);
    }

    /**
     * 매 n일마다 특정 '시:분'에 실행
     * 예: everyXDaysAtHourAndMinute(2, 5, 30) -> "0 30 5 1/2 * ?"
     * => 첫 날(1일)부터 시작해서 2일 간격으로 매일 05:30:00에 실행
     *
     * @param days   반복 간격(일)
     * @param hour   실행 시 (0~23)
     * @param minute 실행 분 (0~59)
     * @return Cron 표현식
     */
    public static String everyXDaysAtHourAndMinute(int days, int hour, int minute) {
        // 초 분 시 (1/days) -> 매 n일
        return String.format("0 %d %d 1/%d * ?", minute, hour, days);
    }

    /**
     * 복합 형태: "A일 B시 C분마다" 실행하고 싶을 때
     * 예: "A=2, B=3, C=15" -> "0 15 3 1/2 * ?" -> 매 2일 간격으로 오전 3시 15분에 실행
     *
     * @param days    (A) 일
     * @param hours   (B) 시
     * @param minutes (C) 분
     * @return Cron 표현식
     */
    public static String everyDaysHoursMinutes(int days, int hours, int minutes) {
        // 초 분 시 1/days(일) * ?
        return String.format("0 %d %d 1/%d * ?", minutes, hours, days);
    }
}
