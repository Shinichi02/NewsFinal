package com.ibraheem.android.internationalpalestinenews;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.ibraheem.android.internationalpalestinenews.NewsActivity.LOG_TAG;


public class QueryUtils {


    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl) {

        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        ArrayList<News> news = extractNews(jsonResponse);

        return news;
    }

    private static ArrayList<News> extractNews(String newsJSON) {

        ArrayList<News> news = new ArrayList<>();

        try {

            JSONObject response = new JSONObject(newsJSON).getJSONObject("response");
            JSONArray newsArray, tags;
            if (response.has("results")) {
                newsArray = response.getJSONArray("results");
                String title = "", sectionName = "", datePublished = "", url = "", author = "";
                for (int i = 0; i < newsArray.length(); i++) {
                    if (newsArray.getJSONObject(i).has("webTitle"))
                        title = newsArray.getJSONObject(i).getString("webTitle");

                    if (newsArray.getJSONObject(i).has("sectionName"))
                        sectionName = newsArray.getJSONObject(i).getString("sectionName");

                    if (newsArray.getJSONObject(i).has("webPublicationDate"))
                        datePublished = newsArray.getJSONObject(i).getString("webPublicationDate");

                    if (newsArray.getJSONObject(i).has("webUrl"))
                        url = newsArray.getJSONObject(i).getString("webUrl");

                    JSONObject TT = newsArray.getJSONObject(i);
                    if (TT.has("tags")) {
                        tags = TT.getJSONArray("tags");
                        if (tags.length()>0&&tags.getJSONObject(0).has("webTitle")) {
                            author = tags.getJSONObject(0).getString("webTitle");
                        }
                    }
                    news.add(new News(title, sectionName, datePublished, url, author));
                }
            } else {
                Log.e(LOG_TAG, "No results available");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }

        return news;
    }

    private static URL createUrl(String stringUrl) {

        URL url;

        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }

        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {

        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();

            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
