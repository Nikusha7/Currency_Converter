package ge.tsu.currency_converter.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebListener
public class CurrencyNames implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(CurrencyNames.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("Names: contextInitialized method called.");
        try {
            String APIKey = System.getenv("API_KEY");
            String currencyCodeApi = "https://api.fastforex.io/fetch-all?api_key="+APIKey;
            URL url = new URL(currencyCodeApi);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream());
                BufferedReader in = new BufferedReader(inputStreamReader);
                String inputLine;
                StringBuilder apiResponse = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    //Writing everything that is read from api to StringBuilder
                    apiResponse.append(inputLine);
                }
                //closes the BufferedReader and the associated input stream (but it does not close HTTP connection)
                in.close();

                // Closing the connection (google said it's a good practice)
                con.disconnect();
                logger.info("Names: Successfully fetched data from API");
                String jsonResponse = apiResponse.toString();

                //creating JSONObject and passing my String jsonResponse. from jsonObject we will fetch preferred data
                JSONObject jsonObject = new JSONObject(jsonResponse);

                String currencyCode = jsonObject.getJSONObject("results").toString();

                Pattern pattern = Pattern.compile("\"([A-Z]{3})\":");
                Matcher matcher = pattern.matcher(currencyCode);
                String singleCurrencyName;
                StringBuilder output = new StringBuilder();
                while (matcher.find()) {
                    singleCurrencyName = matcher.group(1);
                    output.append(singleCurrencyName);
                }
                // req.setAttribute("currency-codes", output);
                ServletContext servletContext = event.getServletContext();

                servletContext.setAttribute("currency-names", output);

                logger.info("Names: contextInitialized method ended.");
            } else {
                // Handle the error based on the status code
                if (responseCode == 404) {
                    // Not Found error
                    throw new Exception("Resource not found");
                } else if (responseCode == 500) {
                    // Server error
                    throw new Exception("Internal Server Error");
                } else {
                    // Handle other status codes as needed
                    throw new Exception("Maybe your API Key has been expired, HTTP request failed with status code " + responseCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Your cleanup code here
    }

}
