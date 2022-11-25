package ResearchInstituteOfCMP.company;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Bot extends TelegramLongPollingBot {
    BufferedImage image = null;

    @Override
    public String getBotUsername () {
        return "";
    }

    @Override
    public String getBotToken () {
        return "";
    }

    @Override
    public void onUpdateReceived (Update update) {
        if(update.hasMessage()){
            if (update.getMessage().hasPhoto()){
                List<PhotoSize> photos = update.getMessage().getPhoto();
                String chatID = update.getMessage().getChatId().toString();

                PhotoSize photoSize = photos.stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElse(null);

                try {
                    assert photoSize != null : "Object photoSize cannot be null!";
                    java.io.File file = downloadFile(Objects.requireNonNull(getFilePath(photoSize)),
                            new java.io.File("temp/" + chatID + "/img.png"));

                    image = ImageIO.read(file);
                    execute(new SendMessage(chatID, "Write width."));
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            } else if (image != null && update.getMessage().hasText()){
                String chatID = update.getMessage().getChatId().toString();
                try {
                    int xSize = Integer.parseInt(update.getMessage().getText());
                    PrintWriter writer = new PrintWriter("temp/" + chatID + "/txtT.txt", StandardCharsets.UTF_8);
                    writer.print(createTxtImg(image, xSize/2));
                    writer.close();

                    SendDocument document = new SendDocument();
                    document.setChatId(chatID);
                    document.setDocument(new InputFile(new File("temp/" + chatID + "/txtT.txt")));
                    execute(document);
                    image = null;

                } catch (IllegalArgumentException iAE) {
                    try {
                        execute(new SendMessage(chatID, "Illegal argument. repeat with only numbers."));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFilePath(final PhotoSize photo) {
        final var filePath = photo.getFilePath();
        if (filePath != null && !filePath.isBlank()) {
            return filePath;
        }
        final GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photo.getFileId());
        try {
            final org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);
            return file.getFilePath();
        } catch (final TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized StringBuilder createTxtImg(BufferedImage originalImage, int xSize) {
        StringBuilder sb = new StringBuilder();
        char[] ch = {'▒', 'M', 'W', 'H', '#', '%', 'X', 'D', '8', 'A', '4', 'w', 'p', '0',
                '0', '3', 'u', '?', '7', 'i', '{', '+', 't', 'c', '!', '<', '"', '~',
                ':', ',', '^', '.', '`', ' '};

        BufferedImage resizedImg = resize(originalImage, xSize);

        for(int i = 0; i<resizedImg.getHeight(); i++) {
            for(int k = 0; k<resizedImg.getWidth(); k++) {
                sb.append(ch[new Color(resizedImg.getRGB(k, i)).getRed() * (ch.length-1) / 255]);
                sb.append(ch[new Color(resizedImg.getRGB(k, i)).getRed() * (ch.length-1) / 255]);
            }
            sb.append("\n");
        }
        return sb;
    }

    private synchronized BufferedImage resize (BufferedImage originalImage, int xSize) {
        float ySize = originalImage.getHeight()*((float) xSize/originalImage.getWidth());
        BufferedImage resizedImg = new BufferedImage(
                xSize, (int) ySize,
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = resizedImg.createGraphics();
        graphics.drawImage(originalImage, 0, 0, xSize, (int) ySize, null);
        return resizedImg;
    }

    @Deprecated (forRemoval = true)
    private synchronized BufferedImage rgbToBlackAndWhite (BufferedImage originalImage) {
        BufferedImage blackAndWhiteImg = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = blackAndWhiteImg.createGraphics();
        graphics.drawImage(originalImage, 0, 0, null);
        return blackAndWhiteImg;
    }
}
