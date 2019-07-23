import facebook4j.*;
import facebook4j.conf.ConfigurationBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class FBfeedExtract {
    public static void main(String[] args) throws FacebookException, IOException, URISyntaxException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("wall");
        cb.setDebugEnabled(true)
                .setOAuthAppId("")
                .setOAuthAppSecret("")
                .setOAuthAccessToken("")
                .setOAuthPermissions("read_stream,public_profile,user_friends");

        FacebookFactory ff = new FacebookFactory(cb.build());
        facebook4j.Facebook facebook = ff.getInstance();

        System.out.println(facebook.getPosts("wall").getPaging().getNext());
        System.out.println("**********************");

        int totalPosts = 50;
        ArrayList<JsonArray> targetTags = new ArrayList<JsonArray>();
        ResponseList<Post> tags;
        ArrayList<String> posts = new ArrayList<String>();
        int count = 0;
        JsonReader rdr;
        int rowCount = 0, columnCount = 0,pageCount = 0,commentCount = 0;
        ResponseList<Post> targetTagsPages = facebook.getPosts("wall");
        URL nextPage = targetTagsPages.getPaging().getNext();
        rdr = Json.createReader(nextPage.openStream());
        JsonObject jsonObject = rdr.readObject();
        JsonArray jsonDataArray = jsonObject.getJsonArray("data");
        String jsonPreviousUrlArray = jsonObject.getJsonObject("paging").getString("previous");
        String jsonNextUrlArray = jsonObject.getJsonObject("paging").getString("next");

        count = jsonDataArray.size();
//


        //get all comments for one post
        int postSize = facebook.getPosts("wall").size();;
        int commentCountOnePage;
        PagableList<Comment> commentList;
        ArrayList<String> comments = new ArrayList<String>();
        URL nextPageURL;
        int totalcount = 0,p;
        JsonArray jsonArray;
        String jsonPagingArray;
        URL nextPostPageURL = new URL(facebook.getPosts("wall").getPaging().getNext().toString());
        JsonObject object, postsJsonObject,postsObject;
        while(comments.size()<5000 ) {

            while (postSize > 0 && comments.size() < 5000) {
                commentCountOnePage = facebook.getPosts("wall").get(postSize - 1).getComments().size();
                commentList = facebook.getPosts("wall").get(postSize - 1).getComments();
                for (int x = 0; x < commentCountOnePage; x++) {
                    comments.add(commentList.get(x).getMessage());
                }
                nextPageURL = facebook.getPosts("wall").get(postSize - 1).getComments().getPaging().getNext();

                while (comments.size() < 5000) {

                    try {
                        rdr = Json.createReader(nextPageURL.openStream());
                        object = rdr.readObject();
                        jsonDataArray = object.getJsonArray("data");
                        jsonPagingArray = object.getJsonObject("paging").getString("next");
                        nextPageURL = new URL(jsonPagingArray);
                        totalcount = jsonDataArray.size();
                        for (p = 0; p < totalcount; p++) {
                            System.out.println(jsonDataArray.getJsonObject(p).getString("message"));
                            comments.add(object.getJsonArray("data").getJsonObject(p).getString("message"));
                        }
                        commentCountOnePage--;
                    } catch (NullPointerException e) {
                        commentCountOnePage--;
                        System.out.println("not found");
                        break;
                    }
                }
                postSize--;
            }
            try {
                postsJsonObject = Json.createReader(nextPostPageURL.openStream()).readObject();
                postsObject = postsJsonObject.getJsonObject("data");
                nextPostPageURL = new URL(postsObject.getJsonObject("paging").getString("next"));
                postSize = postsObject.getJsonObject("data").size();
            }
            catch (NullPointerException e){
                break;
            }
        }

        for (String target : comments
                    ) {
                count++;
                Row row = sheet.createRow(rowCount++);
                Cell message = row.createCell(columnCount);
            message.setCellValue(target);
                columnCount = 0;
            }

        try {
            FileOutputStream outputStream = new FileOutputStream(new File("facebook5000.xlsx"));
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
