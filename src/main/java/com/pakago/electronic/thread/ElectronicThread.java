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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElectronicThread implements Runnable {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();
    private final static Logger LOGGER = LoggerFactory.getLogger(ElectronicThread.class);

    @Override
    public void run() {
        System.out.println("\nKet noi den he thong Pakago");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        while (true) {
            if (Application.linkedQueue.size() > 0) {
                String message = Application.linkedQueue.poll();

                ElectronicScale dataElectronicScale = new Gson().fromJson(message, ElectronicScale.class);

                if (dataElectronicScale != null && !StringUtils.isAllBlank(dataElectronicScale.getWeight(), dataElectronicScale.getBarcode())) {
                    try {
                        File file = new File(ResourceUtils.getValue("folderImage") + "\\" + simpleDateFormat.format(new Date()) + "\\" + dataElectronicScale.getBarcode() + ".jpg");

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
                            LOGGER.info(String.format("Khong toi tai ma [%s]", dataElectronicScale.getBarcode()));
                        } else {
                            LOGGER.info(String.format("Cap nhap thanh cong ma [%s]", dataElectronicScale.getBarcode()));
                        }
                    } catch (Exception ex) {
                        LOGGER.info("Khong ket noi duoc voi he thong Pakago");
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
