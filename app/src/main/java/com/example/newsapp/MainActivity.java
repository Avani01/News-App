package com.example.newsapp;
/*
* first go to toi then go to rss feed in top right (wifi symbol). it is xml file of current live tech news
* code beautify is opened. go to xml viewer. load the url of the xml file.
* tags needed from it are title, description, publishing date
* */
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ArrayList<NewsItem> news;
    private RecyclerView recyclerView;
    private TextView textTitle;
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        textTitle = findViewById(R.id.textTitle);
        news = new ArrayList<>();
        adapter = new NewsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetNews().execute();
    }

    private class GetNews extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            InputStream inputStream = getInputStream();
            if (null != inputStream) {
                try {
                    initXMLPullParser(inputStream);
                }
                catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            adapter.setNews(news);
        }

        private InputStream getInputStream() {
            try {
                URL url = new URL("https://timesofindia.indiatimes.com/rssfeeds/66949542.cms");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.getInputStream();
            }

            /*catch (MalformedURLException e) {
                e.printStackTrace();
            } */

            // malformed url exception extends io exception so no need to specifically give malformed url exception
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void initXMLPullParser(InputStream inputStream) throws XmlPullParserException, IOException {
            Log.d(TAG, "initXMLPullParser: Initializing XML Pull Parser");
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.next();  // from rss, move to channel

            parser.require(XmlPullParser.START_TAG, null, "rss");   // we are in rss part in codebeautify
            while (parser.next() != XmlPullParser.END_TAG) {
                // loop until we get to end of xml file
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                    /* if we don't see opening bracket, continue till you see one
                     all items are inside channel*/
                }

                parser.require(XmlPullParser.START_TAG, null, "channel");
                while (parser.next() != XmlPullParser.END_TAG) {

                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    // after entering <channel>, we need to enter <item>

                    if (parser.getName().equals("item")) {
                        parser.require(XmlPullParser.START_TAG, null, "item");

                        String title="", description="", link="", date="";


                        while (parser.next() != XmlPullParser.END_TAG) {

                            if (parser.getEventType() != XmlPullParser.START_TAG) {
                                continue;
                            }

                            String tagName = parser.getName();
                            if (tagName.equals("title")) {
                                title = getContent(parser, "title");
                            }
                            else if (tagName.equals("description")) {
                                description = getContent(parser, "description");
                            }
                            else if (tagName.equals("link")) {
                                link = getContent(parser, "link");
                            }
                            else if (tagName.equals("pubdate")) {
                                date = getContent(parser, "pubdate");
                            }
                            else {
                                skipTag(parser);
                            }
                        }

                        NewsItem item = new NewsItem(title, description, link, date);
                        news.add(item);
                    }
                    else {
                        skipTag(parser);
                    }
                }
            }
        }

        private String getContent (XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
            String content = "";
            parser.require(XmlPullParser.START_TAG, null, tagName);

            if (parser.next() == XmlPullParser.TEXT) {
                content = parser.getText();
                parser.next();
            }
            return content;
        }

        private void skipTag (XmlPullParser parser) throws XmlPullParserException, IOException {
            // first check if opening tag or not
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }

            int number = 1;
            while (number != 0) {
                switch (parser.next()) {
                    case XmlPullParser.START_TAG:
                        number++;
                        break;

                    case XmlPullParser.END_TAG:
                        number--;
                        break;

                    default:
                        break;
                }
            }
        }
    }
}