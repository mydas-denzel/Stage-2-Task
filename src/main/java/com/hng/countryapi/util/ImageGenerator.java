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
            // Force headless mode (important for Railway)
            System.setProperty("java.awt.headless", "true");

            // Writable cache directory (Railway allows /tmp)
            Path cacheDir = Path.of("/tmp");
            Files.createDirectories(cacheDir);
            Path outputPath = cacheDir.resolve("summary.png");

            // Prepare data
            List<Country> top5 = repository.findAll().stream()
                    .filter(c -> c.getEstimatedGdp() != null)
                    .sorted(Comparator.comparing(Country::getEstimatedGdp).reversed())
                    .limit(5)
                    .toList();

            long totalCountries = repository.count();

            // Image setup
            int width = 700;
            int height = 450;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // Rendering hints for smoother text
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background
            g.setColor(new Color(245, 245, 245));
            g.fillRect(0, 0, width, height);

            // Use built-in fallback font (avoid headless issues)
            Font baseFont = new Font("Dialog", Font.PLAIN, 14);

            // Header
            g.setColor(Color.BLACK);
            g.setFont(baseFont.deriveFont(Font.BOLD, 22f));
            g.drawString("🌍 Country Summary Report", 20, 40);

            // Stats section
            g.setFont(baseFont.deriveFont(16f));
            g.setColor(new Color(30, 30, 30));
            g.drawString("Total Countries: " + totalCountries, 20, 80);

            // Top 5 countries
            g.setFont(baseFont.deriveFont(Font.BOLD, 16f));
            g.drawString("Top 5 by Estimated GDP:", 20, 120);

            g.setFont(baseFont.deriveFont(15f));
            int y = 150;
            for (Country c : top5) {
                g.drawString(c.getName() + " — ₦" + String.format("%,.2f", c.getEstimatedGdp()), 40, y);
                y += 25;
            }

            // Footer timestamp
            g.setColor(new Color(80, 80, 80));
            g.setFont(baseFont.deriveFont(Font.ITALIC, 13f));
            String timestamp = "Generated: " + java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            g.drawString(timestamp, 20, height - 30);

            g.dispose();

            // Write safely
            ImageIO.write(image, "png", outputPath.toFile());
            System.out.println("✅ Summary image saved to: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            // Log instead of crashing app
            System.err.println("⚠️ Failed to generate summary image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
