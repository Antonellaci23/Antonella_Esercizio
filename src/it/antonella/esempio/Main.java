package it.antonella.esempio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException {
        //Proprietà del server
        String url = "jdbc:postgresql://18.184.55.135:5432/whiskydata";
        Properties props = new Properties();
        props.setProperty("user", "whisky");
        props.setProperty("password", "10RnrGWEu3GK");
        Connection conn = null;
        try {
            // Creo file csv
            PrintWriter csvFile = new PrintWriter("Esercizio.csv");
            StringBuilder sb = new StringBuilder();
            JSONObject jsonObject = new JSONObject();

            // Creo file JSON
            PrintWriter jsonFile = new PrintWriter("Esercizio.json");
            JSONArray array = new JSONArray();
            jsonObject.put("Il mio Esercizio", array);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Creo file xml
            PrintWriter xmlFile = new PrintWriter("Esercizio.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element results = doc.createElement("Results");
            doc.appendChild(results);

            // Mi connetto al Server
            conn = DriverManager.getConnection(url, props);
            System.out.println("Test di connessione avvenuto con successo");
            conn.createStatement();
            Statement st = conn.createStatement();

            //Query sulle tre tabelle
            ResultSet rs = st.executeQuery
                    ("SELECT customers.first_name, customers.last_name, customers.email, orders.address, orders.city, orders.state, products.product_name, products.price  " +
                            "FROM orders JOIN customers ON orders.customer_id = id_customers " +
                            "JOIN products ON orders.product_id = id_products"
                    );
            while (rs.next()) {
                //CSV File
                sb.append(rs.getString("product_name").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("price").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("first_name").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("last_name").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("email").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("address").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("city").replace(",", "."));
                sb.append(",");
                sb.append(rs.getString("state").replace(",", "."));
                sb.append("\n");

                //JSON file
                JSONObject record = new JSONObject();
                record.put("product_name", rs.getString("product_name"));
                record.put("price", rs.getString("price"));
                record.put("first_name", rs.getString("first_name"));
                record.put("last_name", rs.getString("last_name"));
                record.put("address", rs.getString("address"));
                record.put("state", rs.getString("state"));
                array.put(record);

                //xml file
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                Element row = doc.createElement("Row");
                results.appendChild(row);
                for (int i = 1; i <= colCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object value = rs.getObject(i);
                    Element node = doc.createElement(columnName);
                    node.appendChild(doc.createTextNode(value.toString()));
                    row.appendChild(node);
                }
            }
            //Formatto il file XML
            DOMSource domSource = new DOMSource(doc);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);

            //Scrivo i file e chiudo
            csvFile.write("Product Name, Price, First Name, Last Name, Email, Address, City, State\n");
            csvFile.write(sb.toString());
            System.out.println("Ho scritto il file .csv");
            csvFile.close();
            jsonFile.write(gson.toJson(array));
            System.out.println("Ho scritto il file .json");
            jsonFile.close();
            xmlFile.write(sw.toString());
            System.out.println("Ho scritto il file .xml");
            xmlFile.close();
            System.out.println("finished");

        } catch (SQLException | FileNotFoundException | TransformerException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
