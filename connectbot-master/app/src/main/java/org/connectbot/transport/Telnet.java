package org.connectbot.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.connectbot.HostListActivity;
import org.connectbot.R;
import org.connectbot.bean.HostBean;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.connectbot.util.HostDatabase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import de.mud.telnet.TelnetProtocolHandler;

/**
 * Telnet transport implementation.<br/>
 * Original idea from the JTA telnet package (de.mud.telnet)
 *
 * @author Kenny Root
 *
 */
public class Telnet extends AbsTransport {
	private static final String TAG = "CB.Telnet";
	private static final String PROTOCOL = "telnet";

	private static final int DEFAULT_PORT = 23;
	private static Context mContext=null;

	private TelnetProtocolHandler handler;
	private Socket socket;

	private InputStream is;
	private OutputStream os;
	private int width;
	private int height;

	private boolean connected = false;

	ArrayList<VechileData> vechileDataArrayList=new ArrayList<>();

	static final Pattern hostmask;
	static {
		hostmask = Pattern.compile("^([0-9a-z.-]+)(:(\\d+))?$", Pattern.CASE_INSENSITIVE);
	}

	public Telnet() {
		handler = new TelnetProtocolHandler() {
			/** get the current terminal type */
			@Override
			public String getTerminalType() {
				return getEmulation();
			}

			/** get the current window size */
			@Override
			public int[] getWindowSize() {
				return new int[] { width, height };
			}

			/** notify about local echo */
			@Override
			public void setLocalEcho(boolean echo) {
				/* EMPTY */
			}

			/** write data to our back end */
			@Override
			public void write(byte[] b) throws IOException {
				if (os != null)
					os.write(b);
			}

			/** sent on IAC EOR (prompt terminator for remote access systems). */
			@Override
			public void notifyEndOfRecord() {
			}

			@Override
			protected String getCharsetName() {
				Charset charset = bridge.getCharset();
				if (charset != null)
					return charset.name();
				else
					return "";
			}
		};
	}

	/**
	 * @param host
	 * @param bridge
	 * @param manager
	 */
	public Telnet(HostBean host, TerminalBridge bridge, TerminalManager manager) {
		super(host, bridge, manager);
	}

	public static String getProtocolName() {
		return PROTOCOL;
	}

	private static void tryAllAddresses(Socket sock, String host, int port) throws IOException {
		InetAddress[] addresses = InetAddress.getAllByName(host);
		for (InetAddress addr : addresses) {
			try {
				sock.connect(new InetSocketAddress(addr, port));
				return;
			} catch (SocketTimeoutException e) {
			}
		}
		throw new SocketTimeoutException("Could not connect; socket timed out");
	}

	@Override
	public void connect() {
		try {
			socket = new Socket();

			vechileDataArrayList.clear();
			tryAllAddresses(socket, host.getHostname(), host.getPort());

			connected = true;

			is = socket.getInputStream();
			os = socket.getOutputStream();

			bridge.onConnected();

			Log.i("TEST","Connected....");
		} catch (UnknownHostException e) {
			Log.d(TAG, "IO Exception connecting to host", e);
		} catch (IOException e) {
			Log.d(TAG, "IO Exception connecting to host", e);
		}
	}

	@Override
	public void close() {
		connected = false;
		if (socket != null)
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				Log.d(TAG, "Error closing telnet socket.", e);
			}
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isSessionOpen() {
		return connected;
	}


