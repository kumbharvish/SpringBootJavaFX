package com.billing.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.billing.constants.AppConstants;
import com.billing.dto.Barcode;
import com.billing.dto.BillDetails;
import com.billing.dto.MyStoreDetails;
import com.billing.dto.ReportMetadata;
import com.billing.utils.AlertHelper;
import com.billing.utils.AppUtils;
import com.billing.utils.PDFReportMapping;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import win.zqxu.jrviewer.JRViewerFX;

@Service
public class PDFReportService {

	private static final Logger logger = LoggerFactory.getLogger(PDFReportService.class);

	@Autowired
	AppUtils appUtils;

	@Autowired
	StoreDetailsService myStoreService;

	@Autowired
	PDFReportMapping pdfReportMapping;

	@Autowired
	AlertHelper alertHelper;

	public void printInvoice(BillDetails bill, String jasperName) {
		try {
			// load report
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jasperLocation = directoryPath + jasperName;
			FileInputStream fis = new FileInputStream(jasperLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			// Get Data Source
			List<Map<String, ?>> dataSourceMap = pdfReportMapping.getDatasourceForInvoice(bill);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();

			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add store details");
				return;
			}
			List<Map<String, ?>> dataSourceMapsSubReport = pdfReportMapping.getDataSourceForSubReports(bill,
					headerParamsMap, jasperName);
			List<Map<String, ?>> dataSourceMapsSubReportTc = pdfReportMapping.getDataSourceForSubReportTC(bill,
					headerParamsMap, jasperName);
			headerParamsMap.put("SUBREPORT_DATA", dataSourceMapsSubReport);
			headerParamsMap.put("SUBREPORT_TC_DATA", dataSourceMapsSubReportTc);

			headerParamsMap.put("SUBREPORT_DIR", directoryPath);
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Show Print Preview
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.SHOW_PRINT_PREVIEW))) {
				previewPrint(jasperPrint, "Invoice");
			} else {
				JasperPrintManager.printReport(jasperPrint, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception: ", e);

		}
	}

	public boolean exportPDF(ReportMetadata reportMetadata) {
		boolean isSucess = true;
		try {
			// load report
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jasperLocation = directoryPath + reportMetadata.getJasperName();

			FileInputStream fis = new FileInputStream(jasperLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(reportMetadata.getDataSourceMap());

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();
			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add store details");
				return false;
			}
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);

			// Export To PDF
			JasperExportManager.exportReportToPdfFile(jasperPrint, reportMetadata.getFilePath());
			if (appUtils.isTrue(appUtils.getAppDataValues(AppConstants.OPEN_REPORT_DOC_ON_SAVE))) {
				appUtils.openWindowsDocument(reportMetadata.getFilePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
			isSucess = false;
			logger.error("Jasper Exception: ", e);
		}
		return isSucess;
	}

	public boolean printBarcodeSheet(Barcode barcode, int noOfLabels, int startPosition, String jasperName) {
		boolean isSucess = true;
		try {

			// load report location
			String directoryPath = appUtils.createDirectory(AppConstants.JASPER);
			String jrxmlLocation = directoryPath + jasperName;
			FileInputStream fis = new FileInputStream(jrxmlLocation);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(bufferedInputStream);

			// Get Data Source
			List<Map<String, ?>> dataSourceMap = pdfReportMapping.getDatasourceForBarcode(barcode, noOfLabels,
					startPosition);

			JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(dataSourceMap);

			// Add Report Headers
			HashMap<String, Object> headerParamsMap = getPDFReportHeadersMap();

			if (headerParamsMap == null) {
				alertHelper.showErrorNotification("Please add store details");
				return true;
			}
			// compile report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, headerParamsMap, dataSource);
			// Show Print Preview
			previewPrint(jasperPrint, "Barcode Sheet");
		} catch (Exception e) {
			e.printStackTrace();
			isSucess = false;
			logger.error("Jasper Exception: ", e);
		}
		return isSucess;
	}

	public void previewPrint(JasperPrint report, String previewName) {
		JRViewerFX viewer = new JRViewerFX(report);
		viewer.setPadding(new Insets(8));
		Stage preview = new Stage();
		preview.initOwner(null);
		preview.initModality(Modality.APPLICATION_MODAL);
		preview.setScene(new Scene(viewer));
		preview.setTitle("Print Preview : " + previewName);
		preview.getIcons().add(new Image(this.getClass().getResource("/images/shop32X32.png").toString()));
		final String styleSheetPath = "/css/printPreviewDialog.css";
		preview.getScene().getStylesheets().add(AlertHelper.class.getResource(styleSheetPath).toExternalForm());
		preview.showAndWait();
	}

	private HashMap<String, Object> getPDFReportHeadersMap() {
		HashMap<String, Object> headerParamsMap = new HashMap<String, Object>();
		MyStoreDetails details = myStoreService.getMyStoreDetails();
		if (details == null) {
			return null;
		} else {
			headerParamsMap.put("StoreName", details.getStoreName());
			headerParamsMap.put("Address", details.getAddress());
			headerParamsMap.put("Address2",
					details.getAddress2() + ", " + details.getCity() + ", " + details.getDistrict());
			headerParamsMap.put("MobileNumber", String.valueOf(details.getMobileNo()));
			headerParamsMap.put("State", details.getState());
			headerParamsMap.put("GSTIN", details.getGstNo());
		}

		return headerParamsMap;
	}

}
