package com.hng.countryapi.util;

import com.hng.countryapi.model.Country;
import com.hng.countryapi.repo.CountryRepo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class ImageGenerator {

    public static void generateSummaryImage(CountryRepo repository) throws Exception {
        List<Country> top5 = repository.findAll().stream()
                .filter(c -> c.getEstimatedGdp() != null)
                .sorted(Comparator.comparing(Country::getEstimatedGdp).reversed())
                .limit(5)
                .toList();

        int width = 600, height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Country Summary", 20, 30);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Total Countries: " + repository.count(), 20, 60);
        g.drawString("Top 5 by GDP:", 20, 90);

        int y = 120;
        for (Country c : top5) {
            g.drawString(c.getName() + " - " + String.format("%.2f", c.getEstimatedGdp()), 40, y);
            y += 25;
        }

        g.drawString("Last Refreshed: " + java.time.LocalDateTime.now(), 20, height - 30);

        g.dispose();

        File dir = new File("cache");
        if (!dir.exists()) dir.mkdirs();

        ImageIO.write(image, "png", new File("cache/summary.png"));
    }
}
