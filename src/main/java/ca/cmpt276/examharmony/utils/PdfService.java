package ca.cmpt276.examharmony.utils;
import ca.cmpt276.examharmony.Model.examSlot.examSlot;
import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private examSlotRepository examSlotRepo;

    public byte[] generatePdf() throws IOException {
        List<examSlot> examSlots = examSlotRepo.findAll();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8);

                // Table header
                float margin = 50;
                float yPosition = 750;
                float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
                float[] columnWidths = {20, 50, 100, 50, 50, 150, 50, 50};
                String[] headers = {"ID", "Course", "Start Time", "Duration", "Rooms", "Assigned Rooms", "Invigilators", "Status"};

                drawRow(contentStream, yPosition, margin, headers, columnWidths);
                yPosition -= 20;

                contentStream.setFont(PDType1Font.HELVETICA, 8);

                // Table rows
                for (examSlot slot : examSlots) {
                    if (yPosition < 50) {
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = 750;
                    }

                    String[] data = {
                            String.valueOf(slot.getId()),
                            slot.getCourseID().getCourseName(),
                            slot.getStartTime().toString(),
                            String.valueOf(slot.getDuration()),
                            String.valueOf(slot.getNumOfRooms()),
                            slot.getAssignedRooms() == null ? "N/A" : slot.getAssignedRooms(),
                            String.valueOf(slot.getNumInvigilator()),
                            slot.getStatus()
                    };

                    drawRow(contentStream, yPosition, margin, data, columnWidths);
                    yPosition -= 20;
                }
            } finally {
                contentStream.close();
            }

            document.save(byteArrayOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private void drawRow(PDPageContentStream contentStream, float y, float margin, String[] data, float[] widths) throws IOException {
        float x = margin;
        for (int i = 0; i < data.length; i++) {
            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(data[i]);
            contentStream.endText();
            x += widths[i];
        }
    }
}
