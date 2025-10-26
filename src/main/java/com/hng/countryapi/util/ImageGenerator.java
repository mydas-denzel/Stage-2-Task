package com.hng.countryapi.util;

import com.hng.countryapi.model.Country;
import com.hng.countryapi.repo.CountryRepo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ImageGenerator {

    public static void generateSummaryImage(CountryRepo repository) {
        try {
            System.setProperty("java.awt.headless", "true");

            Path cacheDir = Path.of("/tmp");
            Files.createDirectories(cacheDir);
            Path outputPath = cacheDir.resolve("summary.png");

            List<Country> top5 = repository.findAll().stream()
                    .filter(c -> c.getEstimatedGdp() != null)
                    .sorted(Comparator.comparing(Country::getEstimatedGdp).reversed())
                    .limit(5)
                    .toList();

            long totalCountries = repository.count();

            int width = 600;
            int height = 400;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // Minimal and safe drawing (no fonts beyond defaults)
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Dialog", Font.PLAIN, 16));
            g.drawString("Country Summary Report", 20, 30);
            g.drawString("Total Countries: " + totalCountries, 20, 60);

            g.drawString("Top 5 by Estimated GDP:", 20, 100);
            int y = 130;
            for (Country c : top5) {
                g.drawString(c.getName() + " - ₦" + String.format("%,.2f", c.getEstimatedGdp()), 40, y);
                y += 25;
            }

            g.drawString(
                    "Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    20, height - 30);

            g.dispose();

            ImageIO.write(image, "png", outputPath.toFile());
            System.out.println("✅ Summary image generated successfully at: " + outputPath.toAbsolutePath());

        } catch (Throwable e) {
            System.err.println("⚠️ Railway-safe image generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
