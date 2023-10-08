package ge.tsu.currency_converter.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "CurrencyConverterServlet", value = "/currency-converter")
public class CurrencyConverterServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CurrencyConverterServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/");

        if (req.getParameter("amount").isEmpty() || req.getParameter("amount") == null) {
            req.setAttribute("error-message", "Please enter some amount!");
            requestDispatcher.forward(req, resp);
            return;
        }

        if (!validateStringInput(req.getParameter("amount"))) {
            req.setAttribute("error-message", "Please enter a valid amount!");
            requestDispatcher.forward(req, resp);
            return;
        }

        //Parsing string input into double
        double amount = Double.parseDouble(req.getParameter("amount"));

        try {
            //creating variable which store currencies that user chose
            String selectedCurrencyFrom = req.getParameter("converting-from");
            String selectedCurrencyTo = req.getParameter("converting-to");
            //getting api key from environmental variables
            String APIKey = System.getenv("API_KEY");
            String currencyRateApiUrl = "https://api.fastforex.io/fetch-one?from=" + selectedCurrencyFrom + "&to=" + selectedCurrencyTo + "&api_key=" + APIKey;
            //creating url object
            URL url = new URL(currencyRateApiUrl);
            //creating HttpURLConnection object, opening connection to our API(specific url) and preparing to make HTTP request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //specifying that HTTP request to the url is "GET" method (typically get method is used to retrieve data from the server)
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                //reading API response
                //creating InputStreamReader to read input stream from the HTTP connection
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                //wrapping inputStream into BufferedReader, because then we will read each line of the api
                BufferedReader in = new BufferedReader(inputStreamReader);
                String inputLine;
                StringBuilder apiResponse = new StringBuilder();

                //loop reads each line of api and finishes when lines are over
                while ((inputLine = in.readLine()) != null) {
                    //Writing everything that is read from api to StringBuilder
                    apiResponse.append(inputLine);
                }
                //closes the BufferedReader and the associated input stream (but it does not close HTTP connection)
                in.close();

                // Closing the connection (google said it's a good practice)
                connection.disconnect();

                //API in String, looks something like that->{"base":"GEL","result":{"USD":0.37289},"updated":"2023-09-29 13:41:46","ms":4}
                String jsonResponse = apiResponse.toString();

                //creating JSONObject and passing my String jsonResponse. from jsonObject we will fetch preferred data
                JSONObject jsonObject = new JSONObject(jsonResponse);

                //calling the method which gives back last update time of currency rate, and passing it to jsp
                req.setAttribute("last-updated-rate-time", getFormattedLastUpdatedTimeOfExchangeRate(jsonObject));

                //fetching exchange rate from jsonObject which is my API
                double exchangeRate = jsonObject.getJSONObject("result").getDouble(selectedCurrencyTo);


                //converting amount
                double convertedAmount;
                convertedAmount = amount * exchangeRate;
                //formatting the last output of converted amount and passing it to jsp
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String finalResult = decimalFormat.format(amount) + " " + selectedCurrencyFrom + " = " + decimalFormat.format(convertedAmount) + " " + selectedCurrencyTo;

                logger.info(selectedCurrencyFrom + " has been successfully converted to " + selectedCurrencyTo);
                req.setAttribute("output-text", finalResult);


                //formatting and passing general information of the currency user is interested in
                DecimalFormat decimalFormat1 = new DecimalFormat("0.0000000");
                double forGeneralInfoTwo = 1 / exchangeRate;

                if (amount != 1) {
                    String generalInfoOne = "1 " + selectedCurrencyFrom + " = " + exchangeRate + " " + selectedCurrencyTo;
                    req.setAttribute("general-info-one", generalInfoOne);

                    String generalInfoTwo = "1 " + selectedCurrencyTo + " = " + decimalFormat1.format(forGeneralInfoTwo) + " " + selectedCurrencyFrom;
                    req.setAttribute("general-info-two", generalInfoTwo);
                    //it is important, otherwise when servlet will be called it won't stay on same page
                    requestDispatcher.forward(req, resp);

                } else {
                    String generalInfoTwo = "1 " + selectedCurrencyTo + " = " + decimalFormat1.format(forGeneralInfoTwo) + " " + selectedCurrencyFrom;
                    req.setAttribute("general-info-two", generalInfoTwo);
                    requestDispatcher.forward(req, resp);
                }

            } else {
                // Handle the API response error
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch data from the API.");
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error parsing the input date and time: " + e.getMessage());
            e.printStackTrace();
            // Handle exceptions
        }

    }

    //validation of an input string (amount)
    public boolean validateStringInput(String amount) {
        // Define a regular expression pattern for the validation
        String pattern = "^\\d+(\\.\\d+)?$";

        // Use the Pattern and Matcher classes to check if the input matches the pattern
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(amount);

        return matcher.matches();
    }

    //this method gets jsonObject which is our API, then fetches date and formats it
    public String getFormattedLastUpdatedTimeOfExchangeRate(JSONObject jsonObject) throws ParseException {
        //fetching date time from api show users when was the last update of exchange rate
        String updateTime = String.valueOf(jsonObject.getString("updated"));
        // Create a SimpleDateFormat object for the input format (our date example-> 2023-09-29 13:41:46)
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Parse the input string into a Date object
        Date date = inputDateFormat.parse(updateTime);
        // Create a SimpleDateFormat object for the desired output format (our desired format-> 29-09-2023 13:41)
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        //out put will be like that-> 29-09-2023 13:41
        return outputDateFormat.format(date);
    }


}

