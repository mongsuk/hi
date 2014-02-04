package com.example.androidwebserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	private static final String TAG = "WebServer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        File path = Environment.getExternalStorageDirectory();
        Log.e(TAG, "############## path = " + path);
        File wwwroot = path.getAbsoluteFile();
        try {
			new NanoHTTPD(8090, wwwroot);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

   
}

class NanoHTTPD
{
	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.<p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method	"GET", "POST" etc.
	 * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @param header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
	{
		Log.e("Respose_serve","_serve_start");
		myOut.println( method + " '" + uri + "' " );

		Enumeration e = header.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			myOut.println( "  HDR: '" + value + "' = '" +
								header.getProperty( value ) + "'" );
		}
		e = parms.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			myOut.println( "  PRM: '" + value + "' = '" +
								parms.getProperty( value ) + "'" );
		}
		e = files.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			myOut.println( "  UPLOADED: '" + value + "' = '" +
								files.getProperty( value ) + "'" );
		}

		return serveFile( uri, header, myRootDir, true );
	}

	/**
	 * HTTP response.
	 * Return one of these from serve().
	 */
	public class Response
	{
		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response()
		{
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 */
		public Response( String status, String mimeType, InputStream data )
		{
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that makes an InputStream out of
		 * given text.
		 */
		public Response( String status, String mimeType, String txt )
		{
			this.status = status;
			this.mimeType = mimeType;
			try
			{
				this.data = new ByteArrayInputStream( txt.getBytes("UTF-8"));
			}
			catch ( java.io.UnsupportedEncodingException uee )
			{
				uee.printStackTrace();
			}
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader( String name, String value )
		{
			header.put( name, value );
		}

		/**
		 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader()
		 * to add lines.
		 */
		public Properties header = new Properties();
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String
		HTTP_OK = "200 OK",
		HTTP_PARTIALCONTENT = "206 Partial Content",
		HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_NOTMODIFIED = "304 Not Modified",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String
		MIME_PLAINTEXT = "text/plain",
		MIME_HTML = "text/html",
		MIME_DEFAULT_BINARY = "application/octet-stream",
		MIME_XML = "text/xml";

	// ==================================================
	// Socket & server code
	// ==================================================

	/**
	 * Starts a HTTP server to given port.<p>
	 * Throws an IOException if the socket is already in use
	 */
	public NanoHTTPD( int port, File wwwroot ) throws IOException
	{
		
		Log.e("NanoHTTPD",""+wwwroot);
		myTcpPort = port;
		this.myRootDir = wwwroot;//file 에 wwwroot(/mnt/sdcard) 경로를 대입
		myServerSocket = new ServerSocket( myTcpPort ); //port번호가 8090인 socket 인스턴스 생성
		myThread = new Thread( new Runnable()
			{
				
				public void run()
				{
					try
					{
						Log.e("Therad","start");
						while( true ){
							Log.e("Before_HTTPSession",""+myServerSocket);
							//myServerSocket = ServerSocket[addr=0.0.0.0/0.0.0.0,port=0,localport=8090]
							// socket에 정보가 없으므로 addr과 port는 0 이다.
							new HTTPSession( myServerSocket.accept());
							//인터넷에 접속 할때 까지 대기. 연결 되면socket을 리턴한다.
							//인터텟 접속하면 HTTPSession으로 이동!
							Log.e("after_HTTPSession",""+myServerSocket);
						}
					}
					catch ( IOException ioe )
					{}
				}
			});
		myThread.setDaemon( true );
		myThread.start();
	}

	/**
	 * Stops the server.
	 */
	public void stop()
	{
		try
		{
			myServerSocket.close();
			myThread.join();
		}
		catch ( IOException ioe ) {}
		catch ( InterruptedException e ) {}
	}


	/**
	 * Starts as a standalone file server and waits for Enter.
	 */
	

	/**
	 * Handles one session, i.e. parses the HTTP request
	 * and returns the response.
	 */
	private class HTTPSession implements Runnable
	{
		public HTTPSession( Socket s )
		{
			Log.e("StartHTTPSession",""+s);
			//(Socket[address=/127.0.0.1,port=59039,localPort=8090])
			// Screen on click 후 HTTPSession에서 부터 다시시작.
			//  소켓의 포트 정보가 변해서  HTTPSession을 다시 호출 하는 것 같다.
			// (Socket[address=/127.0.0.1,port=59040,localPort=8090])
			mySocket = s;//mySocket에 소켓 저장.
			Thread t = new Thread( this );
			Log.e("Before_setDaemon",""+t);
			t.setDaemon( true );
			t.start();
			Log.e("start_thread","call_start");
			//NanoHTTPD 의 while(true) 문으로 다시가서 
			//new HTTPSession( myServerSocket.accept()); 호출
			//바로 run()으로 시작 된다.
		}

		public void run()
		{
			try
			{
				Log.e("StartHTTPSession_run",""+mySocket);
				// Screeon on click 전 Socket[address=/127.0.0.1,port=59039,localPort=8090]
				// Screen on click 후 Socket[address=/127.0.0.1,port=59040,localPort=8090] 
				// 소켓 정보가 변경됨.
				InputStream is = mySocket.getInputStream();
				// Returns an InputStream to read data from this socket.
				// return The InputStream object.
				Log.e("InputStream",""+is);
				//java.net.PlainSocketImpl$PlainSocketInputStream@4178d8a8
				//객체 번호.
				if ( is == null) return;

				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				int bufsize = 8192;
				byte[] buf = new byte[bufsize];//byte 배열 생성
				Log.e("bufsize=8192",""+buf);
				int rlen = is.read(buf, 0, bufsize);
				//Reads up to byteCount bytes(8192) from this stream 
				//and stores them in the byte array buffer(buf) starting at byteOffset(0). 
				//Returns the number of bytes actually read 
				//or -1 if the end of the stream has been reached.
				// This method read bytes from a stream and stores them into a caller supplied buffer.

				if (rlen <= 0) return;
				char[] check_buf = new char[rlen+1];
				Log.e("HTTPSession_run_rlen",""+rlen);
				// 436 접속할때 마다 숫자가 다르게 나왔다. , 
				// click 후 552 , 
				for(int i=0; i<=rlen; i++)
					check_buf[i]=(char)buf[i];
				//buf에 저장된 내용을 문자로 출력하기 위해 char 배열에 다시 저장
				String buf_check = new String(check_buf);
				//char 배열을 문자로 바꿈.
				Log.e("buf_check",""+buf_check);
				// click 전 buf내용
				// GET / HTTP/1.1
				// Host: localhost:8090
				// Connection: keep-alive
				// Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
				// User-Agent: Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
				// Accept-Encoding: gzip,deflate
				// Accept-Language: en-US
				// Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7
				//
				// ��
				// click 전 buf 내용 end
				
				// click 후 buf 내용
				// POST / HTTP/1.1
				// Host: localhost:8090
				// Connection: keep-alive
				// Referer: http://localhost:8090/                    추가
				// Content-Length: 8                                  추가
				// Cache-Control: max-age=0                           추가
				// Origin: http://localhost:8090                      추가
				// Content-Type: application/x-www-form-urlencoded    추가
				// Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
				// User-Agent: Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30
				// Accept-Encoding: gzip,deflate
				// Accept-Language: en-US
				// Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7
				// 
				// screen=1��
				// click 후 buf 내용 end
				
				
				// Create a BufferedReader for parsing the header.
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
				//A specialized InputStream for reading the contents of a byte array.
				//Constructs a new ByteArrayInputStream on the byte array buf 
				//with the initial position set to offset(0) 
				//and the number of bytes available set to offset(0) + length(rlen).
				// Create a new ByteArrayInputStream that will read bytes from the passed in byte array.
				Log.e( "ByteArrayInputStream hbis_buf.length",""+buf.length); //8192

				Log.e("ByteArrayInputSream_hbis",""+hbis);
				BufferedReader hin = new BufferedReader( new InputStreamReader( hbis ));
				//Constructs a new BufferedReader, providing in with a buffer of 8192 characters.
				//The Java.io.BufferedReader class reads text from a character-input stream, 
				//buffering characters so as to provide for the efficient reading of characters, arrays, 
				//and lines.Following are the important points about BufferedReader:
				//InputStreamReader 헷갈림. 
						
				Properties pre = new Properties();		
				Properties parms = new Properties();		
				Properties header = new Properties();		
				Properties files = new Properties();
				
				Log.e("before_decodeHeader_hin",""+hin);
				Log.e("before_decodeHeader_pre",""+pre);
				Log.e("before_decodeHeader_parms",""+parms);
				Log.e("before_decodeHeader_header",""+header);
				
				// Decode the header into parms and header java properties
				decodeHeader(hin, pre, parms, header);
			
				Log.e("after_decodeHeader_hin",""+hin);
				Log.e("after_decodeHeader_pre",""+pre); // {uri=/, method=GET} , 클릭 후 {uri=/, method=POST
				Log.e("after_decodeHeader_parms",""+parms); //  {} , 클릭 후 {}
				Log.e("after_decodeHeader_header",""+header);
				
				//----------클릭 전------------------------------------------------------------------ 
				//{cache-control=no-cache, connection=keep-alive, accept-language=en-US, 
				// host=localhost:8090, accept=text/html,application/xhtml+xml,
				// application/xml;q=0.9,*/*;q=0.8, user-agent=Mozilla/5.0 
				// (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) 
				//  Version/4.0 Mobile Safari/534.30, accept-encoding=gzip,deflate, 
				//  accept-charset=utf-8, iso-8859-1, utf-16, *;q=0.7, pragma=no-cache}
				
				// --------클릭 후 ---------------------------------------------------------------
				// {content-type=application/x-www-form-urlencoded, cache-control=max-age=0,
				//  connection=keep-alive, accept-language=en-US, host=localhost:8090,
				//  accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8,
				//  content-length=8, origin=http://localhost:8090, 
				//  user-agent=Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) 
				//  AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30,
				//  accept-encoding=gzip,deflate, referer=http://localhost:8090/, 
				//  accept-charset=utf-8, iso-8859-1, utf-16, *;q=0.7}  
				
				
				String method = pre.getProperty("method");
				Log.e("method_pre.getProperty",""+method); // GET 출력  , 클릭 후 POST 출력
				String uri = pre.getProperty("uri"); 
				Log.e("uri_pre.getProperty",""+uri); // / 출력
				long size = 0x7FFFFFFFFFFFFFFFl;
				Log.e("first_size",""+size);  //  9223372036854775807 출력
				String contentLength = header.getProperty("content-length");
				
				Log.e("contentLength_header.getProperty",""+contentLength); 
				// null 출력 header에 content-length key 값이 없어서.
				// 클릭 후 header에 contentLength이 존재, 8 출력
				
				if (contentLength != null)
				{
					try { 
						size = Integer.parseInt(contentLength);
						// Converts the specified String into an int. 
						Log.e("contentLength_size",""+size);//8
						}
					catch (NumberFormatException ex) {}
				}

				// We are looking for the byte separating header from body.
				// It must be the last byte of the first two sequential new lines.
				int splitbyte = 0;
				boolean sbfound = false;
				
				
				char char_r=(char)'\r';
				char char_n=(char)'\n';
				int chartoint_r = char_r;
				int chartoint_n = char_n;
				
				Log.e("chartoint_r","chartoint_r ="+chartoint_r); // 13
				Log.e("chartoint_n","chartoint_n ="+chartoint_n);// 10
				
								
				for(int i=0; i<=rlen; i++)
					Log.e("check_buf","i= "+i+" check_buf= "+check_buf[i]); // buf안에 있는 내용을 문자로 출력
				for(int i=0; i<=rlen; i++)
					Log.e("check_buf","i= "+i+" check_buf= "+(int)check_buf[i]); //buf안에 있는 내용을 정수로 출력
				while (splitbyte < rlen)
				{
					Log.e("splibyte < rlen","splitbyte: "+splitbyte+", rlen: "+rlen);
					if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n' && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
						// buf에 '\r', '\n', '\r', '\n'이 연속적으로 있는 곳을 확인.
						Log.e("splibyte_true_check",""+splitbyte); // 클릭전 435 , 클릭후 551
						sbfound = true;
						break; //while문 빠져나온다.중요!!
					}
					splitbyte++;
				}
				Log.e("before_splitbyte","before_splitbyte="+splitbyte); // 435, 클릭후 551
				splitbyte++;
				
				Log.e("after_splitbyte","after_splitbyte="+splitbyte); // 436, 클릭후 552 

				// Write the part of body already read to ByteArrayOutputStream f
				ByteArrayOutputStream f = new ByteArrayOutputStream();
				
				if (splitbyte < rlen) {
					
					Log.e("splitbyte < rlen",""+true);
					f.write(buf, splitbyte, rlen-splitbyte);
					// buf splibyte부터 rlen-splitebyte만큼 내용을 f 객체의 buf에 저장!
					// system.arraraycopy 이용
				}

				// While Firefox sends on the first read all the data fitting
				// our buffer, Chrome and Opera sends only the headers even if
				// there is data for the body. So we do some magic here to find
				// out whether we have already consumed part of body, if we
				// have reached the end of the data to be sent or we should
				// expect the first byte of the body at the next read.
				if (splitbyte < rlen){
					size -= rlen - splitbyte +1; // //size = size - rlen+splitbyte-1;
					Log.e("size -= rlen - splitbyte +1",""+size);
				}
				else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl){ //클릭후 sbfoud= true, size는 8  조건을 만족하지 않는다. 
					Log.e("!sbfound || size == 0x7FFFFFFFFFFFFFFFl",""+true); // true 출력
					size = 0;
				}
				
				Log.e("check_size",""+ size); // 0 출력, 클릭 후 8 출력
				
				Log.e("olde_rlen",""+rlen); // 436 , 클릭 후 552

				// Now read all the body and write it to f
				buf = new byte[512];
				Log.e("bufsize=512",""+buf);
				while ( rlen >= 0 && size > 0 ) // size가 0 이어서 while에 못들어감. , 클릭후 size가 8  조건을 만족함!
				{
					rlen = is.read(buf, 0, 512);
					// This method read bytes from a stream and stores them into a caller supplied buffer. 
					// It starts storing the data at index off into the buffer and attempts to read len bytes. 
					// 512 byte로 읽은것과 8192byte로 읽는것이 나르게 나왔다.
					Log.e("changed_rlen","changed_rlen="+rlen+" changed_size= "+size);
					char[] change_check_buf = new char[rlen+1];
					for(int i=0; i<=rlen; i++)
						change_check_buf[i]=(char)buf[i];
					String change_buf_check = new String(change_check_buf);
					Log.e("change_bytecounte_512",""+change_buf_check); //screen=1�� 출력
					size -= rlen;
					Log.e("size -= rlen",""+size);
					if (rlen > 0)
						f.write(buf, 0, rlen);
					//This method writes len bytes from the passed in array buf starting at index offset into the internal buffer. 
				}

				// Get the raw body as a byte []
				byte [] fbuf = f.toByteArray(); 
				//This method creates a newly allocated Byte array. 
				//Its size would be the current size of the output stream and the contents of the buffer will be copied into it. 
				//Returns the current contents of the output stream as a byte array.
				Log.e("fbuf",""+f.toByteArray()); //[B@4174ce80
				Log.e("fbuf",""+fbuf); // [B@417895f0
				//두개의 주소가 다르다.
				
				Log.e("f_size",""+f.size()); 
				// 0이 출력되었다. buf에 write 한것이 없으므로 count가 증가하지 않아 0을 리턴.
				// 8이 출력 되었다.

				// Create a BufferedReader for easily reading it as string.
				
				Log.e("fbuf_length",""+fbuf.length); // 클릭 후 8 출력
				ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				//The ByteArrayInputStream class allows a buffer in the memory to be used as an InputStream. 
				//The input source is a byte array.
				BufferedReader in = new BufferedReader( new InputStreamReader(bin));
				//The Java.io.BufferedReader class reads text from a character-input stream, 
				//buffering characters so as to provide for the efficient reading of characters, arrays, and lines.

				
				// If the method is POST, there may be parameters
				// in data section, too, read it:
				if ( method.equalsIgnoreCase( "POST" )) //click하기전 method에 POST key 가 없다! 클릭후 POST가 있다.
				{
					Log.e("POST","IN_POT");
					String contentType = "";
					String contentTypeHeader = header.getProperty("content-type");
					Log.e("contentTypeHeader",""+contentTypeHeader);
					StringTokenizer st = new StringTokenizer( contentTypeHeader , "; " );
					if ( st.hasMoreTokens()) {
						Log.e(";_true",""+true);
						contentType = st.nextToken();
						Log.e(";_true",""+contentType);
					}

					if (contentType.equalsIgnoreCase("multipart/form-data"))
					{
						Log.e("true_contentType.equalsIgnoreCase(multipart/form-data)",""+true);
						// Handle multipart/form-data
						if ( !st.hasMoreTokens())
							sendError( HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html" );
						String boundaryExp = st.nextToken();
						st = new StringTokenizer( boundaryExp , "=" );
						if (st.countTokens() != 2)
							sendError( HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html" );
						st.nextToken();
						String boundary = st.nextToken();

						decodeMultipartData(boundary, fbuf, in, parms, files);
					}
					else
					{
						// Handle application/x-www-form-urlencoded
						String postLine = "";
						char pbuf[] = new char[512];
						int read = in.read(pbuf);
						Log.e("read",""+read); // 8 출력.
						while ( read >= 0 && !postLine.endsWith("\r\n") )  
						{
							// .endsWith This method tests if this string ends with the specified suffix.
							postLine += String.valueOf(pbuf, 0, read);
							Log.e("postLine",""+postLine); // screen=1 출력
							read = in.read(pbuf);
							Log.e("while_read",""+read); // -1 출력
						}
						postLine = postLine.trim();
						Log.e("after_postLine",""+postLine);
						decodeParms( postLine, parms );
					}
				}
				//click하기전 method에 PUT 없음.!
				if ( method.equalsIgnoreCase( "PUT" )){
					Log.e("PUT","IN_PUT");
					files.put("content", saveTmpFile( fbuf, 0, f.size()));
				}
				
				Log.e("Before_serve","uri = "+uri+", method="+method);
				Log.e("Before_serve","parms = "+parms+", files="+files);
				Log.e("Before_serve","header = "+header);
				
				// Ok, now do the serve()
				Response r = serve( uri, method, header, parms, files );
				if ( r == null )
					sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
				else
					sendResponse( r.status, r.mimeType, r.header, r.data );

				in.close();
				is.close();
			}
			catch ( IOException ioe )
			{
				try
				{
					sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				}
				catch ( Throwable t ) {}
			}
			catch ( InterruptedException ie )
			{
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the sent headers and loads the data into
		 * java Properties' key - value pairs
		**/
		private  void decodeHeader(BufferedReader in, Properties pre, Properties parms, Properties header)
			throws InterruptedException
		{
			try {
				// Read the request line
				String inLine = in.readLine();
				Log.e("decodeHeader_inLine",""+inLine);
				// GET / HTTP/1.1 BufferedReader in(HTTPSession의 hin)의 버퍼에서 한줄 읽어 들인다.
				//  screeon on 클릭후 >> POST / HTTP/1.1 
				if (inLine == null) return;
				StringTokenizer st = new StringTokenizer( inLine );
				//String inLine에서 공백을 이용 token으로 끊어준다. GET, / , HTTP/1.1
				//   POST, / , HTTP/1.1 
				if ( !st.hasMoreTokens()) //리턴할 다음 토큰이 있으면 true를 다음 토큰이 없으면 false를 리턴한다.
					sendError( HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html" );

				String method = st.nextToken(); // 다음 토큰을 리턴한다. 이전 토큰은 제거한다.
				Log.e("method",""+method); // GET 출력 , 클릭 후 POST 출력
				pre.put("method", method); 
				// Associate the specified value with the specified key in this Hashtable.
				//preoperties extends hashtable 을 해서 properties에서 hashtable 메소드를 사용할 수 있다.
				Log.e("pre", ""+pre); // {method=GET} 출력 , 클릭 후 {method=POST} 

				if ( !st.hasMoreTokens())
					sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );
				
				String uri = st.nextToken();
				Log.e("decodeHeader_uri",""+uri); // / 출력

				// Decode parameters from the URI
				int qmi = uri.indexOf( '?' );
				//uri 문자열에서 특정문자의 위치를 구 할 수 있다. 문자가 없으면 -1 반환
				Log.e("qmi",""+qmi); // -1 출력
				if ( qmi >= 0 )
				{
					Log.e("qmi>=0","true");
					decodeParms( uri.substring( qmi+1 ), parms );
					uri = decodePercent( uri.substring( 0, qmi ));
				}
				else uri = decodePercent(uri);
				//uri에 + 나 %로 인코딩된것을 디코딩 하기 위해서 decodePerect를 호출. 디코딩 한 것을 string으로 반환.!
				
				Log.e("decodeHeader_uri", ""+uri);// / 출력.!( +나 %가 없었다.)
				

				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names lowercase since they are
				// case insensitive and vary by client.
				if ( st.hasMoreTokens())
				{
					String line = in.readLine();
					Log.e("HTTP headers",""+line); // Host: localhost:8090 출력
					Log.e("line.trim()",""+line.trim()); 
					// line.trim() line의 처음과 끝의 공백을 제거한다. Host: localhost:8090 출력
					while ( line != null && line.trim().length() > 0 ) //line이 null이고 line에 연속되는 문자가 없으면 while문 중단.
					{
						int p = line.indexOf( ':' ); // line 안에 : 특정 문자(:)가 시작되는 인덱스를 리턴한다.!
						Log.e("HTTP_header_line.indexof( : )",""+p);
						Log.e("line_in_while","in_while");
						if ( p >= 0 ){
							//key
							Log.e("line.substring(0,p)",line.substring(0,p));
							//line.substring 문자열의 시작위치(0)에서 끝위치(p)까지의 문자를 뽑아내게 된다. 
							//단 끝위치는 포함이 안된다
							Log.e("line.substring(0,p).trim()",""+line.substring(0,p).trim());// 처음과 끝의 공백 제거
							Log.e("line.substring(0,p).trim().toLowerCase()",
									""+line.substring(0,p).trim().toLowerCase());//모두 소문자로 변경
							
							//value
							Log.e("line.substring(p+1)",line.substring(p+1));//line 에서 p+1 부터 끝의 문자를 생성해서 반환.
							Log.e("line.substring(p+1).trim()",line.substring(p+1).trim());//처음과 끝의 공백 제거
							
							//put(key,value)
							header.put( line.substring(0,p).trim().toLowerCase(), line.substring(p+1).trim());
							// The put(K key, V value) method is used 
							// to map the specified key to the specified value in this hashtable.
							//.getProperty(key) 를 하면 value 값을 return 한다.
							Log.e("Http_header_header.put",""+header);
						}
						line = in.readLine();
						Log.e("HTTP_header_next_line",""+line); 
						//+++ LOG: entry corrupt or truncated 마지막에 이 메세지를 출력하고 while문 종료!
					}
				}
				
				Log.e("finish_put_header",""+header);
				//{cache-control=no-cache, connection=keep-alive, accept-language=en-US, 
				// host=localhost:8090, accept=text/html,application/xhtml+xml,
				// application/xml;q=0.9,*/*;q=0.8, user-agent=Mozilla/5.0 
				// (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) AppleWebKit/534.30 (KHTML, like Gecko) 
				// Version/4.0 Mobile Safari/534.30, accept-encoding=gzip,deflate, 
				// accept-charset=utf-8, iso-8859-1, utf-16, *;q=0.7, pragma=no-cache}
				// hashtable은 랜덤으로 저장된다!
				
				// ----------screen on click 후 내용
				// {content-type=application/x-www-form-urlencoded, cache-control=max-age=0,
				//  connection=keep-alive, accept-language=en-US, host=localhost:8090,
				//  accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8,
				//  content-length=8, origin=http://localhost:8090, 
				//  user-agent=Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; sdk Build/ICS_MR0) 
				//  AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30,
				//  accept-encoding=gzip,deflate, referer=http://localhost:8090/, 
				//  accept-charset=utf-8, iso-8859-1, utf-16, *;q=0.7}  
				
				Log.e("before_pre.put(uri, uri)",""+pre);	// {method=GET} , 클릭 후 {method=POST}
				pre.put("uri", uri);
				Log.e("after_pre.put(uri, uri)",""+pre); // {uri=/, method=GET}, 클릭 후 {uri=/, method=POST}
				//HTTPSession으로 돌아간다!
			}
			catch ( IOException ioe )
			{
				sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
		}

		/**
		 * Decodes the Multipart Body data and put it
		 * into java Properties' key - value pairs.
		**/
		private void decodeMultipartData(String boundary, byte[] fbuf, BufferedReader in, Properties parms, Properties files)
			throws InterruptedException
		{
			try
			{
				int[] bpositions = getBoundaryPositions(fbuf,boundary.getBytes());
				int boundarycount = 1;
				String mpline = in.readLine();
				while ( mpline != null )
				{
					if (mpline.indexOf(boundary) == -1)
						sendError( HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html" );
					boundarycount++;
					Properties item = new Properties();
					mpline = in.readLine();
					while (mpline != null && mpline.trim().length() > 0)
					{
						int p = mpline.indexOf( ':' );
						if (p != -1)
							item.put( mpline.substring(0,p).trim().toLowerCase(), mpline.substring(p+1).trim());
						mpline = in.readLine();
					}
					if (mpline != null)
					{
						String contentDisposition = item.getProperty("content-disposition");
						if (contentDisposition == null)
						{
							sendError( HTTP_BADREQUEST, "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html" );
						}
						StringTokenizer st = new StringTokenizer( contentDisposition , "; " );
						Properties disposition = new Properties();
						while ( st.hasMoreTokens())
						{
							String token = st.nextToken();
							int p = token.indexOf( '=' );
							if (p!=-1)
								disposition.put( token.substring(0,p).trim().toLowerCase(), token.substring(p+1).trim());
						}
						String pname = disposition.getProperty("name");
						pname = pname.substring(1,pname.length()-1);

						String value = "";
						if (item.getProperty("content-type") == null) {
							while (mpline != null && mpline.indexOf(boundary) == -1)
							{
								mpline = in.readLine();
								if ( mpline != null)
								{
									int d = mpline.indexOf(boundary);
									if (d == -1)
										value+=mpline;
									else
										value+=mpline.substring(0,d-2);
								}
							}
						}
						else
						{
							if (boundarycount> bpositions.length)
								sendError( HTTP_INTERNALERROR, "Error processing request" );
							int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount-2]);
							String path = saveTmpFile(fbuf, offset, bpositions[boundarycount-1]-offset-4);
							files.put(pname, path);
							value = disposition.getProperty("filename");
							value = value.substring(1,value.length()-1);
							do {
								mpline = in.readLine();
							} while (mpline != null && mpline.indexOf(boundary) == -1);
						}
						parms.put(pname, value);
					}
				}
			}
			catch ( IOException ioe )
			{
				sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
		}

		/**
		 * Find the byte positions where multipart boundaries start.
		**/
		public int[] getBoundaryPositions(byte[] b, byte[] boundary)
		{
			int matchcount = 0;
			int matchbyte = -1;
			Vector matchbytes = new Vector();
			for (int i=0; i<b.length; i++)
			{
				if (b[i] == boundary[matchcount])
				{
					if (matchcount == 0)
						matchbyte = i;
					matchcount++;
					if (matchcount==boundary.length)
					{
						matchbytes.addElement(new Integer(matchbyte));
						matchcount = 0;
						matchbyte = -1;
					}
				}
				else
				{
					i -= matchcount;
					matchcount = 0;
					matchbyte = -1;
				}
			}
			int[] ret = new int[matchbytes.size()];
			for (int i=0; i < ret.length; i++)
			{
				ret[i] = ((Integer)matchbytes.elementAt(i)).intValue();
			}
			return ret;
		}

		/**
		 * Retrieves the content of a sent file and saves it
		 * to a temporary file.
		 * The full path to the saved file is returned.
		**/
		private String saveTmpFile(byte[] b, int offset, int len)
		{
			Log.e("saveTmpFile","start");
			
			String path = "";
			if (len > 0)
			{
				String tmpdir = System.getProperty("java.io.tmpdir");
				try {
					File temp = File.createTempFile("NanoHTTPD", "", new File(tmpdir));
					OutputStream fstream = new FileOutputStream(temp);
					fstream.write(b, offset, len);
					fstream.close();
					path = temp.getAbsolutePath();
				} catch (Exception e) { // Catch exception if any
					myErr.println("Error: " + e.getMessage());
				}
			}
			return path;
		}


		/**
		 * It returns the offset separating multipart file headers
		 * from the file's data.
		**/
		private int stripMultipartHeaders(byte[] b, int offset)
		{
			int i = 0;
			for (i=offset; i<b.length; i++)
			{
				if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r' && b[++i] == '\n')
					break;
			}
			return i+1;
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent( String str ) throws InterruptedException
		{
			try
			{
				Log.e("decodePercent_str->uri",""+str); 
				// / 출력
				// 클릭 후 decodeparms-> decodePercent( e.substring( 0, sep ))  screen 출력
				// 클릭 후 decodeparms-> e.substring( sep+1 ))  1 출력
				StringBuffer sb = new StringBuffer();
				Log.e("decodePercent_str.length",""+str.length()); 
				// 1 출력
				// 클릭 후 decodePercent( e.substring( 0, sep )) , 6출력
				// 클릭 후 decodeparms-> e.substring( sep+1 )) , 1 출력
				for(int index=0; index<str.length(); index++)
					Log.e("str_print",""+str.charAt(index));// / 출력
				
				//Log.e("str_print_null",null);
				for( int i=0; i<str.length(); i++ )
				{
					
					char c = str.charAt( i ); // index로 지정한 string의 특정 offset(좌표)에 문자를 반환
					switch ( c )
					{
						case '+':
							sb.append( ' ' );
							break;
						case '%':
							sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
							i += 2;
							break;
						default:
							sb.append( c );
							break;
					}
				}
				Log.e("sb.toString",""+sb.toString());
				// / 출력
				// 클릭 후 decodePercent( e.substring( 0, sep )) 호출 screen 출력
				// 클릭 후 decodeparms-> e.substring( sep+1 )) 1 출력
				return sb.toString();// string 자료 형으로 변경.
			}
			catch( Exception e )
			{
				sendError( HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format
		 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Properties. NOTE: this doesn't support multiple
		 * identical keys due to the simplicity of Properties -- if you need multiples,
		 * you might want to replace the Properties with a Hashtable of Vectors or such.
		 */
		private void decodeParms( String parms, Properties p )
			throws InterruptedException
		{
			if ( parms == null )
				return;

			StringTokenizer st = new StringTokenizer( parms, "&" );
			Log.e("decodeParms_st",""+st);
			while ( st.hasMoreTokens())
			{
				String e = st.nextToken();
				int sep = e.indexOf( '=' );
				Log.e("e",""+e);//screen=1
				Log.e("sep",""+sep); //6
				Log.e("sep_key",""+e.substring( 0, sep ));//screen
				Log.e("sep_value",""+e.substring( sep+1 ));//1
				
				if ( sep >= 0 )
					p.put( decodePercent( e.substring( 0, sep )).trim(),
						   decodePercent( e.substring( sep+1 )));
				Log.e("after_p",""+p);
			}
		}

		/**
		 * Returns an error message as a HTTP response and
		 * throws InterruptedException to stop further request processing.
		 */
		private void sendError( String status, String msg ) throws InterruptedException
		{
			sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse( String status, String mime, Properties header, InputStream data )
		{
			try
			{
				if ( status == null )
					throw new Error( "sendResponse(): Status can't be null." );

				OutputStream out = mySocket.getOutputStream();
				PrintWriter pw = new PrintWriter( out );
				pw.print("HTTP/1.0 " + status + " \r\n");

				if ( mime != null )
					pw.print("Content-Type: " + mime + "\r\n");

				if ( header == null || header.getProperty( "Date" ) == null )
					pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");

				if ( header != null )
				{
					Enumeration e = header.keys();
					while ( e.hasMoreElements())
					{
						String key = (String)e.nextElement();
						String value = header.getProperty( key );
						pw.print( key + ": " + value + "\r\n");
					}
				}

				pw.print("\r\n");
				pw.flush();

				if ( data != null )
				{
					int pending = data.available();	// This is to support partial sends, see serveFile()
					byte[] buff = new byte[theBufferSize];
					while (pending>0)
					{
						int read = data.read( buff, 0, ( (pending>theBufferSize) ?  theBufferSize : pending ));
						if (read <= 0)	break;
						out.write( buff, 0, read );
						pending -= read;
					}
				}
				out.flush();
				out.close();
				if ( data != null )
					data.close();
			}
			catch( IOException ioe )
			{
				// Couldn't write? No can do.
				try { mySocket.close(); } catch( Throwable t ) {}
			}
		}

		private Socket mySocket;
	}

	/**
	 * URL-encodes everything between "/"-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	private String encodeUri( String uri )
	{
		String newUri = "";
		StringTokenizer st = new StringTokenizer( uri, "/ ", true );
		while ( st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if ( tok.equals( "/" ))
				newUri += "/";
			else if ( tok.equals( " " ))
				newUri += "%20";
			else
			{
				newUri += URLEncoder.encode( tok );
				// For Java 1.4 you'll want to use this instead:
				// try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch ( java.io.UnsupportedEncodingException uee ) {}
			}
		}
		return newUri;
	}

	private int myTcpPort;
	private final ServerSocket myServerSocket;
	private Thread myThread;
	private File myRootDir;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 */
	public Response serveFile( String uri, Properties header, File homeDir,
							   boolean allowDirectoryListing )
	{
		Log.e("serveFile","start_serveFile");
		Response res = null;

		// Make sure we won't die of an exception later
		if ( !homeDir.isDirectory())
			res = new Response( HTTP_INTERNALERROR, MIME_PLAINTEXT,
				"INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );

		if ( res == null )
		{
			// Remove URL arguments
			uri = uri.trim().replace( File.separatorChar, '/' );
			if ( uri.indexOf( '?' ) >= 0 )
				uri = uri.substring(0, uri.indexOf( '?' ));

			// Prohibit getting out of current directory
			if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
				res = new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: Won't serve ../ for security reasons." );
		}

		File f = new File( homeDir, uri );
		if ( res == null && !f.exists())
			res = new Response( HTTP_NOTFOUND, MIME_PLAINTEXT,
				"Error 404, file not found." );

		// List the directory, if necessary
		if ( res == null && f.isDirectory())
		{
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if ( !uri.endsWith( "/" ))
			{
				uri += "/";
				res = new Response( HTTP_REDIRECT, MIME_HTML,
					"<html><body>Redirected: <a href=\"" + uri + "\">" +
					uri + "</a></body></html>");
				res.addHeader( "Location", uri );
			}

			if ( res == null )
			{
				// First try index.html and index.htm
				if ( new File( f, "index.html" ).exists())
					f = new File( homeDir, uri + "/index.html" );
				else if ( new File( f, "index.htm" ).exists())
					f = new File( homeDir, uri + "/index.htm" );
				// No index file, list the directory if it is readable
				else if ( allowDirectoryListing && f.canRead() )
				{
					String[] files = f.list();
					String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

					if ( uri.length() > 1 )
					{
						String u = uri.substring( 0, uri.length()-1 );
						int slash = u.lastIndexOf( '/' );
						if ( slash >= 0 && slash  < u.length())
							msg += "<b><a href=\"" + uri.substring(0, slash+1) + "\">..</a></b><br/>";
					}

					if (files!=null)
					{
						for ( int i=0; i<files.length; ++i )
						{
							File curFile = new File( f, files[i] );
							boolean dir = curFile.isDirectory();
							if ( dir )
							{
								msg += "<b>";
								files[i] += "/";
							}

							msg += "<a href=\"" + encodeUri( uri + files[i] ) + "\">" +
								  files[i] + "</a>";

							// Show file size
							if ( curFile.isFile())
							{
								long len = curFile.length();
								msg += " &nbsp;<font size=2>(";
								if ( len < 1024 )
									msg += len + " bytes";
								else if ( len < 1024 * 1024 )
									msg += len/1024 + "." + (len%1024/10%100) + " KB";
								else
									msg += len/(1024*1024) + "." + len%(1024*1024)/10%100 + " MB";

								msg += ")</font>";
							}
							msg += "<br/>";
							if ( dir ) msg += "</b>";
						}
					}
					msg += "</body></html>";
					res = new Response( HTTP_OK, MIME_HTML, msg );
				}
				else
				{
					res = new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
						"FORBIDDEN: No directory listing." );
				}
			}
		}

		try
		{
			if ( res == null )
			{
				// Get MIME type from file name extension, if possible
				String mime = null;
				int dot = f.getCanonicalPath().lastIndexOf( '.' );
				if ( dot >= 0 )
					mime = (String)theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
				if ( mime == null )
					mime = MIME_DEFAULT_BINARY;

				// Calculate etag
				String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());

				// Support (simple) skipping:
				long startFrom = 0;
				long endAt = -1;
				String range = header.getProperty( "range" );
				if ( range != null )
				{
					if ( range.startsWith( "bytes=" ))
					{
						range = range.substring( "bytes=".length());
						int minus = range.indexOf( '-' );
						try {
							if ( minus > 0 )
							{
								startFrom = Long.parseLong( range.substring( 0, minus ));
								endAt = Long.parseLong( range.substring( minus+1 ));
							}
						}
						catch ( NumberFormatException nfe ) {}
					}
				}

				// Change return code and add Content-Range header when skipping is requested
				long fileLen = f.length();
				if (range != null && startFrom >= 0)
				{
					if ( startFrom >= fileLen)
					{
						res = new Response( HTTP_RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "" );
						res.addHeader( "Content-Range", "bytes 0-0/" + fileLen);
						res.addHeader( "ETag", etag);
					}
					else
					{
						if ( endAt < 0 )
							endAt = fileLen-1;
						long newLen = endAt - startFrom + 1;
						if ( newLen < 0 ) newLen = 0;

						final long dataLen = newLen;
						FileInputStream fis = new FileInputStream( f ) {
							public int available() throws IOException { return (int)dataLen; }
						};
						fis.skip( startFrom );

						res = new Response( HTTP_PARTIALCONTENT, mime, fis );
						res.addHeader( "Content-Length", "" + dataLen);
						res.addHeader( "Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
						res.addHeader( "ETag", etag);
					}
				}
				else
				{
					if (etag.equals(header.getProperty("if-none-match")))
						res = new Response( HTTP_NOTMODIFIED, mime, "");
					else
					{
						res = new Response( HTTP_OK, mime, new FileInputStream( f ));
						res.addHeader( "Content-Length", "" + fileLen);
						res.addHeader( "ETag", etag);
					}
				}
			}
		}
		catch( IOException ioe )
		{
			res = new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
		}

		res.addHeader( "Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
		return res;
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable theMimeTypes = new Hashtable();
	static
	{
		StringTokenizer st = new StringTokenizer(
			"css		text/css "+
			"htm		text/html "+
			"html		text/html "+
			"xml		text/xml "+
			"txt		text/plain "+
			"asc		text/plain "+
			"gif		image/gif "+
			"jpg		image/jpeg "+
			"jpeg		image/jpeg "+
			"png		image/png "+
			"mp3		audio/mpeg "+
			"m3u		audio/mpeg-url " +
			"mp4		video/mp4 " +
			"ogv		video/ogg " +
			"flv		video/x-flv " +
			"mov		video/quicktime " +
			"swf		application/x-shockwave-flash " +
			"js			application/javascript "+
			"pdf		application/pdf "+
			"doc		application/msword "+
			"ogg		application/x-ogg "+
			"zip		application/octet-stream "+
			"exe		application/octet-stream "+
			"class		application/octet-stream " );
		while ( st.hasMoreTokens())
			theMimeTypes.put( st.nextToken(), st.nextToken());
	}

	private static int theBufferSize = 16 * 1024;

	// Change these if you want to log to somewhere else than stdout
	protected static PrintStream myOut = System.out; 
	protected static PrintStream myErr = System.err;

	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;
	static
	{
		gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * The distribution licence
	 */
	private static final String LICENCE =
		"Copyright (C) 2001,2005-2011 by Jarno Elonen <elonen@iki.fi>\n"+
		"and Copyright (C) 2010 by Konstantinos Togias <info@ktogias.gr>\n"+
		"\n"+
		"Redistribution and use in source and binary forms, with or without\n"+
		"modification, are permitted provided that the following conditions\n"+
		"are met:\n"+
		"\n"+
		"Redistributions of source code must retain the above copyright notice,\n"+
		"this list of conditions and the following disclaimer. Redistributions in\n"+
		"binary form must reproduce the above copyright notice, this list of\n"+
		"conditions and the following disclaimer in the documentation and/or other\n"+
		"materials provided with the distribution. The name of the author may not\n"+
		"be used to endorse or promote products derived from this software without\n"+
		"specific prior written permission. \n"+
		" \n"+
		"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"+
		"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
		"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"+
		"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
		"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
		"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"+
		"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"+
		"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"+
		"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"+
		"OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}


