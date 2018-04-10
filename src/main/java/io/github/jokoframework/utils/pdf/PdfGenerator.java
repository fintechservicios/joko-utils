package io.github.jokoframework.utils.pdf;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import be.quodlibet.boxable.utils.PDStreamUtils;
import io.github.jokoframework.utils.constants.JokoConstants;

public class PdfGenerator {
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Retorna un string aleatorio y largo
	 *
	 * @return String aleatorio y largo
	 */
	private static String getRandomString() {
		return new BigInteger(128, random).toString(32);
	}

	/**
	 * Crea un archivo del formato pdf con la ubicación y nombre del argumento "destination", una tabla de datos dada
	 * por "data" y una firma al final del archivo con datos de cuando fue creado, propiedades y el creador "user"
	 * proveído
	 *
	 * @param data Lista de Listas de modo que se escribira una Tabla en el pdf con los datos, cada lista dentro de
	 *             "data" representa una linea de la tabla y seran escritas en ese orden
	 * @param destination Destino del archivo incluyendo el nombre de este, si se pasa "null" se generara un nombre
	 *                    aleatoriamente y se pondra en el directorio raiz del proyecto
	 * @param user String con un nombre de usuario de modo que se agregara una linea hablando de este al final del pdf
	 * @return Archivo de tipo File que se creo y guardo en el directorio especificado
	 */
	@SuppressWarnings("rawtypes") /*https://github.com/dhorions/boxable*/
    public static File fromList(List<List> data, String destination, String user) throws IOException {

		/* New instance of Document */
		try (PDDocument doc = new PDDocument();) {
			/* Total of records */
			Integer total = data.size() - 1;
			
			/* New instance of a page */
			PDPage page = new PDPage();
			page.setMediaBox(new PDRectangle(PDRectangle.A4.getHeight(),
					PDRectangle.A4.getWidth()));

			/* Adding page to the Document */
			doc.addPage(page);

			/* Initializing the DataTable */
			float margin = 10;
			float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
			float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
			float yStart = yStartNewPage;
			float bottomMargin = 0;

			BaseTable baseTable = new BaseTable(yStart, yStartNewPage,
					bottomMargin, tableWidth, margin, doc, page, true, true);
			DataTable dataTable = new DataTable(baseTable, page);
			
			dataTable.getHeaderCellTemplate().setFillColor(new Color(242, 244, 244));
			dataTable.getDataCellTemplateEven().setFillColor(Color.WHITE);
			dataTable.getDataCellTemplateOdd().setFillColor(Color.WHITE);

			/* Se añade la lista a la tabla */
			dataTable.addListToTable(data, false);
			
			float yPosition = baseTable.draw() - 20;
			float leftMargin = 50;
			float titleFontSize = 8;
			PDFont font = PDType1Font.HELVETICA;
			
			Date date = new Date();
	        DateFormat dateFormat = new SimpleDateFormat(JokoConstants.DATE_TIME_FORMAT);
			
            PDPageContentStream cos = new PDPageContentStream(doc, baseTable.getCurrentPage(),
                    PDPageContentStream.AppendMode.APPEND, true, true);
            PDStreamUtils.write(cos, "Generated by " + user + " on " + dateFormat.format(date) + ". Total of records: "
                    + total.toString(), font, titleFontSize, leftMargin, yPosition, Color.BLACK);
			
			cos.close();
			
			File result;
			if (Objects.isNull(destination)) {
				String tempFile = getRandomString().concat(".pdf");
				result = new File(tempFile);
			} else {
				result = new File(destination);
			}

			doc.save(result);
			
			return result;
		}
	}
}
