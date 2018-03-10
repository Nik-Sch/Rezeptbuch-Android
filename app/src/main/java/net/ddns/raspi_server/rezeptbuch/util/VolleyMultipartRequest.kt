package net.ddns.raspi_server.rezeptbuch.util

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Custom volley request to make multipart header and a upload file.
 */
open class VolleyMultipartRequest : Request<NetworkResponse> {

  private val twoHyphens = "--"
  private val lineEnd = "\r\n"
  private val boundary = "apiclient-" + System.currentTimeMillis()

  private var mListener: Response.Listener<NetworkResponse>? = null
  private var mErrorListener: Response.ErrorListener? = null
  private var mProgressListener: MultipartProgressListener? = null
  private val mHeaders: Map<String, String>?

  /**
   * Custom method handle data payload.
   *
   * @return Map data part label with data byte
   * @throws AuthFailureError
   */
  protected open val byteData: Map<String, DataPart>?
    @Throws(AuthFailureError::class)
    get() = null

  /**
   * default constructor
   *
   * @param url           request destination
   * @param headers       predefined custom header
   * @param listener      on success achieved 200 code from request
   * @param errorListener on error http or library timeout
   */
  constructor(url: String, headers: Map<String, String>,
              listener: Response.Listener<NetworkResponse>,
              errorListener: Response.ErrorListener,
              progressListener: MultipartProgressListener) : super(Request.Method.POST, url, errorListener) {
    mListener = listener
    mErrorListener = errorListener
    mHeaders = headers
    mProgressListener = progressListener
  }

  /**
   * Constructor with option method and default header configuration.
   *
   * @param method        method for now accept POST and GET only
   * @param url           request destination
   * @param listener      on success event handler
   * @param errorListener on error event handler
   */
  constructor(method: Int, url: String,
              listener: Response.Listener<NetworkResponse>,
              errorListener: Response.ErrorListener,
              progressListener: MultipartProgressListener) : super(method, url, errorListener) {
    this.mListener = listener
    this.mErrorListener = errorListener
    mHeaders = null
    mProgressListener = progressListener
  }

  @Throws(AuthFailureError::class)
  override fun getHeaders(): Map<String, String> {
    return mHeaders ?: super.getHeaders()
  }

  override fun getBodyContentType(): String {
    return "multipart/form-data;boundary=$boundary"
  }

  @Throws(AuthFailureError::class)
  override fun getBody(): ByteArray? {
    val bos = ByteArrayOutputStream()

    // calculate the length of the message that will be sent
    // 35 bytes are about the bytes that are always sent
    var length: Long = 35
    try {
      val params = params
      if (params != null && params.isNotEmpty()) {
        for ((key, value) in params) {
          // 77 bytes are about the bytes that are always sent
          length += (77 + key.length + value.length).toLong()
        }
      }
      val data = byteData
      if (data != null && data.isNotEmpty()) {
        for ((key, value) in data) {
          // 105 bytes are about the bytes that are always sent
          length += 105 + key.length.toLong() + value.fileLength
        }
      }

      val cos = CountingOutputStream(bos, length, mProgressListener)

      if (params != null && params.isNotEmpty()) {
        textParse(cos, params)
      }

      if (data != null && data.isNotEmpty()) {
        dataParse(cos, data)
      }

      cos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

      return bos.toByteArray()
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return null
  }

  override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
    return try {
      Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    } catch (e: Exception) {
      Response.error(ParseError(e))
    }

  }

  override fun deliverResponse(response: NetworkResponse) {
    mListener?.onResponse(response)
  }

  override fun deliverError(error: VolleyError) {
    mErrorListener?.onErrorResponse(error)
  }

  /**
   * Parse string map into data output stream by key and value.
   *
   * @param countingOutputStream data output stream handle string parsing
   * @param params               string inputs collection
   * @throws IOException
   */

