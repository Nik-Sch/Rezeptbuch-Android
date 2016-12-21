package net.ddns.raspi_server.rezeptbuch.util;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Custom volley request to make multipart header and a upload file.
 */
public class VolleyMultipartRequest extends Request<NetworkResponse> {

  private final String twoHyphens = "--";
  private final String lineEnd = "\r\n";
  private final String boundary = "apiclient-" + System.currentTimeMillis();

  private Response.Listener<NetworkResponse> mListener;
  private Response.ErrorListener mErrorListener;
  private MultipartProgressListener mProgressListener;
  private Map<String, String> mHeaders;

  /**
   * default constructor
   *
   * @param url           request destination
   * @param headers       predefined custom header
   * @param listener      on success achieved 200 code from request
   * @param errorListener on error http or library timeout
   */
  public VolleyMultipartRequest(String url, Map<String, String> headers,
                                Response.Listener<NetworkResponse> listener,
                                Response.ErrorListener errorListener,
                                MultipartProgressListener progressListener) {
    super(Method.POST, url, errorListener);
    mListener = listener;
    mErrorListener = errorListener;
    mHeaders = headers;
    mProgressListener = progressListener;
  }

  /**
   * Constructor with option method and default header configuration.
   *
   * @param method        method for now accept POST and GET only
   * @param url           request destination
   * @param listener      on success event handler
   * @param errorListener on error event handler
   */
  public VolleyMultipartRequest(int method, String url,
                                Response.Listener<NetworkResponse> listener,
                                Response.ErrorListener errorListener,
                                MultipartProgressListener progressListener) {
    super(method, url, errorListener);
    this.mListener = listener;
    this.mErrorListener = errorListener;
    mProgressListener = progressListener;
  }

  @Override
  public Map<String, String> getHeaders() throws AuthFailureError {
    return (mHeaders != null) ? mHeaders : super.getHeaders();
  }

  @Override
  public String getBodyContentType() {
    return "multipart/form-data;boundary=" + boundary;
  }

