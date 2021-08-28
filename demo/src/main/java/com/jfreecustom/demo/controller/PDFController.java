package com.jfreecustom.demo.controller;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.DatatypeConverter;
import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CenterTextMode;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.UnitType;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PDFController {
	
	@GetMapping(path = "/generatePDFWithPieChart")
	public void generatePDFWithPieChart() throws FileNotFoundException{
		PdfWriter writer = new PdfWriter("/output.pdf");
		log.info("Generating PDF with Donut chart...");
        String outputHtml = generateHTMLTemplateReport();
	//generate PDF
		HtmlConverter.convertToPdf(outputHtml, writer, new ConverterProperties());
		log.info("Generated PDF with Donut chart...");
	}

	private String generateHTMLTemplateReport() {
		String demoHTML = "<!DOCTYPE html> <html lang=\"en\"> <head> <meta charset=\"utf-8\"> <title>DemoPDF</title> </head> <body> <div id=\"container\" style=\"width: 100%;height: 215px;\"> <img src=\"graph.jpg\" \"max-width: 100%; height: 200px;margin: 0px auto;display: block;\"/> </div> ";
		Map <String, Float> chartData = new HashMap<String, Float>();
		chartData.put("Value1", 10.5f);
		chartData.put("Value2", 20.3f);
		chartData.put("Value3", 50.7f);
		chartData.put("Value4", 18.8f);
		
		demoHTML = generatePieChart(chartData, demoHTML);
		return demoHTML;
	}
	
	private String generatePieChart(Map<String, Float> pieData, String pieChart) {
		AtomicInteger count = new AtomicInteger(0);
		Integer size = pieData.size();
		try {
			DefaultPieDataset<String> dataset = new DefaultPieDataset<String>();
			pieData.entrySet().stream().forEach(e -> dataset.setValue(e.getKey(), e.getValue()));        
	        JFreeChart chart = ChartFactory.createRingChart("Chart Title", dataset, true, false, false);
	        chart.setBorderVisible(false);
	        chart.getLegend().setBackgroundPaint(Color.WHITE);
	        chart.setBorderVisible(false);
	        chart.setBackgroundPaint(Color.WHITE);
	      
	      //Pie Plot area customization
	        RingPlot rp = (RingPlot)chart.getPlot();
	        rp.setBackgroundPaint(Color.WHITE);
	        rp.setSectionDepth(0.5);
	      //Dynamically generate new color for each section
	        pieData.entrySet().stream().forEach(entry -> {rp.setSectionPaint(entry.getKey(), Color.getHSBColor((float)count.get()/size, 1, 1)); count.getAndIncrement();});
	        rp.setSeparatorsVisible(false);
	        rp.setLegendItemShape(new Rectangle(20,20)); // Customizing List of items on chart legend
	        rp.setLabelBackgroundPaint(Color.WHITE);
	        rp.setLabelOutlinePaint(Color.WHITE);
	        rp.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}:{1}"));  // change this to display only key or value
	        rp.setSimpleLabelOffset(new RectangleInsets(UnitType.RELATIVE, 0.09, 0.09, 0.09, 0.09)); // Customizing Labels on the plot area
	        rp.setSimpleLabels(true); // To avoid lines for labels
	      //Display some text  at centre
	        rp.setCenterTextMode(CenterTextMode.FIXED); 
	        rp.setCenterText("$ Center Total");
	      //Write to an image
	        BufferedImage bufferedImage = chart.createBufferedImage(500, 500);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ImageIO.write(bufferedImage, "png", baos);
		    return pieChart.replace("graph.jpg", "data:image/png;base64,"+DatatypeConverter.printBase64Binary(baos.toByteArray()));
		}catch (Exception e) {
			e.printStackTrace();
			log.error("Exception from Pie Image generate: {}", e.getMessage());
		  }
		return "";
    }	

}
