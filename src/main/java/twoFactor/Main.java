package twoFactor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import twoFactor.util.MatrixToImageConfigLocal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        String secretKey = "BS5AEJ2D47GHFUC2SSM3Y5LGEYJW2TNY";
        String email = "test@gmail.com";
        String companyName = "CashSuite";
        String barCodeUrl = getGoogleAuthenticatorBarCode(secretKey, email, companyName);
        System.out.println(barCodeUrl);
        String lastCode = null;
        System.out.println("Code generator:"+secretKey);
        try {
            System.out.println(createQRCodeBase64(barCodeUrl,"qr.png",300,300));
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String codeScanner = scanner.nextLine();
            if (codeScanner.equals(getTOTPCode(secretKey))) {
                System.out.println("Logged in successfully");
            } else {
                System.out.println("Invalid 2FA Code");
            }

//            String code = getTOTPCode(secretKey);
//            if (!code.equals(lastCode)) {
//                System.out.println(code);
//            }
//            lastCode = code;
//            try {
//                Thread.sleep(1000);
//
//            } catch (InterruptedException e) {};
        }



    }
    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }
    public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void createQRCode(String barCodeData, String filePath, int height, int width)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE,
                width, height);
        try  {
            FileOutputStream out = new FileOutputStream(filePath);
            MatrixToImageWriter.writeToStream(matrix, "png", out);


        }catch (Exception e){

        }
    }



    public static String createQRCodeBase64(String barCodeData, String filePath, int height, int width)
            throws WriterException, IOException {
        MatrixToImageConfigLocal DEFAULT_CONFIG = new MatrixToImageConfigLocal();
        BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE,
                width, height);
        try  {


            FileOutputStream out = new FileOutputStream(filePath);

            BufferedImage image = toBufferedImage(matrix, DEFAULT_CONFIG);

            if (!ImageIO.write(image, "png", out)) {
                throw new IOException("Could not write an image of format " + "png");
            }else {
                return  Base64.getEncoder().encodeToString(toByteArray(image,"png"));
            }
//            MatrixToImageWriter.writeToStream(matrix, "png", out,DEFAULT_CONFIG);


        }catch (Exception e){

        }
        return null;
    }

    public static byte[] toByteArray(BufferedImage bi, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }
    public static BufferedImage toBufferedImage(BitMatrix matrix, MatrixToImageConfigLocal config) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, config.getBufferedImageColorModel());
        int onColor = config.getPixelOnColor();
        int offColor = config.getPixelOffColor();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
            }
        }
        return image;
    }
}