	int tempWait=0;
	int minValue=50;
	@Override
	public int read(byte[] buffer, int start, int len) throws IOException {
		/* process all already read bytes */
		int n = 0;

		/*StringBuffer tempBuffer=new StringBuffer();
		String temp=tempBuffer.append;
*/
		//Log.e("TEST",":read"+buffer.toString());
		String str1 = new String(buffer, "UTF-8");
		Log.e("TEST",":read W:"+str1/*.split(",")[0]*/);


		String lines[] = str1.split("\\r?\\n");

		Log.e("TEST",": lines"+lines.length);


		for (int i=0;i<lines.length;i++)
		{
			boolean  isNumeric=lines[i].split(",")[0].matches("-?\\d+(\\.\\d+)?");



			if(isNumeric)
			{
				/*int updateWait=Integer.parseInt(lines[i].split(",")[0]);
				if(tempWait<updateWait)
				{
					tempWait=updateWait;
				}else {
					VechileData date=new VechileData();
					date.setWeight(tempWait);
					vechileDataArrayList.add(date);
				}*/

				int updateWait=Integer.parseInt(lines[i].split(",")[0]);

				//Log.i("TEST","onProgressChanged" +updateWait);

				if(minValue<updateWait )
				{
					if(updateWait>tempWait)
						tempWait=updateWait;

				}else {

					if(tempWait>minValue) {
						VechileData date = new VechileData();
						date.setWeight(tempWait);
						vechileDataArrayList.add(date);
						Log.i("TEST", "For loop break.........." + tempWait);
						tempWait = 0;
						//break;
					}
				}
			}


		}

		Log.e("TEST",":No.of :::::::"+vechileDataArrayList.size());
		for(int j =0;j<vechileDataArrayList.size();j++)
		{
			Log.e("TEST",":Weight Max :::::::"+vechileDataArrayList.get(j).getWeight());
		}

		if(HostListActivity.vehicleAxle==vechileDataArrayList.size())
		{

			Toast.makeText(mContext,"Done ! weight"+vechileDataArrayList.get(1).getWeight(),Toast.LENGTH_LONG).show();
		}else {
			Toast.makeText(mContext,"Waiting for next count",Toast.LENGTH_LONG).show();
		}

		do {
			n = handler.negotiate(buffer, start);
			if (n > 0)
				return n;
		} while (n == 0);

		while (n <= 0) {
			do {
				n = handler.negotiate(buffer, start);
				if (n > 0)
					return n;
			} while (n == 0);
			n = is.read(buffer, start, len);

			//Log.e("TEST",":IS"+is.toString());
			if (n < 0) {
				bridge.dispatchDisconnect(false);
				throw new IOException("Remote end closed connection.");
			}

			handler.inputfeed(buffer, start, n);
			n = handler.negotiate(buffer, start);
		}
		return n;
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		try {
			Log.e("TEST",":buffer:"+buffer);
			if (os != null)
				os.write(buffer);
		} catch (SocketException e) {
			bridge.dispatchDisconnect(false);
		}
	}

	@Override
	public void write(int c) throws IOException {
		try {
			if (os != null) {
				Log.e("TEST",":"+c);
				os.write(c);
			}
		} catch (SocketException e) {
			bridge.dispatchDisconnect(false);
		}
	}

	@Override
	public void setDimensions(int columns, int rows, int width, int height) {
		try {
			handler.setWindowSize(columns, rows);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't resize remote terminal", e);
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String getDefaultNickname(String username, String hostname, int port) {
		if (port == DEFAULT_PORT) {
			return String.format("%s", hostname);
		} else {
			return String.format("%s:%d", hostname, port);
		}
	}

	public static Uri getUri(String input) {
		Matcher matcher = hostmask.matcher(input);

		if (!matcher.matches())
			return null;

		StringBuilder sb = new StringBuilder();

		sb.append(PROTOCOL)
				.append("://")
				.append(matcher.group(1));

		String portString = matcher.group(3);
		int port = DEFAULT_PORT;
		if (portString != null) {
			try {
				port = Integer.parseInt(portString);
				if (port < 1 || port > 65535) {
					port = DEFAULT_PORT;
				}
			} catch (NumberFormatException nfe) {
				// Keep the default port
			}
		}

		if (port != DEFAULT_PORT) {
			sb.append(':');
			sb.append(port);
		}

		sb.append("/#")
				.append(Uri.encode(input));

		Uri uri = Uri.parse(sb.toString());

		return uri;
	}

	@Override
	public HostBean createHost(Uri uri) {
		HostBean host = new HostBean();

		host.setProtocol(PROTOCOL);

		host.setHostname(uri.getHost());

		int port = uri.getPort();
		if (port < 0 || port > 65535)
			port = DEFAULT_PORT;
		host.setPort(port);

		String nickname = uri.getFragment();
		if (nickname == null || nickname.length() == 0) {
			host.setNickname(getDefaultNickname(host.getUsername(),
					host.getHostname(), host.getPort()));
		} else {
			host.setNickname(uri.getFragment());
		}

		return host;
	}

	@Override
	public void getSelectionArgs(Uri uri, Map<String, String> selection) {
		selection.put(HostDatabase.FIELD_HOST_PROTOCOL, PROTOCOL);
		selection.put(HostDatabase.FIELD_HOST_NICKNAME, uri.getFragment());
		selection.put(HostDatabase.FIELD_HOST_HOSTNAME, uri.getHost());

		int port = uri.getPort();
		if (port < 0 || port > 65535)
			port = DEFAULT_PORT;
		selection.put(HostDatabase.FIELD_HOST_PORT, Integer.toString(port));
	}

	public static String getFormatHint(Context context) {
		mContext=context;
		return String.format("%s:%s",
				context.getString(R.string.format_hostname),
				context.getString(R.string.format_port));


	}

	/* (non-Javadoc)
	 * @see org.connectbot.transport.AbsTransport#usesNetwork()
	 */
	@Override
	public boolean usesNetwork() {
		return true;
	}
}