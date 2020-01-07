package com.example.teamet.light_app;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.*;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Dom {

    private final String TAG = "TOH";

    private final String regular = "http://www.data.jma.go.jp/developer/xml/feed/regular.xml";
    private final String regular_l = "http://www.data.jma.go.jp/developer/xml/feed/regular_l.xml";
    private final String extra = "http://www.data.jma.go.jp/developer/xml/feed/extra.xml";
    private final String extra_l = "http://www.data.jma.go.jp/developer/xml/feed/extra_l.xml";
    private final String eqvol = "http://www.data.jma.go.jp/developer/xml/feed/eqvol.xml";
    private final String eqvol_l = "http://www.data.jma.go.jp/developer/xml/feed/eqvol_l.xml";
    private final String other = "http://www.data.jma.go.jp/developer/xml/feed/other.xml";
    private final String other_l = "http://www.data.jma.go.jp/developer/xml/feed/other_l.xml";

    private SQLiteDatabase warnDB;

    public Dom(SQLiteDatabase warnDB) {
        this.warnDB = warnDB;
    }

    private void getInfo(String url){
        Log.d(TAG, url);
        InputStream is = null;
        String title, link;
        try{
            is = new URL(url).openConnection().getInputStream();
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            XPathExpression xpe = XPathFactory.newInstance().newXPath().compile("/feed/entry");
            NodeList nodes = (NodeList)xpe.evaluate(document, XPathConstants.NODESET);
            for(int i = nodes.getLength() - 1; 0 <= i; i--){
                Node node = ((Element)nodes.item(i)).getFirstChild();
                title = "";
                link = "";
                while(node != null){
                    if(node.getNodeName() == "title") title = node.getTextContent();
                    else if(node.getNodeName() == "link") link = ((Element)node).getAttribute("href");
                    node = node.getNextSibling();
                }
//                System.out.println("==================================================================================");
//                System.out.println("title: " + title);
//                System.out.println("link : " + link);
                Log.d(TAG, "title" + title);
                if(title.matches(".*警報・注意報.*")) this.getWarnInfo(link);
//                if(title.matches("大雨危険度通知.*")) this.getRainRiskInfo(link);
            }
//            System.out.println("==================================================================================");
//            System.out.println(nodes.getLength() + " read.");
            try{
                is.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{

        }
    }

    private void getRainRiskInfo(String url){
        InputStream is = null;
        try{
            Document document;
            XPath xpath;
            Node info, item;
            String areas, kinds;
            is = new URL(url).openConnection().getInputStream();
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            xpath = XPathFactory.newInstance().newXPath();
            info = ((NodeList)xpath.compile("/Report/Head/Headline/Information").evaluate(document, XPathConstants.NODESET)).item(3).getFirstChild();
            while(info != null){
                areas = "";
                kinds = "";
                if(info.getNodeName() == "Item"){
                    item = info.getFirstChild();
                    while(item != null){
                        if(item.getNodeName() == "Areas"){
                            Node area = item.getFirstChild();
                            while(area != null){
                                Node node = area.getFirstChild();
                                while(node != null){
                                    if(node.getNodeName() == "Name") areas += " " + node.getTextContent();
                                    // else if(node.getNodeName() == "Code") areas += "(" + node.getTextContent() + ")";
                                    node = node.getNextSibling();
                                }
                                area = area.getNextSibling();
                            }
                        }else if(item.getNodeName() == "Kind"){
                            Node node = item.getFirstChild();
                            while(node != null){
                                if(node.getNodeName() == "Name") kinds += " " + node.getTextContent();
                                node = node.getNextSibling();
                            }
                        }
                        item = item.getNextSibling();
                    }
//                    System.out.println(areas + " >> " + kinds);
                }
                info = info.getNextSibling();
            }
            try{
                is.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{

        }
    }

    private void getWarnInfo(String url){
        InputStream is = null;
        try{
            Document document;
            XPath xpath;
            Node info, item;
            ContentValues values = new ContentValues();
            is = new URL(url).openConnection().getInputStream();
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            xpath = XPathFactory.newInstance().newXPath();
            String text = ((NodeList)xpath.compile("/Report/Head/Headline/Text").evaluate(document, XPathConstants.NODESET)).item(0).getTextContent();
            String datetime = ((NodeList)xpath.compile("/Report/Control/DateTime").evaluate(document, XPathConstants.NODESET)).item(0).getTextContent();
            info = ((NodeList)xpath.compile("/Report/Body/Warning").evaluate(document, XPathConstants.NODESET)).item(3).getFirstChild();
//            System.out.println("DateTime: " + datetime);
            values.put("datetime", getDatetime(datetime));
//            System.out.println("Text: " + text + "\n");
            int area_code = 0;
            while(info != null){
                area_code = 0;
                String warn_code = "";
                if(info.getNodeName() == "Item"){
                    item = info.getFirstChild();
                    while(item != null){
                        if(item.getNodeName() == "Area"){
                            Node area = item.getFirstChild();
                            while(area != null){
                                if(area.getNodeName() == "Code") {
                                    int code = Integer.parseInt(area.getTextContent());
                                    values.put("area_code", code);
                                    Log.d(TAG, code + "");
                                }
                                area = area.getNextSibling();
                            }
                        }else if(item.getNodeName() == "Kind"){
                            Node node = item.getFirstChild();
                            while(node != null){
                                if(node.getNodeName() == "Code") warn_code += " " + node.getTextContent();
                                node = node.getNextSibling();
                            }
                        }
//                        else if(item.getNodeName() == "Addition") System.out.println(item.getTextContent());
                        item = item.getNextSibling();
                    }
                    values.put("warn_code", warn_code);
                    this.warnDB.update("info", values, "area_code = " + area_code, null);
                }
                info = info.getNextSibling();
            }
            values.clear();
            values.put("message", text);
            this.warnDB.update("pref", values, "code = " + (area_code / 100000), null);
            try{
                is.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{

        }
    }

    private String getDatetime(String datetime) {
        String[] str = datetime.split("-| |:");
        return str[0] + "年" + str[1] + "月" + str[2] + "日 " + str[3] + ":" + str[4];
    }

    public void getLongTermInfo(){
        Log.d(TAG, "getLongTermInfo");
        // this.getInfo(this.regular_l);
        this.getInfo(this.extra_l);
        // this.getInfo(this.eqvol_l);
        // this.getInfo(this.other_l);
    }

    public void getHighFrequencyInfo(){
        Log.d(TAG, "getHighFrequencyInfo");
        // this.getInfo(this.regular);
        this.getInfo(this.extra);
        // this.getInfo(this.eqvol);
        // this.getInfo(this.other);
    }

}