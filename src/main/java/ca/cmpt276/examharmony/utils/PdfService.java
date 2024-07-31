//package ca.cmpt276.examharmony.utils;
//import ca.cmpt276.examharmony.Model.examSlot.examSlot;
//import ca.cmpt276.examharmony.Model.examSlot.examSlotRepository;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.layout.Document;
//import com.itextpdf.layout.element.Paragraph;
//import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.element.Cell;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.util.List;
//
//@Service
//public class PdfService {
//
//    @Autowired
//    private examSlotRepository examSlotRepo;
//
//    public byte[] generatePdf() {
//        List<examSlot> examSlots = examSlotRepo.findAll();
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
//        PdfDocument pdfDocument = new PdfDocument(writer);
//        Document document = new Document(pdfDocument);
//
//        Table table = new Table(new float[]{1, 2, 2, 1, 1, 2, 1, 1});
//        table.addHeaderCell(new Cell().add(new Paragraph("ID")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Course ID")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Start Time")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Duration")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Number of Rooms")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Assigned Rooms")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Number of Invigilators")));
//        table.addHeaderCell(new Cell().add(new Paragraph("Status")));
//
//        for (examSlot slot : examSlots) {
//            table.addCell(new Cell().add(new Paragraph(String.valueOf(slot.getId()))));
//            table.addCell(new Cell().add(new Paragraph(slot.getCourseID().getCourseName())));
//            table.addCell(new Cell().add(new Paragraph(slot.getStartTime().toString())));
//            table.addCell(new Cell().add(new Paragraph(String.valueOf(slot.getDuration()))));
//            table.addCell(new Cell().add(new Paragraph(String.valueOf(slot.getNumOfRooms()))));
//            table.addCell(new Cell().add(new Paragraph(slot.getAssignedRooms())));
//            table.addCell(new Cell().add(new Paragraph(String.valueOf(slot.getNumInvigilator()))));
//            table.addCell(new Cell().add(new Paragraph(slot.getStatus())));
//        }
//
//        document.add(table);
//        document.close();
//
//        return byteArrayOutputStream.toByteArray();
//    }
//}
//
