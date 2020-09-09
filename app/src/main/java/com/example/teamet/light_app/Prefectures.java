package com.example.teamet.light_app;

import android.util.Log;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;

public class Prefectures {

    public static int PREF_NUM = 47;

    public static int HOKKAIDO = 0;
    public static int AOMORI = 1;
    public static int IWATE = 2;
    public static int MIYAGI = 3;
    public static int AKITA = 4;
    public static int YAMAGATA = 5;
    public static int FUKUSHIMA = 6;
    public static int IBARAKI = 7;
    public static int TOCHIGI = 8;
    public static int GUNMA = 9;
    public static int SAITAMA = 10;
    public static int CHIBA = 11;
    public static int TOKYO = 12;
    public static int KANAGAWA = 13;
    public static int NIIGATA = 14;
    public static int TOYAMA = 15;
    public static int ISHIKAWA = 16;
    public static int FUKUI = 17;
    public static int YAMANASHI = 18;
    public static int NAGANO = 19;
    public static int GIFU = 20;
    public static int SHIZUOKA = 21;
    public static int AICHI = 22;
    public static int MIE = 23;
    public static int SHIGA = 24;
    public static int KYOTO = 25;
    public static int OSAKA = 26;
    public static int HYOGO = 27;
    public static int NARA = 28;
    public static int WAKAYAMA = 29;
    public static int TOTTORI = 30;
    public static int SHIMANE = 31;
    public static int OKAYAMA = 32;
    public static int HIROSHIMA = 33;
    public static int YAMAGUCHI = 34;
    public static int TOKUSHIMA = 35;
    public static int KAGAWA = 36;
    public static int EHIME = 37;
    public static int KOCHI = 38;
    public static int FUKUOKA = 39;
    public static int SAGA = 40;
    public static int NAGASAKI = 41;
    public static int KUMAMOTO = 42;
    public static int OITA = 43;
    public static int MIYAZAKI = 44;
    public static int KAGOSHIMA = 45;
    public static int OKINAWA = 46;

    public static String[] Pref = {
            "北海道",
            "青森県",
            "岩手県",
            "宮城県",
            "秋田県",
            "山形県",
            "福島県",
            "茨城県",
            "栃木県",
            "群馬県",
            "埼玉県",
            "千葉県",
            "東京都",
            "神奈川県",
            "新潟県",
            "富山県",
            "石川県",
            "福井県",
            "山梨県",
            "長野県",
            "岐阜県",
            "静岡県",
            "愛知県",
            "三重県",
            "滋賀県",
            "京都府",
            "大阪府",
            "兵庫県",
            "奈良県",
            "和歌山県",
            "鳥取県",
            "島根県",
            "岡山県",
            "広島県",
            "山口県",
            "徳島県",
            "香川県",
            "愛媛県",
            "高知県",
            "福岡県",
            "佐賀県",
            "長崎県",
            "熊本県",
            "大分県",
            "宮崎県",
            "鹿児島県",
            "沖縄県"
    };

    // 各都道府県の座標（緯度，経度）
    private static double[][] coordinates = {
            {43.06417, 141.34694},	// 北海道
            {40.82444, 140.74},	    // 青森県
            {39.70361, 141.1525},	// 岩手県
            {38.26889, 140.87194},	// 宮城県
            {39.71861, 140.1025},	// 秋田県
            {38.24056, 140.36333},	// 山形県
            {37.75, 140.46778},	    // 福島県
            {36.34139, 140.44667},	// 茨城県
            {36.56583, 139.88361},	// 栃木県
            {36.39111, 139.06083},	// 群馬県
            {35.85694, 139.64889},	// 埼玉県
            {35.60472, 140.12333},	// 千葉県
            {35.68944, 139.69167},	// 東京都
            {35.44778, 139.6425},	// 神奈川県
            {37.90222, 139.02361},	// 新潟県
            {36.69528, 137.21139},	// 富山県
            {36.59444, 136.62556},	// 石川県
            {36.06528, 136.22194},	// 福井県
            {35.66389, 138.56833},	// 山梨県
            {36.65139, 138.18111},	// 長野県
            {35.39111, 136.72222},	// 岐阜県
            {34.97694, 138.38306},	// 静岡県
            {35.18028, 136.90667},	// 愛知県
            {34.73028, 136.50861},	// 三重県
            {35.00444, 135.86833},	// 滋賀県
            {35.02139, 135.75556},	// 京都府
            {34.68639, 135.52},	    // 大阪府
            {34.69139, 135.18306},	// 兵庫県
            {34.68528, 135.83278},	// 奈良県
            {34.22611, 135.1675},	// 和歌山県
            {35.50361, 134.23833},	// 鳥取県
            {35.47222, 133.05056},	// 島根県
            {34.66167, 133.935},	// 岡山県
            {34.39639, 132.45944},	// 広島県
            {34.18583, 131.47139},	// 山口県
            {34.06583, 134.55944},	// 徳島県
            {34.34028, 134.04333},	// 香川県
            {33.84167, 132.76611},	// 愛媛県
            {33.55972, 133.53111},	// 高知県
            {33.60639, 130.41806},	// 福岡県
            {33.24944, 130.29889},	// 佐賀県
            {32.74472, 129.87361},	// 長崎県
            {32.78972, 130.74167},	// 熊本県
            {33.23806, 131.6125},	// 大分県
            {31.91111, 131.42389},	// 宮崎県
            {31.56028, 130.55806},	// 鹿児島県
            {26.2125, 127.68111},	// 沖縄県
    };

    static double[] GetCoodinate(int pref){
        return coordinates[pref].clone();
    }

    static Point GetPoint(int pref){
        return new Point(coordinates[pref][1], coordinates[pref][0], SpatialReferences.getWgs84());
    }

    static int GetNearPref(double lat, double lon){
        int pref=0;
        double dist, dist_min;
        dist_min = (coordinates[pref][0]-lat)*(coordinates[pref][0]-lat)+(coordinates[pref][1]-lon)*(coordinates[pref][1]-lon);
        for(int i=1; i<47; i++){
            dist = (coordinates[i][0]-lat)*(coordinates[i][0]-lat)+(coordinates[i][1]-lon)*(coordinates[i][1]-lon);
            if(dist<dist_min){
                dist_min = dist;
                pref = i;
            }
        }

        return pref;
    }

    static int GetIndex(String pref){
        for(int i=0; i<PREF_NUM; i++){
            if (pref.equals(Pref[i])) return i;
        }
        return -1;
    }
}