  @Throws(IOException::class)
  private fun textParse(countingOutputStream: CountingOutputStream, params: Map<String, String>?) {
    if (params != null) {
      for ((key, value) in params) {
        buildTextPart(countingOutputStream, key, value)
      }
    }
  }

  @Throws(IOException::class)
  private fun dataParse(countingOutputStream: CountingOutputStream, data: Map<String, DataPart>?) {
    if (data != null) {
      for ((key, value) in data) {
        buildDataPart(countingOutputStream, value, key)
      }
    }
  }

  @Throws(IOException::class)
  private fun buildTextPart(countingOutputStream: CountingOutputStream, paramName: String, paramValue: String) {
    countingOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
    countingOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + paramName + "\""
            + lineEnd)
    countingOutputStream.writeBytes(lineEnd)
    countingOutputStream.writeBytes(paramValue + lineEnd)
  }

  /**
   * Write data file into header and data output stream.
   *
   * @param countingOutputStream filter output stream handle data parsing
   * @param dataFile             data byte as DataPart from collection
   * @param inputName            name of data input
   * @throws IOException
   */
  @Throws(IOException::class)
  private fun buildDataPart(countingOutputStream: CountingOutputStream, dataFile: DataPart, inputName: String) {
    countingOutputStream.writeBytes(twoHyphens + boundary + lineEnd)
    countingOutputStream.writeBytes("Content-Disposition: form-data; name=\"" +
            inputName + "\"; filename=\"" + dataFile.fileName + "\"" + lineEnd)
    if (dataFile.type?.trim { it <= ' ' }?.isEmpty() == false)
      countingOutputStream.writeBytes("Content-Type: " + dataFile.type + lineEnd)
    countingOutputStream.writeBytes(lineEnd)

    val fileInputStream = ByteArrayInputStream(dataFile.content)
    var bytesAvailable = fileInputStream.available()

    val maxBufferSize = 1048576 // 2^20
    var bufferSize = Math.min(maxBufferSize, bytesAvailable)
    val buffer = ByteArray(bufferSize)

    var bytesRead = fileInputStream.read(buffer, 0, bufferSize)

    while (bytesRead > 0) {
      countingOutputStream.write(buffer, 0, bufferSize)
      bytesAvailable = fileInputStream.available()
      bufferSize = Math.min(maxBufferSize, bytesAvailable)
      bytesRead = fileInputStream.read(buffer, 0, bufferSize)
    }

    countingOutputStream.writeBytes(lineEnd)
  }


  /**
   * simple container used for passing byte file
   */
  class DataPart(name: String, data: ByteArray, internal val fileLength: Long) {
    var fileName: String? = name
    var content: ByteArray? = data
    var type: String? = null
  }

  /**
   * a listener for keeping track of the progress
   */
  interface MultipartProgressListener {
    /**
     * called as soon as more bytes were transmitted but at the most 10 times
     * per second.
     *
     * @param transferred total amount of bytes transferred
     * @param progress    progress in percent
     */
    fun transferred(transferred: Long, progress: Int)
  }

  class CountingOutputStream(out: OutputStream, private val fileLength: Long,
                             private val listener: MultipartProgressListener?) : FilterOutputStream(out) {
    private var transferred: Long = 0
    private var lastProgressUpdate: Long = 0

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
      super.write(b, off, len)
      if (listener != null && lastProgressUpdate + 100 < System.currentTimeMillis()) {
        lastProgressUpdate = System.currentTimeMillis()
        this.transferred += len.toLong()
        val prog = (transferred * 100 / fileLength).toInt()
        listener.transferred(transferred, prog)
      }
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
      super.write(b)
      if (listener != null && lastProgressUpdate + 100 < System.currentTimeMillis()) {
        lastProgressUpdate = System.currentTimeMillis()
        this.transferred++
        val prog = (transferred * 100 / fileLength).toInt()
        listener.transferred(transferred, prog)
      }
    }

    @Throws(IOException::class)
    fun writeBytes(s: String) {
      write(s.toByteArray(), 0, s.toByteArray().size)
    }
  }
}
