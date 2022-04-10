package pro.furry.furbot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pro.furry.furbot.exception.LocalException;
import pro.furry.furbot.type.ApiType;
import pro.furry.furbot.type.GroupSettingType;
import pro.furry.furbot.type.RefererType;
import pro.furry.furbot.util.PublicUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private SettingService settingService;

    @Autowired
    public void setDBService(DBService dbService) {
        this.dbService = dbService;
    }

    @Autowired
    public void setSettingService(SettingService settingService) {
        this.settingService = settingService;
    }

    public Map<String, String> getPixivMemberInfo(Long pid) throws LocalException {
        log.info("Get Pixiv Member Information, PID: {}", pid);
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(pid));
        String url = dbService.getApiURL(ApiType.Hibi_Pixiv_Member);
        String response = sendGetRequest(url, data, String.class, RefererType.FurBot);
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

    public Map<String, String> getRandPMemberWork(Long gid, Long pid, int count, boolean isSpecific)
            throws LocalException  {
        log.info("Get Pixiv Member Illustration, PID: {}", pid);
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(pid));
        String url = dbService.getApiURL(ApiType.Hibi_Pixiv_Member_Illust);
        String response = sendGetRequest(url, data, String.class, RefererType.FurBot);
        JSONObject jsonObject = JSON.parseObject(response);
        if (jsonObject.getString("detail") != null) {
            throw new LocalException("Error: " + jsonObject.getString("detail"));
        }
        JSONArray illustrations = jsonObject.getJSONArray("illusts");
        if (illustrations == null || illustrations.isEmpty()) {
            throw new LocalException("此用户没有作品或此用户不存在");
        }
        int randLIndex = PublicUtil.getRandInt(0, illustrations.size() - 1);
        JSONObject illustration = illustrations.getJSONObject(randLIndex);

        boolean nsfw = isR18(illustration);

        // 是否允许发布R18内容
        String r18 = settingService.getGroupSetting(gid, GroupSettingType.Show_R18_Content);
        if (nsfw && !PublicUtil.parseBoolean(r18)) {
            // 重新选张图片
            if (illustrations.size() == 1) {
                if (count > 3 || dbService.getPMemberCount(gid) == 1) {
                    throw new LocalException("在指定深度内找不到满足筛选条件的作品");
                }

                if (isSpecific) { // 如果是指定画师的请求，找不到就直接报错
                    throw new LocalException("此画师没有满足筛选条件的作品");
                } else {
                    log.info("找不到满足条件的作品，重新选择画师（size = 1）");
                    return getRandPMemberWork(gid, dbService.getRandPMember(gid), count + 1, false);
                }
            }
            for (int index = randLIndex + 1; index < illustrations.size(); index++) {
                JSONObject illustrationNew = illustrations.getJSONObject(index);
                if (isR18(illustrationNew)) {
                    if (index + 1 == illustrations.size()) {
                        index = -1;
                    } else if (index == randLIndex) {
                        if (count > 3 || dbService.getPMemberCount(gid) == 1) {
                            throw new LocalException("在指定深度内找不到满足筛选条件的作品");
                        }

                        if (isSpecific) {
                            throw new LocalException("此画师没有满足筛选条件的作品");
                        } else {
                            log.info("找不到满足条件的作品，重新选择画师（all r18）");
                            return getRandPMemberWork(gid, dbService.getRandPMember(gid), count + 1, false);
                        }
                    }
                } else {
                    nsfw = false;
                    illustration = illustrationNew;
                }
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

    private boolean isR18(JSONObject illustration) {
        JSONArray tags = illustration.getJSONArray("tags");
        for (Object tag : tags) {
            if (((JSONObject) tag).getString("name").equalsIgnoreCase("R-18")) {
                return true;
            }
        }
        return false;
    }

    public InputStream getPictureFromFurBotAPI(String pictureURL, RefererType referer) throws LocalException {
        log.info("Get Picture from FurBotAPI, URL: {}", pictureURL);
        Map<String, String> data = new HashMap<>();
        data.put("url", pictureURL);
        data.put("from", referer.getReferer());
        String url = dbService.getApiURL(ApiType.Bot_Picture);
        Resource resource = sendGetRequest(url, data, Resource.class, RefererType.FurBot);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new LocalException("获取图片IO流时发生错误", e);
        }
    }

    public InputStream getPictureFromURL(String url) throws LocalException {
        log.info("Get Picture from URL: {}", url);
        Resource resource = sendGetRequest(url, null, Resource.class, RefererType.None);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            throw new LocalException("获取图片IO流时发生错误", e);
        }
    }

    public Map<String, String> searchPictureFromSauce(String pictureURL) throws LocalException {
        InputStream inputStream = getPictureFromURL(pictureURL);
        byte[] bytes = PublicUtil.getBytesAndClose(inputStream);
        if (bytes == null) {
            throw new LocalException("图片转换为Bytes时发送错误");
        }

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Contract(pure = true)
            @Override
            public @NotNull String getFilename() {
                return "0.jpg";
            }
        };
        MultiValueMap<String, Object> postData = new LinkedMultiValueMap<>();
        postData.add("file", resource);
        postData.add("size", 1);
        String url = dbService.getApiURL(ApiType.Hibi_Sauce_From);
        JSONObject jsonObject = sendPostRequest(url, postData);
        JSONArray results = jsonObject.getJSONArray("results");
        if (results == null || results.size() == 0) {
            throw new LocalException("获取原图失败");
        }
        JSONObject result = (JSONObject) results.get(0);
        JSONObject header = result.getJSONObject("header");
        JSONObject data   = result.getJSONObject("data");
        JSONArray extURLs = data.getJSONArray("ext_urls");

        Map<String, String> map = new HashMap<>();
        map.put("similarity", header.getString("similarity"));
        map.put("thumbnail", header.getString("thumbnail"));
        map.put("title", data.getString("title"));
        map.put("author", data.getString("author_name"));
        if (extURLs != null) {
            map.put("external", extURLs.getString(0));
        } else {
            map.put("external", "没有找到原链接，可以尝试前往saucenao.com再次搜索");
        }

        return map;
    }

    private <T> T sendGetRequest(String url, Map<String, String> data, Class<T> clazz, RefererType referer)
            throws LocalException {
        if (data != null) {
            StringBuilder stringBuilder = new StringBuilder(url);
            stringBuilder.append("?");
            data.forEach((key, value) -> {
                try {
                    String decode = URLEncoder.encode(value, "UTF-8");
                    stringBuilder.append(key)
                            .append("=")
                            .append(decode)
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    throw new LocalException("编码错误");
                }
            });
            url = stringBuilder.toString();
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }
        }
        log.info("Send Get Request, URL: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.add("referer", referer.getReferer());
        try {
            ResponseEntity<T> response = new RestTemplate().exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), clazz);
            HttpStatus status = response.getStatusCode();
            if (status.is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new LocalException("HTTP请求失败 " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new LocalException("HTTP请求失败", e);
        }
    }

    private JSONObject sendPostRequest(String url, MultiValueMap<String, Object> data) throws LocalException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("referer", RefererType.FurBot.getReferer());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(data, headers);
        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, httpEntity, String.class);
            HttpStatus status = response.getStatusCode();
            if (status.is2xxSuccessful()) {
                return JSON.parseObject(response.getBody());
            } else {
                throw new LocalException("HTTP请求失败 " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new LocalException("HTTP请求失败", e);
        }
    }
}
