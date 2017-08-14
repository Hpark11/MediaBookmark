package hpark.instagramfollowers_prototype.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

/**
 * Created by hpark_ipl on 2017. 8. 13..
 */

public class Base64Codec {
    private int IO_BUFFER_SIZE = 64;
    //private int IO_BUFFER_SIZE = 8192;
    private URL urlObject = null;
    private URLConnection myConn = null;
    ByteArrayOutputStream os = null;

    public void Base64CODEC() {}

    public Bitmap Base64ImageFromURL(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;

        try {
            urlObject = new URL(url);
            myConn = urlObject.openConnection();
            in = myConn.getInputStream();

            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);

            copyCompletely(in, out);

            final byte[] data = dataStream.toByteArray();
            BitmapFactory.Options options = new BitmapFactory.Options();

            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (IOException e) {
            Log.e("TAG", "Could not load Bitmap from: " + url);
        } finally {
            //closeStream(in);
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            //closeStream(out);
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return bitmap;
    }

    private void copyCompletely(InputStream input, OutputStream output) throws IOException {
        // if both are file streams, use channel IO
        if ((output instanceof FileOutputStream) && (input instanceof FileInputStream)) {
            try {
                FileChannel target = ((FileOutputStream) output).getChannel();
                FileChannel source = ((FileInputStream) input).getChannel();

                source.transferTo(0, Integer.MAX_VALUE, target);

                source.close();
                target.close();

                return;
            } catch (Exception e) { /* failover to byte stream version */
            }
        }

        byte[] buf = new byte[8192];
        while (true) {
            int length = input.read(buf);
            if (length < 0)
                break;
            output.write(buf, 0, length);
        }

        try {
            input.close();
        } catch (IOException ignore) {
        }
        try {
            output.close();
        } catch (IOException ignore) {}
    }

    public String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
        byte[] byteArray = os.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }

    public Bitmap convertToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmapResult = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return bitmapResult;
    }
}
