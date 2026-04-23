package com.identityservice.service.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
public class GeoIPService {

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() throws IOException {
        // Load file database khi khởi động app
        ClassPathResource resource = new ClassPathResource("geo/GeoLite2-City.mmdb");
        InputStream inputStream = resource.getInputStream();
        dbReader = new DatabaseReader.Builder(inputStream).build();
    }

    public String getLocationFromIp(String ipAddress) {
        try {
            // Xử lý trường hợp chạy localhost
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                return "Localhost";
            }

            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            String cityName = response.getCity().getName();
            String countryName = response.getCountry().getName();

            if (cityName != null && countryName != null) {
                return cityName + ", " + countryName; // VD: Hanoi, Vietnam
            } else if (countryName != null) {
                return countryName;
            }
            return "Unknown Location";

        } catch (IOException | GeoIp2Exception e) {
            // Log lỗi nếu cần, nhưng đừng throw exception để không chặn luồng login
            return "Unknown Location";
        }
    }
}
