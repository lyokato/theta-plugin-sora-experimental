package com.theta360.pluginapplication;

import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ConfigServer extends NanoHTTPD {

    public ConfigServer() {
        super(8888);
    }

    private String pickParam(Map<String, List<String>> params, String key) {
        if (!params.containsKey(key)) {
           return null;
        }
        List<String> values = params.get(key);
        if (values.size() == 0) {
           return null;
        }
        String value = values.get(0);
        if (value.isEmpty()) {
           return null;
        }
        return value;
    }

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {

      // サーバーを立ち上げる。
      // 8888ポートしか使えない
      // クライアント側はDNS-SDで_osc._tcpで絞ってデバイス検索
      //
      // XXX OpenPluginPageでリダイレクトする時以外はダイジェスト認証必要ない?
      //
      // 以下は適当な実験コードなので、ちゃんと設定値を保存しない

        String uri    = session.getUri();
        Method method = session.getMethod();

        if (method == Method.GET && uri.equals("/")) {

            String body = "<!DOCTYPE><html><html><meta charset='UTF-8'/><head></head><body>"
                    + "<form method='POST' action='/config'>"
                    + "ENDPOINT: <input type='text' name='endpoint'><br />"
                    + "CHANNEL:  <input type='text' name='channel'><br />"
                    + "<button type='submit' value='SET'>"
                    + "</form>"
                    + "</body></html>";

            String mimeType= "text/html";

            return newFixedLengthResponse(
                    Response.Status.OK,
                    mimeType,
                    body);

        } else if (method == Method.POST && uri.equals("/config")) {


            Map<String, List<String>> params = session.getParameters();

            String endpoint = pickParam(params, "endpoint");
            String channel = pickParam(params, "channel");

            if (endpoint != null && channel != null) {

              // TODO 保存する

                String mimeType = "text/html";
                String body = "<!DOCTYPE><html><html><meta charset='UTF-8'/><head></head><body>"
                        + "<p>completed!</p>"
                        + "</body></html>";

                return newFixedLengthResponse(
                        Response.Status.OK,
                        mimeType,
                        body);

            } else {

                String mimeType = "text/html";

                String body = "<!DOCTYPE><html><html><meta charset='UTF-8'/><head></head><body>"
                        + "<p>Failed, try again</p>"
                        + "<form method='POST' action='/config'>"
                        + "ENDPOINT: <input type='text' name='endpoint'><br />"
                        + "CHANNEL:  <input type='text' name='channel'><br />"
                        + "<button type='submit' value='SET'>この内容で更新</button>"
                        + "</form>"
                        + "</body></html>";

                return newFixedLengthResponse(
                        Response.Status.OK,
                        mimeType,
                        body);
            }


        } else if (method == Method.GET && uri.equals("/channel")) {

            String mimeType = "text/plain";
            String body     = "This is channel";

            return newFixedLengthResponse(
                    Response.Status.OK,
                    mimeType,
                    body);

        } else {

            String mimeType = "text/plain";
            String body     = "Not Found";

           return newFixedLengthResponse(
                   Response.Status.NOT_FOUND,
                   mimeType,
                   body);
        }

    }

}
