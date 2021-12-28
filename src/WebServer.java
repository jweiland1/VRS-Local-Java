import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.StringTokenizer;

public class WebServer implements Runnable {

	static final File ROOT = new File(new File("src/assets/").getAbsolutePath());
	static final String DEFAULT_FILE = "homepage.html";
	static final int PORT = 80;
	static boolean canRunThread = true;

	// Client Connection via Socket Class
	private Socket connect;

	public WebServer(Socket c) {
		connect = c;
	}

	public static void main(String[] args) {
//		compileProgram();
		System.out.println("ROOT: " + ROOT.getPath());
		canRunThread = true;
		startServerThread();
	}

	public static void startServerThread() {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

			// we listen until user halts server execution

			try {
				Desktop.getDesktop().browse(new URI("http://localhost:80"));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (canRunThread) {
				WebServer myServer = new WebServer(serverConnect.accept());

				System.out.println("Connecton opened. (" + new Date() + ")");

				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
			}
			serverConnect.close();
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	public static void compileProgram() {
		Object retval = Compiler.command("javac CompilerDemo.java");
	}

	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;
		String fileRequested = null;

		try {
			// we read characters from the client via input stream on the socket
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			// we get character output stream to client (for headers)
			out = new PrintWriter(connect.getOutputStream());
			// get binary output stream to client (for requested data)
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			// get first line of the request from the client
			String input = in.readLine();
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			// we get file requested
			fileRequested = parse.nextToken().toLowerCase();

			// we support only GET and HEAD methods, we check
			if (method.equals("GET") || method.equals("HEAD")) {
				// GET or HEAD method
				if (fileRequested.endsWith("/")) {
					fileRequested += DEFAULT_FILE;
				}

				File file = new File(ROOT, fileRequested);
				int fileLength = (int) file.length();
				String content = getContentType(fileRequested);

				if (method.equals("GET")) { // GET method so we return content
					byte[] fileData = readFileData(file, fileLength);

					// send HTTP Headers
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Java HTTP Server from SSaurel : 1.0");
					out.println("Date: " + new Date());
					out.println("Content-type: " + content);
					out.println("Content-length: " + fileLength);
					out.println(); // blank line between headers and content, very important !
					out.flush(); // flush character output stream buffer

					dataOut.write(fileData, 0, fileLength);
					dataOut.flush();
				}

				System.out.println("File " + fileRequested + " of type " + content + " returned");

			}

		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}

		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			}

			System.out.println("Connection closed.\n");
		}

	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	// return supported MIME Types
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
//		File file = new File(WEB_ROOT, FILE_NOT_FOUND);
//		int fileLength = (int) file.length();
//		String content = "text/html";
//		byte[] fileData = readFileData(file, fileLength);
//
//		out.println("HTTP/1.1 404 File Not Found");
//		out.println("Server: Java HTTP Server from SSaurel : 1.0");
//		out.println("Date: " + new Date());
//		out.println("Content-type: " + content);
//		out.println("Content-length: " + fileLength);
//		out.println(); // blank line between headers and content, very important !
//		out.flush(); // flush character output stream buffer
//
//		dataOut.write(fileData, 0, fileLength);
//		dataOut.flush();
//
//		if (verbose) {
//			System.out.println("File " + fileRequested + " not found");
//		}
	}
}