  @Override
  public byte[] getBody() throws AuthFailureError {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    // calculate the length of the message that will be sent
    // 35 bytes are about the bytes that are always sent
    long length = 35;
    try {
      Map<String, String> params = getParams();
      if (params != null && params.size() > 0) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          // 77 bytes are about the bytes that are always sent
          length += 77 + entry.getKey().length() + entry.getValue().length();
        }
      }
      Map<String, DataPart> data = getByteData();
      if (data != null && data.size() > 0) {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
          // 105 bytes are about the bytes that are always sent
          length += 105 + entry.getKey().length() + entry.getValue().fileLength;
        }
      }

      CountingOutputStream cos = new CountingOutputStream(bos, length,
          mProgressListener);

      if (params != null && params.size() > 0) {
        textParse(cos, params);
      }

      if (data != null && data.size() > 0) {
        dataParse(cos, data);
      }

      cos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

      return bos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Custom method handle data payload.
   *
   * @return Map data part label with data byte
   * @throws AuthFailureError
   */
  protected Map<String, DataPart> getByteData() throws AuthFailureError {
    return null;
  }

  @Override
  protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse
                                                               response) {
    try {
      return Response.success(response, HttpHeaderParser.parseCacheHeaders
          (response));
    } catch (Exception e) {
      return Response.error(new ParseError(e));
    }
  }

  @Override
  protected void deliverResponse(NetworkResponse response) {
    mListener.onResponse(response);
  }

  @Override
  public void deliverError(VolleyError error) {
    mErrorListener.onErrorResponse(error);
  }

  /**
   * Parse string map into data output stream by key and value.
   *
   * @param countingOutputStream data output stream handle string parsing
   * @param params               string inputs collection
   * @throws IOException
   */

  private void textParse(CountingOutputStream countingOutputStream, Map<String,
      String> params) throws IOException {
    for (Map.Entry<String, String> entry : params.entrySet()) {
      buildTextPart(countingOutputStream, entry.getKey(), entry.getValue());
    }
  }

  private void dataParse(CountingOutputStream countingOutputStream, Map<String,
      DataPart> data) throws IOException {
    for (Map.Entry<String, DataPart> entry : data.entrySet()) {
      buildDataPart(countingOutputStream, entry.getValue(), entry.getKey());
    }
  }

  private void buildTextPart(CountingOutputStream countingOutputStream, String
      paramName, String paramValue) throws IOException {
    countingOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
    countingOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
        paramName + "\"" + lineEnd);
    countingOutputStream.writeBytes(lineEnd);
    countingOutputStream.writeBytes(paramValue + lineEnd);
  }

  /**
   * Write data file into header and data output stream.
   *
   * @param countingOutputStream filter output stream handle data parsing
   * @param dataFile             data byte as DataPart from collection
   * @param inputName            name of data input
   * @throws IOException
   */
  private void buildDataPart(CountingOutputStream countingOutputStream, DataPart
      dataFile, String inputName) throws IOException {
    countingOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
    countingOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
        inputName + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd);
    if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty())
      countingOutputStream.writeBytes("Content-Type: " + dataFile.getType() + lineEnd);
    countingOutputStream.writeBytes(lineEnd);

    ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile
        .getContent());
    int bytesAvailable = fileInputStream.available();

    int maxBufferSize = 1048576; // 2^20
    int bufferSize = Math.min(maxBufferSize, bytesAvailable);
    byte[] buffer = new byte[bufferSize];

    int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

    while (bytesRead > 0) {
      countingOutputStream.write(buffer, 0, bufferSize);
      bytesAvailable = fileInputStream.available();
      bufferSize = Math.min(maxBufferSize, bytesAvailable);
      bytesRead = fileInputStream.read(buffer, 0, bufferSize);
    }

    countingOutputStream.writeBytes(lineEnd);
  }


  /**
   * simple container used for passing byte file
   */
  public static class DataPart {
    private String fileName;
    private long fileLength;
    private byte[] content;
    private String type;

    /**
     * default constructor
     */
    public DataPart() {
    }

    /**
     * Constructor with data.
     *
     * @param name label of data
     * @param data byte data
     */
    public DataPart(String name, byte[] data, long fileLength) {
      fileName = name;
      content = data;
      this.fileLength = fileLength;
    }

    /**
     * Constructor with mime data type.
     *
     * @param name     label of data
     * @param data     byte data
     * @param mimeType mime data like "image/jpeg"
     */
    public DataPart(String name, byte[] data, String mimeType) {
      fileName = name;
      content = data;
      type = mimeType;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public byte[] getContent() {
      return content;
    }

    public void setContent(byte[] content) {
      this.content = content;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }
  }

  /**
   * a listener for keeping track of the progress
   */
  public interface MultipartProgressListener {
    /**
     * called as soon as more bytes were transmitted but at the most 10 times
     * per second.
     *
     * @param transferred total amount of bytes transferred
     * @param progress    progress in percent
     */
    void transferred(long transferred, int progress);
  }

  public static class CountingOutputStream extends FilterOutputStream {
    private final MultipartProgressListener listener;
    private long transferred;
    private long fileLength;
    private long lastProgressUpdate = 0;

    public CountingOutputStream(final OutputStream out, long fileLength,
                                final MultipartProgressListener listener) {
      super(out);
      this.fileLength = fileLength;
      this.listener = listener;
      this.transferred = 0;
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
      super.write(b, off, len);
      if (listener != null && lastProgressUpdate + 100 < System.currentTimeMillis()) {
        lastProgressUpdate = System.currentTimeMillis();
        this.transferred += len;
        int prog = (int) (transferred * 100 / fileLength);
        listener.transferred(transferred, prog);
      }
    }

    @Override
    public void write(int b) throws IOException {
      super.write(b);
      if (listener != null && lastProgressUpdate + 100 < System.currentTimeMillis()) {
        lastProgressUpdate = System.currentTimeMillis();
        this.transferred++;
        int prog = (int) (transferred * 100 / fileLength);
        listener.transferred(transferred, prog);
      }
    }

    public void writeBytes(String s) throws IOException {
      write(s.getBytes(), 0, s.getBytes().length);
    }
  }
}
