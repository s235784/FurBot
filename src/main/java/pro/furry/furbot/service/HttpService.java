package pro.furry.furbot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.type.ApiType;
import pro.furry.furbot.type.RefererType;
import pro.furry.furbot.util.PublicUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NuoTian
 * @date 2022/3/24
 */
@Slf4j
@Service
public class HttpService {

    private DBService dbService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    public Map<String, String> getPixivMemberInfo(Long pid) throws LocalException {
        log.info("Get Pixiv Member Information, PID: " + pid);
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(pid));
        String response = sendGetRequest(dbService.getApiURL(ApiType.Hibi_Pixiv_Member), data, String.class);
        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject.getJSONObject("error") != null) {
            throw new LocalException("Error 获取画师信息时发生错误，请检查id是否正确");
        }
        JSONObject user = jsonObject.getJSONObject("user");
        data.clear();
        data.put("name", user.getString("name"));
        data.put("account", user.getString("account"));
        data.put("avatarUrl", user.getJSONObject("profile_image_urls").getString("medium"));
        return data;
    }

    public Map<String, String> getRandPMemberWork(Long pid) throws LocalException  {
        log.info("Get Pixiv Member Illustration, PID: " + pid);
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(pid));
        String response = sendGetRequest(dbService.getApiURL(ApiType.Hibi_Pixiv_Member_Illust), data, String.class);
        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject.getString("detail") != null) {
            throw new LocalException("Error: " + jsonObject.getString("detail"));
        }
        JSONArray illustrations = jsonObject.getJSONArray("illusts");
        int randLIndex = PublicUtil.getRandInt(0, illustrations.size() - 1);
        JSONObject illustration = illustrations.getJSONObject(randLIndex);

        JSONArray tags = illustration.getJSONArray("tags");
        boolean nsfw = false;
        for (Object tag : tags) {
            if (((JSONObject) tag).getString("name").equalsIgnoreCase("R-18")) {
                nsfw = true;
                break;
            }
        }

        JSONArray metaPages = illustration.getJSONArray("meta_pages");
        JSONObject imageURLs;
        if (metaPages.size() == 0) {
            imageURLs = illustration.getJSONObject("image_urls");
        } else {
            int randMIndex = PublicUtil.getRandInt(0, metaPages.size() - 1);
            JSONObject metaPage = metaPages.getJSONObject(randMIndex);
            imageURLs = metaPage.getJSONObject("image_urls");
        }

        JSONObject user = illustration.getJSONObject("user");
        data.clear();
        data.put("id", illustration.getString("id"));
        data.put("title", illustration.getString("title"));
        data.put("username", user.getString("name"));
        data.put("image", imageURLs.getString("large"));
        data.put("type", nsfw ? "NSFW" : "SFW");
        return data;
    }

    public InputStream getPictureFromFurBotAPI(String pictureURL, RefererType referer) throws LocalException {
        log.info("Get Picture, URL: " + pictureURL);
        Map<String, String> data = new HashMap<>();
        data.put("url", pictureURL);
        data.put("from", referer.getReferer());
        Resource resource = sendGetRequest(dbService.getApiURL(ApiType.Bot_Picture), data, Resource.class);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new LocalException("获取图片IO流时发生错误", e);
        }
    }


    private <T> T sendGetRequest(String url, Map<String, String> data, Class<T> clazz) throws LocalException {
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("?");
        data.forEach((key, value) -> stringBuilder.append(key)
                .append("=")
                .append(value)
                .append("&"));
        url = stringBuilder.toString();
        if (url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        log.info("Send Get Request, URL: " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("referer", "https://furbot.furry.pro");
        try {
            ResponseEntity<T> response = new RestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), clazz);
            HttpStatus status = response.getStatusCode();
            if (status.is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new LocalException("HTTP请求失败");
            }
        } catch (Exception e) {
            throw new LocalException("HTTP请求失败", e);
        }
    }
}
