package com.Converter.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HtmlModifierService {
    public String modifyVoucherHtml(String htmlContent) throws IOException {
        Document doc = Jsoup.parse(htmlContent);

        removeRowByText(doc, "Agent Reference:");

        // Remove "Reference Number" and "Itinerary Number" rows
        removeRowByText(doc, "Reference Number");
        removeRowByText(doc, "Itinerary Number");

        // 3. Update the vendor image
        updateVendorImage(doc,
                "https://mcusercontent.com/b9b38543e81e56f3d1e9fc377/_thumbs/a605dd62-c37f-590a-dc11-bbbbf4ad29f1.png");

        // 4. Change "PASSENGER DETAILS" to "GUEST & STAY DETAILS"
        updateFieldNames(doc, "PASSENGER DETAILS", "GUEST & STAY DETAILS");
        updateFieldNames(doc, "Passenger Name", "Guest Name");
        updateFieldNames(doc, "Passenger Nationality", "Guest Nationality");

        // 5. Handle "Additional Services and Requests" logic
        handleAdditionalServicesAndRequests(doc);

        // 6. Convert room rates to INR and update the total booking values
        convertCurrencyToINR(doc);

        // 7. Remove vendor contact information and replace with Unravel’s information
        updateContactInformation(doc);

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // Return the modified HTML as a string
        return doc.outerHtml();
    }

    private void removeRowByText(Document document, String textToMatch) {
        // Find the <td> element that contains the specific text
        Elements rows = document.select("tr:contains(" + textToMatch + ")");
        Element row = rows.get(rows.size() - 1);
        row.remove();
    }

    private void updateVendorImage(Document document, String unravelImageUrl) {
        Element image = document.selectFirst("img[src*='vendor.png']");  // Select the vendor image by URL or class
        if (image != null) {
            image.attr("src", unravelImageUrl);  // Update the image source
        }
    }

    // 4. Change "PASSENGER DETAILS" to "GUEST & STAY DETAILS" and update text below it
    private void updateFieldNames(Document document, String oldTitle, String newTitle) {
        Elements rows = document.select("td:contains(" + oldTitle + ")");
        Element fieldName = rows.get(rows.size() - 1);
        fieldName.text(newTitle);
    }

    // 5. Replace "Additional Services" and "Additional Requests" if both are none
    private void handleAdditionalServicesAndRequests(Document document) {
        Elements services = document.select("td:contains(Additional Services)");
        Elements requests = document.select("td:contains(Additional Requests)");

        Element service = services.get(services.size() - 1);
        Element request = requests.get(requests.size() - 1);

        String servicesText = service.nextElementSibling().text();
        String requestsText = request.nextElementSibling().text();

        if ("none".equalsIgnoreCase(servicesText) && "none".equalsIgnoreCase(requestsText)) {
            service.text("No services/requests availed");
            request.remove();  // Remove the "Additional Requests" row
        } else if ("none".equalsIgnoreCase(servicesText)) {
            service.remove();  // Remove the "Additional Services" row
        } else if ("none".equalsIgnoreCase(requestsText)) {
            request.remove();  // Remove the "Additional Requests" row
        }

    }

    // 6. Convert room rate information from existing currency to INR
    private void convertCurrencyToINR(Document document) {
        double conversionRate = 80.0;  // Example: 1 USD = 80 INR

        Elements priceElements = document.select("td:contains(USD)");  // Find price values in USD
        Element priceElement = priceElements.get(priceElements.size() - 1);

        String priceText = priceElement.text();
        if (priceText.contains("USD")) {
            double amount = Double.parseDouble(priceText.replaceAll(".*?(USD\\s\\d+(\\.\\d{1,2})?).*", "$1").replace("USD ", ""));
            double convertedAmount = amount * conversionRate;
            priceElement.text(String.format("INR %.2f", convertedAmount) + " Discount 5.00% [on adult rate (when booked)]");  // Update price in INR
        }

        // Update total payable amount if necessary
        Element totalAmount = document.selectFirst("p:contains(Total Payable for this Booking)");
        if (totalAmount != null) {
            String totalText = totalAmount.text();
            double totalInUsd = Double.parseDouble(totalText.replaceAll("[^\\d.]", ""));
            double totalInInr = totalInUsd * conversionRate;
            totalAmount.text(String.format("Total Payable for this Booking: INR %.2f", totalInInr));
        }
    }

    // 7. Remove vendor contact info and replace with Unravel’s info
    private void updateContactInformation(Document document) {
        Elements contactSection = document.select("p:contains(Operations Team), p:contains(dummyvendor)");
        for (Element contact : contactSection) {
            contact.remove();  // Remove the existing vendor contact details
        }

        // Add Unravel's contact information
        Element body = document.body();
        body.append("<p>Contact us for any queries at,<br>" +
                "Unravel Support<br>" +
                "Email: support@gounravel.com</p>");
    }
}
