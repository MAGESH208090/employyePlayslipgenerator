package com.example.employee.controller;

import com.example.employee.model.Employee;
import com.example.employee.repository.EmployeeRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/payslip")
public class PayslipController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/generate")
    public String generatePayslip(@RequestParam Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return "Employee not found";
        }

        double gross = employee.getBasicSalary() + employee.getHra() + employee.getVariableAllowance();
        double gross60 = gross * 0.60;
        double balance = gross - gross60;

        String filename = "payslip_" + employee.getName() + ".pdf";
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Add employee photo from the classpath resource
            try {
                ClassPathResource resource = new ClassPathResource(employee.getPhoto());
                Image photo = Image.getInstance(resource.getURL());
                photo.scaleToFit(100, 100);  // Adjust size if needed
                photo.setAlignment(Image.ALIGN_CENTER);
                document.add(photo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            document.add(new Paragraph("Employee ID: " + employee.getId()));
            document.add(new Paragraph("Employee Name: " + employee.getName()));
            document.add(new Paragraph("Basic: " + employee.getBasicSalary()));
            document.add(new Paragraph("HRA: " + employee.getHra()));
            document.add(new Paragraph("Variable Allowance: " + employee.getVariableAllowance()));
            document.add(new Paragraph("Gross: " + gross));
            document.add(new Paragraph("Gross 60%: " + gross60));
            document.add(new Paragraph("Balance: " + balance));
            document.close();
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            return "Error generating payslip";
        }

        return "Payslip generated: " + filename;
    }
}
