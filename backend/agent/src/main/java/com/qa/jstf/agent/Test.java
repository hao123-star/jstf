package com.qa.jstf.agent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class Test {
    private static final String CONTENT_LENGTH = "Content-Length: ";

    private static byte[] retrieveNextImage(InputStream urlStream) throws IOException {
        int currByte = -1;

        String header = null;
        // build headers
        // the DCS-930L stops it's headers

        /*InputStreamReader inputStreamReader = new InputStreamReader(urlStream, "utf8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);

        }*/

        boolean captureContentLength = false;
        StringWriter contentLengthStringWriter = new StringWriter(128);
        StringWriter headerWriter = new StringWriter(128);

        int contentLength = 0;

        while ((currByte = urlStream.read()) > -1) {
            if (captureContentLength) {
                if (currByte == 10 || currByte == 13) {
                    contentLength = Integer.parseInt(contentLengthStringWriter.toString());
                    break;
                }
                contentLengthStringWriter.write(currByte);

            } else {
                headerWriter.write(currByte);
                String tempString = headerWriter.toString();
                int indexOf = tempString.indexOf(CONTENT_LENGTH);
                if (indexOf > 0) {
                    captureContentLength = true;
                }
            }
        }

        // 255 indicates the start of the jpeg image
        StringWriter tmpWriter = new StringWriter(128);
        int tmp = -1;
        while ((tmp = urlStream.read()) != 255) {
            tmpWriter.write(tmp);
            // just skip extras
        }

        System.out.println(tmpWriter.toString());

        // rest is the buffer
        byte[] imageBytes = new byte[contentLength + 1];
        // since we ate the original 255 , shove it back in
        imageBytes[0] = (byte) 255;
        int offset = 1;
        int numRead = 0;
        while (offset < imageBytes.length
                && (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
            offset += numRead;
        }

        return imageBytes;
    }

    public static void main(String[] args) throws IOException {
        URL url = new URL("http://localhost:9100/?action=stream");
        byte [] bytes = retrieveNextImage(url.openStream());
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(bis);
        ImageIO.write(bufferedImage, "jpeg", new File("/Users/ted/" + System.currentTimeMillis() + ".jpeg"));
    }
}
