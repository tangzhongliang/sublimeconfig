package slack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.FileChannel;

public class TestPrint {
	final String PJL_START = "\u001B%-12345X@PJL";
	final String PJL_COPIES = "@PJL SET COPIES";
	final String PJL_ORIENTATION = "@PJL SET ORIENTATION";
	final String PJL_MARGINS = "@PJL SET MARGINS";
	final String PJL_BATCH = "@PJL SET BATCH";
	final String PJL_TRAY = "@PJL SET MEDIASOURCE";
	final String PJL_DENSITY = "@PJL SET DENSITY";
	final String PJL_MEDIATYPE = "@PJL SET MEDIATYPE";
	final String PJL_LANGUAGE = "@PJL ENTER LANGUAGE";
	final String PJL_END = "\u001B%-12345X@PJL EOJ";
	private static final String TAG = "";

	public static void main(String[] args) {
		TestPrint testPrint = new TestPrint();
		testPrint.printPdf("172.25.78.166","C://test/combine.pdf");
	}

	boolean printPdf(String ip,String pdfPath) {
		StringBuffer startCmd = new StringBuffer();
		startCmd.append(PJL_START + "\n");
		try {
			return printAllPdf(startCmd.toString().getBytes(), pdfPath,
					getOutputStream(ip));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void appendCmd(StringBuffer cmd, String key, String value) {
		if (value == null)
			return;
		if (value.equals(""))
			return;
		cmd.append(key + "=" + value + "\n");
	}

	private boolean printAllPdf(byte[] pjlbytes, String pdfPath,
			DataOutputStream dos) {

		long printPdf = System.currentTimeMillis();
		Log.i(TAG, "pjl cmd " + new String(pjlbytes));
		byte[] endbytes = PJL_END.getBytes();
		DataInputStream dis;
		byte[] buf = new byte[1024];
		if (dos == null) {

			return false;
		}
		int len;

		try {
			File pdfFile1 = new File(pdfPath);

			File pdfFile2 = new File("C://test/tmp.pdf");
			// File pdfFile3 = new File("/mnt/hdd/RemoteCopyTest2.pdf");
			 fileChannelCopy(pdfFile1, pdfFile2);
			dis = new DataInputStream(new FileInputStream(pdfFile2));       
			Log.i(TAG,
					"create outputStream time="
							+ (System.currentTimeMillis() - printPdf));
			dos.write(pjlbytes, 0, pjlbytes.length);

			while ((len = dis.read(buf)) != -1) {
				dos.write(buf, 0, len);
			}
			dos.write(endbytes, 0, endbytes.length);
			dos.flush();
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "send pdf time=" + (System.currentTimeMillis() - printPdf));
		return true;
	}

	private DataOutputStream getOutputStream(String ip) throws IOException {
		// if (dataOutputStream == null ) {
		Socket printSocket = new Socket(ip, 9100);
		DataOutputStream dataOutputStream = new DataOutputStream(
				printSocket.getOutputStream());
		// }
		return dataOutputStream;
	}
	public void fileChannelCopy(File s, File t) {

        FileInputStream fi = null;

        FileOutputStream fo = null;

        FileChannel in = null;

        FileChannel out = null;

        try {

            fi = new FileInputStream(s);

            fo = new FileOutputStream(t);

            in = fi.getChannel();

            out = fo.getChannel();

            in.transferTo(0, in.size(), out);

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                fi.close();

                in.close();

                fo.close();

                out.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
    }
}

class Log {
	public static void i(String tag, String text) {
		System.out.println(text);
	}
}