package com.pakago.electronic.thread;

import com.google.gson.Gson;
import com.pakago.electronic.Application;
import com.pakago.electronic.entity.ElectronicScale;
import com.pakago.electronic.util.ResourceUtils;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ElectronicThread implements Runnable {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();
    private final static Logger LOGGER = LoggerFactory.getLogger(ElectronicThread.class);

    @Override
    public void run() {
        System.out.println("\nK\u1EBFt n\u1ED1i \u0111\u1EBFn h\u1EC7 th\u1ED1ng Pakago");

        while (true) {
            if (Application.linkedQueue.size() > 0) {
                String message = Application.linkedQueue.poll();

                ElectronicScale dataElectronicScale = new Gson().fromJson(message, ElectronicScale.class);

                if (dataElectronicScale != null && !StringUtils.isAllBlank(dataElectronicScale.getWeight(), dataElectronicScale.getBarcode())) {
                    try {
                        File file = new File(ResourceUtils.getValue("folderImage") + "\\" + dataElectronicScale.getBarcode() + ".png");

                        String imageEncode = Base64.encodeBase64String(FileUtils.readFileToByteArray(file));

                        dataElectronicScale.setImage(imageEncode);

                        Map<String, String> dataPost = new HashMap<>();
                        dataPost.put("image", dataElectronicScale.getImage());
                        dataPost.put("id" , dataElectronicScale.getBarcode());
                        dataPost.put("weight" , dataElectronicScale.getWeight());

                        String data = post(ResourceUtils.getValue("serviceApi"), new Gson().toJson(dataPost));

                        Map<String, Object> mapResult = new Gson().fromJson(data, Map.class);

                        double errorCode = (double) mapResult.get("errorCode");

                        if (errorCode == -1.0) {
                            LOGGER.info(String.format("Kh\u00F4ng t\u1ED3n t\u1EA1i m\u00E3 [%s]", dataElectronicScale.getBarcode()));
                        } else {
                            LOGGER.info(String.format("C\u1EADp nh\u1EADp th\u00E0nh c\u00F4ng m\u00E3 [%s]", dataElectronicScale.getBarcode()));
                        }
                    } catch (Exception ex) {
                        LOGGER.info("Kh\u00F4ng k\u1EBFt n\u1ED1i \u0111\u01B0\u1EE3c v\u1EDBi h\u1EC7 th\u1ED1ng Pakago");
                    }
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    private static String post(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                throw new IllegalAccessException("Error Code " + response.code());
            }

            return response.body().string();
        }
    }

//    public static void main(String[] args) throws Exception {
//
//        File file = new File(ResourceUtils.getValue("folderImage") + "\\OR14042019002.png");
//
//        String imageEncode = Base64.encodeBase64String(FileUtils.readFileToByteArray(file));
//
//        Map<String, String> dataPost = new HashMap<>();
//        dataPost.put("image", imageEncode);
//        dataPost.put("id" , "O1R14042019002");
//        dataPost.put("weight" , "13.9");
//
//        String data = post(ResourceUtils.getValue("serviceApi"), new Gson().toJson(dataPost));
//
//        Map<String, Object> mapResult = new Gson().fromJson(data, Map.class);
//
//        double errorCode = (double) mapResult.get("errorCode");
//
//        System.out.println(errorCode);
//
//    }
}
