package com.allen.soft.ConvertXMLtoExel.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;

import java.io.StringWriter;
import java.util.Map;

@Service
public class JsonToCsvService {

    public String convertJsonToCsv(Map<String, Object> jsonData) {
        StringWriter writer = new StringWriter();
        try {
            CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader(jsonData.keySet().toArray(new String[0]))
                    .get();

            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            csvPrinter.printRecord(jsonData.values());
            csvPrinter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return writer.toString();
    }
}
