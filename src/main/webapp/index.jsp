<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="ge.tsu.currency_converter.servlet.CurrencyNames" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Currency Converter</title>
    <meta charset="UTF-8"/>
    <link rel="stylesheet" type="text/css" href="CSS/styles.css"> <!-- Linking css file-->

    <link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/css/select2.min.css" rel="stylesheet"/>
</head>

<body>
<%@include file="WEB-INF/shared/header.jsp" %>
<br/>

<%
    final Logger logger = Logger.getLogger(CurrencyNames.class.getName());

    logger.info("Attempting to retrieve currency names/\"currency-names\" attribute.");
    String x = String.valueOf(request.getServletContext().getAttribute("currency-names"));
    logger.info("Retrieved \"currency-names\" attribute value: " + x);
    ArrayList<String> list = new ArrayList<>();
    int j = 0;
    for (int i = 0; i < x.length() / 3; i++) {
        list.add(i, x.substring(j, j + 3));
        j += 3;
    }

    //writing currency names/code from list to array
    String[] currencyOptions = new String[list.size()];
    for (int i = 0; i < list.size(); i++) {
        currencyOptions[i] = list.get(i);
    }
%>

<div class="center-form">

    <form method="post" action="currency-converter">
        <fieldset class="fieldset">
            <legend><h1>Currency Converter!</h1></legend>

            <!--creating input label for user to enter amount of money(integers or non-negative floating point numbers)-->
            Amount:<label>
            <%
                //by this we are pinning input which user entered
                String amountValue = "";
                if (request.getParameter("amount") != null) {
                    amountValue = request.getParameter("amount");
                }
            %>
            <input name="amount" id="amount" type="text" placeholder="Enter Amount..."
                   value="<%=amountValue%>"/>
        </label>

            <!-- Include jQuery -->
            <!-- jQuery is a fast, feature-rich, and widely-used JavaScript library that simplifies DOM manipulation and event handling. -->
            <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
            <!-- Include Select2 JavaScript -->
            <!-- Select2 is a jQuery-based plugin that enhances the functionality and styling of HTML dropdown (select) elements.
            It provides advanced features like searching, tagging, and theming. -->
            <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.13/js/select2.min.js"></script>

            <script>
                // Wait for the document to be fully loaded before executing any code
                $(document).ready(function () {
                    // Initialize Select2 for the "converting-from" dropdown element
                    // This enables enhanced dropdown functionality and styling for element with ID "converting-from"
                    $("#converting-from").select2();

                    // Initialize Select2 for the "converting-to" dropdown element
                    // This enables enhanced dropdown functionality and styling for element with ID "converting-to"
                    $("#converting-to").select2();
                });
            </script>

            <%--this label displays currencies from user chooses to convert from--%>
            <label for="converting-from">From:</label>
            <select name="converting-from" id="converting-from">
                <%
                    String selectedCurrency_From = request.getParameter("converting-from");

                    for (String currency : currencyOptions) {
                        String currencyValue_From = currency.substring(0, 3);//Extracting the currency symbol code and leaving like that->GEL, USD, EUR..
                        String selectedAttribute_From = "";
                        //checking which currency was chosen and making it "selected" (means that chosen currency will be pinned/not changed after refresh)
                        if (selectedCurrency_From != null && selectedCurrency_From.equals(currencyValue_From)) {
                            selectedAttribute_From = "selected";
                        }
                %>
                <%-- example:<option value="GEL"selected>GEL&#8382;</option>--%>
                <option value="<%= currencyValue_From %>" <%= selectedAttribute_From %>><%= currency %>
                </option>
                <%
                    }
                %>

            </select>

            <%-- this label displays currencies from user chooses currency to convert into --%>
            <label for="converting-to">To:</label>
            <select name="converting-to" id="converting-to">
                <%
                    String selectedCurrency_To = request.getParameter("converting-to");

                    for (String currency : currencyOptions) {
                        String currencyValue_To = currency.substring(0, 3);
                        String selectedAttribute_To = " ";
                        //checking which currency was chosen and making it "selected" (means that chosen currency will be pinned/not changed after refresh)
                        if (selectedCurrency_To != null && selectedCurrency_To.equals(currencyValue_To)) {
                            selectedAttribute_To = "selected";
                        }
                %>
                <option value="<%=currencyValue_To%>" <%=selectedAttribute_To%>><%=currency%>
                </option>
                <%
                    }
                %>
            </select>

            <button type="submit" class="convert-button">Convert</button>

            <%
                //printing the result
                String convertedCurrency = String.valueOf(request.getAttribute("output-text"));
                if (!convertedCurrency.equals("null")) {
            %>
            <p><%=convertedCurrency%>
            </p>
            <%
                }
            %>

            <%
                String generalInfoOne;
                generalInfoOne = String.valueOf(request.getAttribute("general-info-one"));
                String generalInfoTwo;
                generalInfoTwo = String.valueOf(request.getAttribute("general-info-two"));
                if (!generalInfoOne.equals("null")) {
            %>
            <p><span class="general-info"><%=generalInfoOne %></span></p>
            <%
                }
            %>

            <%
                if (!generalInfoTwo.equals("null")) {
            %>
            <p><span class="general-info"><%=generalInfoTwo%></span></p>
            <%
                }
            %>

            <%
                String errorMessage = (String) request.getAttribute("error-message");
                if (errorMessage != null) {
            %>
            <p><span class="red-text"> <%=errorMessage%></span></p>
            <%
                }
            %>


            <%
                // Getting the current date
                Calendar calendar = Calendar.getInstance();
                // Converting the Calendar object to a Date
                Date today = calendar.getTime();
                // Format the date as a string
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String todayAsString = dateFormat.format(today);

                String lastUpdatedTimeOfExchangeRate = String.valueOf(request.getAttribute("last-updated-rate-time"));
                if (lastUpdatedTimeOfExchangeRate.equals("null")) {
            %>
            <div class="custom-text"><p><span><%="Last updated " + todayAsString%></span></p></div>
            <%
            } else {
            %>
            <div class="custom-text"><p><span><%="Last updated " + lastUpdatedTimeOfExchangeRate%></span></p>
            </div>
            <%
                }
            %>

        </fieldset>
    </form>

</div>


<div class="all-rights-text">
    <%@include file="WEB-INF/shared/footer.jsp" %>
</div>

</body>

</html>