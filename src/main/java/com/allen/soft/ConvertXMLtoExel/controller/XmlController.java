package com.allen.soft.ConvertXMLtoExel.controller;


import com.allen.soft.ConvertXMLtoExel.service.JsonToCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
public class XmlController {

    @Autowired
    private JsonToCsvService jsonToCsvService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile xmlFile) {
        if (xmlFile.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }
        try {
            InputStream inputStream = xmlFile.getInputStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            document.getDocumentElement().normalize();
            Element root = document.getDocumentElement();
            String rootElement = root.getNodeName();

            Map<String, Object> jsonResult = new HashMap<>();
            jsonResult.put("root", rootElement);
            jsonResult.put("data", parseElement(root));

            // Convertir a CSV
            String csvData = jsonToCsvService.convertJsonToCsv(jsonResult);

            // Preparar la respuesta con el archivo CSV
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/csv");
            headers.add("Content-Disposition", "attachment; filename=\"output.csv\"");

            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    private Map<String, Object> parseElement(Element element) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1️⃣ Extraer atributos del elemento
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            result.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        // 2️⃣ Procesar nodos hijos
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                String nodeName = childElement.getNodeName();
                Map<String, Object> childData = parseElement(childElement); // Llamada recursiva

                // 3️⃣ Si el nodo ya existe, convertirlo en lista
                if (result.containsKey(nodeName)) {
                    Object existingValue = result.get(nodeName);
                    List<Object> valueList;

                    if (existingValue instanceof List) {
                        valueList = (List<Object>) existingValue;
                    } else {
                        valueList = new ArrayList<>();
                        valueList.add(existingValue);
                    }

                    valueList.add(childData);
                    result.put(nodeName, valueList);
                } else {
                    result.put(nodeName, childData);
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String textContent = node.getTextContent().trim();
                if (!textContent.isEmpty()) {
                    result.put("value", textContent);
                }
            }
        }

        return result;
    }

}
